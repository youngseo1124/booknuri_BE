package org.example.booknuri.domain.myBookshelf_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyShelfBookRequestDto {
    private String isbn13;
}
