package org.example.booknuri.domain.bookQuote.service;


import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteLikeEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteLikeRepository;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookQuoteLikeService {

    private final BookQuoteLikeRepository bookQuoteLikeRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final UserRepository userRepository;

    //  좋아요 토글
    public boolean toggleLike(Long quoteId, String username) {
        // 1. 유저 조회
        UserEntity user = userRepository.findByUsername(username);

        // 2. 인용 조회
        BookQuoteEntity quote = bookQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("인용을 찾을 수 없습니다."));

        // 3. 기존 좋아요 여부 확인
        Optional<BookQuoteLikeEntity> existing = bookQuoteLikeRepository.findByUserAndQuote(user, quote);

        if (existing.isPresent()) {
            // 4. 있으면 삭제하고 좋아요 수 감소
            bookQuoteLikeRepository.delete(existing.get());
            quote.decreaseLikeCount();
            bookQuoteRepository.save(quote);
            return false;
        } else {
            // 5. 없으면 추가하고 좋아요 수 증가
            BookQuoteLikeEntity like = BookQuoteLikeEntity.builder()
                    .user(user)
                    .quote(quote)
                    .likedAt(new Date())
                    .build();

            bookQuoteLikeRepository.save(like);
            quote.increaseLikeCount();
            bookQuoteRepository.save(quote);
            return true;
        }
    }
}
