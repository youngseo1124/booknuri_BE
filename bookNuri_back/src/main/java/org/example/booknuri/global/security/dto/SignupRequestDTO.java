package org.example.booknuri.global.security.dto;


import lombok.Getter;

@Getter
public class SignupRequestDTO {
    private String username;
    private String password;
    private String nickname;
    private String email;
}
