package org.example.booknuri.domain.bookQuote.entity;
import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDateTime;

/*
 📌 BookQuoteReportEntity
 - 특정 유저가 어떤 인용(BookQuote)에 대해 "신고"를 한 기록을 저장하는 테이블
 - 동일 유저가 같은 인용에 두 번 이상 신고 못하게 unique 제약 설정
 - 추후 신고 사유 통계, 관리자 알림 등에 활용 가능
*/

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_quote_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "quote_id"})
        }
)
public class BookQuoteReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  신고한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    // ✍ 어떤 인용을 신고했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "quote_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_report_quote_id",
                    foreignKeyDefinition = "FOREIGN KEY (quote_id) REFERENCES book_quotes(id) ON DELETE CASCADE"
            )
    )
    private BookQuoteEntity quote;

    //  신고 사유 (50자 제한)
    @Column(length = 50, nullable = false)
    private String reason;

    //  신고한 날짜/시간
    @Column(nullable = false)
    private LocalDateTime reportedAt;
}
