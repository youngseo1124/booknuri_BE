package org.example.booknuri.domain.bookReflection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_reflection_reports",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "reflection_id"})
        }
)
public class BookReflectionReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 신고했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    // 어떤 독후감을 신고했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reflection_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_report_reflection_id",
                    foreignKeyDefinition = "FOREIGN KEY (reflection_id) REFERENCES book_reflections(id) ON DELETE CASCADE"
            )
    )
    private BookReflectionEntity reflection;

    // 신고 사유
    @Column(length = 50, nullable = false)
    private String reason;

    // 신고 일시
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime reportedAt;
}
