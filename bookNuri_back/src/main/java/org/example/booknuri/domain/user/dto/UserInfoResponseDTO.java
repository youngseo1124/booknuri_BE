
package org.example.booknuri.domain.user.dto;


import lombok.Builder;
import lombok.Getter;
import org.example.booknuri.domain.library.dto.LibrarySimpleDTO;

@Getter
@Builder
public class UserInfoResponseDTO {

    private String username;         // 사용자 ID
    private String nickname;         // 닉네임
    private String email;            // 이메일
    private String role;             // 권한 (ROLE_USER 등)
    private boolean enabled;         // 계정 활성화 여부

    private LibrarySimpleDTO myLibrary;  // 선택한 내 도서관 정보
}
