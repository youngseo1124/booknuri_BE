package org.example.booknuri.domain.myBookshelf_.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.bookQuote.converter.BookQuoteConverter;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.bookReflection_.converter.BookReflectionConverter;
import org.example.booknuri.domain.bookReflection_.dto.BookReflectionResponseDto;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionLikeRepository;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.bookReview_.converter.BookReviewConverter;
import org.example.booknuri.domain.bookReview_.dto.BookReviewResponseDto;
import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview_.repository.BookReviewLikeRepository;
import org.example.booknuri.domain.bookReview_.repository.BookReviewRepository;
import org.example.booknuri.domain.bookReview_.service.BookReviewService;
import org.example.booknuri.domain.myBookshelf_.converter.MyShelfBookConverter;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookResponseDto;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookWithExtrasResponseDto;
import org.example.booknuri.domain.myBookshelf_.dto.PagedResponse;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.myBookshelf_.repository.MyShelfBookRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Transactional
@Service
@RequiredArgsConstructor
public class MyShelfBookService {

    private final MyShelfBookRepository myShelfBookRepository;
    private final UserService userService;
    private final BookService bookService;
    private final MyShelfBookConverter myShelfBookConverter;
    private final BookReviewRepository bookReviewRepository;
    private final BookReflectionRepository bookReflectionRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final BookQuoteConverter bookQuoteConverter;
    private final BookReviewConverter bookReviewConverter;
    private final BookReflectionConverter bookReflectionConverter;


    public boolean addToShelf(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        if (myShelfBookRepository.existsByUserAndBook(user, book)) {
            return false; // 이미 존재함
        }

        MyShelfBookEntity entity = MyShelfBookEntity.builder()
                .user(user)
                .book(book)
                .status(MyShelfBookEntity.BookStatus.WANT_TO_READ)
                .lifeBook(false)
                .build();

        myShelfBookRepository.save(entity);
        return true; // 새로 추가됨
    }


    public void updateStatus(String username, String isbn13, MyShelfBookEntity.BookStatus status) {
        MyShelfBookEntity entity = getByUserAndBook(username, isbn13);
        entity.updateStatus(status);
    }

    public void toggleLifeBook(String username, String isbn13) {
        MyShelfBookEntity entity = getByUserAndBook(username, isbn13);
        entity.updateLifeBook(!entity.isLifeBook());
    }

    private MyShelfBookEntity getByUserAndBook(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        return myShelfBookRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("책장이 존재하지 않습니다."));
    }


    //책장에서 책 삭제
    public void removeBook(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        MyShelfBookEntity entity = myShelfBookRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("책장이에 존재하지 않는 책입니다."));

        myShelfBookRepository.delete(entity);
    }


    public PagedResponse<MyShelfBookWithExtrasResponseDto> getMyShelfWithExtras(
            String username, int page, int size,
            MyShelfBookEntity.BookStatus status,
            Boolean lifeBookOnly,
            String keyword
    ) {
        UserEntity user = userService.getUserByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 기본 조건 먼저 가져오고
        List<MyShelfBookEntity> all = myShelfBookRepository.findByUser(user);

        // 상태 필터
        if (status != null) {
            all = all.stream()
                    .filter(book -> book.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // 인생책 필터
        if (Boolean.TRUE.equals(lifeBookOnly)) {
            all = all.stream()
                    .filter(MyShelfBookEntity::isLifeBook)
                    .collect(Collectors.toList());
        }

        // 키워드 필터 (bookname like)
        if (keyword != null && !keyword.isBlank()) {
            all = all.stream()
                    .filter(book -> book.getBook().getBookname().contains(keyword))
                    .collect(Collectors.toList());
        }

        //  최신순 정렬 추가
        all = all.stream()
                .sorted(Comparator.comparing(MyShelfBookEntity::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 페이징 수동처리
        int start = Math.min((int) pageable.getOffset(), all.size());
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<MyShelfBookEntity> paged = all.subList(start, end);

        List<MyShelfBookWithExtrasResponseDto> content = paged.stream()
                .map(shelf -> {
                    BookEntity book = shelf.getBook();
                    MyShelfBookResponseDto shelfDto = myShelfBookConverter.toDto(shelf);
                    BookReviewEntity review = bookReviewRepository.findByUserAndBookAndIsActiveTrue(user, book).orElse(null);
                    BookReviewResponseDto reviewDto = (review != null) ? bookReviewConverter.toDto(review, user) : null;
                    List<BookQuoteEntity> quoteList = bookQuoteRepository.findAllByUserAndBook(user, book);
                    List<BookQuoteResponseDto> quoteDtos = bookQuoteConverter.toDtoList(quoteList, user);
                    BookReflectionEntity reflection = bookReflectionRepository.findByUserAndBookAndIsActiveTrue(user, book).orElse(null);
                    BookReflectionResponseDto reflectionDto = (reflection != null) ? bookReflectionConverter.toDto(reflection, user) : null;

                    return MyShelfBookWithExtrasResponseDto.builder()
                            .shelfInfo(shelfDto)
                            .myReview(reviewDto)
                            .myQuotes(quoteDtos)
                            .myReflection(reflectionDto)
                            .build();
                })
                .collect(Collectors.toList());

        return PagedResponse.<MyShelfBookWithExtrasResponseDto>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalCount(all.size())
                .isLast(end == all.size())
                .build();
    }






}
