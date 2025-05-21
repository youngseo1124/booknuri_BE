


package org.example.booknuri.domain.user.repository;

import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface UsersRepository extends JpaRepository<UserEntity, String> {

    public UserEntity findByUsername(String username);


    // 아이디(유저네임) 중복 체크를 위한 메서드 (JPA가 자동으로 구현)
    boolean existsByUsername(String username);

    // 이메일 중복 체크를 위한 메서드
    boolean existsByEmail(String email);  // 이메일을 기준으로 중복 확인

}
