package org.example.booknuri.domain.BookReview.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.entity.BookReviewLikeEntity;
import org.example.booknuri.domain.BookReview.repository.BookReviewLikeRepository;
import org.example.booknuri.domain.BookReview.repository.BookReviewRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReviewLikeService {

    private final BookReviewLikeRepository likeRepository;
    private final BookReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 좋아요 토글 기능 (있으면 취소, 없으면 등록)
    public boolean toggleLike(Long reviewId, String username) {
        // 1. 유저 정보 가져오기
        UserEntity user = userRepository.findByUsername(username);

        // 2. 리뷰 정보 가져오기
        BookReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        // 3. 기존 좋아요 있는지 확인
        Optional<BookReviewLikeEntity> existing = likeRepository.findByUserAndReview(user, review);

        if (existing.isPresent()) {
            // 4. 이미 좋아요 했으면 취소하고 → 좋아요 수 감소
            likeRepository.delete(existing.get());
            review.decreaseLikeCount();
            reviewRepository.save(review);
            return false;
        } else {
            // 5. 없으면 새로 좋아요 등록 → 좋아요 수 증가
            BookReviewLikeEntity like = BookReviewLikeEntity.builder()
                    .user(user)
                    .review(review)
                    .likedAt(new Date())
                    .build();

            likeRepository.save(like);
            review.increaseLikeCount();
            reviewRepository.save(review);
            return true;
        }
    }
}
