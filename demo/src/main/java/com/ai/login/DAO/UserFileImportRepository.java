package com.ai.login.DAO;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.login.DTO.BatchTemplate;
import java.util.List;


@Repository
public interface UserFileImportRepository extends JpaRepository<BatchTemplate, String> {
	List<BatchTemplate> findByBatchId(String batchId);
}