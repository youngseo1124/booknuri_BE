package org.example.booknuri.domain.bookQuote.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;
import org.example.booknuri.domain.bookQuote.dto.MyQuoteGroupedByBookResponseDto;
import org.example.booknuri.domain.bookQuote.dto.MyQuoteSimpleDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MyQuoteGroupedConverter {

    public MyQuoteGroupedByBookResponseDto toDto(List<BookQuoteEntity> quoteList) {
        if (quoteList == null || quoteList.isEmpty()) return null;

        BookQuoteEntity first = quoteList.get(0);

        return MyQuoteGroupedByBookResponseDto.builder()
                .bookInfo(BookInfoDto.builder()
                        .isbn13(first.getBook().getIsbn13())
                        .bookTitle(first.getBook().getBookname())
                        .bookAuthor(first.getBook().getAuthors())
                        .bookImageUrl(first.getBook().getBookImageURL())
                        .quoteCount(quoteList.size())
                        .build())
                .quotes(quoteList.stream()
                        .map(q -> MyQuoteSimpleDto.builder()
                                .quoteId(q.getId())
                                .quoteText(q.getQuoteText())
                                .createdAt(q.getCreatedAt())
                                .updatedAt(q.getUpdatedAt())
                                .fontScale(q.getFontScale())
                                .fontColor(q.getFontColor())
                                .backgroundId(q.getBackgroundId())
                                .likeCount(q.getLikeCount())
                                .visibleToPublic(q.isVisibleToPublic())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
