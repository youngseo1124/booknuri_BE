package org.example.booknuri.domain.BookReflection.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

import org.example.booknuri.domain.BookReflection.entity.BookReflectionEntity;
import org.example.booknuri.domain.BookReflection.repository.BookReflectionRepository;
import org.example.booknuri.domain.BookReflection.service.BookReflectionImageService;
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

        // 1. 독후감 ID로 해당 유저의 독후감 찾기
        BookReflectionEntity reflection = bookReflectionRepository.findByIdAndUser(reflectionId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 독후감이 없습니다."));

        // 2. 이미지들을 S3에 업로드하고 URL 리스트로 받기
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String url = s3Uploader.upload(image);
            uploadedUrls.add(url);
        }

        // 3. 업로드된 이미지 URL을 DB에 저장
        bookReflectionImageService.saveImages(uploadedUrls, reflection);

        return ResponseEntity.ok(Map.of("uploadedImageUrls", uploadedUrls));
    }

}
