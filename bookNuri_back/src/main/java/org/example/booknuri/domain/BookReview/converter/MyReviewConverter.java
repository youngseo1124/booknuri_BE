package org.example.booknuri.domain.bookReview.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReview.dto.MyReviewResponseDto;
import org.example.booknuri.domain.bookReview.entity.BookReviewEntity;
import org.example.booknuri.domain.bookReview.repository.BookReviewLikeRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyReviewConverter {

    private final BookReviewLikeRepository bookReviewLikeRepository;

    // ✨ 단일 리뷰 엔티티 → 내가 쓴 리뷰 DTO 변환
    public MyReviewResponseDto toDto(BookReviewEntity entity, UserEntity currentUser) {



        return MyReviewResponseDto.builder()
                .reviewId(entity.getId())                         // 리뷰 ID
                .content(entity.getContent())                     // 리뷰 내용
                .rating(entity.getRating())                       // 별점
                .createdAt(entity.getCreatedAt())                 // 작성일
                .updatedAt(entity.getUpdatedAt())                 // 수정일
                .likeCount(entity.getLikeCount())                 // 좋아요 수
                .containsSpoiler(entity.isContainsSpoiler())
                // 책 정보
                .bookTitle(entity.getBook().getBookname())        // 책 제목
                .isbn13(entity.getBook().getIsbn13())             // 책 ISBN
                .bookImageUrl(entity.getBook().getBookImageURL()) // 책 이미지 URL
                .build();
    }

    //  여러 리뷰 리스트 → 내가 쓴 리뷰 DTO 리스트로 변환
    public List<MyReviewResponseDto> toDtoList(List<BookReviewEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser)) // 위에서 만든 단일 변환 메서드 재사용
                .collect(Collectors.toList());
    }
}
