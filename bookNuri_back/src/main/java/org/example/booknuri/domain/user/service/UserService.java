
/*
서비스(Service)
-서비스는 앱의 주요 기능을 처리하는 곳이야.
-예를 들어, 회원 가입이나 정보 수정 같은 작업을 여기서 해.
 -데이터베이스와 상호작용은 하지만, 직접 DB를 다루지 않고, 리포지터리를 통해 데이터를 가져오고 처리해.

  -핵심:
    앱의 기능을 처리 (회원 가입, 로그인 등)
    리포지터리를 호출해서 데이터를 가져옴
    DB와 직접 상호작용하지 않고, 데이터를 가공하고 로직을 처리함
   즉, 서비스는 리포지터리와 협력해서 실제 앱의 기능을 동작시키는 역할이야.
* */


/*
* <서비스-리포지토리 동작 예시>
사용자가 로그인하려고 할 때:
서비스는 사용자가 입력한 아이디로 해당 사용자가 존재하는지 확인하고, 로그인 로직을 처리.
리포지터리는 해당 아이디로 DB에서 사용자 정보를 찾아오는 역할.*/




package org.example.booknuri.domain.user.service;

import lombok.RequiredArgsConstructor;

import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.repository.LibraryRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UsersRepository;
import org.example.booknuri.global.security.provider.JwtProvider;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service // 이 클래스가 서비스임을 Spring에게 알려주는 어노테이션
public class UserService {


    private final UsersRepository usersRepository; // UsersRepository 의존성 주입

    private final LibraryRepository libraryRepository;



    // 사용자 조회
    public UserEntity getUserByUsername(String username) {
        UserEntity user = usersRepository.findByUsername(username);
        return user;
    }



    // 사용자 삭제 username 기준
    public boolean deleteUserByUsername(String username) {
        UserEntity existingUser = usersRepository.findByUsername (username); // id로 기존 사용자 조회

        if (existingUser!=null) {
            usersRepository.delete(existingUser); // 사용자 삭제
            return true;
        }
        return false; // 사용자 정보가 없으면 삭제할 수 없음
    }


    //  내 도서관 설정
    public boolean setMyLibrary(String username, String libCode) {
        UserEntity user = usersRepository.findByUsername(username);
        LibraryEntity library = libraryRepository.findByLibCode(libCode);

        if (user == null || library == null) return false;

        user.setMyLibrary(library);
        usersRepository.save(user); // 변경사항 저장
        return true;
    }


    //최초로그인시 성별이랑 출생년도 받기
    public boolean setUserSexAndBirth(String username, String gender, Integer birthYear) {
        UserEntity user = usersRepository.findByUsername(username);
        if (user == null) return false;

        user.setGender(gender);
        user.setBirthYear(birthYear);
        usersRepository.save(user);
        return true;
    }



}
