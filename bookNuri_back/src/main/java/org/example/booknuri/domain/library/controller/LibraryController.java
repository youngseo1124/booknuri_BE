package org.example.booknuri.domain.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.example.booknuri.domain.library.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    // 전체 도서관 조회 (keyword 있을 경우 포함)
    @GetMapping("/all")
    public ResponseEntity<List<LibraryResponseDto>> getAllLibrariesPaged(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String keyword
    ) {
        return libraryService.getAllLibrariesPaged(offset, limit, keyword);
    }

    //(페이지네이션o)시(지역) 기반 검색 (keyword 있을 경우 포함)
    @GetMapping("/search/region")
    public ResponseEntity<List<LibraryResponseDto>> getBySiPaged(
            @RequestParam String si,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String keyword
    ) {
        return libraryService.getLibrariesBySiPaged(si, offset, limit, keyword);
    }


    //(페이지네이션o)시 + 구 기반 검색 (keyword 있을 경우 포함)
    @GetMapping("/search/region/detail")
    public ResponseEntity<List<LibraryResponseDto>> getBySiAndGuPaged(
            @RequestParam String si,
            @RequestParam String gu,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String keyword
    ) {
        return libraryService.getLibrariesBySiAndGuPaged(si, gu, offset, limit, keyword);
    }

    // (페이지네이션o)도서관명 LIKE 검색
    @GetMapping("/search/name")
    public ResponseEntity<List<LibraryResponseDto>> searchByLibraryName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return libraryService.searchByLibraryName(keyword, offset, limit);
    }




}
