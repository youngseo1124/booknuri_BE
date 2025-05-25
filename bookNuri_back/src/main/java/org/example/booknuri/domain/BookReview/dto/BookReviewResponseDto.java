package org.example.booknuri.domain.BookReview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewResponseDto {
    private Long id;
    private String content;
    private int rating;
    private String reviewerUsername;
    private Date createdAt;
    private int likeCount;
    private boolean isLikedByCurrentUser;
}
