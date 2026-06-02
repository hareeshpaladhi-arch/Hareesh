package com.ai.hub.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ai.hub.dto.ClassificationMaster;
import com.ai.hub.repository.BatchTemplate;
import com.ai.hub.repository.ClassificationMasterRepository;
import com.ai.hub.repository.UserFileImportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class apiHubService {
	@Autowired
	private RestTemplate restTemplate;
	@Value("${groq.api.key}")
	private String grokApiKey;

	@Autowired
	private ClassificationMasterRepository classMasterRepo;
	
	@Autowired
    private UserFileImportRepository fileRepo;

	public Map<String, Object> classifyDescription(String longDescription) {

		Map<String, Object> result = new HashMap<>();

		try {

			String description = longDescription != null ? longDescription.trim().toUpperCase() : "";

			if (description.isEmpty()) {

				result.put("status", "FAILED");
				result.put("message", "Description is empty");

				return result;
			}

			String category = "";
			String noun = "";
			String modifier = "";
			String source = "";
			double confidence = 0;

			Map<String, String> properties = new HashMap<>();

			Optional<ClassificationMaster> masterData = classMasterRepo.findByLongdescriptionIgnoreCase(description);
			if (masterData.isPresent()) {

				ClassificationMaster data = masterData.get();

				if (data != null) {

					noun = data.getNoun() != null ? data.getNoun() : "";

					modifier = data.getModifier() != null ? data.getModifier() : "";

					category = data.getClassName() != null ? data.getClassName() : "";

					source = "DATABASE";
					confidence = 100;
				}

			} else {

				try {

					String prompt = "Extract category, noun, modifier, properties " + "from MRO material description.\n"
							+ "Return ONLY valid JSON.\n\n" + "Example JSON:\n" + "{\n"
							+ "  \"category\":\"FASTENER\",\n" + "  \"noun\":\"BOLT\",\n"
							+ "  \"modifier\":\"HEX HEAD\"\n" + "}\n\n" + "Description : " + description;

					Map<String, Object> request = new HashMap<>();

					request.put("model", "llama-3.3-70b-versatile");

					request.put("response_format", Map.of("type", "json_object"));

					request.put("messages", Arrays.asList(Map.of("role", "user", "content", prompt)));

					HttpHeaders headers = new HttpHeaders();

					headers.setContentType(MediaType.APPLICATION_JSON);

					headers.setBearerAuth(grokApiKey);

					HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

					RestTemplate restTemplate = new RestTemplate();

					String baseUrl = "https://api.groq.com/openai/v1";

					ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/chat/completions",
							HttpMethod.POST, entity, String.class);

					String responseBody = response.getBody();

					ObjectMapper mapper = new ObjectMapper();

					JsonNode root = mapper.readTree(responseBody);

					String content = root.get("choices").get(0).get("message").get("content").asText();

					Map<String, Object> aiData = mapper.readValue(content, Map.class);

					category = aiData.get("category") != null ? aiData.get("category").toString().trim() : "";

					noun = aiData.get("noun") != null ? aiData.get("noun").toString().trim() : "";

					modifier = aiData.get("modifier") != null ? aiData.get("modifier").toString().trim() : "";

					// REMOVE NULL STRING

					if ("null".equalsIgnoreCase(noun)) {
						noun = "";
					}

					if ("null".equalsIgnoreCase(modifier)) {
						modifier = "";
					}

					if ("null".equalsIgnoreCase(category)) {
						category = "";
					}

					source = "AI";
					confidence = 85;

				} catch (Exception e) {

					e.printStackTrace();

					result.put("status", "FAILED");
					result.put("message", e.getMessage());

					return result;
				}

				String className = (noun + " " + modifier).trim();

				if (className.isEmpty()) {

					className = "NO OBJECT";
				}

				try {

					ClassificationMaster saveObj = new ClassificationMaster();

					saveObj.setClassName(className);

					saveObj.setShortdescription(
							description.length() > 250 ? description.substring(0, 250) : description);

					saveObj.setLongdescription(description);

					saveObj.setNoun(noun);

					saveObj.setModifier(modifier);

					classMasterRepo.save(saveObj);

				} catch (Exception e) {

					// duplicate ignore
					e.printStackTrace();
				}
			}

			Pattern materialPattern = Pattern.compile("\\b(SS|CS|MS|CI)\\b");

			Matcher materialMatcher = materialPattern.matcher(description);

			if (materialMatcher.find()) {

				String materialCode = materialMatcher.group(1);

				properties.put("material", materialCode);
			}

			Pattern sizePattern = Pattern.compile("(M\\d+X\\d+|\\d+MM|\\d+IN)");

			Matcher sizeMatcher = sizePattern.matcher(description);

			if (sizeMatcher.find()) {

				properties.put("size", sizeMatcher.group(1));
			}

			Pattern stdPattern = Pattern.compile("(ASTM\\s?[A-Z0-9]+)");

			Matcher stdMatcher = stdPattern.matcher(description);

			if (stdMatcher.find()) {

				properties.put("standard", stdMatcher.group(1));
			}

			String reviewStatus = "";

			if (confidence >= 90) {

				reviewStatus = "AUTO_APPROVED";

			} else if (confidence >= 70) {

				reviewStatus = "REVIEW_REQUIRED";

			} else {

				reviewStatus = "MANUAL_CHECK";
			}

			result.put("inputDescription", longDescription);

			result.put("category", category);

			result.put("noun", noun);

			result.put("modifier", modifier);

			result.put("properties", properties);

			result.put("confidence", confidence);

			result.put("source", source);

			result.put("reviewStatus", reviewStatus);

			result.put("status", "SUCCESS");

		} catch (Exception e) {

			e.printStackTrace();

			result.put("status", "FAILED");

			result.put("message", e.getMessage());
		}

		return result;
	}
	 public Page<BatchTemplate> getByBatchId(String batchId, Pageable pageable) {
	        return fileRepo.findByBatchId(batchId, pageable);
	    }

	    public Page<BatchTemplate> searchByBatchId(String batchId, String search, Pageable pageable) {
	        return fileRepo.searchByBatchId(batchId, search, pageable);
	    }
	    
}
