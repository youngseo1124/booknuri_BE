package org.example.booknuri.domain.bookQuote.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.bookQuote.dto.*;
import org.example.booknuri.domain.bookQuote.service.BookQuoteService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book/quote")
public class BookQuoteController {

    private final BookQuoteService bookQuoteService;
    private final UserService userService;

    //  인용 작성
    @PostMapping
    public ResponseEntity<?> createQuote(@RequestBody BookQuoteCreateRequestDto dto,
                                         @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            bookQuoteService.createQuote(dto, user);
            return ResponseEntity.ok(Map.of("message", "인용 작성 완료!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    //  인용 수정
    @PutMapping
    public ResponseEntity<?> updateQuote(@RequestBody BookQuoteUpdateRequestDto dto,
                                         @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookQuoteService.updateQuote(dto, user);
        return ResponseEntity.ok(Map.of("message", "인용이 성공적으로 수정되었습니다."));
    }

    //  인용 삭제
    @DeleteMapping("/{quoteId}")
    public ResponseEntity<?> deleteQuote(@PathVariable Long quoteId,
                                         @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookQuoteService.deleteQuote(quoteId, user);
        return ResponseEntity.ok(Map.of("message", "인용이 성공적으로 삭제되었습니다."));
    }

    //  내가 쓴 인용 목록 (마이페이지용)
    @GetMapping("/my")
    public List<MyQuoteResponseDto> getMyQuotes(@AuthenticationPrincipal CustomUser currentUser,
                                                @RequestParam(defaultValue = "0") int offset,
                                                @RequestParam(defaultValue = "10") int limit) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookQuoteService.getMyQuotes(user, offset, limit);
    }

    //  인용 수정화면용 단일 조회
    @GetMapping("/my/{quoteId}")
    public ResponseEntity<?> getMyQuoteById(@PathVariable Long quoteId,
                                            @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        MyQuoteResponseDto dto = bookQuoteService.getMyQuoteFullById(quoteId, user);
        return ResponseEntity.ok(dto);
    }

    //  특정 책의 공개된 인용 목록 (책 상세페이지용), 정렬 가능
    @GetMapping("/list/{isbn13}")
    public BookQuoteListResponseDto getQuotesByBook(@PathVariable String isbn13,
                                                    @RequestParam(defaultValue = "new") String sort, // ✅ 추가
                                                    @RequestParam(defaultValue = "0") int offset,
                                                    @RequestParam(defaultValue = "10") int limit,
                                                    @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookQuoteService.getQuotesByBook(isbn13, sort, offset, limit, user);
    }


    @PostMapping("/ocr")
    public ResponseEntity<?> extractTextFromImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            String extractedText = bookQuoteService.extractTextFromImage(imageFile);
            return ResponseEntity.ok(Map.of("text", extractedText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "OCR 처리 중 오류 발생"));
        }
    }

    //인기인용 목록
    @GetMapping("/popular")
    public BookQuoteListResponseDto getPopularQuotes(@AuthenticationPrincipal CustomUser currentUser,
                                                     @RequestParam(defaultValue = "0") int offset,
                                                     @RequestParam(defaultValue = "10") int limit) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookQuoteService.getPopularQuotes(offset, limit, user);
    }


}
