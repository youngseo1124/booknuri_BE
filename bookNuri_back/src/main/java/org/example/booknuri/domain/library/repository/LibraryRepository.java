package org.example.booknuri.domain.library.repository;

import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntity, String> {

    List<LibraryEntity> findByRegion_Si(String si);

    List<LibraryEntity> findByRegion_SiAndRegion_Gu(String si, String gu);


    //도서관 코드로 도서관 찾기
    LibraryEntity findByLibCode(String libCode);


    // 페이지네이션은 Spring Data JPA가 자동으로 LIMIT / OFFSET 쿼리를 만들어줌
    // 전체 도서관
    Page<LibraryEntity> findAll(Pageable pageable);

    // 지역만 (예: 대구광역시)
    Page<LibraryEntity> findByRegion_Si(String si, Pageable pageable);

    // 지역 + 시군구 (예: 대구광역시 + 달서구)
    Page<LibraryEntity> findByRegion_SiAndRegion_Gu(String si, String gu, Pageable pageable);


}
