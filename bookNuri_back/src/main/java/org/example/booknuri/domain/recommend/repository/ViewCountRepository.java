package org.example.booknuri.domain.recommend.repository;

import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 *  ViewCountRepository
 *
 * ✅ 인기 도서 추천용 "책 ID만 빠르게 조회"하는 리포지토리
 * ✅ 실제 도서 상세 정보는 Elasticsearch 등에서 조회하거나 다른 Repository에서 담당
 * ✅ 주로 성능 최적화를 위해 ID만 먼저 필터링할 때 사용됨
 */
public interface ViewCountRepository extends Repository<BookViewCountLogEntity, Long> {

    /**
     * ✅ 전체 인기 도서 ID 조회 (기간 필터만 적용)
     * - 도서관 필터는 적용되지 않음 (ES에서 후처리)
     * - 조회수 많은 순 정렬
     */
    @Query("""
        SELECT bvc.book.id
        FROM BookViewCountLogEntity bvc
        WHERE bvc.date >= :startDate
        GROUP BY bvc.book.id
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<Long> findTopBookIds(
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );

    /**
     * ✅ 메인 카테고리 기반 인기 도서 ID 조회
     * - 특정 mainCategoryId에 속한 책 중에서 인기순
     */
    @Query("""
        SELECT bvc.book.id
        FROM BookViewCountLogEntity bvc
        WHERE bvc.date >= :startDate
          AND bvc.book.mainCategory.id = :mainCategoryId
        GROUP BY bvc.book.id
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<Long> findTopBookIdsByMainCategory(
            @Param("mainCategoryId") Long mainCategoryId,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );

    /**
     * ✅ 미들 카테고리 기반 인기 도서 ID 조회
     * - 특정 middleCategoryId에 속한 책 중에서 인기순
     */
    @Query("""
        SELECT bvc.book.id
        FROM BookViewCountLogEntity bvc
        WHERE bvc.date >= :startDate
          AND bvc.book.middleCategory.id = :middleCategoryId
        GROUP BY bvc.book.id
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<Long> findTopBookIdsByMiddleCategory(
            @Param("middleCategoryId") Long middleCategoryId,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );

    /**
     * ✅ 서브 카테고리 기반 인기 도서 ID 조회
     * - 특정 subCategoryId에 속한 책 중에서 인기순
     */
    @Query("""
        SELECT bvc.book.id
        FROM BookViewCountLogEntity bvc
        WHERE bvc.date >= :startDate
          AND bvc.book.subCategory.id = :subCategoryId
        GROUP BY bvc.book.id
        ORDER BY SUM(bvc.viewCount) DESC
    """)
    List<Long> findTopBookIdsBySubCategory(
            @Param("subCategoryId") Long subCategoryId,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );
}
