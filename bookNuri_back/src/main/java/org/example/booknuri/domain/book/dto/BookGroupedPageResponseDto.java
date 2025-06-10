package org.example.booknuri.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookGroupedPageResponseDto {
    private int pageNumber;
    private int pageSize;
    private int totalCount;
    private List<BookInfoDto> content;
}
