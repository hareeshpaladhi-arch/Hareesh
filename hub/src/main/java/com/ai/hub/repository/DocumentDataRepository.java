package com.ai.hub.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.hub.dto.DocumentData;

@Repository
public interface DocumentDataRepository
        extends JpaRepository<DocumentData, Long> {

    Page<DocumentData> findByBatchId(
            String batchId,
            Pageable pageable);

}
