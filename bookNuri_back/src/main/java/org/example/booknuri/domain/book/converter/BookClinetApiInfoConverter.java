package org.example.booknuri.domain.book.converter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.dto.BookClinetApiInfoResponseDto;
import org.example.booknuri.domain.book.entity.BookEntity;
import org.example.booknuri.domain.book.entity.MainCategory;
import org.example.booknuri.domain.book.entity.MiddleCategory;
import org.example.booknuri.domain.book.entity.SubCategory;
import org.example.booknuri.domain.book.repository.MainCategoryRepository;
import org.example.booknuri.domain.book.repository.MiddleCategoryRepository;
import org.example.booknuri.domain.book.repository.SubCategoryRepository;
import org.springframework.stereotype.Component;
@Component // 스프링에서 이 클래스를 Bean으로 등록해줌. 즉, 의존성 주입 받을 수 있게해줌
@RequiredArgsConstructor
public class BookClinetApiInfoConverter {

    private final MainCategoryRepository mainCategoryRepository;
    private final MiddleCategoryRepository middleCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    // 📘 외부 API JSON → BookInfoDto 변환

    public BookClinetApiInfoResponseDto toDto(JsonNode bookNode) {

        String classNm = bookNode.path("class_nm").asText();
        String[] categoryParts = classNm.split(" > ");

        return BookClinetApiInfoResponseDto.builder()
                .bookname(bookNode.path("bookname").asText())
                .authors(bookNode.path("authors").asText())
                .publisher(bookNode.path("publisher").asText())
                .publicationDate(convertToYear(bookNode.path("publication_date").asText()))

                .isbn13(bookNode.path("isbn13").asText())
                .description(bookNode.path("description").asText())
                .bookImageURL(bookNode.path("bookImageURL").asText())
                .classNm(bookNode.path("class_nm").asText())
                .mainCategory(categoryParts.length > 0 ? categoryParts[0] : null)
                .middleCategory(categoryParts.length > 1 ? categoryParts[1] : null)
                .subCategory(categoryParts.length > 2 ? categoryParts[2] : null)
                .build();
    }

    // ✨ 문자열을 연도로 파싱하는 유틸 (4자리면 변환, 아니면 null)
    private Integer convertToYear(String str) {
        if (str == null) return null;

        str = str.trim();
        if (str.length() >= 4) {
            String yearPart = str.substring(0, 4);
            if (yearPart.matches("\\d{4}")) {
                return Integer.parseInt(yearPart);
            }
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(str);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        return null;
    }


    public BookEntity toEntity(BookClinetApiInfoResponseDto dto) {
        // 카테고리 이름 파싱
        String main = dto.getMainCategory();
        String middle = dto.getMiddleCategory();
        String sub = dto.getSubCategory();

        // 없으면 만들어서 저장
        MainCategory mainCategory = mainCategoryRepository.findByName(main)
                .orElseGet(() -> mainCategoryRepository.save(
                        MainCategory.builder().name(main).build()));

        MiddleCategory middleCategory = middleCategoryRepository.findByName(middle)
                .orElseGet(() -> middleCategoryRepository.save(
                        MiddleCategory.builder().name(middle).mainCategory(mainCategory).build()));

        SubCategory subCategory = subCategoryRepository.findByName(sub)
                .orElseGet(() -> subCategoryRepository.save(
                        SubCategory.builder().name(sub).middleCategory(middleCategory).build()));


        // 📌 description 1500자 제한 처리
        String safeDescription = dto.getDescription() != null && dto.getDescription().length() > 1500
                ? dto.getDescription().substring(0, 1500)
                : dto.getDescription();



        return BookEntity.builder()
                .bookname(dto.getBookname())
                .authors(dto.getAuthors())
                .publisher(dto.getPublisher())
                .publicationDate(dto.getPublicationDate())
                .isbn13(dto.getIsbn13())
                .description(safeDescription)
                .bookImageURL(dto.getBookImageURL())
                .mainCategory(mainCategory)
                .middleCategory(middleCategory)
                .subCategory(subCategory)
                .build();
    }
}
