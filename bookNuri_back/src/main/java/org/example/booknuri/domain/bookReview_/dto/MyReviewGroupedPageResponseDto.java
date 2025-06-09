package org.example.booknuri.domain.bookReview_.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReviewGroupedPageResponseDto {

    private int pageNumber;
    private int pageSize;
    private int totalCount;
    private int totalReviewCount;
    private List<MyReviewGroupedByBookResponseDto> content;
}
