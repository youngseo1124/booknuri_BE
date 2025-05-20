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

    @GetMapping("/all/paged")
    public ResponseEntity<List<LibraryResponseDto>> getAllLibrariesPaged(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return libraryService.getAllLibrariesPaged(offset, limit);
    }

    // 지역 기반 페이지네이션
    @GetMapping("/region/paged")
    public ResponseEntity<List<LibraryResponseDto>> getBySiPaged(
            @RequestParam String si,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return libraryService.getLibrariesBySiPaged(si, offset, limit);
    }

    // 지역 + 시군구 기반 페이지네이션
    @GetMapping("/region/detail/paged")
    public ResponseEntity<List<LibraryResponseDto>> getBySiAndGuPaged(
            @RequestParam String si,
            @RequestParam String gu,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return libraryService.getLibrariesBySiAndGuPaged(si, gu, offset, limit);
    }



}
