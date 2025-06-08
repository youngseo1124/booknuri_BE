package org.example.booknuri.domain.bookQuote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.bookQuote.converter.BookQuoteConverter;
import org.example.booknuri.domain.bookQuote.converter.MyQuoteConverter;
import org.example.booknuri.domain.bookQuote.converter.MyQuoteGroupedConverter;
import org.example.booknuri.domain.bookQuote.dto.*;
import org.example.booknuri.domain.bookQuote.entity.BookQuoteEntity;
import org.example.booknuri.domain.bookQuote.repository.BookQuoteRepository;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookQuoteService {

    private final BookQuoteRepository bookQuoteRepository;
    private final BookRepository bookRepository;
    private final BookQuoteConverter bookQuoteConverter;
    private final MyQuoteConverter myQuoteConverter;
    private final MyQuoteGroupedConverter myQuoteGroupedConverter;

    //  인용 등록
    public void createQuote(BookQuoteCreateRequestDto dto, UserEntity user) {
        BookEntity book = bookRepository.findByIsbn13(dto.getIsbn13())
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 책이 존재하지 않습니다."));

        BookQuoteEntity entity = bookQuoteConverter.toEntity(dto, book, user);
        bookQuoteRepository.save(entity);
    }

    //  인용 수정
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

    // 인용 삭제
    public void deleteQuote(Long quoteId, UserEntity user) {
        BookQuoteEntity entity = bookQuoteRepository.findByIdAndUser(quoteId, user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 인용이 없습니다."));
        bookQuoteRepository.delete(entity);
    }

    //  마이페이지용 내가 쓴 인용 리스트
    public List<MyQuoteResponseDto> getMyQuotes(UserEntity user, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookQuoteEntity> page = bookQuoteRepository.findByUser(user, pageable);
        return myQuoteConverter.toDtoList(page.getContent(), user);
    }

    //  단일 인용 가져오기 (공개면 누구나, 비공개면 본인만 가능)
    public MyQuoteResponseDto getMyQuoteFullById(Long quoteId, UserEntity currentUser) {
        BookQuoteEntity entity = bookQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("해당 인용이 존재하지 않습니다."));

        // 공개 안 된 인용일 경우 → 본인이 아닌 경우 접근 불가
        if (!entity.isVisibleToPublic() && !entity.getUser().equals(currentUser)) {
            throw new AccessDeniedException("비공개 인용에 접근할 수 없습니다.");
        }

        return myQuoteConverter.toDto(entity, currentUser);
    }


    //  특정 책 인용 전체 조회 (리스트용, 공개된 것만)
    public BookQuoteListResponseDto getQuotesByBook(String isbn13, String sort, int offset, int limit, UserEntity currentUser) {
        Pageable pageable = PageRequest.of(offset / limit, limit, getSortOrder(sort));
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
            case "high" -> Sort.by(Sort.Direction.DESC, "fontScale");
            case "low" -> Sort.by(Sort.Direction.ASC, "fontScale");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
        };
    }



    //이미지->텍스트 ocr 추출
    //  이미지 → 텍스트 OCR 추출
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

                return cleanUpOcrText(tesseract.doOCR(tempFile));
            } catch (TesseractException e) {


                BufferedImage originalImage = ImageIO.read(tempFile);
                if (originalImage == null) {
                    throw new IOException("❌ 이미지 파일을 읽을 수 없습니다.");
                }

                File jpgFile = new File(tempFile.getParent(), tempFile.getName().replace(".png", ".jpg"));
                ImageIO.write(originalImage, "jpg", jpgFile);

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


        rawText = rawText.replaceAll("\n", "");


        String text = rawText.replaceAll("(?<=[가-힣])\\s+(?=[가-힣])", "");


        text = text.replaceAll("([,.;!?])", "$1\n");


        text = text.replaceAll("\\s{2,}", " ").trim();

        return text;
    }



    //인기 인용들 구하기
    public BookQuoteListResponseDto getPopularQuotes(int offset, int limit, UserEntity currentUser) {
        List<BookQuoteEntity> list = bookQuoteRepository.findPopularQuotesWithRecency(offset, limit);
        int totalCount = bookQuoteRepository.countByVisibleToPublicTrueAndIsActiveTrue();

        return BookQuoteListResponseDto.builder()
                .quotes(bookQuoteConverter.toDtoList(list, currentUser))
                .totalCount(totalCount)
                .build();
    }





   //내가 쓴 인용 책 그룹별로 묶어보기
   public MyQuoteGroupedPageResponseDto getMyQuotesGroupedByBook(UserEntity user, int offset, int limit) {
       Pageable pageable = PageRequest.of(offset / limit, limit);
       List<Object[]> groupedBookRaw = bookQuoteRepository.findBooksByUserGroupedAndSorted(user, pageable);

       List<MyQuoteGroupedByBookResponseDto> result = new ArrayList<>();
       int totalQuoteCount = 0;

       for (Object[] row : groupedBookRaw) {
           String isbn13 = (String) row[0];

           BookEntity book = bookRepository.findByIsbn13(isbn13)
                   .orElseThrow(() -> new IllegalArgumentException("책 없음"));
           List<BookQuoteEntity> quotes = bookQuoteRepository.findAllByUserAndBook(user, book);

           totalQuoteCount += quotes.size();
           result.add(myQuoteGroupedConverter.toDto(quotes));
       }

       return MyQuoteGroupedPageResponseDto.builder()
               .pageNumber(offset / limit)
               .pageSize(limit)
               .totalCount(result.size())
               .totalQuoteCount(totalQuoteCount)
               .content(result)
               .build();
   }














}
