package org.example.booknuri.domain.bookQuote.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 *  BookQuoteResponseDto
 * - 인용 조회용 응답 DTO (배너 표시용)
 * - 내가 쓴 인용인지 여부, 좋아요 여부, 공개여부 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookQuoteResponseDto {

    private Long id;                     // 인용 ID
    private String quoteText;           // 인용 문장

    private Float fontScale;            // 글자 크기 비율
    private String fontColor;           // 글자 색상 (HEX)
    private int backgroundId;           // 배경 이미지 ID (1~15)

    private String reviewerUsername;    // 작성자 ID or 닉네임
    private LocalDateTime createdAt;    // 작성 시각

    private int likeCount;              // 좋아요 수
    private boolean isLikedByCurrentUser;   // 내가 좋아요 눌렀는지
    private boolean isWrittenByCurrentUser; // 내가 쓴 인용인지

    private boolean visibleToPublic;    // 👀 공개 여부 (true/false)

    private String isbn13;
}
