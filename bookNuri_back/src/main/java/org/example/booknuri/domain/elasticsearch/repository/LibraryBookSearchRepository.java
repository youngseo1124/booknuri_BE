package org.example.booknuri.domain.elasticsearch.repository;

import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LibraryBookSearchRepository extends ElasticsearchRepository<LibraryBookSearchDocument, String> {
    List<LibraryBookSearchDocument> findByBookIdInAndLibCode(List<Long> bookIds, String libCode);


    List<LibraryBookSearchDocument> findByLibCodeAndMainCategoryId(String libCode, Long mainCategoryId);

    List<LibraryBookSearchDocument> findByLibCodeAndMiddleCategoryId(String libCode, Long categoryId);

    List<LibraryBookSearchDocument> findByLibCodeAndSubCategoryId(String libCode, Long categoryId);
}
