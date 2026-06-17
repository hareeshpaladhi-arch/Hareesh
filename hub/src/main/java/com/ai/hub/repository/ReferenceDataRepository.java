package com.ai.hub.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.hub.dto.ReferenceData;

@Repository
public interface ReferenceDataRepository
        extends JpaRepository<ReferenceData, Long> {

    Page<ReferenceData> findByBatchId(
            String batchId,
            Pageable pageable);

}	
