package org.example.booknuri.domain.Log.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.example.booknuri.domain.Log.repository.BookViewCountLogRepository;
import org.example.booknuri.domain.Log.repository.BookViewLogRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookViewCountScheduler {

    private final BookViewLogRepository bookViewLogRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;

    //  1분마다 실행되는 스케줄러 (개발 중이라 자정 대신 1분으로 설정)
    @Scheduled(fixedDelay = 60 * 1000)
    @Transactional
    public void saveDailyBookViewStats() {
        log.info("[BookViewCountScheduler] 도서 하루 조회갯수 집계시작");

        // 오늘 날짜 (통계는 날짜 단위로만 기록됨)
        LocalDate today = LocalDate.now();

        // 오늘 00:00부터 현재 시간까지 범위 설정
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfNow = LocalDateTime.now();

        //  book_view_log에서 오늘 하루 동안 각 책별 조회수 count 집계 (book_id 기준으로 group by)
        List<Object[]> result = bookViewLogRepository.countViewsByBookIdBetween(startOfDay, endOfNow);

        for (Object[] row : result) {
            Long bookId = (Long) row[0];        // 책 ID
            Long viewCount = (Long) row[1];     // 조회수 (Long이지만 int로 변환 가능)

            // 엔티티에는 BookEntity 자체가 필요하므로 ID만 가진 book 객체 생성
            BookEntity book = BookEntity.builder().id(bookId).build();

            // 기존에 오늘 날짜로 이 책에 대한 통계가 이미 있는지 확인
            BookViewCountLogEntity existing = bookViewCountLogRepository.findByBookIdAndDate(bookId, today);

            if (existing != null) {
                // 이미 있으면 → 조회수 업데이트
                existing.setViewCount(viewCount.intValue());
                bookViewCountLogRepository.save(existing);
            } else {
                // 없으면 → 새로 insert
                BookViewCountLogEntity entity = BookViewCountLogEntity.builder()
                        .book(book)
                        .date(today)
                        .viewCount(viewCount.intValue())
                        .build();
                bookViewCountLogRepository.save(entity);
            }
        }

        //  오래된 데이터 정리 (100일 이전 기록 삭제)
        LocalDate cutoff = today.minusDays(100);
        bookViewCountLogRepository.deleteByDateBefore(cutoff);

        log.info("[BookViewCountScheduler] 인기 도서 집계 완료");
    }
}
