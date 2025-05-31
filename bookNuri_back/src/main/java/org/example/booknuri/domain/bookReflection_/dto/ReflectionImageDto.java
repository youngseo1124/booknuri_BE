package org.example.booknuri.domain.bookReflection_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReflectionImageDto {
    private Long id;
    private String url;
}
