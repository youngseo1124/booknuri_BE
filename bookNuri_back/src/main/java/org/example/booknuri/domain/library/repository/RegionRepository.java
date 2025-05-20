package org.example.booknuri.domain.library.repository;

import org.example.booknuri.domain.library.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Long> {
    Optional<RegionEntity> findBySiAndGu(String si, String gu);
}