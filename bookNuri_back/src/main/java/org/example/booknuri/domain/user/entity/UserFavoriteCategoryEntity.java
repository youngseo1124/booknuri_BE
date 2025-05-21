package org.example.booknuri.domain.user.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.entity.MainCategory;


@Entity
@Table(name = "user_favorite_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavoriteCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private MainCategory category;
}
