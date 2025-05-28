package org.example.booknuri.domain.Log.repository;

import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.example.booknuri.domain.book.entity.BookEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookViewCountLogRepository extends JpaRepository<BookViewCountLogEntity, Long> {

    // 특정 날짜의 인기 도서 조회
    List<BookViewCountLogEntity> findByDate(LocalDate date);

    // 특정 기간 동안의 인기 도서 조회 (7일, 30일 통계용)
    List<BookViewCountLogEntity> findByDateBetween(LocalDate start, LocalDate end);

    // 오래된 통계 삭제용
    void deleteByDateBefore(LocalDate cutoffDate);

    boolean existsByBookAndDate(BookEntity book, LocalDate date); // 중복 방지용
}
