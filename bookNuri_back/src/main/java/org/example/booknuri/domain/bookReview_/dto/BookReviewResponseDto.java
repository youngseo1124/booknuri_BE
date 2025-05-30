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
public class BookReviewResponseDto {
    private Long id;
    private String content;
    private int rating;
    private String reviewerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    private boolean containsSpoiler;
    //내가 쓴 리뷰인지 여부
    private boolean isWrittenByCurrentUser;
}
