package com.ai.login.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.login.DAO.UserFileImportRepository;
import com.ai.login.DTO.BatchTemplate;

@RestController
@RequestMapping("/api")
public class aiDataController {

	@Autowired
	private UserFileImportRepository repository;

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

		List<BatchTemplate> rows = (List<BatchTemplate>) repository.findByBatchId(batchId, null);

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
}
