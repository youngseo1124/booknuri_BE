package org.example.booknuri.domain.bookReview.dto;

//내가 쓴 리뷰 보기


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReviewResponseDto {

    private Long reviewId;            // 리뷰 고유 ID
    private String content;           // 리뷰 내용
    private int rating;               // 별점
    private LocalDateTime createdAt;  // 작성일
    private LocalDateTime updatedAt;  // 수정일
    private int likeCount;            // 좋아요 수
    private boolean containsSpoiler;
    //  리뷰 쓴 책 정보
    private String bookTitle;         // 책 제목
    private String isbn13;            // 책 ISBN
    private String bookImageUrl;      // 책 이미지 URL
}
