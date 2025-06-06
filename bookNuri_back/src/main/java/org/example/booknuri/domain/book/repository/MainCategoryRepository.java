package org.example.booknuri.domain.book.repository;

import org.example.booknuri.domain.book.entity.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
    Optional<MainCategory> findByName(String name);


    @Query("SELECT m FROM MainCategory m WHERE m.name IS NOT NULL AND m.name <> ''")
    List<MainCategory> findAllValidMainCategories();
}
