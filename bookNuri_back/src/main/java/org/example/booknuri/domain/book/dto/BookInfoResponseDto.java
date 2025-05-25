
package org.example.booknuri.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//도서상세정보 정적데이터 다루는 dto

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookInfoResponseDto {
    private String bookname;
    private String authors;
    private String publisher;
    private String publicationDate;
    private String isbn13;
    private String description;
    private String bookImageURL;
    private String mainCategory;
    private String middleCategory;
    private String subCategory;


}
