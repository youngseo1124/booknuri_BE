package org.example.booknuri.domain.Log.repository;

import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.example.booknuri.domain.book.entity.BookEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookViewCountLogRepository extends JpaRepository<BookViewCountLogEntity, Long> {

    // 특정 날짜에 저장된 전체 도서들의 조회수 통계 리스트
    List<BookViewCountLogEntity> findByDate(LocalDate date);

    // 특정 기간 동안의 도서들의 조회수 통계 리스트(7일, 30일 통계용)
    List<BookViewCountLogEntity> findByDateBetween(LocalDate start, LocalDate end);

    // 오래된 통계 삭제용
    void deleteByDateBefore(LocalDate cutoffDate);

    boolean existsByBookAndDate(BookEntity book, LocalDate date); // 중복 방지용

    //오늘날짜 기준 조회용
    @Query("""
    SELECT b FROM BookViewCountLogEntity b
    WHERE b.book.id = :bookId AND b.date = :date
""")
    BookViewCountLogEntity findByBookIdAndDate(@Param("bookId") Long bookId, @Param("date") LocalDate date);


    // “책 1권의 N개월 조회수 총합". 데이터없으면 0 반환
    @Query("""
    SELECT COALESCE(SUM(b.viewCount), 0)
    FROM BookViewCountLogEntity b
    WHERE b.book = :book AND b.date BETWEEN :startDate AND :endDate
""")
    int getTotalViewCountByBookAndDateRange(
            @Param("book") BookEntity book,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



}
