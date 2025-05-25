package org.example.booknuri.domain.BookReview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.BookReview.dto.BookReviewCreateRequestDto;
import org.example.booknuri.domain.BookReview.service.BookReviewService;
import org.example.booknuri.domain.book.dto.BookTotalInfoDto;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book/review")
public class BookReviewController {

    private final BookReviewService bookReviewService;
    private final UserService userService;


    @PostMapping
    public BookTotalInfoDto createBookReview(@RequestBody BookReviewCreateRequestDto dto,
                                             @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user=userService.getUserByUsername(currentUser.getUsername());
        return bookReviewService.createReview(dto, user);
    }

}
