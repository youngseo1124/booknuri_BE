package org.example.booknuri.global.DBConstruction.client;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;



@Component
@RequiredArgsConstructor
public class LibraryApiClient {

    //  HTTP 요청 도구 (스프링이 제공하는 간단한 REST 클라이언트)
    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${library.api.auth-key}")
    private String authKey;

    // 도서관 정보 전체를 API에서 가져와서 DTO 리스트로 반환하는 메서드
    public List<LibraryResponseDto> fetchLibrariesFromApi() {

        List<LibraryResponseDto> libraries = new ArrayList<>();
        int page = 1;

        while (true) {

            String url = String.format("http://data4library.kr/api/libSrch?authKey=%s&pageNo=%d&pageSize=100", authKey, page);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) break; // 응답 실패하면 종료

            //  XML 파싱 (Jsoup + XML 파서)
            Document doc = Jsoup.parse(response.getBody(), "", Parser.xmlParser());
            Elements libElements = doc.select("lib");

            if (libElements.isEmpty()) break; // 더 이상 도서관 없음

            for (Element el : libElements) {

                // 각 태그 값이 없는 경우를 고려해서 안전하게 파싱
                String libCode = getSafeText(el, "libCode");
                String libName = getSafeText(el, "libName");
                String address = getSafeText(el, "address");
                String tel = getSafeText(el, "tel");
                String fax = getSafeText(el, "fax");
                String homepage = getSafeText(el, "homepage");
                String closed = getSafeText(el, "closed");
                String operatingTime = getSafeText(el, "operatingTime");

                Double latitude = parseSafeDouble(el, "latitude");
                Double longitude = parseSafeDouble(el, "longitude");
                Integer bookCount = parseSafeInt(el, "BookCount");

                libraries.add(LibraryResponseDto.builder()
                        .libCode(libCode)
                        .libName(libName)
                        .fullAddress(address)
                        .tel(tel)
                        .fax(fax)
                        .homepage(homepage)
                        .closed(closed)
                        .operatingTime(operatingTime)
                        .latitude(latitude)
                        .longitude(longitude)
                        .bookCount(bookCount)
                        .build());
            }

            page++;
        }

        return libraries;
    }

    // 해당 태그가 없거나 text가 빈 문자열이면 null 리턴
    private String getSafeText(Element el, String tag) {
        Element target = el.selectFirst(tag);
        if (target == null) return null;

        String text = target.text();
        return text.equals("-") || text.isBlank() ? null : text;
    }

    //  Double로 안전하게 변환 (실패 시 null)
    private Double parseSafeDouble(Element el, String tag) {
        try {
            String value = getSafeText(el, tag);
            return value != null ? Double.parseDouble(value) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Integer로 안전하게 변환 (실패 시 null)
    private Integer parseSafeInt(Element el, String tag) {
        try {
            String value = getSafeText(el, tag);
            return value != null ? Integer.parseInt(value) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
