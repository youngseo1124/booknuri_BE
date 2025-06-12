package org.example.booknuri.domain.book.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.example.booknuri.domain.book.entity.MiddleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MiddleCategoryRepository extends JpaRepository<MiddleCategory, Long> {
    Optional<MiddleCategory> findByName(String name);


    Optional<MiddleCategory> findByNameAndMainCategoryName(String name, String mainCategoryName);
}
