package org.example.booknuri.domain.bookReflection_.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_reflections"
)
public class BookReflectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   //독후감 제목
    @Column(nullable = false, length = 100)
    private String title;


    // 독후감 텍스트
    // 전체 내용 텍스트로
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    // ⭐ 별점 (1~10점)
    @Column(nullable = false)
    private int rating;

    // 어떤 책에 대한 독후감인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    // 어떤 유저가 작성했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 독후감 작성 시간
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 독후감 수정 시간
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    // 스포일러 여부 (기본 false)
    @Column(nullable = false)
    private boolean containsSpoiler;

    // 활성화 여부(신고로 인해 독후감 비활성화될 수도 있음)
    private boolean isActive;

    // ❤️ 좋아요 수 (캐시용, 기본값 0)
    @Column(nullable = false)
    private int likeCount;

    // 공개 여부 (기본 true)
    @Column(nullable = false)
    private boolean visibleToPublic;

    // 좋아요 수 증가 메서드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소 메서드
    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    // 독후감 수정 메서드
    public void updateReflection(String title, String content, int rating, boolean containsSpoiler, boolean visibleToPublic) {
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.containsSpoiler = containsSpoiler;
        this.visibleToPublic = visibleToPublic;
        this.updatedAt = LocalDateTime.now();
    }
}
