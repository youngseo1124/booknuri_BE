package org.example.booknuri.domain.Log.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.Log.entity.UserBookViewLogEntity;
import org.example.booknuri.domain.Log.repository.UserBookViewLogRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookViewLogService {

    private final UserBookViewLogRepository userBookViewLogRepository;

    @Transactional
    public void saveRecentView(UserEntity user, BookEntity book) {
        UserBookViewLogEntity existing = userBookViewLogRepository.findByUserAndBook(user, book).orElse(null);
        //이미 조회한 책이면 조회날짜만 갱신
        if (existing != null) {
            existing.setViewedAt(LocalDateTime.now());
            userBookViewLogRepository.save(existing);
            //아니면 새로 엔티티 만들기
        } else {
            UserBookViewLogEntity newLog = UserBookViewLogEntity.builder()
                    .user(user)
                    .book(book)
                    .viewedAt(LocalDateTime.now())
                    .build();
            userBookViewLogRepository.save(newLog);
        }

        // 30개 초과 시 삭제
        userBookViewLogRepository.deleteExceedingLimit(user.getUsername());
    }

    @Transactional
    public List<BookEntity> getRecentViewedBooks(UserEntity user) {
        return userBookViewLogRepository.findTop30ByUserOrderByViewedAtDesc(user)
                .stream()
                .map(UserBookViewLogEntity::getBook)
                .toList();
    }

}
