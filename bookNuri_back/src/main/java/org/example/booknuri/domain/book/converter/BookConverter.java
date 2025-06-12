package org.example.booknuri.domain.book.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.dto.BookSimpleInfoDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookConverter {
    private final BookReviewRepository bookReviewRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final BookReflectionRepository bookReflectionRepository;



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

    public BookInfoDto toBookInfoDto(BookEntity book, UserEntity user) {
        int reviewCount = bookReviewRepository.countByUserAndBookAndIsActiveTrue(user, book);
        int quoteCount = bookQuoteRepository.countByUserAndBook(user, book); // 인용은 비활성화 개념 없을 수도 있음
        int reflectionCount = bookReflectionRepository.countByUserAndBookAndIsActiveTrue(user, book);

        return BookInfoDto.builder()
                .isbn13(book.getIsbn13())
                .bookTitle(book.getBookname())
                .bookAuthor(book.getAuthors())
                .bookImageUrl(book.getBookImageURL())
                .reviewCount(reviewCount)
                .quoteCount(quoteCount)
                .reflectionCount(reflectionCount)
                .build();
    }


    public BookSimpleInfoDto toBookSimpleInfoDto(BookEntity book) {
        return BookSimpleInfoDto.builder()
                .isbn13(book.getIsbn13())
                .bookTitle(book.getBookname())
                .bookAuthor(book.getAuthors())
                .bookImageUrl(book.getBookImageURL())
                .build();
    }


}
