package org.example.booknuri.global.DBConstruction.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.dto.BookClinetApiInfoResponseDto;
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

    // 🔁 기본 전체 수집 (1페이지부터 끝까지)
    public List<BookClinetApiInfoResponseDto> fetchBooksFromLibrary(String libCode) {
        return fetchBooksFromLibrary(libCode, 1, -1); // 시작 1, 끝 없음 (-1)
    }

    // ✅ 도서관 책 목록 수집 (시작~끝 페이지 지정 가능)
    public List<BookClinetApiInfoResponseDto> fetchBooksFromLibrary(String libCode, int startPage, int endPage) {
        log.info("[{}] 도서관 - 도서 목록 수집 시작 ({} ~ {})", libCode, startPage, endPage == -1 ? "끝까지" : endPage);

        List<BookClinetApiInfoResponseDto> results = new ArrayList<>();
        int page = startPage;

        while (endPage == -1 || page <= endPage) {
            String responseBody = null;

            // 각 페이지 최대 3회 재시도
            for (int i = 0; i < 3; i++) {
                try {
                    String url = String.format(
                            "http://data4library.kr/api/itemSrch?type=ALL&libCode=%s&authKey=%s&pageNo=%d&pageSize=200",
                            libCode, authKey, page);

                    log.info("[{}] API 요청 (page {}): {}", libCode, page, url);
                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        log.warn("[{}] 요청 실패 (HTTP {})", libCode, response.getStatusCodeValue());
                        continue;
                    }

                    String body = response.getBody();
                    if (body == null || body.contains("<html")) {
                        log.warn("❌ [{}] page {} - HTML 응답 or null! ({}회차)", libCode, page, i + 1);
                        Thread.sleep(2000);
                        continue;
                    }

                    responseBody = body;
                    break;

                } catch (HttpServerErrorException ex) {
                    log.warn("[{}] page {} - {}회차 요청 실패: {}", libCode, page, i + 1, ex.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // 인터럽트 상태 복구 (좋은 습관)
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return results;
                }
            }

            // 3회 실패 시 skip
            if (responseBody == null) {
                log.error("🚫 [{}] page {} - HTML 포함 API 실패 3회 초과 (skip)", libCode, page);
                page++;
                continue;
            }

            Document doc = Jsoup.parse(responseBody, "", Parser.xmlParser());
            Elements docElements = doc.select("doc");

            // XML에서 resultNum도 같이 파싱해서 0이면 종료로 간주하자
            String resultNumText = doc.selectFirst("resultNum") != null ? doc.selectFirst("resultNum").text() : "0";
            int resultNum = Integer.parseInt(resultNumText);

            if (resultNum == 0 || docElements.isEmpty()) {
                log.info("[{}] page {} - 더 이상 도서 없음", libCode, page);
                break;
            }
            log.info("[{}] page {} - {}권 수집됨", libCode, page, docElements.size());

            for (Element el : docElements) {
                String isbn13 = getSafeText(el, "isbn13");
                String regDate = getSafeText(el, "reg_date");

                results.add(BookClinetApiInfoResponseDto.builder()
                        .isbn13(isbn13)
                        .regDate(regDate)
                        .build());
            }

            page++;
        }

        log.info("[{}] 도서관 - 도서 수집 완료 (총 {}권)", libCode, results.size());
        return results;
    }

    // ✅ ISBN으로 도서 상세 정보 가져오기 (JSON)
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

    // ✅ 안전하게 텍스트 추출
    private String getSafeText(Element el, String tag) {
        Element target = el.selectFirst(tag);
        if (target == null) return null;
        String text = target.text();
        return text.equals("-") || text.isBlank() ? null : text;
    }
}
