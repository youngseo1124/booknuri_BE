package org.example.booknuri.domain.bookReflection.dto;

// 독후감 업데이트 용 dto

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReflectionUpdateRequestDto {
    private Long reflectionId;
    private String content;
    private int rating;
    private boolean containsSpoiler;
    private boolean visibleToPublic; //공개여부
}
