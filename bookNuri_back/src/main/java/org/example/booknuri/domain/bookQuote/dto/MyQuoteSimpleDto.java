package org.example.booknuri.domain.bookQuote.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyQuoteSimpleDto {
    private Long quoteId;               // 인용 고유 ID
    private String quoteText;           // 인용 내용
    private LocalDateTime createdAt;    // 작성일
    private LocalDateTime updatedAt;    // 수정일
    private int likeCount;              // 좋아요 수

    private Float fontScale;            // 글자 크기 비율
    private String fontColor;           // 글자 색상 (HEX)
    private int backgroundId;           // 배경 이미지 ID (1~15)

    private boolean visibleToPublic;    // 공개 여부
}
