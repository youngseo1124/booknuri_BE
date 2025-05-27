package org.example.booknuri.global.security.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.example.booknuri.global.security.converter.UserJoinConverter;
import org.example.booknuri.global.security.dto.SignupRequestDTO;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {





    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final UserJoinConverter userConverter;


    //카카오 로그인
    public Map<String, String> loginOrRegisterKakaoUser(String id, String nickname) {
        UserEntity user = userRepository.findByUsername(id);

        if (user == null) {
            user = new UserEntity();
            user.setUsername(id);
            user.setNickname(nickname);
            user.setEmail(id);
            user.setPassword(""); // 소셜 로그인은 비번 없음
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            userRepository.save(user);
        }

        String accessToken = jwtProvider.createAccessToken(user);
        String redisKey = "RT:" + id;

        String refreshToken;
        String existingEncryptedRT = redisTemplate.opsForValue().get(redisKey);
        if (existingEncryptedRT != null) {
            String decryptedRT = jwtProvider.decrypt(existingEncryptedRT);
            if (jwtProvider.validateToken(decryptedRT)) {
                refreshToken = decryptedRT;
            } else {
                refreshToken = jwtProvider.createRefreshToken(id);
                redisTemplate.opsForValue().set(redisKey, jwtProvider.encrypt(refreshToken), 30, TimeUnit.DAYS);
            }
        } else {
            refreshToken = jwtProvider.createRefreshToken(id);
            redisTemplate.opsForValue().set(redisKey, jwtProvider.encrypt(refreshToken), 30, TimeUnit.DAYS);
        }

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);
        return tokenMap;
    }



    //토큰 재발행
    public String reissueAccessToken(String refreshToken) {
        // 토큰 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰 유효하지 않음");
        }

        // username 추출
        String username = jwtProvider.extractUsername(refreshToken);

        // Redis에서 비교
        String storedEncrypted = redisTemplate.opsForValue().get("RT:" + username);
        if (storedEncrypted == null) {
            throw new IllegalArgumentException("Redis에 토큰 없음");
        }

        String decrypted = jwtProvider.decrypt(storedEncrypted);
        if (!refreshToken.equals(decrypted)) {
            throw new IllegalArgumentException("토큰이 Redis와 일치하지 않음");
        }

        // accessToken 재발급
        String newAccessToken = jwtProvider.refreshAccessToken(refreshToken);
        if (newAccessToken == null) {
            throw new IllegalArgumentException("엑세스 토큰 발급 실패");
        }

        return newAccessToken;
    }

    //유저아이디 중복체크
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    //이메일ㅇ 중복체크
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    //회원가입
    public boolean joinUser(SignupRequestDTO dto) {
        if (isUsernameExists(dto.getUsername()) || isEmailExists(dto.getEmail())) {
            return false;
        }

        UserEntity user = userConverter.toEntity(dto);
        userRepository.save(user);
        return true;
    }




}
