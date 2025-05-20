package org.example.booknuri.domain.book.repository;

import org.example.booknuri.domain.book.entity.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
    Optional<MainCategory> findByName(String name);
}
