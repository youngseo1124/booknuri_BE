package org.example.booknuri.domain.bookReflection_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.book.dto.BookInfoDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyReflectionGroupedByBookResponseDto {
    private BookInfoDto bookInfo;             // 책 정보 (ISBN, 제목, 저자, 이미지, count들 포함)
    private MyReflectionSimpleDto reflection; // 내가 쓴 독후감 요약
}
