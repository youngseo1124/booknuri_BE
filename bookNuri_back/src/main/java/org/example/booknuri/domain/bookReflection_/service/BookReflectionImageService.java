package org.example.booknuri.domain.bookReflection_.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection_.entity.BookReflectionImageEntity;
import org.example.booknuri.domain.bookReflection_.repository.BookReflectionImageRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReflectionImageService {

    private final BookReflectionImageRepository bookReflectionImageRepository;

    //이미지 URL 리스트를 받아서 DB에 저장하는 로직
     // @param imageUrls 업로드된 이미지들의 S3 URL 리스트
     //@param reflection 어떤 독후감과 연결된 이미지인지

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



    //이미지 삭제
    public void deleteImage(Long imageId, UserEntity user) {
        BookReflectionImageEntity image = bookReflectionImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지가 존재하지 않습니다."));

        // 보안: 본인 글인지 확인
        if (!image.getReflection().getUser().getUsername().equals(user.getUsername())) {
            throw new IllegalStateException("본인의 이미지가 아닙니다.");
        }

        bookReflectionImageRepository.delete(image);
    }
}
