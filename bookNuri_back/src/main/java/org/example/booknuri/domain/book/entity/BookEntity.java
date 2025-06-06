package org.example.booknuri.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookname;

    @Column(length = 700)
    private String authors;

    private String publisher;

    //발행년도
    private Integer publicationDate;

    @Column(length = 20, unique = true)
    private String isbn13;

    //응답속도 위해 text아닌 varchar
    @Column(length = 1500)
    private String description;

    private String bookImageURL;

    @Column
    private String es;

    //분류 필드 3개
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_category_id")
    private MainCategory mainCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "middle_category_id")
    private MiddleCategory middleCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;
}
