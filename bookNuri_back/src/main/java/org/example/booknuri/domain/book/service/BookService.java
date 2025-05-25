package org.example.booknuri.domain.book.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public BookInfoResponseDto getBookDetailByIsbn(String isbn13) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ISBN입니다: " + isbn13));

        return BookConverter.toBookInfoResponseDto(book);
    }
}
