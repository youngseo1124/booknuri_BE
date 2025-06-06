package org.example.booknuri.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.MainCategoryDto;
import org.example.booknuri.domain.book.entity.MainCategory;

import org.example.booknuri.domain.book.repository.MainCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendCategoryService {

    private final MainCategoryRepository mainCategoryRepository;

    public List<MainCategoryDto> getAllMainCategories() {
        return mainCategoryRepository.findAllValidMainCategories().stream()
                .map(cat -> new MainCategoryDto(cat.getId(), cat.getName()))
                .toList();
    }
}
