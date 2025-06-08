package org.example.booknuri.domain.bookQuote.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookQuoteRepository extends JpaRepository<BookQuoteEntity, Long> {

    //  특정 책에 대한 공개 인용만 리스트로 (배너에 보여줄 용도)
    Page<BookQuoteEntity> findByBook_Isbn13AndVisibleToPublicTrue(String isbn13, Pageable pageable);

    int countByBook_Isbn13AndIsActiveTrue(String isbn13);


    //  해당 유저가 이미 이 책에 대해 인용 했는지 여부
    boolean existsByUserAndBook(UserEntity user, BookEntity book);

    //  특정 유저가 쓴 인용 리스트 (마이페이지에서 확인용)
    Page<BookQuoteEntity> findByUser(UserEntity user, Pageable pageable);

    // 특정 책에 대해 유저가 쓴 인용 1개 찾기 (수정/삭제용)
    Optional<BookQuoteEntity> findByUserAndBook(UserEntity user, BookEntity book);

    Optional<BookQuoteEntity> findByIdAndUser(Long quoteId, UserEntity user);

    List<BookQuoteEntity> findAllByUserAndBook(UserEntity user, BookEntity book);




// 전체 인기순 + 최신순 반영된 인용 리스트

    /*
     - like_count * 1.0 → 실수 연산으로 점수화
     - DATEDIFF(NOW(), created_at) → 인용 작성 후 경과 일수
     - 최신 인용일수록 DATEDIFF 값이 작음 → * -0.1 해서 가중치 증가 효과

      */

    @Query(value = """
        SELECT * FROM book_quotes
        WHERE visible_to_public = true
        ORDER BY (like_count * 1.0 + DATEDIFF(NOW(), created_at) * -0.1) DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<BookQuoteEntity> findPopularQuotesWithRecency(@Param("offset") int offset,
                                                       @Param("limit") int limit);


    // 전체 공개 & 활성 인용 개수 (카운트용)
    int countByVisibleToPublicTrueAndIsActiveTrue();

}
