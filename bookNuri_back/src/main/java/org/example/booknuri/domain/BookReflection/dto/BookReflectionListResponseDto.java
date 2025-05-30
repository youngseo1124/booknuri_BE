package org.example.booknuri.domain.BookReflection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReflectionListResponseDto {
    private double averageRating;
    private Map<Integer, Integer> ratingDistribution; // 예: 10점 몇 명, 8점 몇 명...
    private List<BookReflectionResponseDto> reflections;
}
