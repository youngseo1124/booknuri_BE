package org.example.booknuri.domain.bookReflection_.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection_.dto.BookReflectionCreateRequestDto;
import org.example.booknuri.domain.bookReflection_.dto.BookReflectionResponseDto;
import org.example.booknuri.domain.bookReflection_.dto.ReflectionImageDto;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionImageEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionImageRepository;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionLikeRepository;
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
    private final BookReflectionImageRepository bookReflectionImageRepository;

    // 단일 변환
    public BookReflectionResponseDto toDto(BookReflectionEntity entity, UserEntity currentUser) {
        boolean isLiked = bookReflectionLikeRepository.existsByUserAndReflection(currentUser, entity);
        boolean isMine = entity.getUser().getUsername().equals(currentUser.getUsername()); // 내가 쓴 독후감인지 체크


        List<ReflectionImageDto> imageList = bookReflectionImageRepository.findByReflection(entity)
                .stream()
                .map(image -> new ReflectionImageDto(image.getId(), image.getImageUrl()))
                .collect(Collectors.toList());

        return BookReflectionResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .rating(entity.getRating())
                .reviewerUsername(entity.getUser().getUsername())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .likeCount(entity.getLikeCount())
                .isLikedByCurrentUser(isLiked)
                .isWrittenByCurrentUser(isMine)
                .containsSpoiler(entity.isContainsSpoiler())
                .visibleToPublic(entity.isVisibleToPublic())
                .isbn13(entity.getBook().getIsbn13())
                .imageList(imageList)
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
                .title(dto.getTitle())
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
