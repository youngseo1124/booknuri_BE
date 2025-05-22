package org.example.booknuri.global.DBConstruction.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryBookApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${library.api.auth-key}")
    private String authKey;

    private static final int MAX_FAILURE_COUNT = 5;

    public List<BookInfoResponseDto> fetchBooksFromLibrary(String libCode) {
        log.info("[{}] 도서관 - 도서 목록 수집 시작", libCode);

        List<BookInfoResponseDto> results = new ArrayList<>();
        int page = 1;
        int htmlFailCount = 0; // ❗ HTML 응답 실패 누적용

        while (true) {
            try {
                String responseBody = null;

                //  최대 3회까지 재시도 (단, HTML 응답 포함 시 pass 조건 위해)
                for (int i = 0; i < 3; i++) {
                    try {
                        String url = String.format(
                                "http://data4library.kr/api/itemSrch?type=ALL&libCode=%s&authKey=%s&pageNo=%d&pageSize=200",
                                libCode, authKey, page);

                        log.info("[{}] API 요청 (page {}): {}", libCode, page, url);
                        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                        if (!response.getStatusCode().is2xxSuccessful()) {
                            log.warn("[{}] 요청 실패 (HTTP {})", libCode, response.getStatusCodeValue());
                            break;
                        }

                        responseBody = response.getBody();

                        // ✅ HTML 에러페이지가 응답됐는지 검사
                        if (responseBody.contains("<html")) {
                            log.warn("❌ [{}] page {} - HTML 응답 수신! ({}회차)", libCode, page, i + 1);
                            Thread.sleep(2000); // 살짝 쉬고 재시도
                            responseBody = null;
                            continue;
                        }

                        break; // 정상 응답이면 탈출

                    } catch (HttpServerErrorException ex) {
                        log.warn("[{}] page {} - {}회차 요청 실패: {}", libCode, page, i + 1, ex.getMessage());
                        Thread.sleep(2000);
                    }
                }

                // ⛔ 10회 재시도에도 실패하면 패스하고 다음 페이지로 이동
                if (responseBody == null) {
                    htmlFailCount++;
                    log.error("🚫 [{}] page {} - HTML 포함 API 실패 3회 초과 (skip, 누적 HTML 실패: uthKey=3c744e2a785643b2c167a2f1ed075842f8a7847bb93c9e6732c91f7741929fea&pageNo=326&pageSize=100{})", libCode, page, htmlFailCount);
                    page++; // 다음 페이지로 이동
                    continue;
                }

                Document doc = Jsoup.parse(responseBody, "", Parser.xmlParser());
                Elements docElements = doc.select("doc");

                if (docElements.isEmpty()) {
                    log.info("[{}] page {} - 더 이상 도서 없음", libCode, page);
                    break;
                }

                log.info("[{}] page {} - {}권 수집됨", libCode, page, docElements.size());

                for (Element el : docElements) {
                    String isbn13 = getSafeText(el, "isbn13");
                    String regDate = getSafeText(el, "reg_date");

                    results.add(BookInfoResponseDto.builder()
                            .isbn13(isbn13)
                            .regDate(regDate)
                            .build());
                }

                page++;

            } catch (Exception e) {
                log.error("[{}] 도서 목록 수집 중 예외 발생: {}", libCode, e.getMessage(), e);
                break; // 그 외 예외는 중단
            }
        }

        log.info("[{}] 도서관 - 도서 수집 완료 (총 {}권)", libCode, results.size());
        return results;
    }

    private String getSafeText(Element el, String tag) {
        Element target = el.selectFirst(tag);
        if (target == null) return null;
        String text = target.text();
        return text.equals("-") || text.isBlank() ? null : text;
    }

    // ✅ 이 메서드는 그대로 유지 (2회 실패 시 throw)
    public JsonNode fetchBookDetailByIsbn(String isbn13) {
        String url = String.format(
                "http://data4library.kr/api/srchDtlList?authKey=%s&isbn13=%s&loaninfoYN=Y&format=json",
                authKey, isbn13);

        String responseBody = null;

        for (int i = 0; i < 2; i++) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn("ISBN {} 상세 정보 요청 실패 (HTTP {})", isbn13, response.getStatusCodeValue());
                    continue;
                }
                responseBody = response.getBody();
                break;

            } catch (HttpServerErrorException ex) {
                log.warn("ISBN {} - {}회차 요청 실패: {}", isbn13, i + 1, ex.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (responseBody == null) {
            throw new RuntimeException("ISBN " + isbn13 + " 도서 상세 정보 요청 2회 실패");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode book = root.path("response").path("detail").get(0).path("book");

            if (book == null || book.isEmpty()) {
                throw new RuntimeException("도서 상세 정보 없음 (ISBN " + isbn13 + ")");
            }

            return book;

        } catch (Exception e) {
            throw new RuntimeException("도서 상세 파싱 오류: " + e.getMessage());
        }
    }
}
