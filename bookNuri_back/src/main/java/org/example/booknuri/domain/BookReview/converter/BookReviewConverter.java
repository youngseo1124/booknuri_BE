package org.example.booknuri.domain.bookReview.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReview.dto.BookReviewCreateRequestDto;
import org.example.booknuri.domain.bookReview.dto.BookReviewResponseDto;
import org.example.booknuri.domain.bookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview.repository.BookReviewLikeRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookReviewConverter {

    private final BookReviewLikeRepository bookReviewLikeRepository;



    //  단일 변환
    public BookReviewResponseDto toDto(BookReviewEntity entity, UserEntity currentUser) {
        boolean isLiked = bookReviewLikeRepository.existsByUserAndReview(currentUser, entity);
        boolean isMine = entity.getUser().getUsername().equals(currentUser.getUsername()); //  내가 쓴 리뷰인지 체크
        return BookReviewResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .rating(entity.getRating())
                .reviewerUsername(entity.getUser().getUsername()) // 유저에서 닉네임 or username 가져오기
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .likeCount(entity.getLikeCount())
                .isLikedByCurrentUser(isLiked)
                .isWrittenByCurrentUser(isMine)
                .containsSpoiler(entity.isContainsSpoiler())
                .build();
    }

    // dto 리뷰 리스트
    public List<BookReviewResponseDto> toDtoList(List<BookReviewEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser))
                .collect(Collectors.toList());
    }

    // DTO → Entity 변환 (리뷰 작성 시 사용)
    public BookReviewEntity toEntity(BookReviewCreateRequestDto dto, BookEntity book, UserEntity user) {
        return BookReviewEntity.builder()
                .book(book)
                .user(user)
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .likeCount(0)
                .containsSpoiler(dto.isContainsSpoiler()) //  스포일러 여부도 포함
                .build();
    }
}
