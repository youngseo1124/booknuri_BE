package org.example.booknuri.domain.library.repository;

import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBookEntity, Long> {

    //특정 책이 해당 도서관에 있는지 여부 확인
    boolean existsByLibCodeAndBook(String libCode, BookEntity book);
}