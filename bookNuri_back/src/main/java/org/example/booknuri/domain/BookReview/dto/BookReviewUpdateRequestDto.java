package org.example.booknuri.domain.bookReview.dto;

//리뷰 업데이트 용 dto

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewUpdateRequestDto {
    private Long reviewId;
    private String content;
    private int rating;
    private boolean containsSpoiler;
}
