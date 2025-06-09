package org.example.booknuri.domain.bookReview_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReviewGroupedByBookResponseDto {

    private BookInfoDto bookInfo;
    private MyReviewSimpleDto review; // 리뷰는 1개만 존재
}
