package org.example.booknuri.domain.myLibrary.entity;
import jakarta.persistence.*;
import lombok.*;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.user.entity.UserEntity;

/*
  MyLibraryEntity
 - 이 엔티티는 유저가 "내 도서관"으로 등록한 도서관 정보를 저장하는 테이블
 - 하나의 유저는 여러 개의 도서관을 등록할 수 있음 (1:N 관계)
 - 그 중 하나는 대표 도서관(isMain = 1)으로 설정 가능
*/

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "my_library")
public class MyLibraryEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어떤 유저가 이 도서관을 등록했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lib_code", referencedColumnName = "lib_code", nullable = false)
    private LibraryEntity library;
}
