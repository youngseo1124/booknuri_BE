package org.example.booknuri.domain.elasticsearch.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "library_books")
public class LibraryBookSearchDocument {

    @Id
    private String id;  // 보통: libCode + "_" + bookId

    private String libCode;
    private Long bookId;

    private String bookname;
    private String authors;
    private String publisher;
    private Integer publicationDate;
    private String isbn13;
    private String bookImageURL;

    private int likeCount;
    private int reviewCount;
}
