package org.example.booknuri.domain.bookReflection_.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReflectionResponseDto {
    private Long id;
    private String title;
    private String content;
    private int rating;
    private String reviewerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    //스포일러 여부
    private boolean containsSpoiler;
    // 내가 쓴 독후감인지 여부
    private boolean isWrittenByCurrentUser;
    private boolean visibleToPublic; //공개여부

    //이미지 url
    private List<ReflectionImageDto> imageList;

    //ISBN
    private String isbn13;

}
