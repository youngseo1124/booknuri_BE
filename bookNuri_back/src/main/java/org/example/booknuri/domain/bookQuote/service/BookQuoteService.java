package org.example.booknuri.domain.bookQuote.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.bookQuote.converter.BookQuoteConverter;
import org.example.booknuri.domain.bookQuote.converter.MyQuoteConverter;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteCreateRequestDto;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.dto.BookQuoteUpdateRequestDto;
import org.example.booknuri.domain.bookQuote.dto.MyQuoteResponseDto;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookQuoteService {

    private final BookQuoteRepository bookQuoteRepository;
    private final BookRepository bookRepository;
    private final BookQuoteConverter bookQuoteConverter;
    private final MyQuoteConverter myQuoteConverter;

    // ✨ 인용 등록
    public void createQuote(BookQuoteCreateRequestDto dto, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        BookQuoteEntity entity = bookQuoteConverter.toEntity(dto, book, user);
        bookQuoteRepository.save(entity);
    }

    // ✨ 인용 수정
    public void updateQuote(BookQuoteUpdateRequestDto dto, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(dto.getQuoteId(), user)
                .orElseThrow(() -> new IllegalArgumentException("수정할 인용이 없습니다."));

        entity.updateQuote(
                dto.getQuoteText(),
                dto.getFontScale(),
                dto.getFontColor(),
                dto.getBackgroundId(),
                dto.isVisibleToPublic()
        );
    }

    // ✨ 인용 삭제
    public void deleteQuote(Long quoteId, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(quoteId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 인용이 없습니다."));
        bookQuoteRepository.delete(entity);
    }

    // ✨ 마이페이지용 내가 쓴 인용 리스트
    public List<MyQuoteResponseDto> getMyQuotes(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookQuoteEntity> page = bookQuoteRepository.findByUser(user, pageable);
        return myQuoteConverter.toDtoList(page.getContent(), user);
    }

    // ✨ 수정 화면용 단일 인용 가져오기 (내가 쓴 것만)
    public MyQuoteResponseDto getMyQuoteFullById(Long quoteId, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(quoteId, user)
                .orElseThrow(() -> new IllegalArgumentException("해당 인용이 없거나 접근 권한이 없습니다."));
        return myQuoteConverter.toDto(entity,user); // 📌 MyQuoteResponseDto 변환기로 넘기기
    }

    // ✨ 특정 책 인용 전체 조회 (리스트용, 공개된 것만)
    public List<BookQuoteResponseDto> getQuotesByBook(String isbn13, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookQuoteEntity> page = bookQuoteRepository.findByBook_Isbn13AndVisibleToPublicTrue(isbn13, pageable);
        return bookQuoteConverter.toDtoList(page.getContent(), currentUser);
    }

    //이미지->텍스트 ocr 추출
    public String extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException {
        File tempFile = File.createTempFile("ocr_", ".png");
        imageFile.transferTo(tempFile);

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(new ClassPathResource("tessdata").getFile().getAbsolutePath());
            tesseract.setLanguage("kor+eng");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(6);

            // 1️⃣ OCR 추출
            String rawText = tesseract.doOCR(tempFile);

            // 2️⃣ OCR 줄바꿈 무시
            rawText = rawText.replaceAll("\n", "");

            // 3️⃣ 한글 사이 공백 제거
            String text = rawText.replaceAll("(?<=[가-힣])\\s+(?=[가-힣])", "");

            // 4️⃣ 줄바꿈 강제 추가할 기호 목록: , . ; ! ?
            text = text.replaceAll("([,.;!?])", "$1\n");

            // 5️⃣ 중복 공백 제거
            text = text.replaceAll("\\s{2,}", " ").trim();

            return text;

        } finally {
            tempFile.delete();
        }
    }











}
