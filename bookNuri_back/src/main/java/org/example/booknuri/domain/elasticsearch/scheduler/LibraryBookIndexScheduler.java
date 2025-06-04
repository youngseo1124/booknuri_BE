package org.example.booknuri.domain.elasticsearch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.elasticsearch.service.LibraryBookIndexService;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryBookIndexScheduler {

    private final LibraryBookIndexService libraryBookIndexService;
    private final LibraryBookRepository libraryBookRepository;

    /**
     *  매일 밤 11시에 실행 (UTC 기준으로 맞춰서 필요시 +시간대 조절)
     */
    @Scheduled(cron = "0 0 23 * * *")
    @Transactional(readOnly = true)
    public void updateChangedBooksToElasticsearch() {
        log.info("📢 [스케줄러 실행] 어제 변경된 리뷰/조회수 기반 es 색인 갱신 시작");

        // 1⃣ 어제 리뷰 or 조회수 변경된 책 ID 조회
        Set<Long> changedBookIds = libraryBookIndexService.getBooksWithChangedStatsYesterday();

        if (changedBookIds.isEmpty()) {
            log.info("✅ 변경된 도서 없음 - 색인 갱신 스킵");
            return;
        }

        log.info("✅ 색인 갱신 대상 도서 수: {}", changedBookIds.size());

        // 2️⃣ 해당 BookId들에 연결된 LibraryBookEntity 가져오기
        List<LibraryBookEntity> libraryBooks = libraryBookRepository.findByBook_IdIn(changedBookIds);

        int successCount = 0;

        for (LibraryBookEntity lb : libraryBooks) {
            try {
                libraryBookIndexService.indexSingleLibraryBook(lb);
                successCount++;
            } catch (Exception e) {
                log.error("❌ 색인 실패 - bookId: {}, error: {}", lb.getBook().getId(), e.getMessage(), e);
            }
        }

        log.info("🎉 색인 갱신 완료! 성공: {}, 전체 대상: {}", successCount, libraryBooks.size());
    }
}
