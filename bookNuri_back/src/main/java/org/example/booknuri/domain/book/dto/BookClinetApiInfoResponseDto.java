//도서관별 소장목록api 응답용+ 도서상세정보api 응답용 dto 두 목적으로 쓰고있음

package org.example.booknuri.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//clientapit도서관별 소장목록api 응답용로 쓰고있음


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookClinetApiInfoResponseDto {
    private String bookname;
    private String authors;
    private String publisher;
    private Integer publicationDate;
    private String isbn13;
    private String description;
    private String bookImageURL;
    private String classNm;
    private String mainCategory;
    private String middleCategory;
    private String subCategory;

    // LibraryBookEntity용
    private String regDate;


}
