package org.example.booknuri.domain.BookReview.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.BookReview.dto.BookReviewResponseDto;
import org.example.booknuri.domain.BookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.BookReview.entity.BookReviewLikeEntity;
import org.example.booknuri.domain.BookReview.repository.BookReviewLikeRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookReviewConverter {

    private final BookReviewLikeRepository bookReviewLikeRepository;



    //  단일 변환
    public BookReviewResponseDto toDto(BookReviewEntity entity, UserEntity currentUser) {
        boolean isLiked = bookReviewLikeRepository.existsByUserAndReview(currentUser, entity);
        return BookReviewResponseDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .rating(entity.getRating())
                .reviewerUsername(entity.getUser().getUsername()) // 유저에서 닉네임 or username 가져오기
                .createdAt(entity.getCreatedAt())
                .likeCount(entity.getLikeCount())
                .isLikedByCurrentUser(isLiked)
                .build();
    }

    // dto 리뷰 리스트
    public List<BookReviewResponseDto> toDtoList(List<BookReviewEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser))
                .collect(Collectors.toList());
    }
}
