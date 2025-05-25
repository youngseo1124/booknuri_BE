package org.example.booknuri.domain.BookReview.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.BookReview.dto.BookReviewCreateRequestDto;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.dto.BookTotalInfoDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.repository.BookReviewRepository;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.BookReview.converter.BookReviewConverter;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BookReviewConverter bookReviewConverter;


    //리뷰 쓰기 로직
    public BookTotalInfoDto createReview(BookReviewCreateRequestDto dto, UserEntity user) {
        // 1. 책 조회
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        // 2. 리뷰 생성 및 저장
        BookReviewEntity review = BookReviewEntity.builder()
                .book(book)
                .user(user)
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(new Date())
                .isActive(true)
                .likeCount(0)
                .build();

        bookReviewRepository.save(review);

        // 3. 책 정보 + 리뷰 리스트 묶어서 리턴
        BookInfoResponseDto bookInfo = bookService.getBookDetailByIsbn(dto.getIsbn13());

        List<BookReviewEntity> allReviews = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(dto.getIsbn13());

        return BookTotalInfoDto.builder()
                .bookInfo(bookInfo)
                .reviews(bookReviewConverter.toDtoList(allReviews, user))
                .build();
    }
}
