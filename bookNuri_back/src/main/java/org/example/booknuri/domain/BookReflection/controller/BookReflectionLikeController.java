package org.example.booknuri.domain.bookReflection.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection.service.BookReflectionLikeService;
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
@RequestMapping("/book/reflection/like")
public class BookReflectionLikeController {

    private final BookReflectionLikeService bookReflectionLikeService;

    @PostMapping("/{reflectionId}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long reflectionId,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        boolean liked = bookReflectionLikeService.toggleLike(reflectionId, currentUser.getUsername());

        // 응답을 JSON 객체로 감싸서 보내기
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
