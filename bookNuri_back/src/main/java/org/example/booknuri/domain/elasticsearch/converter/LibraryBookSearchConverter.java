package org.example.booknuri.domain.elasticsearch.converter;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;

public class LibraryBookSearchConverter {

    public static LibraryBookSearchDocument toDocument(LibraryBookEntity lb, int reviewCount, int viewCount) {
        BookEntity book = lb.getBook();
        Integer pubDate = book.getPublicationDate();
        Integer validDate = (pubDate != null && pubDate >= 1000 && pubDate <= 9999) ? pubDate : null;

        return LibraryBookSearchDocument.builder()
                .id(lb.getLibCode() + "_" + book.getId())
                .libCode(lb.getLibCode())
                .bookId(book.getId())
                .bookname(book.getBookname())
                .authors(book.getAuthors())
                .publisher(book.getPublisher())
                .publicationDate(validDate)
                .isbn13(book.getIsbn13())
                .bookImageURL(book.getBookImageURL())
                .likeCount(viewCount)
                .reviewCount(reviewCount)


                // 카테고리
                .mainCategoryId(book.getMainCategory() != null ? book.getMainCategory().getId() : null)
                .middleCategoryId(book.getMiddleCategory() != null ? book.getMiddleCategory().getId() : null)
                .subCategoryId(book.getSubCategory() != null ? book.getSubCategory().getId() : null)

                .build();
    }
}
