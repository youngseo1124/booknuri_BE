package org.example.booknuri.domain.BookReview.repository;


import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReviewEntity, Long> {

    //  특정 ISBN의 책 리뷰 리스트 (활성화된 것만)
    List<BookReviewEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13);

    //이미 리뷰 썻는지 확인용
    boolean existsByUserAndBook(UserEntity user, BookEntity book);


    //유저가 쓴 리뷰  찾기
    List<BookReviewEntity> findByUser(UserEntity user);

    //특정 책에 대해 유저가 쓴 리뷰 찾기
    Optional<BookReviewEntity> findByIdAndUser(Long id, UserEntity user);


}
