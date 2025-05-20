package org.example.booknuri.domain.library.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.dto.LibraryResponseDto;
import org.example.booknuri.domain.library.entity.LibraryEntity;
import org.example.booknuri.domain.library.entity.RegionEntity;
import org.example.booknuri.domain.library.repository.RegionRepository;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LibraryConverter {

    private final RegionRepository regionRepository;


    //  DTO → Entity 변환 (주소 기반으로 RegionEntity 연동)
    public LibraryEntity toEntity(LibraryResponseDto dto) {
        RegionEntity region = extractRegion(dto.getFullAddress());

        return LibraryEntity.builder()
                .libCode(dto.getLibCode())
                .libName(dto.getLibName())
                .tel(dto.getTel())
                .fax(dto.getFax())
                .homepage(dto.getHomepage())
                .closed(dto.getClosed())
                .operatingTime(dto.getOperatingTime())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .bookCount(dto.getBookCount())
                .fullAddress(dto.getFullAddress())
                .region(region)
                .build();
    }

    // 📤 Entity → DTO 변환 (region 정보 포함)
    public LibraryResponseDto toDto(LibraryEntity entity) {
        return LibraryResponseDto.builder()
                .libCode(entity.getLibCode())
                .libName(entity.getLibName())
                .fullAddress(entity.getFullAddress())
                .tel(entity.getTel())
                .fax(entity.getFax())
                .homepage(entity.getHomepage())
                .closed(entity.getClosed())
                .operatingTime(entity.getOperatingTime())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .bookCount(entity.getBookCount())
                .si(entity.getRegion() != null ? entity.getRegion().getSi() : null)
                .gu(entity.getRegion() != null ? entity.getRegion().getGu() : null)
                .build();
    }

    //  주소에서 시/구 추출 → RegionEntity 매핑
    private RegionEntity extractRegion(String address) {
        if (address == null || address.isEmpty()) return null;

        String[] parts = address.split(" ");
        String si = parts.length > 0 ? parts[0] : "미상";
        String gu = parts.length > 1 ? parts[1] : "미상";

        return regionRepository.findBySiAndGu(si, gu)
                .orElseGet(() -> regionRepository.save(
                        RegionEntity.builder().si(si).gu(gu).build()));
    }
}
