package org.example.booknuri.domain.bookReflection_.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.bookReflection_.dto.*;
import org.example.booknuri.domain.bookReflection_.service.BookReflectionService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book/reflection")
public class BookReflectionController {

    private final BookReflectionService bookReflectionService;
    private final UserService userService;

    // 독후감 썼는지 확인
    @GetMapping("/exist/{isbn13}")
    public ResponseEntity<?> checkIfAlreadyReflected(
            @PathVariable String isbn13,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        boolean alreadyReflected = bookReflectionService.checkAlreadyReflected(isbn13, user);
        return ResponseEntity.ok(Map.of("alreadyReflected", alreadyReflected));
    }

    // 독후감 작성
    @PostMapping
    public ResponseEntity<?> createBookReflection(@RequestBody BookReflectionCreateRequestDto dto,
                                                  @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        try {
            bookReflectionService.createReflection(dto, user);
            return ResponseEntity.ok(Map.of("message", "독후감 작성 완료!"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 독후감 수정화면용 - 기존 내 독후감 불러오기
    @GetMapping("/my/{isbn13}")
    public ResponseEntity<?> getMyReflectionForBook(@PathVariable String isbn13,
                                                    @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        BookReflectionResponseDto dto = bookReflectionService.getMyReflectionForBook(isbn13, user);
        return ResponseEntity.ok(dto);
    }

    // 독후감 수정
    @PutMapping
    public ResponseEntity<?> updateReflection(@RequestBody BookReflectionUpdateRequestDto dto,
                                              @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookReflectionService.updateReflection(dto, user);
        return ResponseEntity.ok(Map.of("message", "독후감이 성공적으로 수정되었습니다."));
    }

    // 독후감 삭제
    @DeleteMapping("/{reflectionId}")
    public ResponseEntity<?> deleteReflection(@PathVariable Long reflectionId,
                                              @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookReflectionService.deleteReflection(reflectionId, user);
        return ResponseEntity.ok(Map.of("message", "독후감이 성공적으로 삭제되었습니다."));
    }

    // 내가 쓴 독후감
    @GetMapping("/my")
    public List<MyReflectionResponseDto> getMyReflections(
            @AuthenticationPrincipal CustomUser currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReflectionService.getMyReflections(user, offset, limit);
    }

    // 특정 책의 모든 독후감 조회 (정렬 + 페이지네이션)
    @GetMapping("/list/{isbn13}")
    public BookReflectionListResponseDto getAllReflectionsForBook(
            @PathVariable String isbn13,
            @RequestParam(defaultValue = "new") String sort,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReflectionService.getReflectionsSummaryByBook(isbn13, sort, offset, limit, user);
    }
}
