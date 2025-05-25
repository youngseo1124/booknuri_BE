package org.example.booknuri.domain.library.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.converter.RegionConverter;
import org.example.booknuri.domain.library.dto.RegionResponseDTO;
import org.example.booknuri.domain.library.repository.RegionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryRegionService {

    private final RegionRepository regionRepository;
    private final RegionConverter regionConverter;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;



    public ResponseEntity<List<RegionResponseDTO>> getAllRegions() {
        String key = "library:region:all";

        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                List<RegionResponseDTO> cachedList = objectMapper.readValue(cached, new TypeReference<>() {});
                return ResponseEntity.ok(cachedList);
            }

            List<RegionResponseDTO> result = regionRepository.findAll().stream()
                    .map(regionConverter::toDto)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // 혹시 캐싱 실패해도 걍 DB 결과만 반환 (실패 무시해도 OK)
            e.printStackTrace();

            List<RegionResponseDTO> result = regionRepository.findAll().stream()
                    .map(regionConverter::toDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        }
    }


}

