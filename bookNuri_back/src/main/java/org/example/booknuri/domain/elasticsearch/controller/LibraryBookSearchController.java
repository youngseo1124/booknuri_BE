package org.example.booknuri.domain.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.elasticsearch.dto.LibraryBookSearchResponseDto;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookIndexService;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library-book-search")
public class LibraryBookSearchController {

    private final LibraryBookSearchService searchService;
    private final LibraryBookIndexService indexService;

    // ✅ 도서 검색 API (페이지네이션 추가)
    @GetMapping
    public ResponseEntity<LibraryBookSearchResponseDto> search(
            @RequestParam String libCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        LibraryBookSearchResponseDto result = searchService.searchBooks(libCode, keyword, sort, offset, limit);
        return ResponseEntity.ok(result);
    }

    // 자동완성 API
    @GetMapping("/autocomplete")
    public ResponseEntity<List<LibraryBookSearchDocument>> autocomplete(
            @RequestParam String libCode,
            @RequestParam String keyword
    ) {
        List<LibraryBookSearchDocument> result = searchService.searchBookAutocomplete(libCode, keyword);
        return ResponseEntity.ok(result);
    }

    // 색인 초기화
    @PostMapping("/init")
    public ResponseEntity<String> initIndex() {
        indexService.indexAllLibraryBooksInBatch();
        return ResponseEntity.ok("색인 작업 완료!");
    }
}
