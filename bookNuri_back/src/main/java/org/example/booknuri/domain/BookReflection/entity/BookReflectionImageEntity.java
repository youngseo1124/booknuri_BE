package org.example.booknuri.domain.bookReflection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "book_reflection_image")
public class BookReflectionImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 독후감에 속한 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reflection_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reflection_image",
                    foreignKeyDefinition = "FOREIGN KEY (reflection_id) REFERENCES book_reflection(id) ON DELETE CASCADE"
            )
    )
    private BookReflectionEntity reflection;


    // 이미지 URL
    @Column(nullable = false)
    private String imageUrl;

    // 업로드 시간
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}
