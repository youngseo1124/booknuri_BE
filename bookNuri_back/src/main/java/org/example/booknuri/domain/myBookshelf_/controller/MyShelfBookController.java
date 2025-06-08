package org.example.booknuri.domain.myBookshelf_.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookRequestDto;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookWithExtrasResponseDto;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.myBookshelf_.service.MyShelfBookService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shelf-book")
public class MyShelfBookController {

    private final MyShelfBookService myShelfBookService;

    //책장추가
    @PostMapping("/add")
    public ResponseEntity<?> addToShelf(@AuthenticationPrincipal CustomUser currentUser,
                                        @RequestBody MyShelfBookRequestDto requestDto) {
        boolean added = myShelfBookService.addToShelf(currentUser.getUsername(), requestDto.getIsbn13());

        if (added) {
            return ResponseEntity.ok("책장에 추가 완료!");
        } else {
            return ResponseEntity.ok("이미 책장에 추가된 책입니다.");
        }
    }


    //책장 상태 변경
    @PutMapping("/status/{isbn13}")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal CustomUser currentUser,
                                          @PathVariable String isbn13,
                                          @RequestParam("status") MyShelfBookEntity.BookStatus status) {
        myShelfBookService.updateStatus(currentUser.getUsername(), isbn13, status);
        return ResponseEntity.ok("책 상태 수정 완료!");
    }

    // 인생책 토글
    @PutMapping("/life-book/{isbn13}")
    public ResponseEntity<?> toggleLifeBook(@AuthenticationPrincipal CustomUser currentUser,
                                            @PathVariable String isbn13) {
        myShelfBookService.toggleLifeBook(currentUser.getUsername(), isbn13);
        return ResponseEntity.ok("인생책 상태가 변경되었습니다!");
    }

    //책장에서책 삭제
    @DeleteMapping("/remove/{isbn13}")
    public ResponseEntity<?> removeFromShelf(@AuthenticationPrincipal CustomUser currentUser,
                                             @PathVariable String isbn13) {
        myShelfBookService.removeBook(currentUser.getUsername(), isbn13);
        return ResponseEntity.ok("책장이에서 삭제되었습니다.");
    }


    //내 책장 책 목록
    @GetMapping("/my")
    public ResponseEntity<?> getMyShelfWithExtras(
            @AuthenticationPrincipal CustomUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) MyShelfBookEntity.BookStatus status // status는 선택
    ) {
        Page<MyShelfBookWithExtrasResponseDto> result =
                myShelfBookService.getMyShelfWithExtras(currentUser.getUsername(), page, size, status);

        return ResponseEntity.ok(result);
    }


}
