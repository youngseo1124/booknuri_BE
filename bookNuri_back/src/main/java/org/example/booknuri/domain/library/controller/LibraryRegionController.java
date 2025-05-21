package org.example.booknuri.domain.library.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.example.booknuri.domain.library.dto.RegionResponseDTO;
import org.example.booknuri.domain.library.service.LibraryRegionService;
import org.example.booknuri.domain.library.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/library/region")
public class LibraryRegionController {

    private final LibraryRegionService regionService;

    //모든 지역조합 dto리스트로 반환(페이지네이션x)
    @GetMapping("/all")
    public ResponseEntity<List<RegionResponseDTO>> getAllRegions() {
        return regionService.getAllRegions();
    }


}
