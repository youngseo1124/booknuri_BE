package org.example.booknuri.domain.myBookshelf.repository;

import org.example.booknuri.domain.myBookshelf.entity.MyBookshelfEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBookshelfRepository extends JpaRepository<MyBookshelfEntity, Long> {

    //  해당 유저가 특정 책을 책장에 담았는지 여부 확인
    boolean existsByUserAndBook(UserEntity user, BookEntity book);
}
