package org.example.booknuri.domain.BookReview.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "book_review_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
public class BookReviewLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    //좋아요 누른 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //좋아요 누른 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private BookReviewEntity review;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date likedAt;
}
