package org.example.booknuri.domain.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library-book-search")
public class LibraryBookSearchController {

    private final LibraryBookSearchService searchService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String libCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "score") String sort
    ) {
        var result = searchService.searchBooks(libCode, keyword, sort);
        return ResponseEntity.ok(result);
    }
}
