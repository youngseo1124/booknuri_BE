package org.example.booknuri.domain.recommend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.Log.entity.BookViewLogEntity;
import org.example.booknuri.domain.Log.entity.UserBookViewLogEntity;
import org.example.booknuri.domain.Log.repository.BookViewLogRepository;
import org.example.booknuri.domain.Log.repository.UserBookViewLogRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.entity.MainCategory;
import org.example.booknuri.domain.book.entity.MiddleCategory;
import org.example.booknuri.domain.book.entity.SubCategory;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.book.repository.MainCategoryRepository;
import org.example.booknuri.domain.book.repository.MiddleCategoryRepository;
import org.example.booknuri.domain.book.repository.SubCategoryRepository;
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
@Slf4j
@RequiredArgsConstructor
public class RecommendService {

    private final ViewCountRepository viewCountRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final RecommendRepository recommendRepository;
    private final UserBookViewLogRepository userBookViewLogRepository;
    private final BookViewLogRepository bookViewLogRepository;
    private final MainCategoryRepository  mainCategoryRepository;
    private final MiddleCategoryRepository middleCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BookRepository bookRepository;


    /**
     * ✅ 주어진 bookId 리스트 중, 실제 해당 도서관(libCode)에 존재하는 책만 필터링한 Map 반환
     * key = bookId, value = LibraryBookSearchDocument
     */
    private Map<Long, LibraryBookSearchDocument> getAvailableBookDocMap(List<Long> bookIds, String libCode) {
        if (bookIds == null || bookIds.isEmpty()) return Map.of();

        List<LibraryBookSearchDocument> docs = searchRepository.findByBookIdInAndLibCode(bookIds, libCode);
        return docs.stream()
                .collect(Collectors.toMap(LibraryBookSearchDocument::getBookId, Function.identity()));
    }

    /**
     * ✅ RecommendBookDto 리스트에서 bookId만 추출
     */
    private List<Long> extractBookIds(List<RecommendBookDto> books) {
        return books.stream()
                .map(RecommendBookDto::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * ✅ 베스트셀러 추천 (기간 및 선택적 메인 카테고리 필터)
     * - 카테고리 없으면 전체 인기 도서
     * - 카테고리 있으면 해당 카테고리 내에서 인기 도서
     */
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

            // ✅ mainCategory 있는 경우
            if (mainCategoryId != null) {
                List<RecommendBookDto> books = recommendRepository.findPopularBooksByCategory(
                        libCode, mainCategoryId, startDate, PageRequest.of(0, 100)
                );
                if (books.isEmpty()) continue;

                List<Long> bookIds = extractBookIds(books);
                Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(bookIds, libCode);

                List<RecommendBookDto> filtered = books.stream()
                        .filter(b -> {
                            LibraryBookSearchDocument doc = docMap.get(b.getId());
                            return doc != null && doc.getBookImageURL() != null && !doc.getBookImageURL().isBlank();
                        })
                        .toList();

                // ✅ fallback 로직: 부족하면 같은 main 카테고리 + 이미지 있는 책으로 채움
                if (filtered.size() < 20) {
                    Set<Long> alreadyIncluded = filtered.stream()
                            .map(RecommendBookDto::getId)
                            .collect(Collectors.toSet());

                    List<LibraryBookSearchDocument> docs = searchRepository.findByLibCodeAndMainCategoryId(libCode, mainCategoryId);

                    List<RecommendBookDto> fallback = docs.stream()
                            .filter(doc ->
                                    doc.getBookImageURL() != null &&
                                            !doc.getBookImageURL().isBlank() &&
                                            !alreadyIncluded.contains(doc.getBookId())
                            )
                            .limit(20 - filtered.size())
                            .map(doc -> new RecommendBookDto(
                                    doc.getBookId(),
                                    doc.getBookname(),
                                    doc.getAuthors(),
                                    doc.getBookImageURL(),
                                    doc.getIsbn13(),
                                    "",
                                    (long) doc.getLikeCount()
                            ))
                            .toList();

                    List<RecommendBookDto> result = new ArrayList<>(filtered);
                    result.addAll(fallback);
                    return result.stream().limit(20).toList(); // 안전 제한
                }

                return filtered.stream().limit(20).toList(); // 이미 충분하면 여기서 반환
            }

            // ✅ mainCategory 없을 경우 기본 전체 인기 도서 추천
            else {
                List<Long> topBookIds = viewCountRepository.findTopBookIds(startDate, PageRequest.of(0, 100));
                if (topBookIds.isEmpty()) continue;

                Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(topBookIds, libCode);

                List<RecommendBookDto> result = topBookIds.stream()
                        .map(docMap::get)
                        .filter(Objects::nonNull)
                        .filter(doc -> doc.getBookImageURL() != null && !doc.getBookImageURL().isBlank())
                        .limit(20)
                        .map(doc -> new RecommendBookDto(
                                doc.getBookId(),
                                doc.getBookname(),
                                doc.getAuthors(),
                                doc.getBookImageURL(),
                                doc.getIsbn13(),
                                "",
                                (long) doc.getLikeCount()
                        ))
                        .toList();

                if (!result.isEmpty()) return result;
            }
        }

        return List.of(); // fallback 다 실패 시 빈 리스트
    }

