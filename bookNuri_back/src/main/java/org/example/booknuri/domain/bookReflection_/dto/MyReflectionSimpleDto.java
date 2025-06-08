package org.example.booknuri.domain.bookReflection_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReflectionSimpleDto {
    private Long reflectionId;            // 독후감 ID
    private String title;                 // 독후감 제목
    private String content;
    private LocalDateTime createdAt;      // 작성일
    private LocalDateTime updatedAt;      // 수정일
    private int rating;                   // 별점
    private int likeCount;                // 좋아요 수
    private boolean containsSpoiler;      // 스포일러 포함 여부
    private boolean visibleToPublic;      // 공개 여부
}
