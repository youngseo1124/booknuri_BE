package org.example.booknuri.domain.book.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    Optional<BookEntity> findByIsbn13(String isbn13);


    // 존재 여부만 확인하는 메서드
    boolean existsByIsbn13(String isbn13);

    // BookRepository.java
    @Query("SELECT b.id FROM BookEntity b WHERE b.subCategory.id = :subCategoryId")
    List<Long> findBookIdsBySubCategoryId(@Param("subCategoryId") Long subCategoryId);

    @Query("SELECT b.id FROM BookEntity b WHERE b.middleCategory.id = :middleCategoryId")
    List<Long> findBookIdsByMiddleCategoryId(@Param("middleCategoryId") Long middleCategoryId);

    @Query("SELECT b.id FROM BookEntity b WHERE b.mainCategory.id = :mainCategoryId")
    List<Long> findBookIdsByMainCategoryId(@Param("mainCategoryId") Long mainCategoryId);










}
