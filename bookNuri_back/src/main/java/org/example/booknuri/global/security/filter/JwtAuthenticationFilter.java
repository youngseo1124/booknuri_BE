package org.example.booknuri.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.example.booknuri.global.security.dto.AuthenticationRequest;
import org.example.booknuri.global.security.entity.CustomUser;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    //  생성자
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   JwtProvider jwtProvider,
                                   UserRepository userRepository,
                                   RedisTemplate<String, String> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;

        setFilterProcessesUrl("/userlogin"); // 요청 URL 설정

        // 성공 시 JSON 응답 내려주기
        setAuthenticationSuccessHandler((request, response, authentication) -> {
            try {
                CustomUser customUser = (CustomUser) authentication.getPrincipal();
                UserEntity user = customUser.getUser();
                String username = user.getUsername();

                String accessToken = jwtProvider.createAccessToken(user);

                // Refresh 토큰 처리
                String existingEncryptedRT = redisTemplate.opsForValue().get("RT:" + username);
                String refreshToken;

                if (existingEncryptedRT != null && jwtProvider.validateToken(jwtProvider.decrypt(existingEncryptedRT))) {
                    refreshToken = jwtProvider.decrypt(existingEncryptedRT);
                } else {
                    refreshToken = jwtProvider.createRefreshToken(username);
                    redisTemplate.opsForValue().set("RT:" + username, jwtProvider.encrypt(refreshToken), 30, TimeUnit.DAYS);
                }

                // JSON으로 내려줄 토큰맵
                Map<String, String> tokenMap = new HashMap<>();
                tokenMap.put("accessToken", accessToken);
                tokenMap.put("refreshToken", refreshToken);

                response.setContentType("application/json;charset=UTF-8");
                new ObjectMapper().writeValue(response.getWriter(), tokenMap);
            } catch (Exception e) {
                log.error("❌ 로그인 응답 처리 중 오류", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        });

        //  실패 시 JSON 에러 메시지 내려주기
        setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, String> error = new HashMap<>();
            error.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
            new ObjectMapper().writeValue(response.getWriter(), error);
        });
    }

    // 인증 시도 메서드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            AuthenticationRequest authRequest = objectMapper.readValue(request.getInputStream(), AuthenticationRequest.class);

            if (authRequest.getUsername() == null || authRequest.getPassword() == null) {
                return null;
            }

            String username = authRequest.getUsername();
            String password = authRequest.getPassword();

            UserEntity user = userRepository.findByUsername(username);
            if (user == null) {
                log.warn("❌ 로그인 실패 - 존재하지 않는 사용자: {}", username);
                throw new UsernameNotFoundException("존재하지 않는 사용자입니다.");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    List.of(new SimpleGrantedAuthority(user.getRole()))
            );

            authentication = authenticationManager.authenticate(authentication);
            log.info(" 로그인 인증 성공: {}", username);

            return authentication;
        } catch (IOException e) {
            log.error("❌ 로그인 요청 JSON 파싱 실패", e);
            throw new RuntimeException(e);
        }
    }
}
