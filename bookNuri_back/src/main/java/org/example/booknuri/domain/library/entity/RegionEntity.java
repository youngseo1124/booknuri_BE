package org.example.booknuri.domain.library.entity;


import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library_regions")
public class RegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String si;  // (지역) 시/도

    @Column(nullable = false)
    private String gu;  // 시군구
}