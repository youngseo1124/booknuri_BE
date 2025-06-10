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
import java.util.Optional;

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
        boolean alreadyReflected = bookReflectionService.checkAlreadyPublicReflected(isbn13, user);
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

    // 독후감 id로 조회 (수정용)
    @GetMapping("/my/edit/{reflectionId}")
    public ResponseEntity<?> getMyReflectionById(@PathVariable Long reflectionId,
                                                 @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        BookReflectionResponseDto dto = bookReflectionService.getMyReflectionById(reflectionId, user);
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
            @RequestParam(defaultValue = "like") String sort,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        return bookReflectionService.getReflectionsSummaryByBook(isbn13, sort, offset, limit, user);
    }

    //내가 쓴 독후감 책 그룹별로 반환
    @GetMapping("/my/grouped")
    public ResponseEntity<MyReflectionGroupedPageResponseDto> getMyReflectionsGroupedByBook(
            @AuthenticationPrincipal CustomUser currentUser,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        MyReflectionGroupedPageResponseDto response = bookReflectionService.getMyReflectionsGroupedByBook(user, offset, limit);
        return ResponseEntity.ok(response);
    }

    // 특정 책에 대해 내가 쓴 모든(활성화된)  독후감 리스트 반환
    @GetMapping("/my/isbn/{isbn13}")
    public ResponseEntity<List<BookReflectionResponseDto>> getMyReflectionsByBook(
            @PathVariable String isbn13,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        List<BookReflectionResponseDto> responseList = bookReflectionService.getMyReflectionsByBook(isbn13, user);
        return ResponseEntity.ok(responseList);
    }



    /**
     *  특정 ISBN에 대해 내가 쓴 가장 최신 독후감의 ID를 반환하는 API
     */
    @GetMapping("/my/latest-id/{isbn13}")
    public ResponseEntity<Map<String, Long>> getLatestMyReflectionIdByIsbn(
            @PathVariable String isbn13,
            @AuthenticationPrincipal CustomUser currentUser
    ) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        Long reflectionId = bookReflectionService.getLatestReflectionIdByIsbn13(isbn13, user);

        return ResponseEntity.ok(Map.of("reflectionId", Optional.ofNullable(reflectionId).orElse(null)));
    }


}
