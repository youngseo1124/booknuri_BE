package org.example.booknuri.global.DBConstruction.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.book.converter.BookInfoConverter;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.global.DBConstruction.client.LibraryApiClient;
import org.example.booknuri.domain.library.converter.LibraryConverter;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.example.booknuri.domain.library.repository.LibraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional
public class DbConstructionService {

    private final LibraryRepository libraryRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LibraryApiClient apiClient;
    private final LibraryConverter libraryConverter;
    private final BookRepository bookRepository;
    private final BookInfoConverter bookConverter;
    private final LibraryProcessorService processorService;


    // 전국 도서관 정보 저장
    // 도서관정보 api응답받은 dto db에 libraries에  저장하는 로직
    public void saveAllLibrariesFromApi() {

        List<LibraryResponseDto> dtos = apiClient.fetchLibrariesFromApi();

        for (LibraryResponseDto dto : dtos) {
            LibraryEntity entity = libraryConverter.toEntity(dto);
            libraryRepository.save(entity);
        }
    }

    //대구광역시 도서관들의 도서 정보 저장 (BookEntity + LibraryBookEntity)
    // 요청 받으면 백그라운드에서 실행할 메서드
    public void saveBooksForDaeguLibrariesAsync() {
        List<LibraryEntity> daeguLibraries = libraryRepository.findByRegion_Si("대구광역시");


        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (LibraryEntity lib : daeguLibraries) {
            futures.add(processorService.processLibraryBooksAsync(lib)); //  이거 Spring이 비동기로 실행
        }

    }
}













