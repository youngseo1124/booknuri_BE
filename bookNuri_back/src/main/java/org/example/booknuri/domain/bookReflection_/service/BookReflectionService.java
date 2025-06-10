package org.example.booknuri.domain.bookReflection_.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.bookReflection_.converter.BookReflectionConverter;
import org.example.booknuri.domain.bookReflection_.converter.MyReflectionConverter;
import org.example.booknuri.domain.bookReflection_.converter.MyReflectionGroupedConverter;
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
    private final MyReflectionGroupedConverter myReflectionGroupedConverter;

    // 내가 이미 이 책에 공개 독후감 썼는지 확인 (true: 이미 작성함)
    // master1124는 무조건 false 반환
    public boolean checkAlreadyPublicReflected(String isbn13, UserEntity user) {
        if ("master1124".equals(user.getUsername())) {
            return false;
        }

        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));

        return bookReflectionRepository.existsByUserAndBookAndVisibleToPublicTrue(user, book);
    }

    // 독후감 쓰기
    public void createReflection(BookReflectionCreateRequestDto dto, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        //공개 독후감이면 1개만 작성 가능하도록 체크
        if (!"master1124".equals(user.getUsername()) && dto.isVisibleToPublic()) {
            boolean alreadyPublic = bookReflectionRepository.existsByUserAndBookAndVisibleToPublicTrue(user, book);
            if (alreadyPublic) {
                throw new IllegalStateException("이미 이 책에 공개 독후감을 작성하셨습니다.");
            }
        }

        BookReflectionEntity reflection = bookReflectionConverter.toEntity(dto, book, user);
        bookReflectionRepository.save(reflection);
    }

    // 독후감 수정화면용: 내가 쓴 특정 책에 대한 독후감 +이미지 가져와서 dto로 반환
    public BookReflectionResponseDto getMyReflectionById(Long reflectionId, UserEntity user) {
        BookReflectionEntity reflection = bookReflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 독후감이 존재하지 않습니다."));

        if (!reflection.getUser().equals(user)) {
            throw new SecurityException("본인의 독후감만 조회할 수 있습니다.");
        }

        return bookReflectionConverter.toDto(reflection, user);
    }
    // 독후감 수정
    public void updateReflection(BookReflectionUpdateRequestDto dto, UserEntity user) {
        BookReflectionEntity reflection = bookReflectionRepository.findByIdAndUser(dto.getReflectionId(), user)
                .orElseThrow(() -> new IllegalArgumentException("독후감을 찾을 수 없습니다."));

        reflection.updateReflection(dto.getTitle(),dto.getContent(), dto.getRating(), dto.isContainsSpoiler(), dto.isVisibleToPublic());

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
        Page<BookReflectionEntity> page = bookReflectionRepository.findByBook_Isbn13AndIsActiveTrueAndVisibleToPublicTrue(isbn13, pageable);
        List<BookReflectionEntity> reflections = page.getContent();



        Double avg = bookReflectionRepository.getAverageReflectionRatingByIsbn13AndVisibleToPublicTrue(isbn13);

        double averageRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        List<BookReflectionEntity> all = bookReflectionRepository.findByBook_Isbn13AndIsActiveTrueAndVisibleToPublicTrue(isbn13);
        Map<Integer, Integer> ratingDistribution = getRatingBuckets(all);

        List<BookReflectionResponseDto> dtos = bookReflectionConverter.toDtoList(reflections, currentUser);

        int totalCount = bookReflectionRepository.countByBook_Isbn13AndIsActiveTrueAndVisibleToPublicTrue(isbn13);


        return BookReflectionListResponseDto.builder()
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .reflections(dtos)
                .totalCount(totalCount)
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

    //-----------------------------------------------------------------------------------\\

    //내가 쓴 독후감 책 그룹별로묶어 반환
    public MyReflectionGroupedPageResponseDto getMyReflectionsGroupedByBook(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookReflectionEntity> page = bookReflectionRepository.findByUser(user, pageable);

        List<MyReflectionGroupedByBookResponseDto> grouped = page.getContent().stream()
                .map(myReflectionGroupedConverter::toDto)
                .toList();

        // ✅ 전체 독후감 개수 따로 count 하기
        int totalReflectionCount = bookReflectionRepository.countByUser(user);

        return MyReflectionGroupedPageResponseDto.builder()
                .pageNumber(offset / limit)
                .pageSize(limit)
                .totalReflectionCount(totalReflectionCount) // ✅ 요게 빠졌던 거
                .content(grouped)
                .build();
    }


    //  특정 책에 대해 내가 쓴 모든 "활성화된" 독후감 리스트 반환
    public List<BookReflectionResponseDto> getMyReflectionsByBook(String isbn13, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new IllegalArgumentException("책이 존재하지 않습니다."));

        List<BookReflectionEntity> reflections = bookReflectionRepository.findAllByUserAndBook_Isbn13AndIsActiveTrueOrderByCreatedAtDesc(user, isbn13);
        return bookReflectionConverter.toDtoList(reflections, user);
    }

    /**
     *  특정 ISBN에 대해 내가 쓴 가장 최신 독후감의 ID를 반환
     */
    public Long getLatestReflectionIdByIsbn13(String isbn13, UserEntity user) {
        // Pageable 객체를 생성하여 결과 수를 1로 제한 (LIMIT 1)
        Pageable topOne = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")); // 정렬은 @Query에서 이미 명시했지만, Pageable에도 포함하는 것이 좋습니다.

        // Repository 메서드 호출
        List<Long> ids = bookReflectionRepository.findLatestReflectionIdByUserAndBook_Isbn13(user, isbn13, topOne);

        // 결과 리스트가 비어있지 않으면 첫 번째 ID 반환, 아니면 null 반환
        return ids.isEmpty() ? null : ids.get(0);
    }

}
