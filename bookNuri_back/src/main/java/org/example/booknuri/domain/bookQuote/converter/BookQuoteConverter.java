package org.example.booknuri.domain.bookQuote.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteCreateRequestDto;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteLikeRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookQuoteConverter {

    private final BookQuoteLikeRepository bookQuoteLikeRepository;

    // 단일 조회용 변환
    public BookQuoteResponseDto toDto(BookQuoteEntity entity, UserEntity currentUser) {
        boolean isLiked = bookQuoteLikeRepository.existsByUserAndQuote(currentUser, entity);
        boolean isMine = entity.getUser().getUsername().equals(currentUser.getUsername());

        return BookQuoteResponseDto.builder()
                .id(entity.getId())
                .quoteText(entity.getQuoteText())
                .fontScale(entity.getFontScale())
                .fontColor(entity.getFontColor())
                .backgroundId(entity.getBackgroundId())
                .reviewerUsername(entity.getUser().getUsername())
                .createdAt(entity.getCreatedAt())
                .likeCount(entity.getLikeCount())
                .isLikedByCurrentUser(isLiked)
                .isWrittenByCurrentUser(isMine)
                .visibleToPublic(entity.isVisibleToPublic())
                .isbn13(entity.getBook().getIsbn13())
                .build();
    }

    public List<BookQuoteResponseDto> toDtoList(List<BookQuoteEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser))
                .collect(Collectors.toList());
    }

    //  등록용 엔티티 변환
    public BookQuoteEntity toEntity(BookQuoteCreateRequestDto dto, BookEntity book, UserEntity user) {
        return BookQuoteEntity.builder()
                .quoteText(dto.getQuoteText())
                .fontScale(dto.getFontScale())
                .fontColor(dto.getFontColor())
                .backgroundId(dto.getBackgroundId())
                .visibleToPublic(dto.isVisibleToPublic())
                .book(book)
                .user(user)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    private static java.time.LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }
}
