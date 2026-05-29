package com.ai.login.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ClassificationService {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${groq.api.key}")
    private String grokApiKey;

    public Map<String, Object> classifyDescription(String longDescription) {

        Map<String, Object> result = new HashMap<>();

        try {

            String description = longDescription.trim().toUpperCase();

            String category = "";
            String noun = "";
            String modifier = "";
            String source = "";
            double confidence = 0;

            Map<String, String> properties = new HashMap<>();

            Map<String, Object> dictionaryData = new HashMap<>();

            if (description.contains("BOLT")) {

                dictionaryData.put("category", "FASTENER");
                dictionaryData.put("noun", "BOLT");
                dictionaryData.put("modifier", "HEX HEAD");

            } else if (description.contains("VALVE")) {

                dictionaryData.put("category", "VALVE");
                dictionaryData.put("noun", "VALVE");
                dictionaryData.put("modifier", "GATE");
            }

            if (!dictionaryData.isEmpty()) {

                category = String.valueOf(dictionaryData.get("category"));
                noun = String.valueOf(dictionaryData.get("noun"));
                modifier = String.valueOf(dictionaryData.get("modifier"));

                source = "DICTIONARY";
                confidence = 95;

            } else {

            	try {

            	    String prompt =
            	            "Extract category, noun, modifier and properties " +
            	            "from MRO material description. " +
            	            "Return ONLY valid JSON.\n\n" +
            	            "Description : " + description;

            	    Map<String, Object> request = new HashMap<>();

            	    // FREE MODELS
            	    request.put("model", "llama-3.3-70b-versatile");

            	    request.put("response_format",
            	            Map.of("type", "json_object"));

            	    request.put("messages", Arrays.asList(
            	            Map.of("role", "user", "content", prompt)
            	    ));

            	    HttpHeaders headers = new HttpHeaders();

            	    headers.setContentType(MediaType.APPLICATION_JSON);

            	    

            	    headers.setBearerAuth(grokApiKey);

            	    HttpEntity<Map<String, Object>> entity =
            	            new HttpEntity<>(request, headers);

            	    RestTemplate restTemplate = new RestTemplate();

            	    String baseUrl ="https://api.groq.com/openai/v1";

            	    ResponseEntity<String> response =
            	            restTemplate.exchange(
            	                    baseUrl + "/chat/completions",
            	                    HttpMethod.POST,
            	                    entity,
            	                    String.class
            	            );

            	    String responseBody = response.getBody();

            	    ObjectMapper mapper = new ObjectMapper();

            	    JsonNode root = mapper.readTree(responseBody);

            	    String content =
            	            root.get("choices")
            	                    .get(0)
            	                    .get("message")
            	                    .get("content")
            	                    .asText();

            	    Map<String, Object> aiData =
            	            mapper.readValue(content, Map.class);

            	    if (aiData != null &&
            	            aiData.get("category") != null) {

            	        category =
            	                String.valueOf(aiData.get("category"));

            	        noun =
            	                String.valueOf(aiData.get("noun"));

            	        modifier =
            	                String.valueOf(aiData.get("modifier"));

            	        source = "AI";
            	        confidence = 85;
            	    }

            	} catch (Exception e) {

            	    e.printStackTrace();
            	}


                if (category == null || "".equals(category)) {

                    try {

                        if (description.contains("PUMP")) {

                            category = "ROTARY EQUIPMENT";
                            noun = "PUMP";
                            modifier = "CENTRIFUGAL";

                        } else if (description.contains("GASKET")) {

                            category = "SEALING";
                            noun = "GASKET";
                            modifier = "SPIRAL WOUND";
                        }

                        source = "GOOGLE_SEARCH";
                        confidence = 70;

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }

            // =====================================================
            // STEP 4 : PROPERTY EXTRACTION
            // =====================================================

            if (description.contains("SS")) {
                properties.put("material", "STAINLESS STEEL");
            }

            if (description.contains("CS")) {
                properties.put("material", "CARBON STEEL");
            }

            Pattern sizePattern =
                    Pattern.compile("(M\\d+X\\d+)");

            Matcher sizeMatcher =
                    sizePattern.matcher(description);

            if (sizeMatcher.find()) {

                properties.put(
                        "size",
                        sizeMatcher.group(1)
                );
            }

            Pattern stdPattern =
                    Pattern.compile("(ASTM\\s?[A-Z0-9]+)");

            Matcher stdMatcher =
                    stdPattern.matcher(description);

            if (stdMatcher.find()) {

                properties.put(
                        "standard",
                        stdMatcher.group(1)
                );
            }

            // =====================================================
            // STEP 5 : CONFIDENCE SCORING
            // =====================================================

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

            result.put("status", "FAILED");
            result.put("message", e.getMessage());
        }

        return result;
    }
}
