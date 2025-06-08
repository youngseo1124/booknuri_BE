package org.example.booknuri.domain.bookQuote.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyQuoteGroupedPageResponseDto {
    private int pageNumber;
    private int pageSize;
    private int totalCount;
    private int totalQuoteCount;
    private List<MyQuoteGroupedByBookResponseDto> content;
}
