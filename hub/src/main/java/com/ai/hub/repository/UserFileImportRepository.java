package com.ai.hub.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface UserFileImportRepository extends JpaRepository<BatchTemplate, String> {
	Page<BatchTemplate> findByBatchId(String batchId, Pageable pageable);

    @Query("SELECT b FROM BatchTemplate b " +
           "WHERE b.batchId = :batchId AND (" +
           "LOWER(b.className) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.shortDesc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.longDesc) LIKE LOWER(CONCAT('%', :search, '%')) )")
    Page<BatchTemplate> searchByBatchId(
            @Param("batchId") String batchId,
            @Param("search") String search,
            Pageable pageable);
	@Query("SELECT DISTINCT b.batchId FROM BatchTemplate b")
    List<String> findDistinctBatchIds();
	
}
