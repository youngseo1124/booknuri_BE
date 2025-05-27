package org.example.booknuri.domain.book.controller;

import org.example.booknuri.domain.BookReview.converter.BookReviewConverter;
import org.example.booknuri.domain.BookReview.dto.BookReviewResponseDto;
import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.repository.BookReviewRepository;
import org.example.booknuri.domain.Log.service.BookViewLogService;
import org.example.booknuri.domain.Log.service.UserBookViewLogService;
import org.example.booknuri.domain.book.converter.BookClinetApiInfoConverter;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.dto.BookTotalInfoDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.global.security.entity.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book")
public class BookController {

    private final RedisTemplate<String, String> redisTemplate;
    private final BookClinetApiInfoConverter bookInfoConverter;
    private final BookService bookService;
    private final BookReviewConverter bookReviewConverter;
    private final BookReviewRepository bookReviewRepository;
    private final BookViewLogService bookViewLogService;
    private final UserService userService;
    private final UserBookViewLogService userBookViewLogService;


    @GetMapping("/{isbn13}")
    public ResponseEntity<?> getBookDetail(@PathVariable String isbn13,
                                           @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());


        try {
            // 0. ISBN 존재 검사
            BookEntity bookEntity = bookService.getBookEntityByIsbn(isbn13);

            // 최근 본 책 로그 저장
            userBookViewLogService.saveRecentView(user, bookEntity);

            // 1. 책 정보 (캐시 or DB)
            BookInfoResponseDto bookInfo = bookService.getBookDetailByIsbn(isbn13);

            // 2. 리뷰 정보
            List<BookReviewEntity> reviews = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(isbn13);
            List<BookReviewResponseDto> reviewDtos = bookReviewConverter.toDtoList(reviews,user);

            // 3. 비동기 로그 저장
            bookViewLogService.logBookView(user, bookEntity);

            // 4. 최종 응답
            return ResponseEntity.ok(
                    BookTotalInfoDto.builder()
                            .bookInfo(bookInfo)
                            .reviews(reviewDtos)
                            .build()
            );

        } catch (IllegalArgumentException e) {
            // ISBN이 잘못된 경우
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




    @GetMapping("/my-ip")
    public String getMyIp() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("https://api.ipify.org", String.class);
    }

}
