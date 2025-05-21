package org.example.booknuri.domain.user.dto;

//내 도서관 설정할떄 프론트에서 보내는 도서관코드 dto

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SetMyLibraryRequestDTO {
    private String libCode; // 선택한 도서관 코드
}
