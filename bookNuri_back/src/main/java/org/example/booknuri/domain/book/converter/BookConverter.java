package org.example.booknuri.domain.book.converter;

import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;

public class BookConverter {

    public static BookInfoResponseDto toBookInfoResponseDto(BookEntity book) {
        return BookInfoResponseDto.builder()
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .publisher(book.getPublisher())
                .publicationDate(book.getPublicationDate())
                .isbn13(book.getIsbn13())
                .description(book.getDescription())
                .bookImageURL(book.getBookImageURL())
                .mainCategory(book.getMainCategory() != null ? book.getMainCategory().getName() : null)
                .middleCategory(book.getMiddleCategory() != null ? book.getMiddleCategory().getName() : null)
                .subCategory(book.getSubCategory() != null ? book.getSubCategory().getName() : null)
                .build();
    }
}
