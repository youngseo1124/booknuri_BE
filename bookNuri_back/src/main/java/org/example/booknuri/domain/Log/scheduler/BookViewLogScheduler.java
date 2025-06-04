package org.example.booknuri.domain.Log.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.Log.entity.BookViewLogEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.Log.repository.BookViewLogRepository;
import org.example.booknuri.domain.user.entity.UserEntity;

import org.example.booknuri.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookViewLogScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final BookViewLogRepository bookViewLogRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    //10분마다 실행되는 스케줄러
    //Redis에 쌓인 조회 로그를 MySQL로 저장
    //개발중이라 1분마다로 변경
    @Scheduled(fixedDelay = 10 * 60 * 100)
    @Transactional
    public void syncBookViewLogs() {
        log.info("📚 [BookViewLogScheduler] Redis → DB 저장 시작");

        // 1. Redis에서 로그 추출
        List<String> logs = redisTemplate.opsForList().range("book_view_queue", 0, -1);
        if (logs == null || logs.isEmpty()) {
            return;
        }

        // 2. Redis 비우기 (이후 DB에 insert될 예정이므로 삭제)
        redisTemplate.delete("book_view_queue");

        // 3. 하나씩 파싱해서 DB에 저장
        for (String logLine : logs) {
            try {
                // 예: "user:3|book:77|gender:F|birthYear:1999|2025-05-27T13:30"
                String[] parts = logLine.split("\\|");
                String userId = parts[0].split(":")[1];
                Long bookId = Long.parseLong(parts[1].split(":")[1]);
                String gender = parts[2].split(":")[1];
                Integer birthYear = Integer.parseInt(parts[3].split(":")[1]);
                LocalDateTime viewedAt = LocalDateTime.parse(parts[4]);

                UserEntity user = userRepository.findById(userId).orElse(null);
                BookEntity book = bookRepository.findById(bookId).orElse(null);
                if (user == null || book == null) continue;

                BookViewLogEntity existing = bookViewLogRepository.findByUserAndBook(user, book).orElse(null);

                if (existing != null) {
                    existing.setViewedAt(viewedAt); //  시간만 갱신
                    bookViewLogRepository.save(existing);
                } else {
                    BookViewLogEntity entity = BookViewLogEntity.builder()
                            .user(user)
                            .book(book)
                            .gender(gender)
                            .birthYear(birthYear)
                            .viewedAt(viewedAt)
                            .build();
                    bookViewLogRepository.save(entity);
                }

            } catch (Exception e) {
                log.warn("🚨 로그 파싱 실패: " + logLine, e);
            }
        }

        // 5. 오래된 로그 / 초과 로그 삭제
        deleteOldLogs();

        log.info("✅ [BookViewLogScheduler] DB 저장 완료!");
    }

    // 오래된 로그 및 초과 로그 정리 메서드(6개월 초과시 삭제)
    private void deleteOldLogs() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        bookViewLogRepository.deleteOlderThan(sixMonthsAgo);

        List<String> userIds = bookViewLogRepository.findAllUserIds();
        for (String userId : userIds) {
            bookViewLogRepository.deleteExceedingLimit(userId);
        }
    }
}
