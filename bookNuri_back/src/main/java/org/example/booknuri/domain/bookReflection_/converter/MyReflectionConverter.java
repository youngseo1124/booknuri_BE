package org.example.booknuri.domain.bookReflection_.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection_.dto.MyReflectionResponseDto;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionLikeRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyReflectionConverter {

    private final BookReflectionLikeRepository bookReflectionLikeRepository;

    // ✨ 단일 독후감 엔티티 → 내가 쓴 독후감 DTO 변환
    public MyReflectionResponseDto toDto(BookReflectionEntity entity, UserEntity currentUser) {

        return MyReflectionResponseDto.builder()
                .reflectionId(entity.getId())                         // 독후감 ID
                .content(entity.getContent())                         // 독후감 내용
                .rating(entity.getRating())                           // 별점
                .createdAt(entity.getCreatedAt())                     // 작성일
                .updatedAt(entity.getUpdatedAt())                     // 수정일
                .likeCount(entity.getLikeCount())                     // 좋아요 수
                .containsSpoiler(entity.isContainsSpoiler())
                // 책 정보
                .bookTitle(entity.getBook().getBookname())            // 책 제목
                .isbn13(entity.getBook().getIsbn13())                 // 책 ISBN
                .bookImageUrl(entity.getBook().getBookImageURL())     // 책 이미지 URL
                .visibleToPublic(entity.isVisibleToPublic())
                .build();
    }

    // 여러 독후감 리스트 → 내가 쓴 독후감 DTO 리스트로 변환
    public List<MyReflectionResponseDto> toDtoList(List<BookReflectionEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser)) // 위에서 만든 단일 변환 메서드 재사용
                .collect(Collectors.toList());
    }
}
