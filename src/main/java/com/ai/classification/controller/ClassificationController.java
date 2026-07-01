package com.ai.classification.controller;

import com.ai.classification.dto.ClassificationRequest;
import com.ai.classification.dto.ClassificationResponse;
import com.ai.classification.loader.ExcelDataLoader;
import com.ai.classification.service.ClassificationService;
import com.ai.hub.repository.BatchTemplate;
import com.ai.hub.repository.UserFileImportRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;



@RestController
public class ClassificationController {
	@Autowired
	private UserFileImportRepository repository;

    private final ClassificationService classificationService;
    private final ExcelDataLoader excelDataLoader;

    public ClassificationController(ClassificationService classificationService,
                                     ExcelDataLoader excelDataLoader) {
        this.classificationService = classificationService;
        this.excelDataLoader = excelDataLoader;
    }

    @PostMapping("/api/classification")
    public ResponseEntity<Map<String, Object>> classify(@RequestParam String batchId) {

        List<BatchTemplate> rows = repository.findByBatchId(batchId, Pageable.unpaged()).getContent();

        List<BatchTemplate> updatedRows = new ArrayList<>();

        int totalCount = rows.size();
        int predictedCount = 0;

        for (BatchTemplate row : rows) {

            if (row.getClassName() == null || row.getClassName().trim().isEmpty()) {

                String description = row.getLongDesc();

                if (description != null && !description.trim().isEmpty()) {

                    String predictedClass = classificationService.totalClassProcedure(description);

                    if (predictedClass != null && !predictedClass.trim().isEmpty()) {
                        row.setClassName(predictedClass);
                        updatedRows.add(row);
                        predictedCount++;
                    }
                }
            }
        }
        repository.saveAll(updatedRows);

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", totalCount);
        response.put("predictedRecords", predictedCount);
        response.put("remainingRecords", totalCount - predictedCount);

        return ResponseEntity.ok(response);
    }

   

    /**
     * Re-imports the reference workbooks from the configured data directory and refreshes
     * the in-memory caches, without restarting the application.
     */
    @PostMapping("/api/reference-data/reload")
    public ResponseEntity<String> reload() {
        excelDataLoader.loadAllFromRepositories();
        classificationService.refreshCache();
        return ResponseEntity.ok("Reference data reloaded");
    }

    @GetMapping("/api/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
