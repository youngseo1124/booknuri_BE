package org.example.booknuri.domain.Log.repository;



import org.example.booknuri.domain.Log.entity.UserBookViewLogEntity;
import org.example.booknuri.domain.book.entity.BookEntity;

import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBookViewLogRepository extends JpaRepository<UserBookViewLogEntity, Long> {

    Optional<UserBookViewLogEntity> findByUserAndBook(UserEntity user, BookEntity book);

    //20개 넘는거 삭제
    @Modifying
    @Query(value = """
        DELETE FROM user_book_view_log
        WHERE id NOT IN (
            SELECT id FROM (
                SELECT id FROM user_book_view_log
                WHERE user_id = :userId
                ORDER BY viewed_at DESC
                LIMIT 20
            ) tmp
        ) AND user_id = :userId
    """, nativeQuery = true)
    void deleteExceedingLimit(@Param("userId") String userId);


    //최근 목록 20개 불러오기
    List<UserBookViewLogEntity> findTop20ByUserOrderByViewedAtDesc(UserEntity user);
}
