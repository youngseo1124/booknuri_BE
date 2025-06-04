package org.example.booknuri.domain.bookQuote.repository;

import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteLikeEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface BookQuoteLikeRepository extends JpaRepository<BookQuoteLikeEntity, Long> {

    //  로그인한 유저가 해당 인용에 좋아요 눌렀는지 확인
    boolean existsByUserAndQuote(UserEntity user, BookQuoteEntity quote);

    //  좋아요 엔티티 조회 (좋아요 토글 처리용)
    Optional<BookQuoteLikeEntity> findByUserAndQuote(UserEntity user, BookQuoteEntity quote);

    //  인용 좋아요 수 카운트
    Long countByQuote(BookQuoteEntity quote);
}
