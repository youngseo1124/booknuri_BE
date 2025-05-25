package org.example.booknuri.domain.BookReview.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "book_id"})
        }
)

public class BookReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 텍스트 (최대 1000자)
    @Column(length = 1000, nullable = false)
    private String content;

    // ⭐ 별점 (1~5점)
    @Column(nullable = false)
    private int rating;

    //  어떤 책에 대한 리뷰인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    //  어떤 유저가 작성했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 리뷰 작성 시간
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    //  리뷰 수정 시간
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    //활성화 여부
    private boolean isActive;

    // ❤️ 좋아요 수 (캐시용, 기본값 0)
    @Column(nullable = false)
    private int likeCount;

    //좋아요 수 증가 메서드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    //좋아요 수 감소 메서드
    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

}
