package org.example.booknuri.domain.bookReflection_.repository;

import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReflectionImageRepository extends JpaRepository<BookReflectionImageEntity, Long> {

    // 특정 독후감에 연결된 이미지 전부 조회
    List<BookReflectionImageEntity> findByReflection(BookReflectionEntity reflection);
}
