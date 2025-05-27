package org.example.booknuri.domain.Log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDateTime;

//유저가 최근 본 책들 저장

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_book_view_log",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_book", columnNames = {"user_id", "book_id"}),
        indexes = @Index(name = "idx_user_viewed", columnList = "user_id, viewed_at DESC"))
public class UserBookViewLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(nullable = false)
    private LocalDateTime viewedAt;
}
