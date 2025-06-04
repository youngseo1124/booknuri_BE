package org.example.booknuri.domain.elasticsearch.repository;

import org.example.booknuri.domain.elasticsearch.document.LibraryBookSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LibraryBookSearchRepository extends ElasticsearchRepository<LibraryBookSearchDocument, String> {
}
