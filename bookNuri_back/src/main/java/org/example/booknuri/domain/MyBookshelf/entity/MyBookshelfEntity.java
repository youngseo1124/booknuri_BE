package org.example.booknuri.domain.myBookshelf.entity;

import jakarta.persistence.*;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDate;


@Entity
@Table(name = "my_bookshelf",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
public class MyBookshelfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // 책 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookEntity book;

    @Enumerated(EnumType.STRING)
    private BookStatus status; // WANT_TO_READ / READING / FINISHED

    private boolean lifeBook; // 인생책 여부

    public enum BookStatus {
        WANT_TO_READ, READING, FINISHED
    }

    // 담은 날짜
    private LocalDate createdAt;

    //  완독 날짜 (FINISHED일 때만 사용)
    private LocalDate finishedAt;

}
