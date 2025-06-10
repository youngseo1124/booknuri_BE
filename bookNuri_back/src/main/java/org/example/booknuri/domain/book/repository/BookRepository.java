package org.example.booknuri.domain.book.repository;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    Optional<BookEntity> findByIsbn13(String isbn13);


    // 존재 여부만 확인하는 메서드
    boolean existsByIsbn13(String isbn13);






}
