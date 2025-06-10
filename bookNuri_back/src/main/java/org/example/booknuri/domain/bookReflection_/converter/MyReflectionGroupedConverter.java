package org.example.booknuri.domain.bookReflection_.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;
import org.example.booknuri.domain.bookReflection_.dto.MyReflectionGroupedByBookResponseDto;
import org.example.booknuri.domain.bookReflection_.dto.MyReflectionSimpleDto;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyReflectionGroupedConverter {

    public MyReflectionGroupedByBookResponseDto toDto(BookReflectionEntity reflection) {
        return MyReflectionGroupedByBookResponseDto.builder()
                .bookInfo(BookInfoDto.builder()
                        .isbn13(reflection.getBook().getIsbn13())
                        .bookTitle(reflection.getBook().getBookname())
                        .bookAuthor(reflection.getBook().getAuthors())
                        .bookImageUrl(reflection.getBook().getBookImageURL())
                        .reflectionCount(1) // 한 권당 하나만 가능
                        .build())

                .build();
    }
}