    /**
     * ✅ 기간 문자열에 따른 시작일 반환
     */
    private LocalDate getStartDateByPeriod(String period) {
        return switch (period) {
            case "weekly" -> LocalDate.now().minusDays(7);
            case "monthly" -> LocalDate.now().minusDays(30);
            case "yearly" -> LocalDate.now().minusDays(365);
            default -> throw new IllegalArgumentException("잘못된 period: " + period);
        };
    }

    /**
     * ✅ 특정 카테고리 기반 추천 시도 (중복 제거 및 limit까지)
     */
    private List<RecommendBookDto> recommendFromCategory(
            String categoryType, Long categoryId, Set<Long> excludeIds, String libCode
    ) {
        LocalDate startDate = LocalDate.now().minusDays(10);
        List<Long> ids = switch (categoryType) {
            case "sub" -> viewCountRepository.findTopBookIdsBySubCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "middle" -> viewCountRepository.findTopBookIdsByMiddleCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "main" -> viewCountRepository.findTopBookIdsByMainCategory(categoryId, startDate, PageRequest.of(0, 100));
            default -> List.of();
        };

        Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(ids, libCode);

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
                            "",
                            (long) doc.getLikeCount()
                    );
                })
                .limit(10)
                .toList();
    }

    /**
     * ✅ 카테고리 집합 (Map<categoryId, count>) 기반 추천
     */
    private List<RecommendBookDto> recommendFromCategoryMap(
            Map<Long, Long> categoryMap,
            String categoryType,
            Set<Long> excludeIds,
            String libCode,
            int maxCount
    ) {
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
                excludeIds.add(dto.getId());
            }
            if (result.size() >= maxCount) break;
        }
        return result;
    }


    /**
     * ✅ 메인/미들/서브 카테고리 기반 추천
     * - sub → middle → main 순서로 fallback
     * - 실제 내 도서관(libCode)에 있는 책만 10권 추천
     */
    public List<RecommendBookDto> getCategoryBasedRecommend(
            UserEntity user,
            String mainCategoryName,
            String middleCategoryName,
            String subCategoryName
    ) {
        final int targetCount = 10; //  추천할 총 책 수

        String libCode = user.getMyLibrary().getLibCode();
        LocalDate startDate = LocalDate.now().minusDays(7);

        Long mainCategoryId = mainCategoryRepository.findByName(mainCategoryName)
                .map(MainCategory::getId)
                .orElseThrow(() -> new IllegalArgumentException("mainCategoryName 잘못됨"));

        Long middleCategoryId = (middleCategoryName != null) ?
                middleCategoryRepository.findByNameAndMainCategoryName(middleCategoryName, mainCategoryName)
                        .map(MiddleCategory::getId)
                        .orElse(null) : null;

        Long subCategoryId = (subCategoryName != null) ?
                subCategoryRepository.findByNameAndMiddleCategoryName(subCategoryName, middleCategoryName)
                        .map(SubCategory::getId)
                        .orElse(null) : null;

        Set<Long> addedBookIds = new HashSet<>();
        List<RecommendBookDto> result = new ArrayList<>();

        if (subCategoryId != null) {
            result.addAll(getTopBooksByCategory(
                    subCategoryId, libCode, startDate, addedBookIds, targetCount - result.size(), "sub"));
        }

        if (result.size() < targetCount && middleCategoryId != null) {
            result.addAll(getTopBooksByCategory(
                    middleCategoryId, libCode, startDate, addedBookIds, targetCount - result.size(), "middle"));
        }

        if (result.size() < targetCount) {
            result.addAll(getTopBooksByCategory(
                    mainCategoryId, libCode, startDate, addedBookIds, targetCount - result.size(), "main"));
        }

        return result.stream().limit(targetCount).toList();
    }



    //카테별 인기도서
    private List<RecommendBookDto> getTopBooksByCategory(
            Long categoryId,
            String libCode,
            LocalDate startDate,
            Set<Long> addedBookIds,
            int limit,
            String level
    ) {
        List<Long> ids = switch (level) {
            case "main" -> viewCountRepository.findTopBookIdsByMainCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "middle" -> viewCountRepository.findTopBookIdsByMiddleCategory(categoryId, startDate, PageRequest.of(0, 100));
            case "sub" -> viewCountRepository.findTopBookIdsBySubCategory(categoryId, startDate, PageRequest.of(0, 100));
            default -> List.of();
        };

        Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(ids, libCode);

        List<RecommendBookDto> result = ids.stream()
                .filter(id -> {
                    LibraryBookSearchDocument doc = docMap.get(id);
                    return doc != null &&
                            !addedBookIds.contains(id) &&
                            doc.getBookImageURL() != null &&
                            !doc.getBookImageURL().isBlank();
                })
                .limit(limit)
                .map(id -> {
                    LibraryBookSearchDocument doc = docMap.get(id);
                    addedBookIds.add(id);
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
                .collect(Collectors.toList());

        // ✅ fallback: ES에서 같은 카테고리 전체 중 이미지 있는 애들로 부족분 채우기
        if (result.size() < limit) {
            int remain = limit - result.size();
            Set<Long> included = new HashSet<>(addedBookIds);

            List<LibraryBookSearchDocument> docs = switch (level) {
                case "main" -> searchRepository.findByLibCodeAndMainCategoryId(libCode, categoryId);
                case "middle" -> searchRepository.findByLibCodeAndMiddleCategoryId(libCode, categoryId);
                case "sub" -> searchRepository.findByLibCodeAndSubCategoryId(libCode, categoryId);
                default -> List.of();
            };

            List<RecommendBookDto> fallback = docs.stream()
                    .filter(doc ->
                            doc.getBookImageURL() != null &&
                                    !doc.getBookImageURL().isBlank() &&
                                    !included.contains(doc.getBookId()))
                    .limit(remain)
                    .map(doc -> {
                        addedBookIds.add(doc.getBookId());
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

            result.addAll(fallback);
        }

        return result;
    }





    /**
     * ✅ 개인 맞춤 추천------------------------------------------------------------
     */
    public List<RecommendBookDto> getPersonalizedRecommendation(UserEntity user) {
        String libCode = user.getMyLibrary().getLibCode();

        List<UserBookViewLogEntity> logs = userBookViewLogRepository
                .findTop5ByUserOrderByViewedAtDesc(user);

        if (logs.size() < 3) {
            return getBestSeller(user, "weekly", null);
        }

        Set<Long> viewedBookIds = logs.stream()
                .map(log -> log.getBook().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> subMap = new HashMap<>();
        Map<Long, Long> middleMap = new HashMap<>();
        Map<Long, Long> mainMap = new HashMap<>();

        for (UserBookViewLogEntity log : logs) {
            BookEntity book = log.getBook();
            if (book.getSubCategory() != null) subMap.merge(book.getSubCategory().getId(), 1L, Long::sum);
            if (book.getMiddleCategory() != null) middleMap.merge(book.getMiddleCategory().getId(), 1L, Long::sum);
            if (book.getMainCategory() != null) mainMap.merge(book.getMainCategory().getId(), 1L, Long::sum);
        }

        List<RecommendBookDto> result = new ArrayList<>();
        result.addAll(recommendFromCategoryMap(subMap, "sub", viewedBookIds, libCode, 7 - result.size()));
        result.addAll(recommendFromCategoryMap(middleMap, "middle", viewedBookIds, libCode, 7 - result.size()));
        result.addAll(recommendFromCategoryMap(mainMap, "main", viewedBookIds, libCode, 7 - result.size()));

        if (result.size() < 7) {
            List<RecommendBookDto> fallback = getBestSeller(user, "weekly", null).stream()
                    .filter(book -> !viewedBookIds.contains(book.getId()))
                    .limit(7 - result.size())
                    .toList();
            result.addAll(fallback);
        }

        return result.stream().limit(7).toList();
    }

    /**
     * ✅ 연령 + 성별 기반 베스트셀러
     */
    public List<RecommendBookDto> getDemographicRecommend(
            UserEntity user,
            String gender,
            int birthYearGroup
    ) {
        String libCode = user.getMyLibrary().getLibCode();
        int minYear = LocalDate.now().getYear() - birthYearGroup - 9;
        int maxYear = LocalDate.now().getYear() - birthYearGroup;

        List<Long> latestBookIds = bookViewLogRepository
                .findTopBookIdsByGenderAndBirthYear(gender, minYear, maxYear, PageRequest.of(0, 1000));

        if (latestBookIds.isEmpty()) return List.of();

        Map<Long, Long> freqMap = latestBookIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Long> topBookIds = freqMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(100)
                .toList();

        Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(topBookIds, libCode);

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

    //------------연관 책 추천하기---------------
    public List<RecommendBookDto> getRelatedBooks(UserEntity user, Long bookId) {
        String libCode = user.getMyLibrary().getLibCode();

        // 1. A책을 본 최근 100명 유저 조회
        List<String> usernames = bookViewLogRepository.findTopUsernamesByBook(bookId, PageRequest.of(0, 100));
        log.info("✅ A책 [{}]을 본 최근 유저 수: {}", bookId, usernames.size());

        Map<Long, Long> bookCountMap = new HashMap<>();

        for (String username : usernames) {
            List<BookViewLogEntity> logs = bookViewLogRepository.findAllByUsernameOrderByViewedAt(username);

            // ✅ 20대 여성 제외
            if (!logs.isEmpty()) {
                BookViewLogEntity firstLog = logs.get(0);
                if ("F".equals(firstLog.getGender()) &&
                        firstLog.getBirthYear() >= 1996 &&
                        firstLog.getBirthYear() <= 2005) {
                    log.info("🚫 제외됨: 20대 여성 ({}), {}년생", username, firstLog.getBirthYear());
                    continue;
                }
            }

            int targetIndex = -1;
            for (int i = 0; i < logs.size(); i++) {
                if (logs.get(i).getBook().getId().equals(bookId)) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex == -1) {
                log.info("⚠️ {} 유저는 A책을 본 로그가 없음", username);
                continue;
            }

            int start = Math.max(0, targetIndex - 10);
            int end = Math.min(logs.size(), targetIndex + 11);

            for (int i = start; i < end; i++) {
                Long otherBookId = logs.get(i).getBook().getId();
                if (!otherBookId.equals(bookId)) {
                    bookCountMap.merge(otherBookId, 1L, Long::sum);
                }
            }
        }

        log.info("📊 연관 책 후보 수: {}", bookCountMap.size());

        List<Long> sortedBookIds = bookCountMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, LibraryBookSearchDocument> docMap = getAvailableBookDocMap(sortedBookIds, libCode);
        log.info("📚 사용 가능한 책 수 (도서관 필터링 결과): {}", docMap.size());

        List<RecommendBookDto> result = sortedBookIds.stream()
                .filter(docMap::containsKey)
                .limit(10)
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
                .collect(Collectors.toList());

        // ✅ fallback 진입 여부 로그
        log.info("📦 최종 추천 도서 수: {}", result.size());

        // 3. 부족하면 → A책 카테고리 기반 fallback
        if (result.size() < 10) {
            log.info("📉 연관 도서 부족으로 fallback 시작 ({}권)", result.size());

            // 🔁 bookId로 BookEntity 직접 조회
            Optional<BookEntity> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isEmpty()) {
                log.warn("❌ fallback 실패 - BookEntity 찾을 수 없음: bookId={}", bookId);
                return result;
            }

            BookEntity book = bookOpt.get();
            String main = book.getMainCategory().getName();
            String middle = book.getMiddleCategory() != null ? book.getMiddleCategory().getName() : null;
            String sub = book.getSubCategory() != null ? book.getSubCategory().getName() : null;

            List<RecommendBookDto> fallback = getCategoryBasedRecommend(user, main, middle, sub);
            Set<Long> existing = result.stream().map(RecommendBookDto::getId).collect(Collectors.toSet());

            fallback.stream()
                    .filter(dto -> !existing.contains(dto.getId()))
                    .limit(10 - result.size())
                    .forEach(result::add);

            log.info("✅ fallback 추천 결과 추가 완료. 최종 추천 도서 수: {}", result.size());
        }

        return result.stream().limit(10).toList();
    }



}
