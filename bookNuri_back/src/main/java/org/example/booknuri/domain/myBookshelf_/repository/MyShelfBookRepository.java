package org.example.booknuri.domain.myBookshelf_.repository;

import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyShelfBookRepository extends JpaRepository<MyShelfBookEntity, Long> {

    //  해당 유저가 특정 책을 책장에 담았는지 여부 확인
    boolean existsByUserAndBook(UserEntity user, BookEntity book);


    Optional<MyShelfBookEntity> findByUserAndBook(UserEntity user, BookEntity book);
}
