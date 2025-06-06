package org.example.booknuri.domain.bookReview_.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReview_.converter.MyReviewConverter;
import org.example.booknuri.domain.bookReview_.dto.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.bookReview_.converter.BookReviewConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BookReviewConverter bookReviewConverter;
    private final MyReviewConverter myReviewConverter;
    private final BookReviewLikeService bookReviewLikeService;


    //내가 이미 이 책에 리뷰썼는지 아닌지 확인(이미 썼으면 T, 아직 안썻으면 F반환)
    public boolean checkAlreadyReviewed(String isbn13, UserEntity user) {
        // 👉 master1124는 항상 false 반환
        if ("master1124".equals(user.getUsername())) {
            return false;
        }

        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));

        return bookReviewRepository.existsByUserAndBook(user, book);
    }



    //리뷰 쓰기 로직
    public void createReview(BookReviewCreateRequestDto dto, UserEntity user) {
        // 1. 책 조회
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        //  2. 중복 리뷰 체크
        // ✅ "master1124"가 아닌 경우만 중복 리뷰 체크
        if (!user.getUsername().equals("master1124")) {
            boolean alreadyReviewed = bookReviewRepository.existsByUserAndBook(user, book);
            if (alreadyReviewed) {
                throw new IllegalStateException("이미 이 책에 리뷰를 작성하셨습니다.");
            }
        }

        // 3. 리뷰 생성 및 저장
        BookReviewEntity review = bookReviewConverter.toEntity(dto, book, user);
        bookReviewRepository.save(review);
    }

    //리뷰 수정화면에서 기존 특정책에 대한 내 리뷰 가져올떄 쓰는 서비스
    public BookReviewResponseDto getMyReviewForBook(String isbn13, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));

        BookReviewEntity review = bookReviewRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("작성한 리뷰가 없습니다."));

        return bookReviewConverter.toDto(review, user);
    }






    // 리뷰 수정
    public void updateReview(BookReviewUpdateRequestDto dto, UserEntity user) {
        BookReviewEntity review = bookReviewRepository.findByIdAndUser(dto.getReviewId(), user)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        review.updateReview(dto.getContent(), dto.getRating(),dto.isContainsSpoiler());
    }

    //내가 쓴 리뷰들 (책 정보 포함된 MyReviewResponseDto로 변경)
    public List<MyReviewResponseDto> getMyReviews(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Page 객체로 받아오기
        Page<BookReviewEntity> page = bookReviewRepository.findByUser(user, pageable);

        // 리스트 추출
        List<BookReviewEntity> reviews = page.getContent();

        //
        return myReviewConverter.toDtoList(reviews, user);
    }




    // 리뷰 삭제
    public void deleteReview(Long reviewId, UserEntity user) {
        BookReviewEntity review = bookReviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 리뷰가 없습니다."));
        bookReviewRepository.delete(review);
    }


    //특정책에 대해한 리뷰들 불러오기(정렬 필터링 선택가능)
    public List<BookReviewResponseDto> getReviewsByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort));
        Page<BookReviewEntity> page = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(isbn13, pageable);
        List<BookReviewEntity> reviews = page.getContent();


        return bookReviewConverter.toDtoList(reviews, currentUser);
    }

    private Sort getSortOrder(String sort) {
        return switch (sort.toLowerCase()) {
            case "like" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "high" -> Sort.by(Sort.Direction.DESC, "rating");
            case "low" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    //리뷰 별점 분포
    public BookReviewListResponseDto getReviewsSummaryByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort));
        Page<BookReviewEntity> page = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(isbn13, pageable);
        List<BookReviewEntity> reviews = page.getContent();

        // 평균 별점 계산
        Double avg = bookReviewRepository.getAverageReviewRatingByIsbn13(isbn13);
        double averageRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        //  별점 분포 계산
        List<BookReviewEntity> all = bookReviewRepository.findByBook_Isbn13AndIsActiveTrue(isbn13); // 전체 불러옴
        Map<Integer, Integer> ratingDistribution = getRatingBuckets(all);

        //  변환
        List<BookReviewResponseDto> dtos = bookReviewConverter.toDtoList(reviews, currentUser);
        int totalCount= bookReviewRepository. countByBook_Isbn13AndIsActiveTrue(isbn13);

        return BookReviewListResponseDto.builder()
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .reviews(dtos)
                .totalCount(totalCount)
                .build();
    }

    //별점 분포 로직
    private Map<Integer, Integer> getRatingBuckets(List<BookReviewEntity> reviews) {
        Map<Integer, Integer> map = new HashMap<>();
        int[] keys = {10, 8, 6, 4, 2};

        for (int key : keys) {
            map.put(key, 0); // 기본 0으로 초기화
        }

        for (BookReviewEntity r : reviews) {
            int rounded = (int) Math.ceil(r.getRating() / 2.0) * 2; // 1~10 => 2단위 버킷
            rounded = Math.min(10, Math.max(2, rounded)); // 예외 방지
            map.put(rounded, map.getOrDefault(rounded, 0) + 1);
        }

        return map;
    }




}
