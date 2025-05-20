
/*
<UserDetailsServiceImpl 클래스가 하는 일>
이 클래스는 Spring Security에서 사용자가 로그인할 때,
해당 사용자 정보를 DB에서 조회하고,
 Spring Security가 사용할 수 있도록 Customuser객체로 변환하는 역할을 해.
사용자 정보가 없다면 예외를 던져, 로그인 과정에서 유효한 사용자인지 체크하는 작업을 도와줘.

* */

/*
<UserDetailsServiceImpl 클래스가 하는 일>
-사용자가 로그인할 때, loadUserByUsername 메서드가 호출돼.

-이 메서드는 **사용자의 (username)**을 받으면, 해당 아이디로 DB에서 사용자 정보를 찾아와.
만약 사용자 정보가 없다면, UsernameNotFoundException 예외를 던져.

-즉, 사용자 정보를 찾을 수 없으면 로그인할 수 없게 되는 거지.
사용자 정보가 잘 조회되면, 그 정보를 CustomUser 객체로 변환해서 Spring Security가 처리할 수 있게 해.
* */

package org.example.booknuri.global.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.global.security.entity.CustomUser;
import org.example.booknuri.domain.user.entity.UserEntity;

import org.example.booknuri.domain.user.repository.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *  🔐 UserDetailsService : 사용자 정보 불러오는 인터페이스
 *  ✅ 이 인터페이스를 구현하여, 사용자 정보를 로드하는 방법을 정의할 수 있습니다.
 */


//UserDetailsService : 자체 로그인시 db에서 사용자 정보 검증하는 인터페이스
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;


    // loadUserByUsername: 시큐리티 기본 메서드. 이름 고정됨 String만 매개변수로 받음
    //users db테이블에 해당 회원정보 있는지 확인하고 있으면 Customuser객체(userDetails 구현체) 반환함
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            UserEntity user = usersRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다. username: " + username);
            }

            // CustomUser 객체로 변환 후 반환
            return new CustomUser(user);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("잘못된 username형식: " + username);
        }
    }



}
