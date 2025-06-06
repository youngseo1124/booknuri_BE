package org.example.booknuri.domain.recommend.repository;


import io.lettuce.core.dynamic.annotation.Param;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.entity.LibraryBookEntity;
import org.example.booknuri.domain.recommend.dto.RecommendBookDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface RecommendRepository extends Repository<LibraryBookEntity, Long> {


    //내 도서관에 있는 기간별 베스트 셀러 구하기
    @Query("""
        SELECT new org.example.booknuri.domain.recommend.dto.RecommendBookDto(
            b.id, b.bookname, b.authors, b.bookImageURL,
            b.isbn13, b.description, SUM(bvc.viewCount)
        )
        FROM BookViewCountLogEntity bvc
        JOIN bvc.book b
        WHERE bvc.date >= :startDate
          AND b.id IN (
              SELECT lb.book.id
              FROM LibraryBookEntity lb
              WHERE lb.libCode = :libCode
          )
        GROUP BY b.id, b.bookname, b.authors, b.bookImageURL, b.isbn13, b.description
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<RecommendBookDto> findPopularBooksWithViewCount(
            @Param("libCode") String libCode,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );


    //
    //카테고리별로 인기도서 가져오기
    @Query("""
    SELECT new org.example.booknuri.domain.recommend.dto.RecommendBookDto(
        b.id, b.bookname, b.authors, b.bookImageURL,
        b.isbn13, b.description, SUM(bvc.viewCount)
    )
    FROM BookViewCountLogEntity bvc
    JOIN bvc.book b
    WHERE bvc.date >= :startDate
      AND b.mainCategory.id = :mainCategoryId
      AND b.id IN (
          SELECT lb.book.id
          FROM LibraryBookEntity lb
          WHERE lb.libCode = :libCode
      )
    GROUP BY b.id, b.bookname, b.authors, b.bookImageURL, b.isbn13, b.description
    ORDER BY SUM(bvc.viewCount) DESC
""")
    List<RecommendBookDto> findPopularBooksByCategory(
            @Param("libCode") String libCode,
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );

}
