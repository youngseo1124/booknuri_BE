package org.example.booknuri.domain.bookQuote.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 MyQuoteResponseDto
 * - 마이페이지에서 내가 쓴 인용 목록 조회 시 사용
 * - 책 정보 + 인용 스타일 + 좋아요 수 + 공개 여부 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyQuoteResponseDto {

    private Long quoteId;               // 인용 고유 ID
    private String quoteText;           // 인용 내용
    private LocalDateTime createdAt;    // 작성일
    private LocalDateTime updatedAt;    // 수정일
    private int likeCount;              // 좋아요 수

    private Float fontScale;            // 글자 크기 비율
    private String fontColor;           // 글자 색상 (HEX)
    private int backgroundId;           // 배경 이미지 ID (1~15)

    private boolean visibleToPublic;    // 공개 여부

    // 인용한 책 정보
    private String bookTitle;           // 책 제목
    private String bookAuthor;          // 저자명
    private String isbn13;              // ISBN
    private String bookImageUrl;        // 책 이미지 URL
}
