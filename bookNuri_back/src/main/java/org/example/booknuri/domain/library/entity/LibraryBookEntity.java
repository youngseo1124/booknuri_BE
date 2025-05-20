package org.example.booknuri.domain.library.entity;

//  이 테이블은 "어떤 도서관(libCode)이 어떤 책(BookEntity)을 보유하고 있는지"를 저장하는 테이블임

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;


@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library_book")
public class LibraryBookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 도서관이 보유하고 있는지
    @Column(name = "lib_code")
    private String libCode;

    // 어떤 책인지 (BookEntity 내부 ID 기준으로 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookEntity book;

    // 해당 책이 도서관에 등록된 날짜
    private String regDate;
}
