package org.example.booknuri.domain.book.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.converter.BookConverter;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookConverter bookConverter;
    private final RedisTemplate<String, Object> redisTemplate;

    //책 상세정보 반환(정적 데이터)
    public BookInfoResponseDto getBookDetailByIsbn(String isbn13) {
        // 1. Redis 캐시 먼저 확인
        BookInfoResponseDto cached = (BookInfoResponseDto) redisTemplate.opsForValue().get("book:detail:" + isbn13);
        if (cached != null) {
            return cached;
        }

        // 2. 없으면 DB에서 조회 후 Redis에 저장
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn13));

        BookInfoResponseDto dto = bookConverter.toBookInfoResponseDto(book);

        redisTemplate.opsForValue().set("book:detail:" + isbn13, dto);
        return dto;
    }

    //isbn으로 책 찾기
    public BookEntity getBookEntityByIsbn(String isbn13) {
        return bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN: " + isbn13));
    }


}
