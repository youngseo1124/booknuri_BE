package org.example.booknuri.domain.Log.repository;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.Log.entity.BookViewLogEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookViewLogRepository extends JpaRepository<BookViewLogEntity, Long> {


    Optional<BookViewLogEntity> findByUserAndBook(UserEntity user, BookEntity book);



    //  1개월 이상 지난 오래된 로그 삭제용
    @Modifying
    @Query("DELETE FROM BookViewLogEntity b WHERE b.viewedAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);

    // 유저별 1000개 초과 로그 삭제용 (MySQL 네이티브 쿼리 사용)
    @Modifying
    @Query(value = """
        DELETE FROM book_view_log
        WHERE id NOT IN (
            SELECT id FROM (
                SELECT id FROM book_view_log
                WHERE user_id = :userId
                ORDER BY viewed_at DESC
                LIMIT 1000
            ) tmp
        ) AND user_id = :userId
    """, nativeQuery = true)
    void deleteExceedingLimit(@Param("userId") String userId);

    // 중복 제거 + 삭제 대상 유저 목록 조회용
    @Query("SELECT DISTINCT b.user.username FROM BookViewLogEntity b")
    List<String> findAllUserIds();



    // 책아이디 기준 그룹화해서 오늘날짜 특정책 몇번 조회됏는지 카운트
    @Query("""
    SELECT b.book.id, COUNT(b)
    FROM BookViewLogEntity b
    WHERE b.viewedAt BETWEEN :start AND :end
    GROUP BY b.book.id
""")
    List<Object[]> countViewsByBookIdBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    //연령,성별 별 인기도서
    @Query("""
    SELECT bvl.book.id
    FROM BookViewLogEntity bvl
    WHERE bvl.gender = :gender
      AND bvl.birthYear BETWEEN :minYear AND :maxYear
    ORDER BY bvl.viewedAt DESC
""")
    List<Long> findTopBookIdsByGenderAndBirthYear(
            @Param("gender") String gender,
            @Param("minYear") int minYear,
            @Param("maxYear") int maxYear,
            Pageable pageable
    );


    //--------------연관 책 추천---------------
// ✅ 최근 A책 본 유저의 username 조회
    @Query("""
    SELECT b.user.username
    FROM BookViewLogEntity b
    WHERE b.book.id = :bookId
    ORDER BY b.viewedAt DESC
""")
    List<String> findTopUsernamesByBook(@Param("bookId") Long bookId, Pageable pageable);


    // ✅ username으로 viewed 로그 정렬 조회
    @Query("""
    SELECT b
    FROM BookViewLogEntity b
    WHERE b.user.username = :username
    ORDER BY b.viewedAt
""")
    List<BookViewLogEntity> findAllByUsernameOrderByViewedAt(@Param("username") String username);


}
