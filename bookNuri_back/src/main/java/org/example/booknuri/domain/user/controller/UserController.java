package org.example.booknuri.domain.user.controller;


//수정할떄 쓰는 dto, 새로 글쓸떄 쓰는 dto



import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.converter.UserConverter;
import org.example.booknuri.domain.user.dto.UserInfoResponseDTO;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.global.security.entity.CustomUser;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import org.springframework.http.ResponseEntity;


@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor // ✅ 생성자 주입 자동 적용 (Lombok)
public class UserController {

    private final UserService userService;
    private final UserConverter userConverter;




    //  로그인한 사용자 정보 조회 API
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomUser customUser) {

        UserEntity user=userService.getUserByUsername(customUser.getUsername());
        UserInfoResponseDTO dto = userConverter.toDTO(user);
        return ResponseEntity.ok(dto);
    }




    // 아이디 중복 체크 메서드
    @GetMapping("/check/username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable("username") String username) {
        log.info("아이디 중복 체크 요청: {}", username);

        // 아이디가 이미 존재하는지 확인
        boolean exists = userService.isUsernameExists(username);

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
        boolean exists = userService.isEmailExists(email);

        // 이메일이 이미 존재하면 "중복된 이메일" 반환
        if (exists) {
            log.info("이메일이 중복되었습니다.");
            return new ResponseEntity<>("이메일이 중복됩니다.", HttpStatus.BAD_REQUEST);
        } else {
            log.info("이메일 사용 가능.");
            return new ResponseEntity<>("이메일 사용 가능", HttpStatus.OK);
        }
    }




    // 회원 삭제(탈퇴), username기준
    @PreAuthorize(" hasRole('ROLE_ADMIN') or #p0 == authentication.name ")
    @DeleteMapping("/{username}")
    public ResponseEntity<?> delete(
            @PathVariable("username") String username
    ) throws Exception {
        try {
            boolean result = userService.deleteUserByUsername(username);
            if( result )
                return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
            else
                return new ResponseEntity<>("FAIL", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }













}
