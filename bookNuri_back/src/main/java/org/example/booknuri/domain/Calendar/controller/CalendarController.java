package org.example.booknuri.domain.Calendar.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.Calendar.dto.DailyCalendarResponseDto;
import org.example.booknuri.domain.Calendar.service.CalendarService;
import org.example.booknuri.domain.Calendar.dto.MonthlyCalendarThumbnailResponseDto;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/calendar/books")
public class CalendarController {

    private final CalendarService calendarService;

    /**
     *  월별 독서 썸네일 조회
     *
     * @param year   연도 (예: 2025)
     * @param month  월 (1~12)
     * @param user   로그인한 사용자 정보
     * @return 날짜별 최신 썸네일 1권씩
     */
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyCalendarThumbnailResponseDto> getMonthlyCalendar(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam int year,
            @RequestParam int month) {

        MonthlyCalendarThumbnailResponseDto response = calendarService.getMonthlyThumbnails(user.getUsername(), year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyCalendarResponseDto> getDailyCalendar(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam String date  // 예: "2025-06-11"
    ) {
        DailyCalendarResponseDto response = calendarService.getDailyBooks(user.getUsername(), date);
        return ResponseEntity.ok(response);
    }
}
