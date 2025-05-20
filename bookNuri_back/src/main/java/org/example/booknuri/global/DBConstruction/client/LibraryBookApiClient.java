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

    // RestTemplate은 HTTP 요청을 보내기 위한 Spring 기본 객체
    // 기본적으로 재사용 가능하게 한 번만 생성해서 사용
    private final RestTemplate restTemplate = new RestTemplate();

    // application.yml 또는 환경변수에서 주입되는 인증 키
    @Value("${library.api.auth-key}")
    private String authKey;

    // 도서관 코드를 기반으로 해당 도서관이 보유한 도서 목록을 수집하는 메서드
    // XML 응답을 받아서 Jsoup으로 파싱 후 필요한 필드를 추출해 Dto로 만들어 반환
    public List<BookInfoResponseDto> fetchBooksFromLibrary(String libCode) {
        log.info("[{}] 도서관 - 도서 목록 수집 시작", libCode);

        List<BookInfoResponseDto> results = new ArrayList<>();
        int page = 1;

        while (true) {
            try {
                // 최대 2회 재시도 로직 구현 (예: 네트워크 오류 등)
                String responseBody = null;
                for (int i = 0; i < 2; i++) {
                    try {
                        String url = String.format("http://data4library.kr/api/itemSrch?type=ALL&libCode=%s&authKey=%s&pageNo=%d&pageSize=400",
                                libCode, authKey, page);
                        log.info("[{}] API 요청 (page {}): {}", libCode, page, url);

                        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            log.warn("[{}] 요청 실패 (HTTP {}) - 재시도 중단", libCode, response.getStatusCodeValue());
                            break;
                        }

                        responseBody = response.getBody();
                        break; // 성공하면 루프 탈출

                    } catch (HttpServerErrorException ex) {
                        log.warn("[{}] page {} - {}회차 요청 실패: {}", libCode, page, i + 1, ex.getMessage());
                        Thread.sleep(2000); // 2초 대기 후 재시도
                    }
                }

                if (responseBody == null) {
                    log.error("[{}] page {} - 2회 재시도 후 실패 (중단)", libCode, page);
                    break;
                }

                // 응답 XML을 Jsoup으로 파싱
                Document doc = Jsoup.parse(responseBody, "", Parser.xmlParser());
                Elements docElements = doc.select("doc");

                // 더 이상 수집할 도서가 없으면 종료
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

                page++; // 다음 페이지로 이동

            } catch (Exception e) {
                // 모든 예외는 여기서 처리
                log.error("[{}] 도서 목록 수집 중 예외 발생: {}", libCode, e.getMessage(), e);
                break;
            }
        }

        log.info("[{}] 도서관 - 도서 수집 완료 (총 {}권)", libCode, results.size());
        return results;
    }

    // 특정 태그를 안전하게 꺼내오는 메서드
    // 존재하지 않거나 빈 값이면 null 반환
    private String getSafeText(Element el, String tag) {
        Element target = el.selectFirst(tag);
        if (target == null) return null;
        String text = target.text();
        return text.equals("-") || text.isBlank() ? null : text;
    }

    // isbn13 코드로 단일 도서 상세 정보를 JSON으로 조회하는 메서드
    // 최대 2회 재시도 로직 포함
    public JsonNode fetchBookDetailByIsbn(String isbn13) {
        String url = String.format(
                "http://data4library.kr/api/srchDtlList?authKey=%s&isbn13=%s&loaninfoYN=Y&format=json",
                authKey, isbn13);

        String responseBody = null;

        // 최대 2회 재시도
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
                    Thread.sleep(2000); // 재시도 전 2초 대기
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // 응답 실패
        if (responseBody == null) {
            throw new RuntimeException("ISBN " + isbn13 + " 도서 상세 정보 요청 2회 실패");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            // 도서 정보 추출
            JsonNode book = root.path("response").path("detail").get(0).path("book");

            // 책 정보가 비어있거나 없으면 예외 처리
            if (book == null || book.isEmpty()) {
                throw new RuntimeException("도서 상세 정보 없음 (ISBN " + isbn13 + ")");
            }

            return book;

        } catch (Exception e) {
            throw new RuntimeException("도서 상세 파싱 오류: " + e.getMessage());
        }
    }
}
