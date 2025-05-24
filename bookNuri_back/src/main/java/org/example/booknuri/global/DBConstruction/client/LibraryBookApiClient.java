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

    // ✅ 도서관 책 목록 수집 (도서관 코드 기반, 전체 페이지 탐색)
    public List<BookInfoResponseDto> fetchBooksFromLibrary(String libCode) {
        log.info("[{}] 도서관 - 도서 목록 수집 시작", libCode);

        List<BookInfoResponseDto> results = new ArrayList<>();
        int page = 1;

        while (true) {
            String responseBody = null;

            // ✅ 각 page 마다 최대 3번까지 시도 (HTML 오류 응답 포함되면 실패로 간주)
            for (int i = 0; i < 3; i++) {
                try {
                    String url = String.format(
                            "http://data4library.kr/api/itemSrch?type=ALL&libCode=%s&authKey=%s&pageNo=%d&pageSize=200",
                            libCode, authKey, page);

                    log.info("[{}] API 요청 (page {}): {}", libCode, page, url);
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                    // 응답 코드가 200대 아니면 실패로 간주
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.warn("[{}] 요청 실패 (HTTP {})", libCode, response.getStatusCodeValue());
                        continue;
                    }

                    String body = response.getBody();

                    // ❗ HTML 포함되면 API가 에러페이지 응답한 것 → 실패 간주
                    if (body.contains("<html")) {
                        log.warn("❌ [{}] page {} - HTML 응답 수신! ({}회차)", libCode, page, i + 1);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); // 현재 쓰레드의 interrupted 상태를 복구
                        }
                        continue;
                    }

                    // ✅ 정상 응답일 경우에만 responseBody에 저장하고 break
                    responseBody = body;
                    break;

                } catch (HttpServerErrorException ex) {
                    log.warn("[{}] page {} - {}회차 요청 실패: {}", libCode, page, i + 1, ex.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // ❌ 3회 모두 실패 → 다음 페이지로 skip
            if (responseBody == null) {
                log.error("🚫 [{}] page {} - HTML 포함 API 실패 3회 초과 (skip)", libCode, page);
                page++;
                continue;
            }

            // ✅ XML 파싱 및 도서 정보 추출
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
        }

        log.info("[{}] 도서관 - 도서 수집 완료 (총 {}권)", libCode, results.size());
        return results;
    }

    // ✅ null-safe하게 태그에서 텍스트 추출
    private String getSafeText(Element el, String tag) {
        Element target = el.selectFirst(tag);
        if (target == null) return null;
        String text = target.text();
        return text.equals("-") || text.isBlank() ? null : text;
    }

    // ✅ ISBN으로 도서 상세정보 조회 (JSON 응답)
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
