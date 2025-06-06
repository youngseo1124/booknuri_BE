package org.example.booknuri.domain.recommend.controller;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.recommend.dto.RecommendBookDto;
import org.example.booknuri.domain.recommend.service.RecommendService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final UserService userService;

    @GetMapping("/bestseller")
    public List<RecommendBookDto> getBestSeller(
            @AuthenticationPrincipal CustomUser jwtuser,
            @RequestParam("period") String period, // "weekly" | "monthly" | "yearly"
            @RequestParam(value = "mainCategoryId", required = false) Long mainCategoryId // ✅ 선택 파라미터
    ) {
        UserEntity user = userService.getUserByUsername(jwtuser.getUsername());
        return recommendService.getBestSeller(user, period, mainCategoryId);
    }

    //개인 맞춤 추천
    @GetMapping("/personal")
    public List<RecommendBookDto> getPersonalRecommend(@AuthenticationPrincipal CustomUser jwtuser) {
        UserEntity user = userService.getUserByUsername(jwtuser.getUsername());
        return recommendService.getPersonalizedRecommendation(user);
    }

    //연령,성별별 베스트셀러(Ex20대 여성 베스트셀러)
    // 연령/성별 베스트셀러 API
    @GetMapping("/bestseller/demographic")
    public List<RecommendBookDto> getDemographicRecommend(
            @AuthenticationPrincipal CustomUser jwtuser,
            @RequestParam("gender") String gender,
            @RequestParam("birthYearGroup") int birthYearGroup
    ) {
        UserEntity user = userService.getUserByUsername(jwtuser.getUsername());
        return recommendService.getDemographicRecommend(user, gender, birthYearGroup);
    }


}
