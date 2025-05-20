

package org.example.booknuri.global.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.example.booknuri.domain.user.repository.UsersRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsersRepository userRepository;
    private final JwtProvider jwtProvider;
    private final HttpServletResponse response;
    private final RedisTemplate<String, String> redisTemplate;



    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String id = null;
        String nickname = null;

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            id = attributes.get("id").toString();
            nickname = (String) profile.get("nickname");
        } else if ("google".equals(registrationId)) {
            id = attributes.get("sub").toString();
            nickname = (String) attributes.get("name");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + registrationId);
        }

        // DB 조회 or 신규 생성
        UserEntity user = userRepository.findByUsername(id);
        if (user == null) {
            user = new UserEntity();
            user.setUsername(id);
            user.setNickname(nickname);
            user.setRole("ROLE_USER");
            user.setPassword(""); // 소셜 로그인은 비밀번호 없음
            user.setEmail(id);
            user.setEnabled(1);
            userRepository.save(user);
        }

        // SecurityContext 등록
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);


        // JWT 발급
        String accessToken = jwtProvider.createAccessToken(user);

        String refreshToken;
        String existingEncryptedRT = redisTemplate.opsForValue().get("RT:" + id);

        if (existingEncryptedRT != null) {
            String decryptedRT = jwtProvider.decrypt(existingEncryptedRT);
            if (jwtProvider.validateToken(decryptedRT)) {
                refreshToken = decryptedRT;
            } else {
                refreshToken = jwtProvider.createRefreshToken(id);
                redisTemplate.opsForValue().set("RT:" + id, jwtProvider.encrypt(refreshToken), 30, TimeUnit.DAYS);
            }
        } else {
            refreshToken = jwtProvider.createRefreshToken(id);
            redisTemplate.opsForValue().set("RT:" + id, jwtProvider.encrypt(refreshToken), 30, TimeUnit.DAYS);
        }



        // JSON 응답 (accessToken + refreshToken + user 정보 포함)
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            new ObjectMapper().writeValue(response.getWriter(), tokenMap);
        } catch (IOException e) {
            throw new RuntimeException("OAuth2 로그인 응답 실패", e);
        }

        // 실제 인증 로직엔 필요하니까 사용자 객체는 그대로 반환
        return new CustomOAuth2User(user, attributes);
    }


}
