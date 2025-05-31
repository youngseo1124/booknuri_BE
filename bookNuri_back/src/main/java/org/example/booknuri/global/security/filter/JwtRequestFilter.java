package org.example.booknuri.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public JwtRequestFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    // Authorization 헤더에서 "Bearer {토큰}" 형식으로 accessToken을 꺼내는 메서드

    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // "Bearer " 이후 문자열
        }
        return null;
    }

    /**
     * doFilterInternal은 요청이 들어올 때마다 실행되는 메서드
     * 이 필터는 다음과 같은 역할을 수행
     * 1. 클라이언트 요청에서 Authorization 헤더로부터 accessToken을 추출한다.
     * 2. 해당 accessToken이 유효한지 확인한다.
     * 3. 유효하면 인증 객체(Authentication)을 생성해 SecurityContext에 저장한다.
     * 4. 이후 요청은 인증된 사용자로 간주된다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ 1. 요청 URI 로그
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        log.info("📩 요청 도착 - [{}] {}", method, requestUri);

        // ✅ 2. Authorization 헤더 로그
        String authHeader = request.getHeader("Authorization");
        log.info("🔐 Authorization 헤더: {}", authHeader);

        String jwt = extractTokenFromHeader(request); // Authorization 헤더에서 JWT 추출

        // 토큰이 없으면 다음 필터로 진행 (인증 X)
        if (jwt == null || jwt.isEmpty()) {
            log.info("Authorization 헤더에서 JWT를 찾을 수 없음. 다음 필터로 진행");
            filterChain.doFilter(request, response);
            return;
        }

        // JWT를 통해 인증 객체 생성
        Authentication authentication = jwtProvider.getAuthenticationToken(jwt);

        // 토큰 유효성 검증 (만료 또는 변조 확인)
        boolean valid = jwtProvider.validateToken(jwt);

        if (valid && authentication != null && authentication.isAuthenticated()) {
            // 유효하고 인증 객체도 있으면 SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // 토큰이 유효하지 않으면 SecurityContext 초기화 (인증 제거)
            log.info("유효하지 않은 JWT 토큰. SecurityContext 초기화");
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 특정 URI에서는 이 필터를 적용하지 않도록 설정
     * 여기선 /users/extract 경로는 필터 적용 제외(재발급 메서드)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().equals("/users/extract");
    }
}
