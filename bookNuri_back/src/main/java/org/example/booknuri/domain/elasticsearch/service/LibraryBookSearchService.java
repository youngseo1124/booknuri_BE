package org.example.booknuri.domain.elasticsearch.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.elasticsearch.dto.LibraryBookSearchResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryBookSearchService {

    private final ElasticsearchOperations operations;

    // 📚 도서 검색 (띄어쓰기 포함 검색 가능)
    public LibraryBookSearchResponseDto searchBooks(String libCode, String keywordType, String keyword, String sortType, int offset, int limit) {
        //  필드 결정: 제목 or 저자
        String field = keywordType.equals("authors") ? "authors" : "bookname";

        //  검색 키워드 (소문자 trim)
        String keywordLower = keyword.toLowerCase().trim();

        //  얼마나 많이 가져올지 결정 (짧은 키워드는 결과 많으니까 넉넉히)
        int searchFetchLimit = keyword.length() <= 2 ? 10_000 : 5_000;

        // Elasticsearch 검색 조건
        Criteria criteria = new Criteria("libCode").is(libCode)
                .and(new Criteria(field).matches(keyword));

        Sort sort = switch (sortType) {
            case "like" -> Sort.by(Sort.Order.desc("likeCount"));
            case "review" -> Sort.by(Sort.Order.desc("reviewCount"));
            case "new" -> Sort.by(Sort.Order.desc("publicationDate"));
            case "old" -> Sort.by(Sort.Order.asc("publicationDate"));
            default -> Sort.unsorted();
        };

        // 넉넉하게 가져온 후 자바에서 필터링
        Query query = new CriteriaQuery(criteria, PageRequest.of(0, searchFetchLimit, sort));
        SearchHits<LibraryBookSearchDocument> hits = operations.search(query, LibraryBookSearchDocument.class);

        List<LibraryBookSearchDocument> filtered = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(doc -> {
                    String value = field.equals("authors") ? doc.getAuthors() : doc.getBookname();
                    return value != null && value.toLowerCase().contains(keywordLower);
                })
                .toList();

        //  offset ~ offset+limit 자르기
        List<LibraryBookSearchDocument> paged = filtered.stream()
                .skip(offset)
                .limit(limit)
                .toList();

        //  결과 반환
        return LibraryBookSearchResponseDto.builder()
                .totalCount(filtered.size()) // 필터된 전체 개수
                .results(paged)              // 잘라낸 결과
                .build();
    }



    // ✏️ 자동완성 검색
    public List<LibraryBookSearchDocument> searchBookAutocomplete(String libCode, String keyword) {
        Criteria criteria = new Criteria("libCode").is(libCode)
                .and(new Criteria("bookname").matches(keyword)); // 1차 검색은 그대로

        Query query = new CriteriaQuery(
                criteria,
                PageRequest.of(0, 30, Sort.by(Sort.Order.desc("likeCount"))) // 넉넉하게 가져오자
        );

        SearchHits<LibraryBookSearchDocument> hits = operations.search(query, LibraryBookSearchDocument.class);

        // ✅ 2차 필터링: bookname에 정확히 포함되는 것만 남김
        String keywordLower = keyword.toLowerCase().trim();

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(doc -> {
                    String value = doc.getBookname();
                    return value != null && value.toLowerCase().contains(keywordLower);
                })
                .limit(10) // 결과 10개만 잘라서 리턴
                .toList();
    }

}
