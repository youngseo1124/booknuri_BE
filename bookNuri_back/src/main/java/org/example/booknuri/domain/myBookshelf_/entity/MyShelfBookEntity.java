package org.example.booknuri.domain.myBookshelf_.entity;
//내 책장에 담긴 책
import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_shelf_books",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyShelfBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookEntity book;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private boolean lifeBook;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    public enum BookStatus {
        WANT_TO_READ, READING, FINISHED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime .now();
        if (this.status == null) this.status = BookStatus.WANT_TO_READ;
    }

    // 📌 책 상태 변경 메서드
    public void updateStatus(BookStatus status) {
        this.status = status;
        if (status == BookStatus.FINISHED) {
            this.finishedAt = LocalDateTime .now();
        } else {
            this.finishedAt = null;
        }
    }

    // 📌 인생책 여부 토글 메서드
    public void updateLifeBook(boolean isLifeBook) {
        this.lifeBook = isLifeBook;
    }
}
