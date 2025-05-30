package org.example.booknuri.domain.bookReview_.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReview_.service.BookReviewLikeService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/review/like")
public class BookReviewLikeController {

    private final BookReviewLikeService bookReviewLikeService;

    @PostMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        boolean liked = bookReviewLikeService.toggleLike(reviewId, currentUser.getUsername());

        //  응답을 JSON 객체로 감싸서 보내기
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
