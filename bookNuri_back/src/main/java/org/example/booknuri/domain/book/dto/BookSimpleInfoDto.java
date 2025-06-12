package org.example.booknuri.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSimpleInfoDto {
    private String isbn13;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;

}
