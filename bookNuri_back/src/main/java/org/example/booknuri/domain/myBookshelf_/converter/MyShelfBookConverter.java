package org.example.booknuri.domain.myBookshelf_.converter;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookResponseDto;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MyShelfBookConverter {

    private final BookReviewRepository bookReviewRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final BookReflectionRepository bookReflectionRepository;

    public MyShelfBookResponseDto toDto(MyShelfBookEntity entity) {
        BookEntity book = entity.getBook();

        // 책당 개수 집계
        int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
        int quoteCount = bookQuoteRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
        int reflectionCount = bookReflectionRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());

        return MyShelfBookResponseDto.builder()
                .isbn13(book.getIsbn13())
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .bookImageURL(book.getBookImageURL())
                .lifeBook(entity.isLifeBook())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .finishedAt(entity.getFinishedAt())

                // 책당 리뷰/인용/독후감 개수 포함된 BookInfoDto 확장 형태
                .reviewCount(reviewCount)
                .quoteCount(quoteCount)
                .reflectionCount(reflectionCount)
                .build();
    }
}
