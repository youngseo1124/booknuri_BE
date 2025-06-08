package org.example.booknuri.domain.myBookshelf_.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookReflection_.dto.BookReflectionResponseDto;
import org.example.booknuri.domain.bookReview_.dto.BookReviewResponseDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyShelfBookWithExtrasResponseDto {
    private MyShelfBookResponseDto shelfInfo;
    // 내가 쓴 리뷰 (1개)
    private BookReviewResponseDto myReview;
    // 인용은 여러 개
    private List<BookQuoteResponseDto> myQuotes;

    // 내가 쓴 독후감 (1개)
    private BookReflectionResponseDto myReflection;
}
