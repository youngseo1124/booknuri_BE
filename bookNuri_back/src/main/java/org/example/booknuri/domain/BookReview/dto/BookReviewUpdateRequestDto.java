package org.example.booknuri.domain.BookReview.dto;

//리뷰 업데이트 용 dto

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
