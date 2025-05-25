package org.example.booknuri.domain.BookReview.repository;


import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.entity.BookReviewLikeEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewLikeRepository extends JpaRepository<BookReviewLikeEntity, Long> {

    // 로그인 유저가 해당 리뷰에 좋아요 눌렀는지 확인
    boolean existsByUserAndReview(UserEntity user, BookReviewEntity review);


}
