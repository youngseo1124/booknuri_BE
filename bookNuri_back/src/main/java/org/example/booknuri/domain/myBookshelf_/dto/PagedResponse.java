package org.example.booknuri.domain.myBookshelf_.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private int pageNumber;
    private int pageSize;
    private long totalCount;
    private boolean isLast;


    private List<T> content;

}
