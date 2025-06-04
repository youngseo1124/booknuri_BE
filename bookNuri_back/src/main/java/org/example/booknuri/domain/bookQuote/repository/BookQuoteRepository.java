package org.example.booknuri.domain.bookQuote.repository;

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

}
