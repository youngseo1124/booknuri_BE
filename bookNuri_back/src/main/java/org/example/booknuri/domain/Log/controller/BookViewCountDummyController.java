package org.example.booknuri.domain.Log.controller;



//더미 데이터 삽입용 1회용 컨트롤러라 로직 분리안함ㅎㅎ;;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.Log.entity.BookViewCountLogEntity;
import org.example.booknuri.domain.Log.entity.BookViewLogEntity;
import org.example.booknuri.domain.Log.repository.BookViewCountLogRepository;
import org.example.booknuri.domain.Log.repository.BookViewLogRepository;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.repository.BookRepository;

import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BookViewCountDummyController {

    private final BookRepository bookRepository;
    private final BookViewCountLogRepository bookViewCountLogRepository;
    private final BookViewLogRepository bookViewLogRepository;
    private final UserRepository userRepository;


    // 생략된 import 및 클래스 선언 동일

    @GetMapping("/api/dummy/book-view-count")
    public ResponseEntity<String> insertDummyBookViewCounts() {
        int totalInserted = 0;
        int totalUpdated = 0;
        int totalChecked = 0;
        int totalSkipped = 0;

        try {
            int totalPages = 25;
            for (int page = 1; page <= totalPages; page++) {
                log.info("📄 [{}페이지] 인기 대출 도서 데이터 수집 시작", page);
                String apiUrl = "http://data4library.kr/api/loanItemSrch" +
                        "?authKey=3c744e2a785643b2c167a2f1ed075842f8a7847bb93c9e6732c91f7741929fea" +
                        "&startDt=2025-01-01&endDt=2025-06-03" +
                        "&pageNo=" + page + "&pageSize=200";

                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = docBuilder.parse(new URL(apiUrl).openStream());
                doc.getDocumentElement().normalize();

                NodeList docs = doc.getElementsByTagName("doc");
                log.info("🔍 [{}페이지] 도서 수: {}", page, docs.getLength());

                for (int i = 0; i < docs.getLength(); i++) {
                    totalChecked++;

                    Node node = docs.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element element = (Element) node;

                    String isbn13 = getTagValue("isbn13", element).trim();
                    String loanCountStr = getTagValue("loan_count", element).trim();

                    int loanCount;
                    try {
                        loanCount = Integer.parseInt(loanCountStr);
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ 잘못된 loanCount: {}", loanCountStr);
                        continue;
                    }

                    Optional<BookEntity> optionalBook = bookRepository.findByIsbn13(isbn13);
                    if (optionalBook.isPresent()) {
                        BookEntity book = optionalBook.get();
                        LocalDate today = LocalDate.now();

                        // ✅ 이미 있는지 체크해서 있으면 update
                        var existingLogs = bookViewCountLogRepository.findAllByBook_IdAndDate(book.getId(), today);
                        if (!existingLogs.isEmpty()) {
                            BookViewCountLogEntity existing = existingLogs.get(0);
                            existing.setViewCount(loanCount); // 갱신
                            bookViewCountLogRepository.save(existing);
                            totalUpdated++;
                            log.info("🔄 [{}] 업데이트 - ISBN: {}, count: {}", i + 1, isbn13, loanCount);
                        } else {
                            BookViewCountLogEntity logEntity = BookViewCountLogEntity.builder()
                                    .book(book)
                                    .date(today)
                                    .viewCount(loanCount)
                                    .build();
                            bookViewCountLogRepository.save(logEntity);
                            totalInserted++;
                            log.info("✅ [{}] 새로 저장 - ISBN: {}, count: {}", i + 1, isbn13, loanCount);
                        }
                    } else {
                        totalSkipped++;
                        log.info("❌ [{}] BookEntity 없음 - ISBN: {}", i + 1, isbn13);
                    }
                }
            }

            String result = String.format("🎉 삽입 완료! 총: %d, 저장: %d, 갱신: %d, 없음: %d", totalChecked, totalInserted, totalUpdated, totalSkipped);
            log.info(result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("🚨 더미 데이터 삽입 실패", e);
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }


    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() == 0) return "";
        Node node = nodeList.item(0);
        return node.getTextContent();
    }

    @PostMapping("/view-log")
    public ResponseEntity<String> generateDummyBookViews() {
        List<BookViewCountLogEntity> topBooks = bookViewCountLogRepository.findTop70ByOrderByViewCountDesc();

        List<String> genders = List.of("M", "F");
        List<Integer> birthYears = List.of(2008, 1998, 1988, 1978, 1968); // 10~50대

        LocalDateTime now = LocalDateTime.of(2025, 6, 6, 12, 0);

        int maxUsers = 30;

        for (String gender : genders) {
            for (Integer birthYear : birthYears) {
                int userIndex = 1;

                for (int i = 0; i < topBooks.size(); i++) {
                    int count = maxUsers - i;
                    BookEntity book = topBooks.get(i).getBook();

                    for (int j = 0; j < count; j++) {
                        String username = String.format("user_%d%s_%04d", birthYear, gender, userIndex++);
                        UserEntity user = userRepository.findByUsername(username);
                        if (user == null) {
                            user = userRepository.save(UserEntity.builder()
                                    .username(username)
                                    .gender(gender)
                                    .birth(birthYear * 10000 + 101)  // Integer로 저장 (ex. 19980101)
                                    .build());
                        }


                        BookViewLogEntity log = BookViewLogEntity.builder()
                                .user(user)
                                .book(book)
                                .gender(gender)
                                .birthYear(birthYear)
                                .viewedAt(now.minusSeconds(new Random().nextInt(3600 * 12))) // 약간 랜덤화
                                .build();

                        bookViewLogRepository.save(log);
                    }
                }
            }
        }

        return ResponseEntity.ok("더미 book_view_log 데이터 생성 완료");
    }




}
