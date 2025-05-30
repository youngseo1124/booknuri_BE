package org.example.booknuri.domain.bookReflection_.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionRepository;
import org.example.booknuri.domain.bookReflection_.service.BookReflectionImageService;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.service.UserService;
import org.example.booknuri.global.imageUpload.S3Uploader;
import org.example.booknuri.global.security.entity.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/book/reflection/image")
public class BookReflectionImageController {

    private final S3Uploader s3Uploader;
    private final UserService userService;
    private final BookReflectionRepository bookReflectionRepository;
    private final BookReflectionImageService bookReflectionImageService;

    @PostMapping("/{reflectionId}/upload")
    public ResponseEntity<?> uploadReflectionImages(@PathVariable Long reflectionId,
                                                    @RequestPart List<MultipartFile> images,
                                                    @AuthenticationPrincipal CustomUser currentUser) throws IOException {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());

        //  reflectionId와 유저 정보로 해당 독후감 찾기
        BookReflectionEntity reflection = bookReflectionRepository.findByIdAndUser(reflectionId, user)
                .orElseThrow(() -> {
                    return new IllegalArgumentException("해당 독후감이 없습니다.");
                });

        // 이미지들을 하나씩 S3에 업로드
        List<String> uploadedUrls = new ArrayList<>();
        log.info(" 업로드할 이미지 수: {}", images.size());

        for (MultipartFile image : images) {
            log.info("📷 업로드 시작 - 파일명: {}, 크기: {} bytes", image.getOriginalFilename(), image.getSize());
            String url = s3Uploader.upload(image);
            uploadedUrls.add(url);

        }

        //  DB에 업로드된 이미지 정보 저장
        bookReflectionImageService.saveImages(uploadedUrls, reflection);



        return ResponseEntity.ok(Map.of("uploadedImageUrls", uploadedUrls));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteReflectionImage(@PathVariable Long imageId,
                                                   @AuthenticationPrincipal CustomUser currentUser) {
        UserEntity user = userService.getUserByUsername(currentUser.getUsername());
        bookReflectionImageService.deleteImage(imageId, user);
        return ResponseEntity.ok(Map.of("message", "이미지가 삭제되었습니다."));
    }



}
