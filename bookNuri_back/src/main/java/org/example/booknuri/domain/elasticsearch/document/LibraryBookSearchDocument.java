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

    @Field(type = FieldType.Keyword)
    private String libCode;
    private Long bookId;


    @Field(type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "standard")
    private String bookname;

    private String authors;
    private String publisher;
    private Integer publicationDate;
    private String isbn13;
    private String bookImageURL;

    private int likeCount;
    private int reviewCount;

    // ✅ 추가된 카테고리
    private Long mainCategoryId;
    private Long middleCategoryId;
    private Long subCategoryId;


}
