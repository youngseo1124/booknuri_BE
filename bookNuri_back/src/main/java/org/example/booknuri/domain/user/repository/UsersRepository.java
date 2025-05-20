
/*
* 1. 리포지터리(Repository)
-리포지터리는 데이터베이스와의 상호작용을 담당하는 부분이야.
 즉, 데이터베이스에서 데이터를 가져오고, 수정하고, 삭제하는 일을 해.
   리포지터리는 JPA에서 JpaRepository 같은 인터페이스를 상속받아서 제공되는
   기본적인 CRUD 기능을 사용할 수 있어.

    -핵심 포인트:
    데이터를 다루는 역할 (DB에 저장된 데이터를 가져오거나 수정하거나 삭제하는 작업)
    findByUsername()처럼 쿼리 메서드를 정의하여, DB에서 데이터를 가져오는 일을 한다.
    DB와의 연결을 관리하며, 데이터 액세스 레벨에서 필요한 모든 작업을 처리해.
* */






package org.example.booknuri.domain.user.repository;

import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/*
<JpaRespository 상속하는 이유>
- JpaRepository를 상속하면 Users 엔티티에 대해 기본적인 CRUD 기능을 제공해.
    여기서 Long은 Users 엔티티의 id의 타입이야.
-JpaRepository를 상속하면 기본적인 CRUD 기능을 제공받기 때문에 findByUsername 같은 메서드만 추가로 작성해주면 됨
* */

@Repository // 이 클래스가 Repository임을 알려주는 어노테이션
public interface UsersRepository extends JpaRepository<UserEntity, String> {


    //findById()로도 users객체 가져올수 있는데 findByUsername()을 왜 추가할까?
    //->Optional을 쓰지 않고 바로 객체를 가져오고 싶을 때
    //  findById()는 Optional<Users>을 반환하지만, findByUsername()을 추가하면 그냥 Users 객체를 바로 가져올 수 있음.
    public UserEntity findByUsername(String username);


    // 아이디(유저네임) 중복 체크를 위한 메서드 (JPA가 자동으로 구현)
    boolean existsByUsername(String username);

    // 이메일 중복 체크를 위한 메서드
    boolean existsByEmail(String email);  // 이메일을 기준으로 중복 확인

}
