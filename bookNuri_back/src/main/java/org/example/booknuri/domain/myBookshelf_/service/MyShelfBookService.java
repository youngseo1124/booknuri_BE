package org.example.booknuri.domain.myBookshelf_.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.service.BookService;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.example.booknuri.domain.myBookshelf_.repository.MyShelfBookRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyShelfBookService {

    private final MyShelfBookRepository myShelfBookRepository;
    private final UserService userService;
    private final BookService bookService;

    public void addToShelf(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        if (myShelfBookRepository.existsByUserAndBook(user, book)) {
            throw new IllegalArgumentException("이미 책장에 추가된 책입니다.");
        }

        MyShelfBookEntity entity = MyShelfBookEntity.builder()
                .user(user)
                .book(book)
                .status(MyShelfBookEntity.BookStatus.WANT_TO_READ)
                .lifeBook(false)
                .build();

        myShelfBookRepository.save(entity);
    }

    public void updateStatus(String username, String isbn13, MyShelfBookEntity.BookStatus status) {
        MyShelfBookEntity entity = getByUserAndBook(username, isbn13);
        entity.updateStatus(status);
    }

    public void toggleLifeBook(String username, String isbn13) {
        MyShelfBookEntity entity = getByUserAndBook(username, isbn13);
        entity.updateLifeBook(!entity.isLifeBook());
    }

    private MyShelfBookEntity getByUserAndBook(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        return myShelfBookRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("책장이 존재하지 않습니다."));
    }


    //책장에서 책 삭제
    public void removeBook(String username, String isbn13) {
        UserEntity user = userService.getUserByUsername(username);
        BookEntity book = bookService.getBookEntityByIsbn(isbn13);

        MyShelfBookEntity entity = myShelfBookRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("책장이에 존재하지 않는 책입니다."));

        myShelfBookRepository.delete(entity);
    }

}
