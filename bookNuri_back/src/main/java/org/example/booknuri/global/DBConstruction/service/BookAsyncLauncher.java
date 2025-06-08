package org.example.booknuri.global.DBConstruction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.domain.book.repository.BookRepository;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.repository.LibraryBookRepository;
import org.example.booknuri.domain.library.repository.LibraryRepository;
import org.example.booknuri.domain.library.service.LibraryService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookAsyncLauncher {

    private final LibraryRepository libraryRepository;
    private final LibraryProcessorService processorService;


    @Async
    public void launchDaeguSaveJob() {
        List<LibraryEntity> daeguLibraries = libraryRepository.findByRegion_Si("충청남도");

        //나중에 주석처리
        daeguLibraries = daeguLibraries.stream()
                .filter(lib -> lib.getBookCount() != null) //  null인 도서관 제외!
                .sorted(Comparator.comparingInt(LibraryEntity::getBookCount)) // 도서 수 오름차순
                .skip(45) // 상위 10개 건너뛰고
                .limit(9) // 다음 10개 가져오기
                .toList();



        log.info(" 도서관 수: {}", daeguLibraries.size());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (LibraryEntity lib : daeguLibraries) {
            log.info("🔥 비동기 호출됨: {}", lib.getLibName());            futures.add(processorService.processLibraryBooksAsync(lib));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
