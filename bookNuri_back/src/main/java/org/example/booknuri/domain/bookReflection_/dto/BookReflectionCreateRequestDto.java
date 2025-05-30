package org.example.booknuri.domain.bookReflection_.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReflectionCreateRequestDto {
    private String isbn13;     // 어떤 책에 대한 독후감인지
    private String content;    // 독후감 내용
    private int rating;        // 별점 (1~5)
    private boolean containsSpoiler;
    private boolean visibleToPublic; //공개여부
}
