package org.example.booknuri.domain.book.controller;

import org.example.booknuri.domain.book.dto.BookGroupedPageResponseDto;
import org.example.booknuri.domain.bookReview_.converter.BookReviewConverter;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.bookReview_.service.BookReviewService;
import org.example.booknuri.domain.Log.service.BookViewLogService;
import org.example.booknuri.domain.Log.service.UserBookViewLogService;
import org.example.booknuri.domain.myBookshelf_.repository.MyShelfBookRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    private final BookReviewService bookReviewService;
    private final MyShelfBookRepository myBookshelfRepository;


    @GetMapping("/{isbn13}")
    public ResponseEntity<?> getBookDetail(@PathVariable String isbn13,
                                           @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            // 0. ISBN 존재 검사
            BookEntity bookEntity = bookService.getBookEntityByIsbn(isbn13);

            // 1. 최근 본 책 로그 저장
            userBookViewLogService.saveRecentView(user, bookEntity);

            // 2. 책 정보
            BookInfoResponseDto bookInfo = bookService.getBookDetailByIsbn(isbn13);

            // 3. 내가 책장에 담았는지 여부 확인
            boolean isAdded = myBookshelfRepository.existsByUserAndBook(user, bookEntity);

            // 4. 로그 저장(비동기용)
            bookViewLogService.logBookView(user, bookEntity);

            // 5. 응답 반환
            return ResponseEntity.ok(
                    BookTotalInfoDto.builder()
                            .bookInfo(bookInfo)
                            .addedToBookshelf(isAdded)
                            .build()
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    @GetMapping("/my-ip")
    public String getMyIp() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("https://api.ipify.org", String.class);
    }

    //  ISBN 존재 여부 확인 API
    @GetMapping("/exist/{isbn13}")
    public ResponseEntity<?> checkBookExistence(@PathVariable String isbn13) {
        boolean exists = bookService.existsBookByIsbn(isbn13);
        return ResponseEntity.ok(exists);
    }



    /*
    * [내가 작성한 글 기준 책 목록 조회 API]
    GET /book/my/grouped → 사용자가 작성한 리뷰/인용/독후감 중 선택한 타입 기준으로, 작성한 책들 목록을 최신 작성일 순으로 반환하는 API (검색 키워드 + 페이징 지원)*/
    @GetMapping("/my/grouped")
    public ResponseEntity<?> getGroupedBooksByType(
            @AuthenticationPrincipal CustomUser currentUser,
            @RequestParam(defaultValue = "review") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            BookGroupedPageResponseDto response =
                    bookService.getGroupedBooksByTypeAndKeywordSimple(user, type, keyword, offset, limit);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
