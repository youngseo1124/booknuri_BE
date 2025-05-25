package org.example.booknuri.domain.book.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booknuri.domain.book.converter.BookClinetApiInfoConverter;
import org.example.booknuri.domain.book.dto.BookClinetApiInfoResponseDto;
import org.example.booknuri.domain.book.dto.BookInfoResponseDto;
import org.example.booknuri.domain.book.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.global.security.provider.JwtProvider;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/book")
public class BookController {

    private final RedisTemplate<String, String> redisTemplate;
    private final BookClinetApiInfoConverter bookInfoConverter;
    private final BookService bookService;


    // ISBN으로 도서 상세 조회
    @GetMapping("/{isbn13}")
    public BookInfoResponseDto getBookDetail(@PathVariable String isbn13) {
        return bookService.getBookDetailByIsbn(isbn13);
    }






    //List.of(...)는 Java 9 이상에서 제공되는 정적 팩토리 메서드야.
    //
    //내부적으로는 List 인터페이스의 불변(immutable) 구현체를 리턴
    //ArrayList	자유롭게 수정 가능한 리스트
    //List.of()	읽기 전용! 바꿀 수 없는 리스트
    private final List<String> sampleIsbnList = List.of(
            "9791168340770", //미움받을용기
            "9788937460449", //데미안
            "9788901272580", //역행자
            "9788932917245", //어린왖아
            "9791194171560", //줍는순간
            "9788934996309", //창가의 토토
            "9791173740275",
            "9788997780624"

    );


    @Value("${library.api.auth-key}")
    private String authKey;

    @GetMapping("/random")
    public BookClinetApiInfoResponseDto getRandomBookInfo() {
        String isbn = sampleIsbnList.get(new Random().nextInt(sampleIsbnList.size()));
        log.info("선택된 ISBN: {}", isbn);

        String url = "https://data4library.kr/api/srchDtlList?authKey=" + authKey
                + "&isbn13=" + isbn
                + "&loaninfoYN=Y"
                + "&format=json";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode bookNode = root.path("response").path("detail").get(0).path("book");

            // 🔄 여기서 Converter 호출!
            return bookInfoConverter.toDto(bookNode);

        } catch (Exception e) {
            log.error("❌ JSON 파싱 오류: {}", e.getMessage());
            log.warn("📨 전체 응답 내용:\n{}", response);
            return null;
        }
    }


    @GetMapping("/list")
    public List<BookClinetApiInfoResponseDto> getAllBookInfoListParallel() {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // ✅ 병렬 처리를 위한 쓰레드 풀 (고정된 10개 쓰레드)
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // ✅ sampleIsbnList 각각에 대해 병렬로 요청
        List<CompletableFuture<BookClinetApiInfoResponseDto>> futureList = sampleIsbnList.stream()
                .map(isbn -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("📖 [비동기] 조회중 ISBN: {}", isbn);

                        String url = "https://data4library.kr/api/srchDtlList?authKey=" + authKey
                                + "&isbn13=" + isbn
                                + "&loaninfoYN=Y"
                                + "&format=json";

                        String response = restTemplate.getForObject(url, String.class);
                        JsonNode root = objectMapper.readTree(response);
                        JsonNode bookNode = root.path("response").path("detail").get(0).path("book");

                        return bookInfoConverter.toDto(bookNode);
                    } catch (Exception e) {
                        log.error("❌ [비동기] ISBN {} 파싱 실패: {}", isbn, e.getMessage());
                        return null;
                    }
                }, executor)) // ✅ executor를 사용해 병렬로 수행
                .collect(Collectors.toList());

        // ✅ 모든 비동기 작업이 완료될 때까지 기다리고, 결과를 모은다
        List<BookClinetApiInfoResponseDto> bookList = futureList.stream()
                .map(CompletableFuture::join) // 결과 꺼내기 (join은 예외 throw 안 하고 null 리턴)
                .filter(dto -> dto != null)   // 실패한 항목은 제외
                .collect(Collectors.toList());

        // ✅ 쓰레드풀 종료 (메모리 누수 방지)
        executor.shutdown();

        return bookList;
    }

    @GetMapping("/my-ip")
    public String getMyIp() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("https://api.ipify.org", String.class);
    }

}
