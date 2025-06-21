package org.example.booknuri.domain.library.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBookEntity, Long> {

    //특정 책이 해당 도서관에 있는지 여부 확인
    boolean existsByLibCodeAndBook(String libCode, BookEntity book);

    // 🔍 여러 bookId에 해당하는 LibraryBook 리스트 가져오기
    List<LibraryBookEntity> findByBook_IdIn(Set<Long> bookIds);

    List<LibraryBookEntity> findByLibCodeIn(List<String> libCodeList);

    long countByLibCodeIn(List<String> libCodeList);

    Page<LibraryBookEntity> findByLibCodeIn(List<String> libCodeList, Pageable pageable);

    @Query("SELECT lb FROM LibraryBookEntity lb JOIN FETCH lb.book WHERE lb.libCode IN :libCodeList")
    Page<LibraryBookEntity> findByLibCodeInFetchBook(@Param("libCodeList") List<String> libCodeList, Pageable pageable);

    @Query("SELECT lb FROM LibraryBookEntity lb JOIN FETCH lb.book WHERE lb.book.id IN :bookIds")
    List<LibraryBookEntity> findByBookIdInFetchBook(@Param("bookIds") Set<Long> bookIds);


}
