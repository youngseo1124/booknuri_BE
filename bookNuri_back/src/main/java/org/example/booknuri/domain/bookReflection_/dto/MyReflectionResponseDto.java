package org.example.booknuri.domain.bookReflection_.dto;

// 내가 쓴 독후감 보기

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReflectionResponseDto {

    private Long reflectionId;        // 독후감 고유 ID
    private String content;           // 독후감 내용
    private int rating;               // 별점
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
    private int likeCount;            // 좋아요 수
    private boolean containsSpoiler;
    private boolean visibleToPublic; //공개여부

    // 독후감 쓴 책 정보
    private String bookTitle;         // 책 제목
    private String isbn13;            // 책 ISBN
    private String bookImageUrl;      // 책 이미지 URL
}
