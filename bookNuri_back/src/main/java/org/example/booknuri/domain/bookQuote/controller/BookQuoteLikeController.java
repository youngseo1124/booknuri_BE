package org.example.booknuri.domain.bookQuote.controller;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookQuote.service.BookQuoteLikeService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book/quote/like")
public class BookQuoteLikeController {

    private final BookQuoteLikeService bookQuoteLikeService;

    @PostMapping("/{quoteId}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long quoteId,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        boolean liked = bookQuoteLikeService.toggleLike(quoteId, currentUser.getUsername());

        //  응답 JSON 형태로 반환
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
