package org.example.booknuri.domain.bookQuote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.bookQuote.converter.BookQuoteConverter;
import org.example.booknuri.domain.bookQuote.converter.MyQuoteConverter;
import org.example.booknuri.domain.bookQuote.dto.*;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
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
    public BookQuoteListResponseDto getQuotesByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort)); // ✅ 정렬 추가
        Page<BookQuoteEntity> page = bookQuoteRepository.findByBook_Isbn13AndVisibleToPublicTrue(isbn13, pageable);

        int totalCount = bookQuoteRepository.countByBook_Isbn13AndIsActiveTrue(isbn13);

        return BookQuoteListResponseDto.builder()
                .quotes(bookQuoteConverter.toDtoList(page.getContent(), currentUser))
                .totalCount(totalCount)
                .build();
    }


    private Sort getSortOrder(String sort) {
        return switch (sort.toLowerCase()) {
            case "like" -> Sort.by(Sort.Direction.DESC, "likeCount");
            case "high" -> Sort.by(Sort.Direction.DESC, "fontScale"); // ✨ 폰트 크기 기준 (예시)
            case "low" -> Sort.by(Sort.Direction.ASC, "fontScale");  // ✨ 폰트 작을수록 먼저 (예시)
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
        };
    }



    //이미지->텍스트 ocr 추출
    // ✨ 이미지 → 텍스트 OCR 추출
    public String extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException {

        File tempFile = File.createTempFile("ocr_", ".png");
        imageFile.transferTo(tempFile);
        log.info("📸 업로드된 이미지 파일: {}", tempFile.getAbsolutePath());

        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(new ClassPathResource("tessdata").getFile().getAbsolutePath());
            tesseract.setLanguage("kor+eng");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(6);

            // try 범위를 넓혀서 ImageIO.read()까지 포함시킴
            try {
                log.info("🔍 OCR 원본 파일 시도");
                return cleanUpOcrText(tesseract.doOCR(tempFile));
            } catch (TesseractException e) {
                log.warn("⚠️ PNG로 OCR 실패, JPG 변환 후 재시도 👉 {}", e.getMessage());

                BufferedImage originalImage = ImageIO.read(tempFile);
                if (originalImage == null) {
                    throw new IOException("❌ 이미지 파일을 읽을 수 없습니다.");
                }

                File jpgFile = new File(tempFile.getParent(), tempFile.getName().replace(".png", ".jpg"));
                ImageIO.write(originalImage, "jpg", jpgFile);
                log.info("📤 JPG 변환 완료: {}", jpgFile.getAbsolutePath());

                return cleanUpOcrText(tesseract.doOCR(jpgFile));
            }

        } finally {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.warn("🧹 tempFile 삭제 실패: {}", tempFile.getAbsolutePath());
            }
        }
    }


    // ✂OCR 결과 클린업 함수
    private String cleanUpOcrText(String rawText) {
        log.info("📝 원본 OCR 결과:\n{}", rawText);

        // 1️⃣ 줄바꿈 제거
        rawText = rawText.replaceAll("\n", "");

        // 2️⃣ 한글 사이 공백 제거
        String text = rawText.replaceAll("(?<=[가-힣])\\s+(?=[가-힣])", "");

        // 3️⃣ 문장 기호 뒤에 줄바꿈
        text = text.replaceAll("([,.;!?])", "$1\n");

        // 4️⃣ 중복 공백 제거
        text = text.replaceAll("\\s{2,}", " ").trim();

        log.info("🧼 정리된 텍스트 결과:\n{}", text);
        return text;
    }











}
