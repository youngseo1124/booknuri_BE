package org.example.booknuri.domain.recommend.repository;

import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ViewCountRepository extends Repository<BookViewCountLogEntity, Long> {


    //(아직 도서관 필터링안함) 조회수 인기책 날짜 구간 필터링해서 가져오기
    @Query("""
        SELECT bvc.book.id
        FROM BookViewCountLogEntity bvc
        WHERE bvc.date >= :startDate
        GROUP BY bvc.book.id
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<Long> findTopBookIds(
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );
}
