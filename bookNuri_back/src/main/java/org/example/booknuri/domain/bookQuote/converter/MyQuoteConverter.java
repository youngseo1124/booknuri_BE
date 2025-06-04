package org.example.booknuri.domain.bookQuote.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.dto.MyQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookReview_.dto.MyReviewResponseDto;
import org.example.booknuri.domain.bookReview_.entity.BookReviewEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyQuoteConverter {

    //  단일 인용 엔티티 → 내가 쓴 인용 DTO 변환
    public MyQuoteResponseDto toDto(BookQuoteEntity entity, UserEntity currentUser) {
        return MyQuoteResponseDto.builder()
                .quoteId(entity.getId())                           // 인용 ID
                .quoteText(entity.getQuoteText())                 // 인용 문장
                .createdAt(entity.getCreatedAt())                 // 작성일
                .updatedAt(entity.getUpdatedAt())                 // 수정일
                .likeCount(entity.getLikeCount())

                .fontScale(entity.getFontScale())                 // 글자 크기 비율
                .fontColor(entity.getFontColor())                 // 글자 색상
                .backgroundId(entity.getBackgroundId())           // 배경 ID
                .visibleToPublic(entity.isVisibleToPublic())      // 공개 여부

                // 책 정보
                .bookTitle(entity.getBook().getBookname())        // 책 제목
                .bookAuthor(entity.getBook().getAuthors())
                .isbn13(entity.getBook().getIsbn13())               // ISBN
                .bookImageUrl(entity.getBook().getBookImageURL()) // 책 이미지 URL

                .build();
    }



    //  인용 리스트 → 내가 쓴 인용 DTO 리스트 변환
    public List<MyQuoteResponseDto> toDtoList(List<BookQuoteEntity> entities, UserEntity currentUser) {
        return entities.stream()
                .map(entity -> toDto(entity, currentUser)) // 단일 변환 재사용
                .collect(Collectors.toList());
    }
}
