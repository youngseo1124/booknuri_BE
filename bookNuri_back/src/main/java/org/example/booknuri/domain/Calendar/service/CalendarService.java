package org.example.booknuri.domain.Calendar.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.Calendar.converter.CalendarBookConverter;
import org.example.booknuri.domain.Calendar.dto.DailyCalendarResponseDto;
import org.example.booknuri.domain.Calendar.dto.MonthlyCalendarThumbnailResponseDto;
import org.example.booknuri.domain.myBookshelf_.converter.MyShelfBookConverter;
import org.example.booknuri.domain.myBookshelf_.dto.MyShelfSimpleBookResponseDto;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.myBookshelf_.repository.MyShelfBookRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final MyShelfBookRepository myShelfBookRepository;
    private final UserService userService;
    private final CalendarBookConverter calendarBookConverter;
    private final MyShelfBookConverter myShelfBookConverter;


    private LocalDateTime getLatestTime(MyShelfBookEntity entity) {
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime finishedAt = entity.getFinishedAt();

        if (createdAt == null) return finishedAt;
        if (finishedAt == null) return createdAt;
        return createdAt.isAfter(finishedAt) ? createdAt : finishedAt;
    }

    public MonthlyCalendarThumbnailResponseDto getMonthlyThumbnails(String username, int year, int month) {
        UserEntity user = userService.getUserByUsername(username);
        List<MyShelfBookEntity> books = myShelfBookRepository.findByUser(user);

        // 날짜별로 담은 책과 완독 책을 따로 수집
        Map<String, List<MyShelfBookEntity>> shelvedMap = new HashMap<>();
        Map<String, List<MyShelfBookEntity>> finishedMap = new HashMap<>();

        for (MyShelfBookEntity book : books) {
            // 1. 책장 등록 날짜 처리
            if (book.getCreatedAt() != null && book.getCreatedAt().getYear() == year && book.getCreatedAt().getMonthValue() == month) {
                String dateKey = book.getCreatedAt().toLocalDate().toString();
                shelvedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(book);
            }

            // 2. 완독 날짜 처리
            if (book.getFinishedAt() != null && book.getFinishedAt().getYear() == year && book.getFinishedAt().getMonthValue() == month) {
                String dateKey = book.getFinishedAt().toLocalDate().toString();
                finishedMap.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(book);
            }
        }

        Map<String, MonthlyCalendarThumbnailResponseDto.ThumbnailBookDto> resultMap = new HashMap<>();

        // 해당 월의 날짜들 모두 순회
        Set<String> allDates = new HashSet<>();
        allDates.addAll(shelvedMap.keySet());
        allDates.addAll(finishedMap.keySet());

        for (String date : allDates) {
            List<MyShelfBookEntity> shelved = shelvedMap.getOrDefault(date, List.of());
            List<MyShelfBookEntity> finished = finishedMap.getOrDefault(date, List.of());

            // 1순위: 책장에 담은 책 중 썸네일 있는 최신 1권
            Optional<MyShelfBookEntity> shelvedBook = shelved.stream()
                    .filter(b -> isValidThumbnail(b))
                    .sorted(Comparator.comparing(MyShelfBookEntity::getCreatedAt).reversed())
                    .findFirst();

            if (shelvedBook.isPresent()) {
                resultMap.put(date, calendarBookConverter.toThumbnailDto(shelvedBook.get()));
                continue;
            }

            // 2순위: 완독한 책 중 썸네일 있는 최신 1권
            Optional<MyShelfBookEntity> finishedBook = finished.stream()
                    .filter(b -> isValidThumbnail(b))
                    .sorted(Comparator.comparing(MyShelfBookEntity::getFinishedAt).reversed())
                    .findFirst();

            finishedBook.ifPresent(book -> resultMap.put(date, calendarBookConverter.toThumbnailDto(book)));
        }

        return MonthlyCalendarThumbnailResponseDto.builder()
                .year(year)
                .month(month)
                .data(resultMap)
                .build();
    }

    private boolean isValidThumbnail(MyShelfBookEntity book) {
        return book.getBook().getBookImageURL() != null && !book.getBook().getBookImageURL().isBlank();
    }


    public DailyCalendarResponseDto getDailyBooks(String username, String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        UserEntity user = userService.getUserByUsername(username);
        List<MyShelfBookEntity> allBooks = myShelfBookRepository.findByUser(user);

        List<MyShelfSimpleBookResponseDto> shelvedBooks = allBooks.stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().toLocalDate().isEqual(targetDate))
                .map(myShelfBookConverter::toSimpleDto)
                .collect(Collectors.toList());

        List<MyShelfSimpleBookResponseDto> finishedBooks = allBooks.stream()
                .filter(b -> b.getFinishedAt() != null && b.getFinishedAt().toLocalDate().isEqual(targetDate))
                .map(myShelfBookConverter::toSimpleDto)
                .collect(Collectors.toList());

        return DailyCalendarResponseDto.builder()
                .date(dateStr)
                .shelvedBooks(shelvedBooks)
                .finishedBooks(finishedBooks)
                .build();
    }



}
