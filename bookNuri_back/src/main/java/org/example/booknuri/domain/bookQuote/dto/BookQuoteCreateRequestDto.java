package org.example.booknuri.domain.bookQuote.dto;

import lombok.*;

/**
 *  BookQuoteCreateRequestDto
 * - 유저가 새로운 인용을 작성할 때 사용하는 요청 DTO
 * - quoteText + 스타일 정보 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookQuoteCreateRequestDto {

    private String isbn13;         // 어떤 책의 인용인지 (책 식별자)
    private String quoteText;      // 인용 문장

    private Float fontScale;       // 글자 크기 비율 (0.0 ~ 1.0)
    private String fontColor;      // 글자 색상 (HEX 예: #FFFFFF)
    private int backgroundId;      // 배경 이미지 ID (1~15)

    private boolean visibleToPublic; // 공개 여부 (true = 전체공개)
}
