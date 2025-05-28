package org.example.booknuri.domain.BookReview.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_review_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "review_id"})
        }
)
public class BookReviewReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  누가 신고했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    // 어떤 리뷰를 신고했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_report_review_id",
                    foreignKeyDefinition = "FOREIGN KEY (review_id) REFERENCES book_reviews(id) ON DELETE CASCADE"
            )
    )
    private BookReviewEntity review;



    // 신고 사유
    @Column(length = 50, nullable = false)
    private String reason;

    //  신고 일시
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime reportedAt;
}
