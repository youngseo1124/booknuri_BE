package org.example.booknuri.domain.BookReview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.BookReview.dto.*;
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


    //리뷰 썼는지 안썻는지 확인용
    @GetMapping("/exist/{isbn13}")
    public ResponseEntity<?> checkIfAlreadyReviewed(
            @PathVariable String isbn13,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        boolean alreadyReviewed = bookReviewService.checkAlreadyReviewed(isbn13, user);
        return ResponseEntity.ok(Map.of("alreadyReviewed", alreadyReviewed));
    }



    //리뷰작성
    @PostMapping
    public ResponseEntity<?> createBookReview(@RequestBody BookReviewCreateRequestDto dto,
                                              @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            bookReviewService.createReview(dto, user);
            return ResponseEntity.ok(Map.of("message", "리뷰 작성 완료!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 리뷰 수정할떄 기존 리뷰 불러오기
    @GetMapping("/my/{isbn13}")
    public ResponseEntity<?> getMyReviewForBook(@PathVariable String isbn13,
                                                @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        BookReviewResponseDto dto = bookReviewService.getMyReviewForBook(isbn13, user);
        return ResponseEntity.ok(dto);
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
    public List<MyReviewResponseDto> getMyReviews(
            @AuthenticationPrincipal CustomUser currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReviewService.getMyReviews(user, offset, limit);
    }

    // 특정 책의 모든 리뷰 조회 (정렬 + 페이지네이션)
    @GetMapping("/list/{isbn13}")
    public BookReviewListResponseDto getAllReviewsForBook(
            @PathVariable String isbn13,
            @RequestParam(defaultValue = "new") String sort,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReviewService.getReviewsSummaryByBook(isbn13, sort, offset, limit, user);
    }





}
