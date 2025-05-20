package org.example.booknuri.domain.library.entity;

import jakarta.persistence.*;
import lombok.*;


@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "libraries")
public class LibraryEntity {

    //도서관 고유 코드
    @Id
    @Column(name = "lib_code")
    private String libCode;

    //도서관 이름
    @Column(nullable = false)
    private String libName;

    //전화번호
    @Column
    private String tel;

    //팩스번호
    @Column
    private String fax;

    //홈페이지 주소
    @Column
    private String homepage;

    //휴관일 정보 (예: 매주 월요일, 법정공휴일 등등)
    @Column(length = 700)
    private String closed;

    // 운영시간 정보 (예: 평일 9~18시, 주말 9~17시 등)
    @Column(length = 500)
    private String operatingTime;


    // 도서관의 위도 (지도에서 위치 찍기 위해 필요)
    @Column
    private Double latitude;


    // 도서관의 경도
    @Column
    private Double longitude;

    //도서관 소장 도서 수
    @Column
    private Integer bookCount;

    //전체 주소 (예: "서울특별시 노원구 동일로 1405")
    //( → 시/구 나누기 전 원본 주소 저장)
    @Column
    private String fullAddress;  // 원본 주소 문자열

    // 시/구 정보 (RegionEntity와 연관됨 → 검색용으로 사용)
    // 예: 서울특별시 + 노원구
    @ManyToOne
    @JoinColumn(name = "region_id")
    private RegionEntity region;
}
