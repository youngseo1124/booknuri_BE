package org.example.booknuri.domain.book.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.example.booknuri.domain.book.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findByName(String name);

    Optional<SubCategory> findByNameAndMiddleCategoryName(String name, String middleCategoryName);

    Optional<SubCategory> findByNameAndMiddleCategoryId(String name, Long middleCategoryId);
}
