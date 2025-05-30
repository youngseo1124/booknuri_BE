package org.example.booknuri.domain.bookReflection.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.user.entity.UserEntity;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "book_reflection_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "reflection_id"})
})
public class BookReflectionLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 좋아요 누른 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 좋아요 누른 독후감
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reflection_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_like_reflection_id",
                    foreignKeyDefinition = "FOREIGN KEY (reflection_id) REFERENCES book_reflections(id) ON DELETE CASCADE"
            )
    )
    private BookReflectionEntity reflection;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date likedAt;
}
