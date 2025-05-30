package org.example.booknuri.domain.bookReflection.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection.entity.BookReflectionImageEntity;
import org.example.booknuri.domain.bookReflection.repository.BookReflectionImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReflectionImageService {

    private final BookReflectionImageRepository bookReflectionImageRepository;

    //이미지 URL 리스트를 받아서 DB에 저장하는 로직
     // @param imageUrls 업로드된 이미지들의 S3 URL 리스트
     //@param reflection 어떤 독후감과 연결된 이미지인지
    @Transactional
    public void saveImages(List<String> imageUrls, BookReflectionEntity reflection) {
        List<BookReflectionImageEntity> imageEntities = imageUrls.stream()
                .map(url -> BookReflectionImageEntity.builder()
                        .reflection(reflection)               //  어떤 독후감에 속한 이미지인지 연결
                        .imageUrl(url)                        // S3에 저장된 이미지 URL
                        .uploadedAt(LocalDateTime.now())      //  업로드 시간 기록
                        .build())
                .toList();

        bookReflectionImageRepository.saveAll(imageEntities); // 일괄 저장
    }
}
