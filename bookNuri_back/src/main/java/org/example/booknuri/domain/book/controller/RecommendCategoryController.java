package org.example.booknuri.domain.book.controller;

import lombok.RequiredArgsConstructor;

import org.example.booknuri.domain.book.dto.MainCategoryDto;
import org.example.booknuri.domain.book.service.RecommendCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendCategoryController {

    private final RecommendCategoryService recommendCategoryService;

    @GetMapping("/categories")
    public List<MainCategoryDto> getMainCategories() {
        return recommendCategoryService.getAllMainCategories();
    }
}
