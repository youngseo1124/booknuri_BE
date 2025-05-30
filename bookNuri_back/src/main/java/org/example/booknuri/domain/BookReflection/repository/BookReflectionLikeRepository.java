package org.example.booknuri.domain.BookReflection.repository;

import org.example.booknuri.domain.BookReflection.entity.BookReflectionEntity;
import org.example.booknuri.domain.BookReflection.entity.BookReflectionLikeEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookReflectionLikeRepository extends JpaRepository<BookReflectionLikeEntity, Long> {

    // 로그인 유저가 해당 독후감에 좋아요 눌렀는지 확인
    boolean existsByUserAndReflection(UserEntity user, BookReflectionEntity reflection);

    // 유저가 해당 독후감에 좋아요 눌렀는지 조회 (토글용)
    // 좋아요 삭제하거나 정보가 필요할 때
    Optional<BookReflectionLikeEntity> findByUserAndReflection(UserEntity user, BookReflectionEntity reflection);

    // 독후감 좋아요 수 카운트
    Long countByReflection(BookReflectionEntity reflection);
}
