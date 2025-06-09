package org.example.booknuri.global.DBConstruction.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.service.LibraryService;
import org.example.booknuri.global.DBConstruction.service.BookAsyncLauncher;
import org.example.booknuri.global.DBConstruction.service.DbConstructionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/db")
public class DbConStructionController {

    private final DbConstructionService dbConstructionService;
    private final LibraryService libraryService;
    private final BookAsyncLauncher bookAsyncLauncher;

    //전국 도서관 정보 db 구축하기
    //(libraries+ libraries_region)
    @PostMapping("/libraries")
    public ResponseEntity<String> initLibraryData() {
        dbConstructionService.saveAllLibrariesFromApi();
        return ResponseEntity.ok("도서관 데이터 DB 저장 완료!");
    }

    // 컨트롤러에서 이렇게 호출
    @PostMapping("/chungnam-books")
    public ResponseEntity<String> startChungnamBookJob(
            @RequestParam(value = "startPage", required = false) Integer startPage,
            @RequestParam(value = "endPage", required = false) Integer endPage
    ) {
        bookAsyncLauncher.launchChungnamSaveJobWithPaging(startPage, endPage);
        return ResponseEntity.ok("📦 충청남도 도서 저장 백그라운드 실행 중!");
    }




}
