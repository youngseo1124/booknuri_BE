package org.example.booknuri.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.converter.BookConverter;
import org.example.booknuri.domain.book.dto.BookGroupedPageResponseDto;
import org.example.booknuri.domain.book.dto.BookInfoDto;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookConverter bookConverter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookReviewRepository bookReviewRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final BookReflectionRepository bookReflectionRepository;

    //책 상세정보 반환(정적 데이터)
    public BookInfoResponseDto getBookDetailByIsbn(String isbn13) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn13));

        return bookConverter.toBookInfoResponseDto(book);



     /*   // 1. Redis 캐시 먼저 확인
        BookInfoResponseDto cached = (BookInfoResponseDto) redisTemplate.opsForValue().get("book:detail:" + isbn13);
        if (cached != null) {
            return cached;
        }

        // 2. 없으면 DB에서 조회 후 Redis에 저장
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn13));

        BookInfoResponseDto dto = bookConverter.toBookInfoResponseDto(book);

        redisTemplate.opsForValue().set("book:detail:" + isbn13, dto);
        return dto;*/
    }

    //isbn으로 책 찾기
    public BookEntity getBookEntityByIsbn(String isbn13) {
        return bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN: " + isbn13));
    }


    public boolean existsBookByIsbn(String isbn13) {
        return bookRepository.existsByIsbn13(isbn13);
    }




    public BookGroupedPageResponseDto getGroupedBooksByTypeAndKeywordSimple(
            UserEntity user,
            String type,
            String keyword,
            int offset,
            int limit
    ) {
        List<BookEntity> books;

        // 🔁 내가 쓴 글 중 해당 타입의 책 리스트 수집
        switch (type.toLowerCase()) {
            case "review" -> books = bookReviewRepository.findAllByUserAndIsActiveTrue(user)
                    .stream().map(BookReviewEntity::getBook).toList();
            case "quote" -> books = bookQuoteRepository.findAllByUserAndIsActiveTrue(user)
                    .stream().map(BookQuoteEntity::getBook).toList();
            case "reflection" -> books = bookReflectionRepository.findAllByUserAndIsActiveTrue(user)
                    .stream().map(BookReflectionEntity::getBook).toList();
            default -> throw new IllegalArgumentException("유효하지 않은 type입니다. (review | quote | reflection)");
        }

        // ✅ 책별 최신 작성일 매핑
        Map<BookEntity, LocalDateTime> latestMap = new HashMap<>();

        for (BookEntity book : books) {
            LocalDateTime latest = switch (type.toLowerCase()) {
                case "review" -> bookReviewRepository
                        .findTopByUserAndBookAndIsActiveTrueOrderByCreatedAtDesc(user, book)
                        .map(entity -> entity.getCreatedAt()) // 여기!!
                        .orElse(LocalDateTime.MIN);
                case "quote" -> bookQuoteRepository
                        .findTopByUserAndBookAndIsActiveTrueOrderByCreatedAtDesc(user, book)
                        .map(entity -> entity.getCreatedAt())
                        .orElse(LocalDateTime.MIN);
                case "reflection" -> bookReflectionRepository
                        .findTopByUserAndBookAndIsActiveTrueOrderByCreatedAtDesc(user, book)
                        .map(entity -> entity.getCreatedAt())
                        .orElse(LocalDateTime.MIN);
                default -> LocalDateTime.MIN;
            };

            latestMap.put(book, latest);
        }

        // 🔍 중복 제거 + 키워드 필터 + 최신 작성일 순 정렬
        List<BookEntity> dedupedBooks = books.stream()
                .distinct()
                .filter(book -> keyword == null || book.getBookname().contains(keyword))
                .sorted((b1, b2) -> latestMap.get(b2).compareTo(latestMap.get(b1))) // 최신순
                .toList();

        // 📄 페이징
        int start = Math.min(offset, dedupedBooks.size());
        int end = Math.min(start + limit, dedupedBooks.size());
        List<BookEntity> pageBooks = dedupedBooks.subList(start, end);

        List<BookInfoDto> content = pageBooks.stream()
                .map(book -> bookConverter.toBookInfoDto(book, user))
                .toList();

        return BookGroupedPageResponseDto.builder()
                .pageNumber(offset / limit)
                .pageSize(limit)
                .totalCount(dedupedBooks.size())
                .content(content)
                .build();
    }




}
