package org.example.booknuri.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookInfoDto {
    private String isbn13;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;

    private int reviewCount;
    private int quoteCount;
    private int reflectionCount;
}
