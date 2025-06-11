package org.example.booknuri.domain.Calendar.dto;

import lombok.Builder;
import lombok.Data;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfSimpleBookResponseDto;

import java.util.List;

@Data
@Builder
public class DailyCalendarResponseDto {
    private String date;
    private List<MyShelfSimpleBookResponseDto> shelvedBooks;
    private List<MyShelfSimpleBookResponseDto> finishedBooks;
}
