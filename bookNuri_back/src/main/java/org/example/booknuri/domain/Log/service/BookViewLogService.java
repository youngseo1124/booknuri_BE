package org.example.booknuri.domain.Log.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

import org.example.booknuri.domain.Log.entity.UserBookViewLogEntity;
import org.example.booknuri.domain.Log.repository.UserBookViewLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookViewLogService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserBookViewLogRepository userBookViewLogRepository;

    //- 유저가 책을 조회했을 때 실행됨!
    //레디스
    @Async("taskExecutor")
    public void logBookView(UserEntity user, BookEntity book) {
        try {
            int birthYear = user.getBirth() != null ? user.getBirth() / 10000 : 0;

            //로그 문자열 생성 "user:아이디|book:책ID|gender:성별|birthYear:출생년도|timestamp"
            String redisLog = String.format(
                    "user:%s|book:%d|gender:%s|birthYear:%d|%s",
                    user.getUsername(),              // username 사용!
                    book.getId(),
                    user.getGender(),
                    birthYear,
                    LocalDateTime.now()
            );

            //Redis 리스트에 로그문자열 redisLog push하기
            redisTemplate.opsForList().leftPush("book_view_queue", redisLog);
            log.info("✅Redis bookViewLog 로그 저장 완료: {}", redisLog);
        } catch (Exception e) {
            log.error("🚨 Redis 로그 저장 실패", e);
        }
    }


}
