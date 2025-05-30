package org.example.booknuri.domain.bookReflection.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection.dto.BookReflectionCreateRequestDto;
import org.example.booknuri.domain.bookReflection.dto.BookReflectionResponseDto;
import org.example.booknuri.domain.bookReflection.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection.repository.BookReflectionLikeRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookReflectionConverter {

    private final BookReflectionLikeRepository bookReflectionLikeRepository;

    // 단일 변환
    public BookReflectionResponseDto toDto(BookReflectionEntity entity, UserEntity currentUser) {
        boolean isLiked = bookReflectionLikeRepository.existsByUserAndReflection(currentUser, entity);
        boolean isMine = entity.getUser().getUsername().equals(currentUser.getUsername()); // 내가 쓴 독후감인지 체크
        return BookReflectionResponseDto.builder()
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
                .visibleToPublic(entity.isVisibleToPublic())

                .build();
    }

    // DTO 독후감 리스트
    public List<BookReflectionResponseDto> toDtoList(List<BookReflectionEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser))
                .collect(Collectors.toList());
    }

    // DTO → Entity 변환 (독후감 작성 시 사용)
    public BookReflectionEntity toEntity(BookReflectionCreateRequestDto dto, BookEntity book, UserEntity user) {
        return BookReflectionEntity.builder()
                .book(book)
                .user(user)
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .likeCount(0)
                .containsSpoiler(dto.isContainsSpoiler()) // 스포일러 여부도 포함
                .visibleToPublic(dto.isVisibleToPublic())
                .build();
    }
}
