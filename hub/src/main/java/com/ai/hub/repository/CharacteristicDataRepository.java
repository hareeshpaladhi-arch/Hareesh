package com.ai.hub.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.hub.dto.CharacteristicData;

@Repository
public interface CharacteristicDataRepository
        extends JpaRepository<CharacteristicData, Long> {

    Page<CharacteristicData> findByBatchId(
            String batchId,
            Pageable pageable);

}
