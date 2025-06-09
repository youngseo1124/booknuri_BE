package org.example.booknuri.domain.bookReview_.converter;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;
import org.example.booknuri.domain.bookReview_.dto.MyReviewGroupedByBookResponseDto;
import org.example.booknuri.domain.bookReview_.dto.MyReviewSimpleDto;
import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyReviewGroupedConverter {

    public MyReviewGroupedByBookResponseDto toDto(BookReviewEntity review) {
        if (review == null) return null;

        return MyReviewGroupedByBookResponseDto.builder()
                .bookInfo(BookInfoDto.builder()
                        .isbn13(review.getBook().getIsbn13())
                        .bookTitle(review.getBook().getBookname())
                        .bookAuthor(review.getBook().getAuthors())
                        .bookImageUrl(review.getBook().getBookImageURL())
                        .reviewCount(1) // 리뷰는 1개니까 그냥 1
                        .quoteCount(0)  // 기본값 0 → 조회할 땐 바꿔도 됨
                        .reflectionCount(0)
                        .build())
                .review(MyReviewSimpleDto.builder()
                        .reviewId(review.getId())
                        .content(review.getContent())
                        .rating(review.getRating())
                        .createdAt(review.getCreatedAt())
                        .updatedAt(review.getUpdatedAt())
                        .likeCount(review.getLikeCount())
                        .containsSpoiler(review.isContainsSpoiler())
                        .isWrittenByCurrentUser(true) // 본인이 쓴 거니까 true 고정
                        .build())
                .build();
    }
}
