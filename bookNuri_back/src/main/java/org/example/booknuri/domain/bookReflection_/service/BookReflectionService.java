package org.example.booknuri.domain.bookReflection_.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.bookReflection_.converter.BookReflectionConverter;
import org.example.booknuri.domain.bookReflection_.converter.MyReflectionConverter;
import org.example.booknuri.domain.bookReflection_.dto.*;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class BookReflectionService {

    private final BookReflectionRepository bookReflectionRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BookReflectionConverter bookReflectionConverter;
    private final MyReflectionConverter myReflectionConverter;

    // 내가 이미 이 책에 독후감 썼는지 확인 (true: 이미 작성함)
    public boolean checkAlreadyReflected(String isbn13, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));
        return bookReflectionRepository.existsByUserAndBook(user, book);
    }

    // 독후감 쓰기
    public void createReflection(BookReflectionCreateRequestDto dto, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        boolean alreadyReflected = bookReflectionRepository.existsByUserAndBook(user, book);
        if (alreadyReflected) {
            throw new IllegalStateException("이미 이 책에 독후감을 작성하셨습니다.");
        }

        log.info("isPublic 값: {}", dto.isVisibleToPublic()); // 여기에 true 찍히는지 확인!

        BookReflectionEntity reflection = bookReflectionConverter.toEntity(dto, book, user);
        bookReflectionRepository.save(reflection);
    }

    // 독후감 수정화면용: 내가 쓴 특정 책에 대한 독후감 +이미지 가져와서 dto로 반환
    public BookReflectionResponseDto getMyReflectionForBook(String isbn13, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));

        BookReflectionEntity reflection = bookReflectionRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("작성한 독후감이 없습니다."));

        return bookReflectionConverter.toDto(reflection, user);
    }

    // 독후감 수정
    public void updateReflection(BookReflectionUpdateRequestDto dto, UserEntity user) {
        BookReflectionEntity reflection = bookReflectionRepository.findByIdAndUser(dto.getReflectionId(), user)
                .orElseThrow(() -> new IllegalArgumentException("독후감을 찾을 수 없습니다."));

        reflection.updateReflection(dto.getContent(), dto.getRating(), dto.isContainsSpoiler(), dto.isVisibleToPublic());

    }

    // 내가 쓴 독후감 리스트
    public List<MyReflectionResponseDto> getMyReflections(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookReflectionEntity> page = bookReflectionRepository.findByUser(user, pageable);
        List<BookReflectionEntity> reflections = page.getContent();
        return myReflectionConverter.toDtoList(reflections, user);
    }

    // 독후감 삭제
    public void deleteReflection(Long reflectionId, UserEntity user) {
        BookReflectionEntity reflection = bookReflectionRepository.findByIdAndUser(reflectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 독후감이 없습니다."));
        bookReflectionRepository.delete(reflection);
    }

    // 특정 책의 독후감 목록 조회 (정렬 선택 가능)
    public List<BookReflectionResponseDto> getReflectionsByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort));
        Page<BookReflectionEntity> page = bookReflectionRepository.findByBook_Isbn13AndIsActiveTrue(isbn13, pageable);
        List<BookReflectionEntity> reflections = page.getContent();
        return bookReflectionConverter.toDtoList(reflections, currentUser);
    }

    // 정렬 방식
    private Sort getSortOrder(String sort) {
        return switch (sort.toLowerCase()) {
            case "like" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "high" -> Sort.by(Sort.Direction.DESC, "rating");
            case "low" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    // 특정 책에 대한 독후감들 반환(ㅠㅔ이지네이션 o)
    public BookReflectionListResponseDto getReflectionsSummaryByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort));
        Page<BookReflectionEntity> page = bookReflectionRepository.findByBook_Isbn13AndIsActiveTrue(isbn13, pageable);
        List<BookReflectionEntity> reflections = page.getContent();

        Double avg = bookReflectionRepository.getAverageReflectionRatingByIsbn13(isbn13);
        double averageRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        List<BookReflectionEntity> all = bookReflectionRepository.findByBook_Isbn13AndIsActiveTrue(isbn13);
        Map<Integer, Integer> ratingDistribution = getRatingBuckets(all);

        List<BookReflectionResponseDto> dtos = bookReflectionConverter.toDtoList(reflections, currentUser);

        return BookReflectionListResponseDto.builder()
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .reflections(dtos)
                .build();
    }

    // 별점 분포 계산 로직
    private Map<Integer, Integer> getRatingBuckets(List<BookReflectionEntity> reflections) {
        Map<Integer, Integer> map = new HashMap<>();
        int[] keys = {10, 8, 6, 4, 2};

        for (int key : keys) {
            map.put(key, 0); // 기본값 세팅
        }

        for (BookReflectionEntity r : reflections) {
            int rounded = (int) Math.ceil(r.getRating() / 2.0) * 2;
            rounded = Math.min(10, Math.max(2, rounded));
            map.put(rounded, map.getOrDefault(rounded, 0) + 1);
        }

        return map;
    }
}
