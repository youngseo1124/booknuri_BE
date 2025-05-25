package org.example.booknuri.global.DBConstruction.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.converter.BookClinetApiInfoConverter;
import org.example.booknuri.domain.book.dto.BookClinetApiInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.example.booknuri.global.DBConstruction.client.LibraryBookApiClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryProcessorService {

    private final LibraryBookApiClient apiClient;
    private final BookClinetApiInfoConverter bookConverter;
    private final BookRepository bookRepository;
    private final LibraryBookRepository libraryBookRepository;

    @Async
    public CompletableFuture<Void> processLibraryBooksAsync(LibraryEntity library) {

        if (library == null) {
            log.error("❌ 도서관 정보가 null.비동기 스킵됨");
            return CompletableFuture.completedFuture(null);
        }

        String libCode = library.getLibCode();

        try {
            log.info("📖 [START] {} 도서관 도서 처리 시작", libCode);

            // 해당 도서관에서 책 기본 정보 리스트 받아옴 (isbn13, 등록일 등)
            List<BookClinetApiInfoResponseDto> bookList = apiClient.fetchBooksFromLibrary(libCode);
            log.info(" {} 도서관 - 도서 수집 완료 ({}권)", libCode, bookList.size());

            for (BookClinetApiInfoResponseDto basicDto : bookList) {
                String isbn = basicDto.getIsbn13();
                String regDate = basicDto.getRegDate();

                if (isbn == null || isbn.isBlank()) {
                    log.warn("⚠️ ISBN 없음 - 스킵 (libCode: {})", libCode);
                    continue;
                }

                try {
                    // Book 저장 (트랜잭션 단위)
                    BookEntity book = processSingleBook(isbn);

                    if (book == null) {
                        log.warn("🚫 [{}] 도서 저장 실패해서 소장정보도 패스함", isbn);
                        continue;
                    }

                    // LibraryBook 저장 (트랜잭션 단위)
                    processLibraryBookIfNeeded(libCode, regDate, book);

                } catch (Exception e) {
                    log.error("💥 [{}] 한 권 저장 중 오류 발생 - {}", isbn, e.getMessage(), e);
                }
            }

            log.info(" [COMPLETE] {} 도서관 처리 완료!", libCode);

        } catch (Exception e) {
            log.error("🔥 [FAIL] {} 도서관 처리 중 전체 예외 발생: {}", libCode, e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // 단일 ISBN에 대해 BookEntity 저장 처리
     // 이미 있으면 skip, 없으면 상세 API 호출 후 저장
    @Transactional
    public BookEntity processSingleBook(String isbn) {
        BookEntity book = bookRepository.findByIsbn13(isbn).orElse(null);

        if (book != null) {
            log.debug(" 기존 도서 있음 - {}", isbn);
            return book;
        }

        try {
            Thread.sleep(150);
            JsonNode bookNode = apiClient.fetchBookDetailByIsbn(isbn);
            BookClinetApiInfoResponseDto detailDto = bookConverter.toDto(bookNode);
            book = bookConverter.toEntity(detailDto);

            book = bookRepository.save(book);
            log.info("✅ [신규도서 저장] {} ({})", detailDto.getBookname(), isbn);
            return book;

        } catch (Exception e) {
            log.error("❌ [도서 저장 실패] ISBN: {}, 이유: {}", isbn, e.getMessage(), e);
            return null;
        }
    }

    //2. 도서관 소장 관계 저장 처리
    //이미 존재하면 skip, 없으면 저장
    @Transactional
    public void processLibraryBookIfNeeded(String libCode, String regDate, BookEntity book) {
        if (!libraryBookRepository.existsByLibCodeAndBook(libCode, book)) {
            LibraryBookEntity libBook = LibraryBookEntity.builder()
                    .libCode(libCode)
                    .book(book)
                    .regDate(regDate)
                    .build();

            libraryBookRepository.save(libBook);
            log.info("📚 [{}] - 소장 정보 저장 완료 (ISBN: {})", libCode, book.getIsbn13());
        } else {
            log.debug("🟡 [{}] - 이미 소장 정보 존재함 (ISBN: {})", libCode, book.getIsbn13());
        }
    }
}
