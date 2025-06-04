package org.example.booknuri.domain.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookIndexService;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/library-book-search")
public class LibraryBookSearchController {

    private final LibraryBookSearchService searchService;
    private final LibraryBookIndexService indexService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String libCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "score") String sort
    ) {
        var result = searchService.searchBooks(libCode, keyword, sort);
        return ResponseEntity.ok(result);
    }


    //자동완성 기능
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(
            @RequestParam String libCode,
            @RequestParam String keyword
    ) {
        var result = searchService.searchBookAutocomplete(libCode, keyword);
        return ResponseEntity.ok(result);
    }




    // 도서 색인 초기화
    @PostMapping("/init")
    public ResponseEntity<?> initIndex() {
        indexService.indexAllLibraryBooksInBatch();
        return ResponseEntity.ok("색인 작업 완료!");
    }

}
