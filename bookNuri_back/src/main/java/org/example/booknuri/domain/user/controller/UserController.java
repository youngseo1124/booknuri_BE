package org.example.booknuri.domain.user.controller;


//수정할떄 쓰는 dto, 새로 글쓸떄 쓰는 dto



import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.user.converter.UserConverter;
import org.example.booknuri.domain.user.dto.*;
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
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(@AuthenticationPrincipal CustomUser customUser) {

        UserEntity user=userService.getUserByUsername(customUser.getUsername());
        UserInfoResponseDTO dto = userConverter.toDTO(user);
        return ResponseEntity.ok(dto);
    }

    // 내 도서관 설정 API
    @PatchMapping("/my-library")
    public ResponseEntity<String> setMyLibrary(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SetMyLibraryRequestDTO dto
    ) {
        boolean result = userService.setMyLibrary(customUser.getUsername(), dto.getLibCode());

        if (result) {
            return ResponseEntity.ok().body("내 도서관 설정 완료!");
        } else {
            return ResponseEntity.badRequest().body("도서관 설정 실패 (유저 또는 도서관 없음)");
        }
    }


    // 성별만 설정
    @PatchMapping("/sex")
    public ResponseEntity<?> setUserSex(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SetUserGenderRequestDTO dto
    ) {
        boolean result = userService.setUserGender(customUser.getUsername(), dto.getGender());
        return result ? ResponseEntity.ok("성별 설정 완료!") :
                ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
    }

    // 출생년도만 설정
    @PatchMapping("/birth")
    public ResponseEntity<?> setUserBirth(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody SetUserBirthRequestDTO dto
    ) {
        boolean result = userService.setUserBirthYear(customUser.getUsername(), dto.getBirth());
        return result ? ResponseEntity.ok("출생년도 설정 완료!") :
                ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
    }






}
