package org.example.booknuri.domain.Log.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.Log.service.UserBookViewLogService;
import org.example.booknuri.domain.book.converter.BookConverter;
import org.example.booknuri.domain.book.dto.BookSimpleInfoDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final UserBookViewLogService userBookViewLogService;
    private final UserService userService;
    private final BookConverter bookConverter;

    @GetMapping("/recent-books")
    public ResponseEntity<?> getRecentlyViewedBooks(@AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        List<BookEntity> books = userBookViewLogService.getRecentViewedBooks(user);
        List<BookSimpleInfoDto> result = books.stream()
                .map(bookConverter::toBookSimpleInfoDto)
                .toList();

        return ResponseEntity.ok(result);
    }
}
