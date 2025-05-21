/*
*  CustomOAuth2User의 역할
 OAuth2 로그인한 사용자 정보를 감싸서 Spring Security에서 사용할 수 있도록 변환하는 클래스!
* */




package org.example.booknuri.global.security.service;



import lombok.Getter;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Getter
public class CustomOAuth2User implements OAuth2User {

    private final UserEntity user; // 우리 서버의 유저 정보 (User 엔티티)
    private final Map<String, Object> attributes; // OAuth2에서 받은 사용자 정보 (카카오에서 받은 정보)


    public CustomOAuth2User(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole() ));
    }


    @Override
    public String getName() {
        return user.getUsername().toString();
    }
}
