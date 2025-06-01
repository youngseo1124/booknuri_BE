package org.example.booknuri.domain.bookQuote.dto;

import lombok.*;

/**
 * 📌 BookQuoteUpdateRequestDto
 * - 인용 수정 시 사용하는 요청 DTO
 * - 인용 문구 및 스타일 요소 수정 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookQuoteUpdateRequestDto {

    private Long quoteId;         // 수정할 인용 ID
    private String quoteText;     // 인용 문장

    private Float fontScale;      // 글자 크기 비율 (0.0 ~ 1.0)
    private String fontColor;     // 글자 색상 (HEX)
    private int backgroundId;     // 배경 이미지 ID (1~15)

    private boolean visibleToPublic; // 공개 여부
}
