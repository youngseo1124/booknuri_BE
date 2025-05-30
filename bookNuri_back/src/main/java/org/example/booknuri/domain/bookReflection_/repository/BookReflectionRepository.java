package org.example.booknuri.domain.bookReflection_.repository;

import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReflectionRepository extends JpaRepository<BookReflectionEntity, Long> {

    // 특정 ISBN의 책 독후감 리스트 (활성화된 것만)
    List<BookReflectionEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13);

    // 이미 독후감 작성했는지 확인용
    boolean existsByUserAndBook(UserEntity user, BookEntity book);

    // 유저가 쓴 독후감 찾기(+페이지네이션)
    Page<BookReflectionEntity> findByUser(UserEntity user, Pageable pageable);

    // 특정 책에 대해 유저가 쓴 독후감 찾기 (아이디로)
    Optional<BookReflectionEntity> findByIdAndUser(Long id, UserEntity user);

    // 특정 책에 대해 유저가 쓴 독후감 찾기 (BookEntity로)
    Optional<BookReflectionEntity> findByUserAndBook(UserEntity user, BookEntity book);

    // 특정 책에 대해 활성화된 독후감들 (페이지네이션 O)
    Page<BookReflectionEntity> findByBook_Isbn13AndIsActiveTrue(String isbn13, Pageable pageable);

    // 평균 별점 가져오기 (활성화된 독후감만)
    @Query("SELECT AVG(r.rating) FROM BookReflectionEntity r WHERE r.book.isbn13 = :isbn13 AND r.isActive = true")
    Double getAverageReflectionRatingByIsbn13(@Param("isbn13") String isbn13);

    // 독후감 개수 조회 (isActive = true인 것만)
    int countByBook_Isbn13AndIsActiveTrue(String isbn13);
}
