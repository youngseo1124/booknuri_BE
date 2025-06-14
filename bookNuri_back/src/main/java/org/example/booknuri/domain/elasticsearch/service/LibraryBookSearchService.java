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
        // ✅ 필드 결정: 제목 or 저자
        String field = keywordType.equals("authors") ? "authors" : "bookname";

        Criteria criteria = new Criteria("libCode").is(libCode)
                .and(new Criteria(field).matches(keyword));  // 🔥 핵심 변경

        Sort sort = switch (sortType) {
            case "like" -> Sort.by(Sort.Order.desc("likeCount"));
            case "review" -> Sort.by(Sort.Order.desc("reviewCount"));
            case "new" -> Sort.by(Sort.Order.desc("publicationDate"));
            case "old" -> Sort.by(Sort.Order.asc("publicationDate"));
            default -> Sort.unsorted();
        };

        int page = offset / limit;
        Query query = new CriteriaQuery(criteria, PageRequest.of(page, limit, sort));

        SearchHits<LibraryBookSearchDocument> hits = operations.search(query, LibraryBookSearchDocument.class);

        return LibraryBookSearchResponseDto.builder()
                .totalCount(hits.getTotalHits())
                .results(hits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .toList())
                .build();
    }


    // ✏️ 자동완성 검색
    public List<LibraryBookSearchDocument> searchBookAutocomplete(String libCode, String keyword) {
        Criteria criteria = new Criteria("libCode").is(libCode)
                .and(new Criteria("bookname").matches(keyword)); // ✅ 핵심 변경

        Query query = new CriteriaQuery(
                criteria,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("likeCount")))
        );

        SearchHits<LibraryBookSearchDocument> hits = operations.search(query, LibraryBookSearchDocument.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
