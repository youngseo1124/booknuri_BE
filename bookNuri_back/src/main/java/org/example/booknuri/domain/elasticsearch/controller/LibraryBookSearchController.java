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

    // 도서 검색 API
    @GetMapping
    public ResponseEntity<LibraryBookSearchResponseDto> search(
            @RequestParam String libCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "score") String sort
    ) {
        LibraryBookSearchResponseDto result = searchService.searchBooks(libCode, keyword, sort);
        return ResponseEntity.ok(result);
    }

    //  자동완성 API
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
