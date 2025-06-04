package org.example.booknuri.domain.elasticsearch.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.elasticsearch.repository.LibraryBookSearchRepository;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.Log.repository.BookViewCountLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryBookIndexService {

    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;

    public void indexAllLibraryBooks() {
        List<LibraryBookEntity> allLibraryBooks = libraryBookRepository.findAll();

        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        List<LibraryBookSearchDocument> documents = allLibraryBooks.stream().map(lb -> {
            BookEntity book = lb.getBook();

            // 1️⃣ 리뷰 수 (isActive = true)
            int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());

            // 2️⃣ 6개월간 조회수 합산
            int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);

            return LibraryBookSearchDocument.builder()
                    .id(lb.getLibCode() + "_" + book.getId())
                    .libCode(lb.getLibCode())
                    .bookId(book.getId())
                    .bookname(book.getBookname())
                    .authors(book.getAuthors())
                    .publisher(book.getPublisher())
                    .publicationDate(book.getPublicationDate())
                    .isbn13(book.getIsbn13())
                    .description(book.getDescription())
                    .bookImageURL(book.getBookImageURL())
                    .likeCount(viewCount) // 🔥 인기 = 6개월간 조회수
                    .reviewCount(reviewCount)
                    .build();
        }).collect(Collectors.toList());

        searchRepository.saveAll(documents);
    }
}
