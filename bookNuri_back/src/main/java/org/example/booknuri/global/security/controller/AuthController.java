package org.example.booknuri.global.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.dto.SignupRequestDTO;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.example.booknuri.global.security.service.AuthService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final AuthService authService;




     @PostMapping("/reissue")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Authorization 헤더 없음");
        }

        try {
            String refreshToken = header.substring(7);
            String newAccessToken = authService.reissueAccessToken(refreshToken);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }



    //카카오 로그인
     @PostMapping("/kakaologin")
     public ResponseEntity<?> loginWithKakao(@RequestBody Map<String, String> body) {
         String id = body.get("id");
         String nickname = body.get("nickname");

         if (id == null || nickname == null) {
             return ResponseEntity.badRequest().body("id 또는 nickname 없음");
         }

         Map<String, String> tokenMap = authService.loginOrRegisterKakaoUser(id, nickname);
         return ResponseEntity.ok(tokenMap);
     }


     //회원가입
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody SignupRequestDTO dto) {
        boolean result = authService.joinUser(dto);

        if (result) {
            log.info("회원가입 성공");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원가입 성공"
            ));

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "아이디 또는 이메일 중복"
            ));
        }
    }



    // 아이디 중복 체크 메서드
    @GetMapping("/check/username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable("username") String username) {
        log.info("아이디 중복 체크 요청: {}", username);

        // 아이디가 이미 존재하는지 확인
        boolean exists = authService.isUsernameExists(username);

        // 아이디가 이미 존재하면 "중복된 아이디" 반환
        if (exists) {
            log.info("아이디가 중복되었습니다.");
            return new ResponseEntity<>("아이디가 중복됩니다.", HttpStatus.BAD_REQUEST);
        } else {
            log.info("아이디 사용 가능.");
            return new ResponseEntity<>("아이디 사용 가능", HttpStatus.OK);
        }
    }

    // 이메일 중복 체크 메서드
    @GetMapping("/check/email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable("email") String email) {
        log.info("이메일 중복 체크 요청: {}", email);

        // 이메일이 이미 존재하는지 확인
        boolean exists = authService.isEmailExists(email);

        // 이메일이 이미 존재하면 "중복된 이메일" 반환
        if (exists) {
            log.info("이메일이 중복되었습니다.");
            return new ResponseEntity<>("이메일이 중복됩니다.", HttpStatus.BAD_REQUEST);
        } else {
            log.info("이메일 사용 가능.");
            return new ResponseEntity<>("이메일 사용 가능", HttpStatus.OK);
        }
    }








}
