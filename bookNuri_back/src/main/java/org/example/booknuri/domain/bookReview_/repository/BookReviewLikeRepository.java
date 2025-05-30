package org.example.booknuri.domain.bookReview_.repository;


import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview_.entity.BookReviewLikeEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookReviewLikeRepository extends JpaRepository<BookReviewLikeEntity, Long> {

    // 로그인 유저가 해당 리뷰에 좋아요 눌렀는지 확인
    boolean existsByUserAndReview(UserEntity user, BookReviewEntity review);

    // 유저가 해당 리뷰에 좋아요 눌렀는지 조회 (토글용)
    //	좋아요 삭제하거나 정보가 필요할 때
    Optional<BookReviewLikeEntity> findByUserAndReview(UserEntity user, BookReviewEntity review);

    // 리뷰 좋아요 수 카운트
    Long countByReview(BookReviewEntity review);

}
