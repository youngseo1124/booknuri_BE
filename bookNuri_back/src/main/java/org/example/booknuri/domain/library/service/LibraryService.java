package org.example.booknuri.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.converter.LibraryConverter;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.repository.LibraryRepository;
import org.example.booknuri.global.DBConstruction.client.LibraryApiClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final LibraryApiClient apiClient;
    private final LibraryConverter libraryConverter;



   //전체도서관 리스트 페이지네이션 목록+ (도서관 제목 검색 )
   public ResponseEntity<List<LibraryResponseDto>> getAllLibrariesPaged(int offset, int limit, String keyword) {
       PageRequest pageRequest = PageRequest.of(offset / limit, limit);
       Page<LibraryEntity> page;

       if (keyword != null && !keyword.isBlank()) {
           page = libraryRepository.findByLibNameContainingIgnoreCase(keyword, pageRequest);
       } else {
           page = libraryRepository.findAll(pageRequest);
       }

       List<LibraryResponseDto> result = page.getContent().stream()
               .map(libraryConverter::toDto)
               .collect(Collectors.toList());

       return ResponseEntity.ok(result);
   }




    //페이지네이션+ // 지역만 선택한 경우 (예: '대구광역시')+ (도서관 이름 검색)
    public ResponseEntity<List<LibraryResponseDto>> getLibrariesBySiPaged(String si, int offset, int limit, String keyword) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        Page<LibraryEntity> page;

        if (keyword != null && !keyword.isBlank()) {
            page = libraryRepository.findByRegion_SiAndLibNameContainingIgnoreCase(si, keyword, pageRequest);
        } else {
            page = libraryRepository.findByRegion_Si(si, pageRequest);
        }

        List<LibraryResponseDto> result = page.getContent().stream()
                .map(libraryConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    //페이지네이션+   // 지역 + 시/군/구 모두 선택한 경우 (예: '대구광역시', '달서구')+(도서관 이름 검색)
    public ResponseEntity<List<LibraryResponseDto>> getLibrariesBySiAndGuPaged(String si, String gu, int offset, int limit, String keyword) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        Page<LibraryEntity> page;

        if (keyword != null && !keyword.isBlank()) {
            page = libraryRepository.findByRegion_SiAndRegion_GuAndLibNameContainingIgnoreCase(si, gu, keyword, pageRequest);
        } else {
            page = libraryRepository.findByRegion_SiAndRegion_Gu(si, gu, pageRequest);
        }

        List<LibraryResponseDto> result = page.getContent().stream()
                .map(libraryConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    //페이지네이션+  전체도서관 대상으로 도서관 이름으로 검색
    public ResponseEntity<List<LibraryResponseDto>> searchByLibraryName(String keyword, int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        Page<LibraryEntity> page = libraryRepository.findByLibNameContainingIgnoreCase(keyword, pageRequest);

        List<LibraryResponseDto> result = page.getContent().stream()
                .map(libraryConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }





}
