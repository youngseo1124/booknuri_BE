package org.example.booknuri.domain.bookReview_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReviewSimpleDto {

    private Long reviewId; // 리뷰 ID
    private String content; // 리뷰 본문
    private int rating; // 평점
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int likeCount;
    private boolean containsSpoiler;

    private boolean isWrittenByCurrentUser;
}
