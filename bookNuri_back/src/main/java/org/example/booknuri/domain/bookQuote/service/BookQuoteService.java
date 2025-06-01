package org.example.booknuri.domain.bookQuote.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.bookQuote.converter.BookQuoteConverter;
import org.example.booknuri.domain.bookQuote.converter.MyQuoteConverter;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteCreateRequestDto;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteUpdateRequestDto;
import org.example.booknuri.domain.bookQuote.dto.MyQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookQuoteService {

    private final BookQuoteRepository bookQuoteRepository;
    private final BookRepository bookRepository;
    private final BookQuoteConverter bookQuoteConverter;
    private final MyQuoteConverter myQuoteConverter;

    // ✨ 인용 등록
    public void createQuote(BookQuoteCreateRequestDto dto, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        BookQuoteEntity entity = bookQuoteConverter.toEntity(dto, book, user);
        bookQuoteRepository.save(entity);
    }

    // ✨ 인용 수정
    public void updateQuote(BookQuoteUpdateRequestDto dto, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(dto.getQuoteId(), user)
                .orElseThrow(() -> new IllegalArgumentException("수정할 인용이 없습니다."));

        entity.updateQuote(
                dto.getQuoteText(),
                dto.getFontScale(),
                dto.getFontColor(),
                dto.getBackgroundId(),
                dto.isVisibleToPublic()
        );
    }

    // ✨ 인용 삭제
    public void deleteQuote(Long quoteId, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(quoteId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 인용이 없습니다."));
        bookQuoteRepository.delete(entity);
    }

    // ✨ 마이페이지용 내가 쓴 인용 리스트
    public List<MyQuoteResponseDto> getMyQuotes(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookQuoteEntity> page = bookQuoteRepository.findByUser(user, pageable);
        return myQuoteConverter.toDtoList(page.getContent(), user);
    }

    // ✨ 수정 화면용 단일 인용 가져오기 (내가 쓴 것만)
    public BookQuoteResponseDto getMyQuoteById(Long quoteId, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(quoteId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 인용이 없거나 접근 권한이 없습니다."));
        return bookQuoteConverter.toDto(entity, user);
    }

    // ✨ 특정 책 인용 전체 조회 (리스트용, 공개된 것만)
    public List<BookQuoteResponseDto> getQuotesByBook(String isbn13, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookQuoteEntity> page = bookQuoteRepository.findByBook_Isbn13AndVisibleToPublicTrue(isbn13, pageable);
        return bookQuoteConverter.toDtoList(page.getContent(), currentUser);
    }
}
