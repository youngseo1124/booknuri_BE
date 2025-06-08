package org.example.booknuri.domain.myBookshelf_.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class MyShelfBookResponseDto {
    private String isbn13;
    private String bookname;
    private String authors;
    private String bookImageURL;

    private boolean lifeBook;
    private MyShelfBookEntity.BookStatus status;
    private LocalDate createdAt;
    private LocalDate finishedAt;
}
