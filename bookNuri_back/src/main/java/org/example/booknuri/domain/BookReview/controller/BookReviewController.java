package org.example.booknuri.domain.BookReview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.BookReview.dto.BookReviewCreateRequestDto;
import org.example.booknuri.domain.BookReview.dto.BookReviewResponseDto;
import org.example.booknuri.domain.BookReview.dto.BookReviewUpdateRequestDto;
import org.example.booknuri.domain.BookReview.service.BookReviewService;
import org.example.booknuri.domain.book.dto.BookTotalInfoDto;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book/review")
public class BookReviewController {

    private final BookReviewService bookReviewService;
    private final UserService userService;


    @PostMapping
    public ResponseEntity<?> createBookReview(@RequestBody BookReviewCreateRequestDto dto,
                                              @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            BookTotalInfoDto result = bookReviewService.createReview(dto, user);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // 이미 리뷰 작성했을 경우
            // "message": "이미 이 책에 리뷰를 작성하셨습니다."
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 책 없음 등
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }



    // 리뷰 수정
    @PutMapping
    public ResponseEntity<?> updateReview(@RequestBody BookReviewUpdateRequestDto dto,
                                          @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookReviewService.updateReview(dto, user);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 수정되었습니다."));
    }

    //  리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                          @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookReviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(Map.of("message", "리뷰가 성공적으로 삭제되었습니다."));
    }


    //내가 쓴 리뷰
    @GetMapping("/my")
    public List<BookReviewResponseDto> getMyReviews(@AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReviewService.getMyReviews(user);
    }


}
