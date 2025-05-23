
package org.example.booknuri.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.repository.UsersRepository;
import org.example.booknuri.global.security.filter.JwtAuthenticationFilter;
import org.example.booknuri.global.security.filter.JwtRequestFilter;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.example.booknuri.global.security.service.UserDetailServiceImpl;
import org.example.booknuri.global.security.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {


    private final CustomOAuth2UserService customOAuth2UserService;
    private final  UserDetailServiceImpl userDetailService;
    private final JwtProvider jwtProvider;
    private final UsersRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;



    // 비밀번호 암호화 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    //  AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }

    //  CORS 설정 (React 프론트엔드 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(login -> login.disable()) // 🔥 기본 로그인 UI 비활성화
                .httpBasic(basic -> basic.disable()) // 🔥 HTTP Basic 인증 비활성화
                .csrf(csrf -> csrf.disable()) // 🔥 CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 🔥 세션 사용 X
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login","/userlogin", "/oauth2/**", "/auth/**","/users/join").permitAll() // 🔥 `/login`, `/oauth2/**` 인증 없이 허용
                        .anyRequest().authenticated()
                );


        //  AuthenticationManager 가져오기
        AuthenticationManager authenticationManager = authenticationManager(http);

        //  JWT 필터 추가
        http.addFilterAt(new JwtAuthenticationFilter(authenticationManager, jwtProvider, userRepository, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtRequestFilter(authenticationManager, jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);

        //  CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //  OAuth2 로그인 설정 (카카오 로그인)
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)) // 🔹 카카오 사용자 정보 처리
                .successHandler((request, response, authentication) -> {
                    log.info("✅ OAuth2 로그인 성공! 사용자 인증 완료됨.");
                })
                .failureHandler((request, response, exception) -> {
                    log.error("🚨 OAuth2 로그인 실패! 예외 발생", exception);
                    response.sendRedirect("/login?error");
                })
        );


        return http.build();
    }
}
