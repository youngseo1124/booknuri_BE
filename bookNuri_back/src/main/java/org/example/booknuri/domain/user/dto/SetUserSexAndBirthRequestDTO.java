package org.example.booknuri.domain.user.dto;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SetUserSexAndBirthRequestDTO {
    private String gender;     // M / F / O
    private Integer birthYear; // 4자리 숫자 (ex: 1999)
}
