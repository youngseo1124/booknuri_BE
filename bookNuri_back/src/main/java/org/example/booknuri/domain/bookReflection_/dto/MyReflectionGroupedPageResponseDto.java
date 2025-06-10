package org.example.booknuri.domain.bookReflection_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReflectionGroupedPageResponseDto {
    private int pageNumber;    // 현재 페이지 번호
    private int pageSize;      // 한 페이지 크기
    private int totalReflectionCount;  // 전체 내가 쓴 독후감 개수
    private List<MyReflectionGroupedByBookResponseDto> content;
}
