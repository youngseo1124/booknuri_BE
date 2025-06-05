package org.example.booknuri.domain.elasticsearch.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBookSearchResponseDto {
    private long totalCount;
    private List<LibraryBookSearchDocument> results;
}
