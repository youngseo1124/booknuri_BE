package org.example.booknuri.domain.bookQuote.dto;

import lombok.*;

import java.util.List;

/**
 * BookQuoteListResponseDto
 * - 특정 책에 대한 인용 리스트 응답 DTO (가로배너용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookQuoteListResponseDto {
    private List<BookQuoteResponseDto> quotes;

    private int totalCount;
}
