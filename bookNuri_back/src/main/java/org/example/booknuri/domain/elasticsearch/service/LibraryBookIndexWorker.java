package org.example.booknuri.domain.elasticsearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.example.booknuri.domain.elasticsearch.repository.LibraryBookSearchRepository;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.Log.repository.BookViewCountLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryBookIndexWorker {

    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;

    @Transactional(readOnly = true)
    public void indexPage(int pageNumber, int pageSize) {
        Page<LibraryBookEntity> libraryBooks = libraryBookRepository.findAll(PageRequest.of(pageNumber, pageSize));

        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        List<LibraryBookSearchDocument> documents = libraryBooks.getContent().stream().map(lb -> {
            try {
                BookEntity book = lb.getBook(); // Lazy 가능
                String isbn = book.getIsbn13();
                Integer rawDate = book.getPublicationDate();
                Integer validDate = (rawDate != null && rawDate >= 1000 && rawDate <= 9999) ? rawDate : null;


                int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(isbn);
                int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);

                return LibraryBookSearchDocument.builder()
                        .id(lb.getLibCode() + "_" + book.getId())
                        .libCode(lb.getLibCode())
                        .bookId(book.getId())
                        .bookname(book.getBookname())
                        .authors(book.getAuthors())
                        .publisher(book.getPublisher())
                        .publicationDate(validDate)
                        .isbn13(isbn)
                        .bookImageURL(book.getBookImageURL())
                        .likeCount(viewCount)
                        .reviewCount(reviewCount)
                        .build();
            } catch (Exception e) {
                log.warn("❌ 색인 오류: " + e.getMessage());
                return null;
            }
        }).filter(doc -> doc != null).toList();

        searchRepository.saveAll(documents);
        log.info("✅ 색인 완료 - 페이지: {}", pageNumber);
    }
}
