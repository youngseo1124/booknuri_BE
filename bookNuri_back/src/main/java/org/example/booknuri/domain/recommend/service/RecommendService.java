package org.example.booknuri.domain.recommend.service;

import lombok.RequiredArgsConstructor;
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
}
