package org.example.booknuri.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.converter.RegionConverter;
import org.example.booknuri.domain.library.dto.RegionResponseDTO;
import org.example.booknuri.domain.library.repository.RegionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryRegionService {

    private final RegionRepository regionRepository;
    private final RegionConverter regionConverter;



    //모든 지역조합 dto리스트로 반환
    public ResponseEntity<List<RegionResponseDTO>> getAllRegions() {
        List<RegionResponseDTO> result = regionRepository.findAll().stream()
                .map(regionConverter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
