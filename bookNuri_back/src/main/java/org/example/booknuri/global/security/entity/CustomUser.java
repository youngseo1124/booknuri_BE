
/*
*
*



/*
< CustomUser 클래스 설명 >
역할: Spring Security에서 사용자 인증을 처리할 때 사용하는 객체입니다.
어디에 사용?: DB에서 가져온 Users 엔티티 객체를 Spring Security가 사용할 수 있는 UserDetails 형식으로 변환해줍니다.
변환 이유: UserDetails는 Spring Security가 요구하는 사용자 정보를 담기 위한 인터페이스입니다. 그래서 Users 엔티티를 바로 사용할 수 없고, CustomUser를 통해 변환해야 합니다.
중요 포인트: CustomUser는 로그인 인증을 위해 사용자가 가진 권한, 비밀번호, 계정 상태 등을 제공하는 역할을 합니다.


/*
* <userDetails 인터페이스와  Spring Security에서 사용자 인증>
-로그인할 때, 사용자의 정보(아이디, 비밀번호)를 확인을 userdeatilserviceImpl에서 함
  userdeatilserviceImpl서 해당 아이디를 바탕으로 DB에서 사용자 정보(Users 객체)를 찾아오고,
   이 정보를 CustomUser 객체로 변환함
     -<USER엔티티(또는 도메인) 안쓰고 CustomerUser객체로 변환하는 이유>
        : 1. Spring Security는 UserDetails 인터페이스를 구현한 객체만 사용하기 때문에,
        *  Users 엔티티를 그대로 사용하면 안 돼.
           2. CustomUser 클래스는 UserDetails 인터페이스를 구현해서,
           Spring Security가 사용하는 방식에 맞춰 사용자 정보를 제공하는 객체야.
          3. 즉, CustomUser는 Spring Security가 사용할 수 있는 형식으로 사용자 정보를 제공하는 객체라서 변환이 필요해! 😊
        쉽게 말하면, **CustomUser는 Spring Security를 위한 "사용자 정보 포장지"**라고 생각하면 돼! 🎁

* */





package org.example.booknuri.global.security.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

//CustomUser: DB에서 가져온 Users 엔티티 객체를 Spring Security가 사용할 수 있는 UserDetails 형식으로 변환한 엔티티
//왜쓰냐? Spring Security는 UserDetails 인터페이스를 구현한 객체만 사용하기 때문
@Slf4j
@Getter
public class CustomUser implements UserDetails {


    //🔹 DB에서 가져온 사용자 정보를 담을 Users 엔티티 객체
    private UserEntity user;



    // 🔹 CustomUser 생성자 (로그인한 사용자의 정보를 담기 위해)
    public CustomUser(UserEntity user) {
        this.user = user;

    }


    /**
     * ✅ 사용자의 권한을 반환하는 메서드
     * Spring Security는 권한을 "ROLE_" 접두사를 붙여서 사용해! (예: ROLE_USER)
     */
    /***getAuthorities()**는 사용자가 어떤 권한들을 가졌는지 반환해야 하는 시큐리티 기본 메서드야.*/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 단일 권한(auth)을 사용하므로, 리스트 대신 SimpleGrantedAuthority로 하나의 권한만 반환
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    // ✅ 사용자의 비밀번호를 반환
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 사용자의 아이디(Username)를 반환
    @Override
    public String getUsername() {
        return user.getUsername();
    }


    //✅ 계정이 만료되지 않았는지 여부 (true: 사용 가능, false: 만료됨)
    //     * → true를 반환하면 "계정이 만료되지 않음"
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    ///**
    //     * ✅ 계정이 잠겨있지 않은지 여부 (true: 잠기지 않음, false: 잠김)
    //     * → true를 반환하면 "계정이 잠기지 않음"
    //     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //  * ✅ 비밀번호가 만료되지 않았는지 여부 (true: 사용 가능, false: 만료됨)
    //     * → true를 반환하면 "비밀번호가 만료되지 않음"
    //     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // /**
    //     * ✅ 계정이 활성화(Enabled) 상태인지 여부
    //     * → DB에서 enabled 값이 0이면 비활성화 (false), 1이면 활성화 (true)
    //     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled(); // 바로 반환하면 됨!
    }

}
