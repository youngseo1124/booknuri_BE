package org.example.booknuri.domain.Calendar.converter;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.Calendar.dto.MonthlyCalendarThumbnailResponseDto;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.springframework.stereotype.Component;

@Component
public class CalendarBookConverter {

    //이 컨버터는 날짜 필터링 끝난 최신 MyShelfBookEntity 기준으로 DTO 하나 뽑아주는 역할
    public MonthlyCalendarThumbnailResponseDto.ThumbnailBookDto toThumbnailDto(MyShelfBookEntity entity) {
        BookEntity book = entity.getBook();
        return MonthlyCalendarThumbnailResponseDto.ThumbnailBookDto.builder()
                .bookId(book.getId())
                .title(book.getBookname())
                .imageUrl(book.getBookImageURL())
                .build();
    }
}
