package org.example.booknuri.domain.library.dto;

//프론트에게 모든 지역 조합 줄떄 보내는 dto

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionResponseDTO {
    private String si; // 시/도
    private String gu; // 시군구
}
