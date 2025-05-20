package org.example.booknuri.domain.bookQuote.entity;

//책 인용 엔티티
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
@Table(name = "book_quotes")  // 인용 문구 저장 테이블
public class BookQuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✍ 사용자가 입력한 인용 문장
    @Column(length = 1500, nullable = false)
    private String quoteText;

    // 글씨 크기 비율 (0.0 ~ 1.0) → 화면 가로폭 * 이 값 = 글자 크기
    @Column(nullable = false)
    private Float fontScale;


    //  글자 색상: HEX 코드로 저장 (예: "#FFFFFF", "black")
    @Column(nullable = false)
    private String fontColor;

    // 어떤 배경이미지를 썼는지 (1~10 중 선택)
    @Column(nullable = false)
    private int backgroundId;

    //  어떤 책에 대한 인용인지 (BookEntity 와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookEntity book;

    //  인용 작성 유저 (UserEntity 와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    //  인용 작성 시각
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;
}
