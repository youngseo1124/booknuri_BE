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
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryBookIndexService {

    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;
    private final LibraryBookIndexWorker indexWorker;

    public void indexAllLibraryBooks() {
        List<LibraryBookEntity> allLibraryBooks = libraryBookRepository.findAll();
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        List<LibraryBookSearchDocument> documents = allLibraryBooks.stream().map(lb -> {
            BookEntity book = lb.getBook();

            int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
            int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);
            Integer year = book.getPublicationDate(); // ✅ Integer 그대로 넘김

            return LibraryBookSearchDocument.builder()
                    .id(lb.getLibCode() + "_" + book.getId())
                    .libCode(lb.getLibCode())
                    .bookId(book.getId())
                    .bookname(book.getBookname())
                    .authors(book.getAuthors())
                    .publisher(book.getPublisher())
                    .publicationDate(year) // ✅ 변환 없이 Integer로 전달
                    .isbn13(book.getIsbn13())
                    .bookImageURL(book.getBookImageURL())
                    .likeCount(viewCount)
                    .reviewCount(reviewCount)
                    .build();
        }).filter(Objects::nonNull).toList();

        searchRepository.saveAll(documents);
    }

    public void indexAllLibraryBooksInBatch() {
        int pageSize = 370;
        int totalPages = libraryBookRepository.findAll().size() / pageSize + 1;

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int page = 0; page < totalPages; page++) {
            final int currentPage = page;
            executor.submit(() -> indexWorker.indexPage(currentPage, pageSize));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("🎉 전체 색인 완료");
    }

    @Transactional(readOnly = true)
    public void indexSingleLibraryBook(LibraryBookEntity lb) {
        BookEntity book = lb.getBook();
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        try {
            int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
            int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);
            Integer year = book.getPublicationDate();

            LibraryBookSearchDocument doc = LibraryBookSearchDocument.builder()
                    .id(lb.getLibCode() + "_" + book.getId())
                    .libCode(lb.getLibCode())
                    .bookId(book.getId())
                    .bookname(book.getBookname())
                    .authors(book.getAuthors())
                    .publisher(book.getPublisher())
                    .publicationDate(year) // ✅ 여기도 Integer 그대로!
                    .isbn13(book.getIsbn13())
                    .bookImageURL(book.getBookImageURL())
                    .likeCount(viewCount)
                    .reviewCount(reviewCount)
                    .build();

            searchRepository.save(doc);
            log.info("🔄 Elasticsearch 색인 완료 - {}", doc.getId());

        } catch (Exception e) {
            log.error("❌ 색인 중 오류 - bookId: {}, message: {}", book.getId(), e.getMessage(), e);
        }
    }

    public Set<Long> getBooksWithChangedStatsYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Long> reviewChangedBookIds = bookReviewRepository.findDistinctBookIdsByCreatedAtBetween(
                yesterday.atStartOfDay(), today.atStartOfDay()
        );

        List<Long> viewedBookIds = bookViewCountLogRepository.findDistinctBookIdsByDate(yesterday);

        Set<Long> allChangedBookIds = new HashSet<>();
        allChangedBookIds.addAll(reviewChangedBookIds);
        allChangedBookIds.addAll(viewedBookIds);

        return allChangedBookIds;
    }

    public void indexLibraryBooksByLibCodes(List<String> libCodeList) {
        List<LibraryBookEntity> books = libraryBookRepository.findByLibCodeIn(libCodeList);
        log.info("📚 색인 대상 도서관 수: {}, 도서 수: {}", libCodeList.size(), books.size());

        ExecutorService executor = Executors.newFixedThreadPool(10);

        int pageSize = 370;
        int totalPages = (books.size() + pageSize - 1) / pageSize;

        for (int page = 0; page < totalPages; page++) {
            final int currentPage = page;
            executor.submit(() -> {
                int start = currentPage * pageSize;
                int end = Math.min(start + pageSize, books.size());
                List<LibraryBookEntity> subList = books.subList(start, end);
                indexWorker.indexLibraryBooksBatch(subList, currentPage); // 이 메서드는 아래에서 만들거야
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("🎉 선택된 도서관 색인 완료");
    }

}
