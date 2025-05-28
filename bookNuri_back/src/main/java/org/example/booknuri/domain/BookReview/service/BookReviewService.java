package org.example.booknuri.domain.BookReview.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.BookReview.dto.BookReviewCreateRequestDto;
import org.example.booknuri.domain.BookReview.dto.BookReviewResponseDto;
import org.example.booknuri.domain.BookReview.dto.BookReviewUpdateRequestDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
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

        //  2. 중복 리뷰 체크
        boolean alreadyReviewed = bookReviewRepository.existsByUserAndBook(user, book);
        if (alreadyReviewed) {
            throw new IllegalStateException("이미 이 책에 리뷰를 작성하셨습니다.");
        }

        // 3. 리뷰 생성 및 저장
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

        // 4. 책 정보 + 리뷰 리스트 묶어서 리턴
        BookInfoResponseDto bookInfo = bookService.getBookDetailByIsbn(dto.getIsbn13());
        List<BookReviewEntity> allReviews = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(dto.getIsbn13());

        return BookTotalInfoDto.builder()
                .bookInfo(bookInfo)
                .reviews(bookReviewConverter.toDtoList(allReviews, user))
                .build();
    }



    // 리뷰 수정
    public void updateReview(BookReviewUpdateRequestDto dto, UserEntity user) {
        BookReviewEntity review = bookReviewRepository.findByIdAndUser(dto.getReviewId(), user)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        review.updateReview(dto.getContent(), dto.getRating());
    }

    // 내가 쓴 리뷰 목록
    public List<BookReviewResponseDto> getMyReviews(UserEntity user) {
        List<BookReviewEntity> reviews = bookReviewRepository.findByUser(user);
        return bookReviewConverter.toDtoList(reviews, user);
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, UserEntity user) {
        BookReviewEntity review = bookReviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 리뷰가 없습니다."));
        bookReviewRepository.delete(review);
    }


}
