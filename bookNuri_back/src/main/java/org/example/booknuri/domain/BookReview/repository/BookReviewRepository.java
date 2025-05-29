package org.example.booknuri.domain.BookReview.repository;


import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReviewEntity, Long> {

    //  특정 ISBN의 책 리뷰 리스트 (활성화된 것만)
    List<BookReviewEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13);

    //이미 리뷰 썻는지 확인용
    boolean existsByUserAndBook(UserEntity user, BookEntity book);


    //유저가 쓴 리뷰  찾기(+페이지네이션)
    Page<BookReviewEntity> findByUser(UserEntity user, Pageable pageable);


    //특정 책에 대해 유저가 쓴 리뷰 찾기(리뷰아이디로)
    Optional<BookReviewEntity> findByIdAndUser(Long id, UserEntity user);

    //특정 책에 대해 유저가 쓴 리뷰 찾기(북엔티티로)
    Optional<BookReviewEntity> findByUserAndBook(UserEntity user, BookEntity book);


    //특정책에 대해 활성화T상태인 리뷰들 (페이지네이션 O)
    Page<BookReviewEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13, Pageable pageable);



    @Query("SELECT AVG(r.rating) FROM BookReviewEntity r WHERE r.book.isbn13 = :isbn13 AND r.isActive = true")
    Double getAverageReviewRatingByIsbn13(@Param("isbn13") String isbn13);


    // 리뷰 개수 조회 (isActive = true인 것만)
    int countByBook_Isbn13AndIsActiveTrue(String isbn13);




}
