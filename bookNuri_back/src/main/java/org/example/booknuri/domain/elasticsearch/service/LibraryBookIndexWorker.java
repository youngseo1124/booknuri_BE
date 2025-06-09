package org.example.booknuri.domain.elasticsearch.service;
import java.util.Objects;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryBookIndexWorker {

    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookSearchRepository searchRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;


    /*//색인 초기화
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
    }*/


    //색인 키 없는 경우만 insert
    @Transactional(readOnly = true)
    public void indexPage(int pageNumber, int pageSize) {
        Page<LibraryBookEntity> libraryBooks = libraryBookRepository.findAll(PageRequest.of(pageNumber, pageSize));

        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        // ✅ 미리 ID 리스트 만들기
        List<String> idList = libraryBooks.getContent().stream()
                .map(lb -> lb.getLibCode() + "_" + lb.getBook().getId())
                .toList();

  // ✅ Elasticsearch에 이미 있는 ID 한 번에 조회
        Iterable<LibraryBookSearchDocument> existingDocs = searchRepository.findAllById(idList);
        Set<String> existingIdSet = StreamSupport.stream(existingDocs.spliterator(), false)
                .map(LibraryBookSearchDocument::getId)
                .collect(Collectors.toSet());

        List<LibraryBookSearchDocument> documents = libraryBooks.getContent().stream()
                .map(lb -> {
                    try {
                        BookEntity book = lb.getBook();
                        String id = lb.getLibCode() + "_" + book.getId();

                        if (existingIdSet.contains(id)) {
                            return null;
                        }

                        String isbn = book.getIsbn13();
                        Integer rawDate = book.getPublicationDate();
                        Integer validDate = (rawDate != null && rawDate >= 1000 && rawDate <= 9999) ? rawDate : null;

                        int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(isbn);
                        int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);

                        return LibraryBookSearchDocument.builder()
                                .id(id)
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
                })
                .filter(Objects::nonNull)
                .toList();

        searchRepository.saveAll(documents);
        log.info("✅ 색인 완료 - 페이지: {}", pageNumber);
    }



    //--------------------선택한 도서관 코드들 만 색인추가-------------------
    @Transactional(readOnly = true)
    public void indexLibraryBooksBatch(List<LibraryBookEntity> libraryBooks, int pageNumber) {
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);

        List<String> idList = libraryBooks.stream()
                .map(lb -> lb.getLibCode() + "_" + lb.getBook().getId())
                .toList();

        Iterable<LibraryBookSearchDocument> existingDocs = searchRepository.findAllById(idList);
        Set<String> existingIdSet = StreamSupport.stream(existingDocs.spliterator(), false)
                .map(LibraryBookSearchDocument::getId)
                .collect(Collectors.toSet());

        List<LibraryBookSearchDocument> documents = libraryBooks.stream()
                .map(lb -> {
                    try {
                        BookEntity book = lb.getBook();
                        String id = lb.getLibCode() + "_" + book.getId();

                        if (existingIdSet.contains(id)) return null;

                        int reviewCount = bookReviewRepository.countByBook_Isbn13AndIsActiveTrue(book.getIsbn13());
                        int viewCount = bookViewCountLogRepository.getTotalViewCountByBookAndDateRange(book, sixMonthsAgo, now);
                        Integer pubDate = book.getPublicationDate();
                        Integer validDate = (pubDate != null && pubDate >= 1000 && pubDate <= 9999) ? pubDate : null;

                        return LibraryBookSearchDocument.builder()
                                .id(id)
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
                                .build();
                    } catch (Exception e) {
                        log.warn("❌ 색인 오류: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        searchRepository.saveAll(documents);
        log.info("✅ 색인 완료 - 페이지: {}", pageNumber);
    }

}
