package org.example.booknuri.domain.library.dto;

//유저가 로그인시 주는 userinfodto에 담긴 내 도서관 정보 dto

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LibrarySimpleDTO {

    private String libCode;       // 도서관 코드
    private String libName;       // 도서관 이름
    private String tel;           // 전화번호
    private String homepage;      // 홈페이지
    private String closed;        // 휴관일
    private String operatingTime; // 운영 시간
    private Double latitude;      // 위도
    private Double longitude;     // 경도
    private Integer bookCount;    // 소장 도서 수
    private String fullAddress;   // 전체 주소
}
