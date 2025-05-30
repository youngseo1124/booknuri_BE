package org.example.booknuri.domain.book.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//책 상세페이지 통합 dto!
//정적데이터인 책상세정보(캐싱) + db에서 가져오는 리뷰, 인용, 독후감 리스트들 통합으로 담는 dto임 ㅎㅎ


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookTotalInfoDto {

    private BookInfoResponseDto bookInfo;                 // 기본 책 정보 (Redis에서)

    // 내 책장에 담겼는지 여부
    private boolean addedToBookshelf;
}
