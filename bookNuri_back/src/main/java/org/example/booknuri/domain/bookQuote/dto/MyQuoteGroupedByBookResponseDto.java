package org.example.booknuri.domain.bookQuote.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyQuoteGroupedByBookResponseDto {

    private BookInfoDto bookInfo;
    private List<MyQuoteSimpleDto> quotes;
}
