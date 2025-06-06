package org.example.booknuri.domain.Log.entity;

//자정마다 스케쥴드로 저장함 ㅇㅇ
import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.book.entity.BookEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "book_view_count_log",
        indexes = {
                @Index(name = "idx_date_book", columnList = "date, book_id")
        }
)
public class BookViewCountLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 책의 조회수인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    // 조회 집계 날짜 (ex. 2025-05-28)
    @Column(nullable = false)
    private java.time.LocalDate date;

    // 몇 번 조회됐는지
    @Column(nullable = false)
    private int viewCount;
}
