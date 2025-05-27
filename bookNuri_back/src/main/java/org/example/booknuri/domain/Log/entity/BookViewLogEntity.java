
package org.example.booknuri.domain.Log.entity;


//어떤 유저가 어떤 책을 봤는지 기록용. (나중에 이것 기반으로 책 인기순추천/ 맞춤 책 추천 할것임)

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
@Table(name = "book_view_log",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_book", columnNames = {"user_id", "book_id"}),
        indexes = @Index(name = "idx_user_viewed", columnList = "user_id, viewed_at DESC"))
public class BookViewLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 어떤 책을
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    // 성별 (통계용)
    private String gender;

    // 출생년도 (통계용)

    //유저엔티티 BIRTH에선 1998112이렇게 저장됏다면 이 엔티티에선 BIRTHYEAR=1998일케 저장됨
    private Integer birthYear;

    // 언제 조회했는지
    private LocalDateTime viewedAt;


    //갱신용 메서드
    //유저가 a b c d a순으로 책 보면 a같은 경우는최신순 한번만 저장됨(이때 기존 엔티티에서 viewat만 업데이트)
    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
