package org.example.booknuri.domain.bookReflection.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookReflection.entity.BookReflectionEntity;
import org.example.booknuri.domain.bookReflection.entity.BookReflectionLikeEntity;
import org.example.booknuri.domain.bookReflection.repository.BookReflectionLikeRepository;
import org.example.booknuri.domain.bookReflection.repository.BookReflectionRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class BookReflectionLikeService {

    private final BookReflectionLikeRepository likeRepository;
    private final BookReflectionRepository reflectionRepository;
    private final UserRepository userRepository;

    // 좋아요 토글 기능 (있으면 취소, 없으면 등록)
    public boolean toggleLike(Long reflectionId, String username) {
        // 1. 유저 정보 가져오기
        UserEntity user = userRepository.findByUsername(username);

        // 2. 독후감 정보 가져오기
        BookReflectionEntity reflection = reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new RuntimeException("독후감 없음"));

        // 3. 기존 좋아요 있는지 확인
        Optional<BookReflectionLikeEntity> existing = likeRepository.findByUserAndReflection(user, reflection);

        if (existing.isPresent()) {
            // 4. 이미 좋아요 했으면 취소하고 → 좋아요 수 감소
            likeRepository.delete(existing.get());
            reflection.decreaseLikeCount();
            reflectionRepository.save(reflection);
            return false;
        } else {
            // 5. 없으면 새로 좋아요 등록 → 좋아요 수 증가
            BookReflectionLikeEntity like = BookReflectionLikeEntity.builder()
                    .user(user)
                    .reflection(reflection)
                    .likedAt(new Date())
                    .build();

            likeRepository.save(like);
            reflection.increaseLikeCount();
            reflectionRepository.save(reflection);
            return true;
        }
    }
}
