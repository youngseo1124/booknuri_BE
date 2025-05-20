package org.example.booknuri.global.security.converter;

import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.global.security.dto.SignupRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//회원가입dto->엔티티 변환 컨버터


@Component
public class UserJoinConverter {

    private final PasswordEncoder passwordEncoder;

    public UserJoinConverter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity toEntity(SignupRequestDTO dto) {
        return UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .role("ROLE_USER")
                .enabled(1)
                .build();
    }
}
