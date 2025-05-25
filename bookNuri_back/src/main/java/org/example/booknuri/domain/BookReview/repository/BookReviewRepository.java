package org.example.booknuri.domain.BookReview.repository;


import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.entity.BookReviewLikeEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReviewRepository extends JpaRepository<BookReviewEntity, Long> {

    //  특정 ISBN의 책 리뷰 리스트 (활성화된 것만)
    List<BookReviewEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13);

}
