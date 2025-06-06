package org.example.booknuri.domain.recommend.dto;


import lombok.*;

@Getter
@NoArgsConstructor
public class RecommendBookDto {
    private Long id;
    private String bookname;
    private String authors;
    private String bookImageURL;
    private String isbn13;
    private String description;
    private Long viewCount;

    public RecommendBookDto(
            Long id,
            String bookname,
            String authors,
            String bookImageURL,
            String isbn13,
            String description,
            Long viewCount
    ) {
        this.id = id;
        this.bookname = bookname;
        this.authors = authors;
        this.bookImageURL = bookImageURL;
        this.isbn13 = isbn13;
        this.description = description;
        this.viewCount = viewCount;
    }
}
