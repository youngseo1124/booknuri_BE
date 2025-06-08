package org.example.booknuri.domain.recommend.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.Log.entity.UserBookViewLogEntity;
import org.example.booknuri.domain.Log.repository.BookViewLogRepository;
import org.example.booknuri.domain.Log.repository.UserBookViewLogRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.elasticsearch.repository.LibraryBookSearchRepository;
import org.example.booknuri.domain.recommend.dto.RecommendBookDto;
import org.example.booknuri.domain.recommend.repository.RecommendRepository;
import org.example.booknuri.domain.recommend.repository.ViewCountRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ViewCountRepository viewCountRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final RecommendRepository recommendRepository;
    private final UserBookViewLogRepository userBookViewLogRepository;
    private final BookViewLogRepository bookViewLogRepository;

    public List<RecommendBookDto> getBestSeller(UserEntity user, String period, Long mainCategoryId) {
        String libCode = user.getMyLibrary().getLibCode();

        List<String> priority = switch (period) {
            case "weekly" -> List.of("weekly", "monthly", "yearly");
            case "monthly" -> List.of("monthly", "yearly");
            case "yearly" -> List.of("yearly");
            default -> throw new IllegalArgumentException("지원하지 않는 period입니다: " + period);
        };

        for (String p : priority) {
            LocalDate startDate = getStartDateByPeriod(p);

            // ✅ mainCategoryId가 있는 경우 → DB에서 카테고리별 인기 책 먼저 조회
            if (mainCategoryId != null) {
                List<RecommendBookDto> books = recommendRepository.findPopularBooksByCategory(
                        libCode, mainCategoryId, startDate, PageRequest.of(0, 100)
                );

                if (books.isEmpty()) continue;

                // → 그 책들 중 실제 도서관(libCode)에 있는 책만 ES로 필터링
                List<Long> bookIds = books.stream()
                        .map(RecommendBookDto::getId)
                        .toList();

                List<LibraryBookSearchDocument> docs = searchRepository.findByBookIdInAndLibCode(bookIds, libCode);
                Set<Long> availableBookIds = docs.stream()
                        .map(LibraryBookSearchDocument::getBookId)
                        .collect(Collectors.toSet());

                List<RecommendBookDto> filteredBooks = books.stream()
                        .filter(book -> availableBookIds.contains(book.getId()))
                        .limit(20)
                        .toList();

                if (!filteredBooks.isEmpty()) return filteredBooks;
            }

            // ✅ mainCategoryId 없으면 기존 방식 유지
            else {
                List<Long> topBookIds = viewCountRepository.findTopBookIds(startDate, PageRequest.of(0, 100));
                if (topBookIds.isEmpty()) continue;

                List<LibraryBookSearchDocument> docs = searchRepository.findByBookIdInAndLibCode(topBookIds, libCode);
                Map<Long, LibraryBookSearchDocument> docMap = docs.stream()
                        .collect(Collectors.toMap(LibraryBookSearchDocument::getBookId, Function.identity()));

                List<RecommendBookDto> result = topBookIds.stream()
                        .filter(docMap::containsKey)
                        .map(bookId -> {
                            LibraryBookSearchDocument doc = docMap.get(bookId);
                            return new RecommendBookDto(
                                    doc.getBookId(),
                                    doc.getBookname(),
                                    doc.getAuthors(),
                                    doc.getBookImageURL(),
                                    doc.getIsbn13(),
                                    "", // 필요 시 description도 doc.getDescription()
                                    (long) doc.getLikeCount()
                            );
                        })
                        .limit(20)
                        .toList();

                if (!result.isEmpty()) return result;
            }
        }

        return List.of(); // 🔚 fallback 다 실패한 경우
    }

    private LocalDate getStartDateByPeriod(String period) {
        return switch (period) {
            case "weekly" -> LocalDate.now().minusDays(7);
            case "monthly" -> LocalDate.now().minusDays(30);
            case "yearly" -> LocalDate.now().minusDays(365);
            default -> throw new IllegalArgumentException("잘못된 period: " + period);
        };
    }


    /*--------------개인 맞춤 추천-----------------------------------*/

    // ✅서브메서드: 특정 카테고리 맵에서 count가 2 이상인 가장 많이 본 ID 반환
    private Optional<Long> getMaxIfAtLeast(Map<Long, Long> map, long minCount) {
        return map.entrySet().stream()
                .filter(e -> e.getValue() >= minCount)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    // ✅서브메서드:  카테고리 타입에 따라 ViewCountRepository에서 인기 ID 가져오고 → ES로 필터링해서 추천 결과 반환
    private List<RecommendBookDto> recommendFromCategory(
            String categoryType, Long categoryId, Set<Long> excludeIds, String libCode
    ) {
        LocalDate startDate = LocalDate.now().minusDays(7);
        List<Long> ids = switch (categoryType) {
            case "sub" -> viewCountRepository.findTopBookIdsBySubCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "middle" -> viewCountRepository.findTopBookIdsByMiddleCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "main" -> viewCountRepository.findTopBookIdsByMainCategory(categoryId, startDate, PageRequest.of(0, 100));
            default -> List.of();
        };

        if (ids.isEmpty()) return List.of();

        //  ES에서 실제 도서관(libCode)에 있는 책만 필터
        List<LibraryBookSearchDocument> docs = searchRepository.findByBookIdInAndLibCode(ids, libCode);
        Map<Long, LibraryBookSearchDocument> docMap = docs.stream()
                .collect(Collectors.toMap(LibraryBookSearchDocument::getBookId, Function.identity()));

        return ids.stream()
                .filter(id -> docMap.containsKey(id) && !excludeIds.contains(id))
                .map(id -> {
                    LibraryBookSearchDocument doc = docMap.get(id);
                    return new RecommendBookDto(
                            doc.getBookId(),
                            doc.getBookname(),
                            doc.getAuthors(),
                            doc.getBookImageURL(),
                            doc.getIsbn13(),
                            "", // 필요시 doc.getDescription()
                            (long) doc.getLikeCount()
                    );
                })
                .limit(10)
                .toList();
    }

    // ✅서브메서드: 특정 카테고리 맵에서 count 순으로 여러 개 시도해서 추천 ID 추출
    private List<RecommendBookDto> recommendFromCategoryMap(
            Map<Long, Long> categoryMap,
            String categoryType,
            Set<Long> excludeIds,
            String libCode,
            int maxCount
    ) {
        if (categoryMap.isEmpty() || maxCount <= 0) return List.of();

        // 등장 횟수 내림차순으로 정렬
        List<Long> sortedIds = categoryMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        List<RecommendBookDto> result = new ArrayList<>();

        for (Long id : sortedIds) {
            List<RecommendBookDto> rec = recommendFromCategory(categoryType, id, excludeIds, libCode);
            for (RecommendBookDto dto : rec) {
                if (result.size() >= maxCount) break;
                if (excludeIds.contains(dto.getId())) continue;
                result.add(dto);
                excludeIds.add(dto.getId()); // 중복 제거를 위해 추가
            }
            if (result.size() >= maxCount) break;
        }

        return result;
    }


    // ✅메인 메서드:  카테고리별 추천 결과 누적해서 7개 채우는 맞춤 추천 로직
    public List<RecommendBookDto> getPersonalizedRecommendation(UserEntity user) {
        String libCode = user.getMyLibrary().getLibCode();

        // ✅ 1. 최근 본 책 5개 조회
        List<UserBookViewLogEntity> logs = userBookViewLogRepository
                .findTop5ByUserOrderByViewedAtDesc(user);

        // ✅ 2. 기록이 너무 적으면 fallback
        if (logs.size() < 3) {
            return getBestSeller(user, "weekly", null);
        }

        // ✅ 3. 이미 본 책 ID 저장
        Set<Long> viewedBookIds = logs.stream()
                .map(log -> log.getBook().getId())
                .collect(Collectors.toSet());

        // ✅ 4. 카테고리별 등장 횟수 집계
        Map<Long, Long> subMap = new HashMap<>();
        Map<Long, Long> middleMap = new HashMap<>();
        Map<Long, Long> mainMap = new HashMap<>();

        for (UserBookViewLogEntity log : logs) {
            BookEntity book = log.getBook();
            if (book.getSubCategory() != null) {
                subMap.merge(book.getSubCategory().getId(), 1L, Long::sum);
            }
            if (book.getMiddleCategory() != null) {
                middleMap.merge(book.getMiddleCategory().getId(), 1L, Long::sum);
            }
            if (book.getMainCategory() != null) {
                mainMap.merge(book.getMainCategory().getId(), 1L, Long::sum);
            }
        }

        // ✅ 5. 결과 누적 리스트
        List<RecommendBookDto> result = new ArrayList<>();

        // ✅ 6. sub → middle → main 순서대로 추천 시도
        result.addAll(recommendFromCategoryMap(subMap, "sub", viewedBookIds, libCode, 7 - result.size()));
        if (result.size() < 7) {
            result.addAll(recommendFromCategoryMap(middleMap, "middle", viewedBookIds, libCode, 7 - result.size()));
        }
        if (result.size() < 7) {
            result.addAll(recommendFromCategoryMap(mainMap, "main", viewedBookIds, libCode, 7 - result.size()));
        }

        // ✅ 7. 부족하면 fallback에서 채우기
        if (result.size() < 7) {
            List<RecommendBookDto> fallback = getBestSeller(user, "weekly", null).stream()
                    .filter(book -> !viewedBookIds.contains(book.getId()))
                    .limit(7 - result.size())
                    .toList();
            result.addAll(fallback);
        }

        return result.stream().limit(7).toList(); // 혹시 모르니 마지막에도 딱 7개 제한
    }

    /*-------------------------연령, 성별별 베스트 도서----------------------------*/
    public List<RecommendBookDto> getDemographicRecommend(
            UserEntity user,
            String gender,
            int birthYearGroup
    ) {
        String libCode = user.getMyLibrary().getLibCode();

        int minYear = LocalDate.now().getYear() - birthYearGroup - 9;
        int maxYear = LocalDate.now().getYear() - birthYearGroup;

        // 1. 최근 한 달 기준 book_view_log에서 최신순 1000개 조회
        List<Long> latestBookIds = bookViewLogRepository
                .findTopBookIdsByGenderAndBirthYear(gender, minYear, maxYear, PageRequest.of(0, 1000));

        if (latestBookIds.isEmpty()) return List.of();

        // 2. bookId → count
        Map<Long, Long> freqMap = latestBookIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 3. 인기순 상위 100개 추출
        List<Long> topBookIds = freqMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(100)
                .toList();

        // 4. 내 도서관에서 실제 있는 책만 필터링
        List<LibraryBookSearchDocument> docs = searchRepository.findByBookIdInAndLibCode(topBookIds, libCode);
        Map<Long, LibraryBookSearchDocument> docMap = docs.stream()
                .collect(Collectors.toMap(LibraryBookSearchDocument::getBookId, Function.identity()));

        return topBookIds.stream()
                .filter(docMap::containsKey)
                .limit(20)
                .map(id -> {
                    LibraryBookSearchDocument doc = docMap.get(id);
                    return new RecommendBookDto(
                            doc.getBookId(),
                            doc.getBookname(),
                            doc.getAuthors(),
                            doc.getBookImageURL(),
                            doc.getIsbn13(),
                            "",
                            (long) doc.getLikeCount()
                    );
                })
                .toList();
    }



}
