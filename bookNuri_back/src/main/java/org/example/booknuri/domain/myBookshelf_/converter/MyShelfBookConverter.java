package org.example.booknuri.domain.myBookshelf_.converter;


import org.example.booknuri.domain.myBookshelf_.dto.MyShelfBookResponseDto;
import org.example.booknuri.domain.myBookshelf_.entity.MyShelfBookEntity;
import org.springframework.stereotype.Component;

@Component
public class MyShelfBookConverter {

    public MyShelfBookResponseDto toDto(MyShelfBookEntity entity) {
        return MyShelfBookResponseDto.builder()
                .isbn13(entity.getBook().getIsbn13())
                .bookname(entity.getBook().getBookname())
                .authors(entity.getBook().getAuthors())
                .bookImageURL(entity.getBook().getBookImageURL())
                .lifeBook(entity.isLifeBook())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .finishedAt(entity.getFinishedAt())
                .build();
    }
}
