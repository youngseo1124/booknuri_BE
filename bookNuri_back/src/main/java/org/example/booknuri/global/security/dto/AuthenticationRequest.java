/*
* 6. AuthenticationRequest.java- dto임!!
-사용자가 **로그인할 때 보내는 데이터(보통 username과 password)를 담는 객체(dto)**야.
쉽게 말하면:
"사용자가 로그인할 때 작성하는 로그인 정보 양식"이라고 보면 돼!

* */



package org.example.booknuri.global.security.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthenticationRequest {

    private String username;

    private String password;

}
