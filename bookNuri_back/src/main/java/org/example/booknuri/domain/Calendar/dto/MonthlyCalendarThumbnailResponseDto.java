package org.example.booknuri.domain.Calendar.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyCalendarThumbnailResponseDto {
    private int year;
    private int month;
    private Map<String, ThumbnailBookDto> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ThumbnailBookDto {
        private Long bookId;
        private String title;
        private String imageUrl;
    }
}
