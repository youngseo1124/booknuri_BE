package org.example.booknuri.domain.bookQuote.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.util.Date;

/*
 📌 BookQuoteLikeEntity
 - 특정 유저가 특정 인용(BookQuote)에 좋아요를 누른 기록을 저장하는 테이블이야
 - 한 유저는 한 인용에 한 번만 좋아요 가능 (unique 제약 걸려있음)
 - 나중에 좋아요 순 정렬 / 내가 누른 인용 체크할 때 사용 가능
*/

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "book_quote_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "quote_id"})
})
public class BookQuoteLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  좋아요 누른 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 좋아요 대상 인용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "quote_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_like_quote_id",
                    foreignKeyDefinition = "FOREIGN KEY (quote_id) REFERENCES book_quotes(id) ON DELETE CASCADE"
            )
    )
    private BookQuoteEntity quote;

    //  좋아요 누른 시각
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date likedAt;
}
