package org.example.booknuri.domain.myBookshelf_.repository;

import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyShelfBookRepository extends JpaRepository<MyShelfBookEntity, Long> {

    //  해당 유저가 특정 책을 책장에 담았는지 여부 확인
    boolean existsByUserAndBook(UserEntity user, BookEntity book);


    //응답 페이지네이션
    Page<MyShelfBookEntity> findByUser(UserEntity user, Pageable pageable);

    List<MyShelfBookEntity> findByUser(UserEntity user);




    Optional<MyShelfBookEntity> findByUserAndBook(UserEntity user, BookEntity book);

    Page<MyShelfBookEntity> findByUserAndStatus(UserEntity user, MyShelfBookEntity.BookStatus status, Pageable pageable);

}
