package org.example.booknuri.domain.library.converter;

import org.example.booknuri.domain.library.dto.RegionResponseDTO;
import org.example.booknuri.domain.library.entity.RegionEntity;
import org.springframework.stereotype.Component;

@Component
public class RegionConverter {

    public RegionResponseDTO toDto(RegionEntity entity) {
        return RegionResponseDTO.builder()
                .si(entity.getSi())
                .gu(entity.getGu())
                .build();
    }
}
