package org.example.booknuri.domain.Log.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookIndexService;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookViewLogSyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final LibraryBookRepository libraryBookRepository;
    private final LibraryBookIndexService libraryBookIndexService;

    //  3분마다 실행 (180초)
    @Scheduled(fixedRate = 18000)
    public void syncBookViewLogsFromRedisToElasticsearch() {
        String redisKey = "book_view_queue";

        List<String> logs = redisTemplate.opsForList().range(redisKey, 0, -1);
        redisTemplate.delete(redisKey); //  로그 다 꺼냈으면 비워줌

        if (logs == null || logs.isEmpty()) {
            log.info(" [ES 동기화] Redis 로그 없음, 건너뜀");
            return;
        }

        Set<String> uniqueUserBookPairs = new HashSet<>();

        for (String logEntry : logs) {
            try {
                String[] parts = logEntry.split("\\|");
                String userPart = Arrays.stream(parts).filter(p -> p.startsWith("user:")).findFirst().orElse(null);
                String bookPart = Arrays.stream(parts).filter(p -> p.startsWith("book:")).findFirst().orElse(null);

                if (userPart != null && bookPart != null) {
                    String key = userPart + "|" + bookPart;
                    uniqueUserBookPairs.add(key);
                }
            } catch (Exception e) {
                log.warn("❌ Redis 로그 파싱 실패: {}", logEntry, e);
            }
        }

        //  관련된 bookId만 추출 (중복제거)
        Set<Long> bookIds = uniqueUserBookPairs.stream()
                .map(entry -> entry.split("\\|")[1]) // "book:12345"
                .map(bookStr -> bookStr.replace("book:", ""))
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        log.info(" [ES 색인 대상] 중복 제거된 책 {}권", bookIds.size());

        if (bookIds.isEmpty()) return;

        List<LibraryBookEntity> targetLibraryBooks = libraryBookRepository.findByBookIdInFetchBook(bookIds);


        int successCount = 0;

        for (LibraryBookEntity lb : targetLibraryBooks) {
            try {
                libraryBookIndexService.indexSingleLibraryBook(lb);
                successCount++;
            } catch (Exception e) {
                log.error(" ES 색인 실패 - bookId: {}, error: {}", lb.getBook().getId(), e.getMessage());
            }
        }

        log.info("[완료] ES 동기화: {} / {}", successCount, targetLibraryBooks.size());
    }
}
