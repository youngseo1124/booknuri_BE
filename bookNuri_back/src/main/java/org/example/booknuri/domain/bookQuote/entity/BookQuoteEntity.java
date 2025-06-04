package org.example.booknuri.domain.bookQuote.entity;

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
        name = "book_quotes"
)
public class BookQuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  인용 문장
    @Column(length = 1000, nullable = false)
    private String quoteText;

    //  스타일 정보
    @Column(nullable = false)
    private Float fontScale;

    @Column(nullable = false)
    private String fontColor;

    @Column(nullable = false)
    private int backgroundId;

    @Column(nullable = false)
    private boolean isActive = true;

    // 공개 여부
    @Column(nullable = false)
    private boolean visibleToPublic;

    // 어떤 책의 인용인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    //  누가 작성했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //  작성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    private LocalDateTime updatedAt;

    //  좋아요 수 (캐시)
    @Column(nullable = false)
    private int likeCount;

    // 좋아요 수 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    // 인용 수정 메서드
    public void updateQuote(String quoteText, Float fontScale, String fontColor, int backgroundId, boolean visibleToPublic) {
        this.quoteText = quoteText;
        this.fontScale = fontScale;
        this.fontColor = fontColor;
        this.backgroundId = backgroundId;
        this.visibleToPublic = visibleToPublic;
        this.updatedAt = LocalDateTime.now();
    }
}
