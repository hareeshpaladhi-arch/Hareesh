package com.ai.hub.controller;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai.hub.service.FileUploadService;
import com.ai.hub.service.ProgressService;
import com.ai.hub.service.apiHubService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ai.hub.batch.config.BatchCounterListener;
import com.ai.hub.dto.BatchJobResult;
import com.ai.hub.dto.CharacteristicData;
import com.ai.hub.dto.DocumentData;
import com.ai.hub.dto.FieldMapping;
import com.ai.hub.dto.ReferenceData;
import com.ai.hub.repository.BatchTemplate;
import com.ai.hub.repository.CharacteristicDataRepository;
import com.ai.hub.repository.DocumentDataRepository;
import com.ai.hub.repository.ReferenceDataRepository;
import com.ai.hub.repository.UserFileImportRepository;

@RestController
@RequestMapping("/api")
public class apiHubController {
	@Autowired
	private UserFileImportRepository repository;

	@Autowired
	private CharacteristicDataRepository charRepo;

	@Autowired
	private ReferenceDataRepository refRepo;

	@Autowired
	private DocumentDataRepository docRepo;

	@Autowired
	private apiHubService apiService;

	@Autowired
	private ProgressService progressService;

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private Job importJob;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private BatchCounterListener counterListener;

	@GetMapping("/batchList")
	public ResponseEntity<List<String>> getBatchList() {

		try {

			List<String> batchList = repository.findDistinctBatchIds();

			return ResponseEntity.ok(batchList);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonList("Error : " + e.getMessage()));
		}
	}

	@GetMapping("/duplicates/{batchId}")
	public ResponseEntity<Map<String, Object>> findDuplicates(@PathVariable String batchId,
			@RequestParam(defaultValue = "70") double accuracy) {

		Page<BatchTemplate> page = repository.findByBatchId(batchId, Pageable.unpaged());

		List<BatchTemplate> rows = page.getContent();

		List<Map<String, Object>> duplicateRows = new ArrayList<>();

		for (int i = 0; i < rows.size(); i++) {

			for (int j = i + 1; j < rows.size(); j++) {

				BatchTemplate row1 = rows.get(i);
				BatchTemplate row2 = rows.get(j);

				String text1 = normalize(row1.getLongDesc());
				String text2 = normalize(row2.getLongDesc());

				double percentage = calculatePercentage(text1, text2);

				if (percentage >= accuracy) {

					Map<String, Object> map = new HashMap<>();

					map.put("row1Id", row1.getId());
					map.put("row2Id", row2.getId());

					map.put("row1Desc", row1.getLongDesc());
					map.put("row2Desc", row2.getLongDesc());

					map.put("matchPercentage", Math.round(percentage));

					duplicateRows.add(map);
				}
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("batchId", batchId);
		response.put("accuracy", accuracy);
		response.put("duplicateCount", duplicateRows.size());
		response.put("duplicates", duplicateRows);

		return ResponseEntity.ok(response);
	}

	private String normalize(String value) {

		if (value == null) {
			return "";
		}

		return value.toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim();
	}

	private double calculatePercentage(String s1, String s2) {

		Set<String> set1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));

		Set<String> set2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));

		Set<String> common = new HashSet<>(set1);
		common.retainAll(set2);

		int max = Math.max(set1.size(), set2.size());

		if (max == 0) {
			return 0;
		}

		return ((double) common.size() / max) * 100;
	}

	@PostMapping("/classify")
	public ResponseEntity<?> classify(@RequestParam String batchId) {

		try {

			Page<BatchTemplate> page = repository.findByBatchId(batchId, Pageable.unpaged());

			List<BatchTemplate> rows = page.getContent();

			List<Map<String, Object>> responseList = new ArrayList<>();

			for (BatchTemplate row : rows) {

				String description = row.getLongDesc();
				String className = row.getClassName();
				if (className == null || "".equalsIgnoreCase(className)) {
					if (description != null && !"".equalsIgnoreCase(description.trim())) {

						Map<String, Object> response = apiService.classifyDescription(description);

						String noun = (String) response.get("noun");
						noun = noun != null && !"".equalsIgnoreCase(noun) && !"null".equalsIgnoreCase(noun) ? noun : "";

						String modifier = (String) response.get("modifier");
						modifier = modifier != null && !"".equalsIgnoreCase(modifier)
								&& !"null".equalsIgnoreCase(modifier) ? modifier : "";

						className = (noun + " " + modifier).trim();

						if ("".equals(className)) {

							row.setClassName("NO OBJECT");

						} else {

							row.setClassName(className);
						}

						repository.save(row);

						CharacteristicData charData = new CharacteristicData();
						charData.setRecordNo(row.getId());
						charData.setClassTerm(row.getClassName());
						charData.setCharacteristicName("TYPE");
						charData.setCharacteristicValue("35 MM");
						charData.setShortDescription(row.getShortDesc());
						charData.setLongDescription(row.getLongDesc());
						charData.setBatchId(batchId);

						charRepo.save(charData);

						responseList.add(response);
					}
				}
			}

			return ResponseEntity.ok(responseList);

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	// ✅ FILE UPLOAD (FIXED)
	@PostMapping("/upload")
	public ResponseEntity<BatchJobResult> upload(@RequestParam("file") MultipartFile file,
			@RequestParam("mappedData") String mappedData) {

		try {

			ObjectMapper mapper = new ObjectMapper();

			List<FieldMapping> mappings = mapper.readValue(mappedData, new TypeReference<List<FieldMapping>>() {
			});

			System.out.println("Mappings: " + mappings);

			String safeCsvPath = fileUploadService.prepareFileForBatch(file);

			String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
			long time = System.currentTimeMillis();
			String batchId = "BATCH_" + time + "_" + random;

			JobParameters params = new JobParametersBuilder().addString("filePath", safeCsvPath)
					.addString("batchId", batchId).addString("mappedData", mappedData) // optional if needed in batch
					.addLong("timestamp", time).toJobParameters();

			JobExecution execution = jobLauncher.run(importJob, params);

			BatchJobResult result = new BatchJobResult(batchId, counterListener.getTotalRead(),
					counterListener.getTotalSaved(), counterListener.getTotalSkipped(),
					execution.getStatus().toString(), "Batch completed successfully");

			return ResponseEntity.ok(result);

		} catch (Exception e) {

			BatchJobResult error = new BatchJobResult("N/A", 0, 0, 0, "FAILED", "Upload failed: " + e.getMessage());

			return ResponseEntity.status(500).body(error);
		}
	}

	// ✅ ERROR DOWNLOAD
	@GetMapping("/errors")
	public ResponseEntity<InputStreamResource> downloadError() throws Exception {

		File file = new File("uploads/error.csv");

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error.csv")
				.body(new InputStreamResource(new FileInputStream(file)));
	}

	@GetMapping("/batch/{batchId}")
	public Map<String, Object> getData(@PathVariable String batchId, @RequestParam(defaultValue = "") String tableId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "") String search, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {

		sortBy = (sortBy != null && !sortBy.trim().isEmpty() && !"null".equalsIgnoreCase(sortBy)) ? sortBy : "id";

		Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<?> pageData;

		if (tableId != null && tableId.toLowerCase().contains("char")) {

			if (batchId != null && "NULL".equalsIgnoreCase(batchId)) {
				pageData = charRepo.findAll(pageable);
			} else {
				pageData = charRepo.findByBatchId(batchId, pageable);
			}

		} else if (tableId != null && tableId.toLowerCase().contains("ref")) {
			if (batchId != null && "NULL".equalsIgnoreCase(batchId)) {
				pageData = refRepo.findAll(pageable);
			} else {
				pageData = refRepo.findByBatchId(batchId, pageable);
			}

		} else if (tableId != null && tableId.toLowerCase().contains("doc")) {
			if (batchId != null && "NULL".equalsIgnoreCase(batchId)) {
				pageData = docRepo.findAll(pageable);
			} else {
				pageData = docRepo.findByBatchId(batchId, pageable);
			}

		} else if (search != null && !search.trim().isEmpty()) {

			pageData = apiService.searchByBatchId(batchId, search, pageable);

		} else {

			pageData = apiService.getByBatchId(batchId, pageable);
		}

		List<?> dataList = pageData.getContent();

		List<Map<String, Object>> columns = new ArrayList<>();

		if (dataList != null && !dataList.isEmpty()) {

			Object firstRecord = dataList.get(0);

			Field[] fields = firstRecord.getClass().getDeclaredFields();

			for (Field field : fields) {

				Map<String, Object> col = new HashMap<>();
				col.put("data", field.getName());

				if ("longDesc".equalsIgnoreCase(field.getName()) || field.getName().contains("long")) {
					col.put("type", "textarea");
					col.put("width", "250px");
				} else {
					col.put("type", "text");
				}

				columns.add(col);
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("columns", columns);
		response.put("data", dataList);
		response.put("totalRecords", pageData.getTotalElements());

		return response;
	}

	@Autowired
	private ObjectMapper objectMapper;

	@PostMapping("/updateRecords")
	public ResponseEntity<String> updateRecords(
	        @RequestBody List<Map<String, Object>> records,
	        @RequestParam String tableId) {

		String table = tableId.toLowerCase();

		if (table.contains("ref")) {

		    List<ReferenceData> referenceRecords = records.stream()
		            .map(record -> objectMapper.convertValue(record, ReferenceData.class))
		            .collect(Collectors.toList());

		    for (ReferenceData record : referenceRecords) {
		        ReferenceData existing = refRepo.findById(record.getId())
		                .orElseThrow(() -> new RuntimeException("Record not found"));

		        existing.setClassTerm(record.getClassTerm());
		        existing.setShortDescription(record.getShortDescription());
		        existing.setLongDescription(record.getLongDescription());

		        refRepo.save(existing);
		    }

		} else if (table.contains("doc")) {

		    List<DocumentData> documentRecords = records.stream()
		            .map(record -> objectMapper.convertValue(record, DocumentData.class))
		            .collect(Collectors.toList());

		    for (DocumentData record : documentRecords) {
		        DocumentData existing = docRepo.findById(record.getId())
		                .orElseThrow(() -> new RuntimeException("Record not found"));

		        existing.setDocumentName(record.getDocumentName());
		        existing.setShortDescription(record.getShortDescription());

		        docRepo.save(existing);
		    }

		} else if (table.contains("char")) {

		    List<CharacteristicData> characteristicRecords = records.stream()
		            .map(record -> objectMapper.convertValue(record, CharacteristicData.class))
		            .collect(Collectors.toList());

		    for (CharacteristicData record : characteristicRecords) {
		        CharacteristicData existing = charRepo.findById(record.getId())
		                .orElseThrow(() -> new RuntimeException("Record not found"));

		        existing.setClassTerm(record.getClassTerm());
		        existing.setCharacteristicName(record.getCharacteristicName());
		        existing.setCharacteristicValue(record.getCharacteristicValue());
		        existing.setShortDescription(record.getShortDescription());
		        existing.setLongDescription(record.getLongDescription());

		        charRepo.save(existing);
		    }

		} else {

		    List<BatchTemplate> batchRecords = records.stream()
		            .map(record -> objectMapper.convertValue(record, BatchTemplate.class))
		            .collect(Collectors.toList());

		    for (BatchTemplate record : batchRecords) {
		        BatchTemplate existing = repository.findById(record.getId())
		                .orElseThrow(() -> new RuntimeException("Record not found"));

		        existing.setClassName(record.getClassName());
		        existing.setMaterialType(record.getMaterialType());
		        existing.setShortDesc(record.getShortDesc());
		        existing.setLongDesc(record.getLongDesc());

		        repository.save(existing);
		    }
		}

	    return ResponseEntity.ok(records.size() + " record(s) updated successfully.");
	}	

	@PostMapping("/deleteRecords")
	public ResponseEntity<String> deleteRecords(@RequestBody List<String> ids) {

		int deletedCount = apiService.deleteRecords(ids);
		return ResponseEntity.ok(deletedCount + " record(s) deleted successfully.");
	}

}
