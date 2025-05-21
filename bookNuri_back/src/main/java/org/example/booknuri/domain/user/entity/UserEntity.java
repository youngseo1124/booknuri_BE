
/*
<users 엔티티>--><db의 users테이블과 매핑됨>
-JPA 엔티티는 데이터베이스 테이블과 매핑되는 클래스예요
-JPA에서는 @Entity 어노테이션을 사용해서 클래스를 엔티티로 지정하면,
  Spring Boot가 실행될 때 해당 클래스에 대응하는 테이블을 자동으로 생성해줘.
  단, 데이터베이스에 테이블이 없으면 자동으로 생성되고,
  테이블이 있으면 기존 테이블을 기반으로 동작할 거야.
*
* */




package org.example.booknuri.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.springframework.web.bind.annotation.BindParam;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class UserEntity {


    @Id
    private String username;


    @Column
    private String password;

    @Column
    private String nickname;

    @Column( unique = true)  // 이메일 컬럼도 고유값을 갖도록 설정
    private String email;


    @Column
    private String gender; // "M", "F", "O" 등

    @Column
    private Integer birthYear; // 예: 1999



    @Column
    private String role;  // 사용자의 권한 (예: "ROLE_USER" 또는 "ROLE_ADMIN")

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updatedAt;

    @Column(nullable = false)
    private boolean enabled = true;

    //내 도서관
    @ManyToOne
    @JoinColumn(name = "my_library")
    private LibraryEntity myLibrary;




    //  @PrePersist  :엔티티가 생성될 때와 수정될 때 자동으로 메서드 실행되도록 해줌
    @PrePersist  // 객체가 저장되기 전에 실행되는 메서드
    public void prePersist() {
        // 생성 시간을 현재 시간으로 설정
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate  // 객체가 업데이트되기 전에 실행되는 메서드
    public void preUpdate() {
        // 수정 시간을 현재 시간으로 설정
        this.updatedAt = new Date();
    }
}

