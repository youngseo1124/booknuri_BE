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
import java.util.stream.Collectors;

@Service
@Slf4j
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



    //초기 색인작업
    @Transactional(readOnly = true)
    public void indexAllLibraryBooksInBatch() {
        int page = 0;
        int size = 370;
        Page<LibraryBookEntity> libraryBooks;

        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        do {
            System.out.println("📄 색인 시작 - 페이지: " + page);
            libraryBooks = libraryBookRepository.findAll(PageRequest.of(page, size));

            List<LibraryBookSearchDocument> documents = libraryBooks.getContent().stream().map(lb -> {
                BookEntity book = lb.getBook();
                String isbn = book.getIsbn13();

                try {
                    System.out.println("📚 처리 중 - bookId: " + book.getId() + ", isbn: " + isbn);

                    int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(isbn);
                    System.out.println("✅ 리뷰 카운트 완료: " + reviewCount);

                    int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);
                    System.out.println("✅ 조회수 카운트 완료: " + viewCount);

                    LibraryBookSearchDocument doc = LibraryBookSearchDocument.builder()
                            .id(lb.getLibCode() + "_" + book.getId())
                            .libCode(lb.getLibCode())
                            .bookId(book.getId())
                            .bookname(book.getBookname())
                            .authors(book.getAuthors())
                            .publisher(book.getPublisher())
                            .publicationDate(book.getPublicationDate())
                            .isbn13(isbn)
                            .description(book.getDescription())
                            .bookImageURL(book.getBookImageURL())
                            .likeCount(viewCount)
                            .reviewCount(reviewCount)
                            .build();

                    System.out.println("✅ 문서 build 완료 - bookId: " + book.getId());
                    return doc;

                } catch (Exception e) {
                    System.out.println("❌ 오류 발생 - bookId: " + book.getId() + ", message: " + e.getMessage());
                    return null;  // 이건 추후 필터링
                }

            }).filter(doc -> doc != null).toList();

            searchRepository.saveAll(documents);
            System.out.println("✅ 색인 완료 - 페이지: " + page);

            page++;
        } while (!libraryBooks.isLast());
    }

    // LibraryBookIndexService.java

    @Transactional(readOnly = true)
    public void indexSingleLibraryBook(LibraryBookEntity lb) {
        BookEntity book = lb.getBook();
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        try {
            int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
            int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);

            LibraryBookSearchDocument doc = LibraryBookSearchDocument.builder()
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
                    .likeCount(viewCount)
                    .reviewCount(reviewCount)
                    .build();

            searchRepository.save(doc);
            log.info("🔄 Elasticsearch 색인 완료 - {}", doc.getId());

        } catch (Exception e) {
            log.error("❌ 색인 중 오류 - bookId: {}, message: {}", book.getId(), e.getMessage(), e);
        }
    }



}
