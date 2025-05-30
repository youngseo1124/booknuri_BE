package org.example.booknuri.domain.bookReview_.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewCreateRequestDto {
    private String isbn13;     // 어떤 책에 대한 리뷰인지
    private String content;    // 리뷰 내용
    private int rating;        // 별점 (1~5)


    private boolean containsSpoiler;
}
