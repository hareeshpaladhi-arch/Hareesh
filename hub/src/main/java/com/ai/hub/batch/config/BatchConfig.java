package com.ai.hub.batch.config;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ai.hub.batch.util.ExcelItemReader;
import com.ai.hub.repository.BatchTemplate;
import com.ai.hub.repository.UserFileImportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private UserFileImportRepository repository;

    @Autowired
    private BatchCounterListener counterListener;

    private final ObjectMapper mapper = new ObjectMapper();

    // ==========================
    // PROCESSOR
    // ==========================
    @Bean
    @StepScope
    public ItemProcessor<Map<String, String>, BatchTemplate> processor(
            @Value("#{jobParameters['batchId']}") String batchId,
            @Value("#{jobParameters['mappedData']}") String mappedDataJson) {

        return row -> {

            try {

                List<FieldMapping> mappings = mapper.readValue(
                        mappedDataJson,
                        new TypeReference<List<FieldMapping>>() {}
                );

                Map<String, String> normalizedRow = normalizeRow(row);

                BatchTemplate entity = new BatchTemplate();

                // 🔥 ALWAYS SET ID (CRITICAL FIX)
                entity.setId(java.util.UUID.randomUUID().toString());

                for (FieldMapping m : mappings) {

                    String source = normalize(m.getSourceField());
                    String target = m.getTargetField();
                    target=target.toLowerCase();
                    String value = normalizedRow.get(target);
                    System.out.println("MAP => " + source + " -> " + target + " = " + value);

                    setField(entity, target, value);
                }

                entity.setBatchId(batchId);
                entity.setCreatedBy("system");
                entity.setCreatedDate(LocalDateTime.now());

                return entity;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    // ==========================
    // NORMALIZE ROW (FIXED NBSP + SPACES)
    // ==========================
    private Map<String, String> normalizeRow(Map<String, String> row) {

        Map<String, String> map = new LinkedHashMap<>();

        if (row == null || row.isEmpty()) {
            return map;
        }

        for (Map.Entry<String, String> e : row.entrySet()) {

            // -------------------------
            // KEY HANDLING
            // -------------------------
            String rawKey = e.getKey();
            if (rawKey == null) {
                continue;
            }

            String key = normalize(rawKey).trim();

            // -------------------------
            // VALUE HANDLING
            // -------------------------
            String value = e.getValue();

            if (value != null) {

                value = value.trim();

                // Fix encoding issues (common Excel/API corruption)
                value = value
                        .replace("â", "-")
                        .replace("â€“", "–")
                        .replace("â€”", "—")
                        .replace("â€", "\"")
                        .replace("Â", "");

                // Normalize multiple spaces
                value = value.replaceAll("\\s+", " ");
            }

            map.put(key, value);
        }

        return map;
    }

    // ==========================
    // SAFE NORMALIZER (IMPORTANT FIX)
    // ==========================
    private String normalize(String value) {

    	 if (value == null) return null;

    	    // Step 1: Trim early (safe cleanup)
    	    value = value.trim();

    	    // Step 2: Fix common encoding corruption (Excel/API/CSV issues)
    	    value = value
    	            .replace("â", "-")
    	            .replace("â€“", "-")
    	            .replace("â€”", "-")
    	            .replace("â€", "\"")
    	            .replace("Â", "")
    	            .replace("â‚¬", "€")
    	            .replace("â€™", "'")
    	            .replace("â€œ", "\"")
    	            .replace("â€�", "\"");

    	    // Step 3: Remove hidden / special whitespace characters
    	    value = value
    	            .replace("\u00A0", " ")   // NBSP (Excel)
    	            .replace("\u200B", "")    // zero-width space
    	            .replace("\u200C", "")    // zero-width non-joiner
    	            .replace("\u200D", "");   // zero-width joiner

    	    // Step 4: Normalize spaces
    	    value = value.replaceAll("\\s+", " ");

    	    // Step 5: Safe lowercase (optional — remove if case-sensitive system)
    	    value = value.toLowerCase();

    	    return value;
    }

    // ==========================
    // REFLECTION FIELD SETTER
    // ==========================
    private void setField(Object obj, String fieldName, String value) {

        if (fieldName == null) return;

        try {

            Field field = obj.getClass().getDeclaredField(toCamel(fieldName));
            field.setAccessible(true);
            field.set(obj, value);

        } catch (Exception e) {
            System.out.println("⚠ No field: " + fieldName);
        }
    }

    // LONG_DESC → longDesc
    private String toCamel(String input) {

        if (input == null) return null;

        StringBuilder sb = new StringBuilder();
        boolean upper = false;

        for (char c : input.toLowerCase().toCharArray()) {

            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }

        return sb.toString();
    }

    // ==========================
    // WRITER
    // ==========================
    @Bean
    public ItemWriter<BatchTemplate> writer() {

        return items -> {

            System.out.println("WRITER SIZE = " + items.size());

            for (BatchTemplate i : items) {

                System.out.println("SAVE => ID=" + i.getId()
                        + ", LONG_DESC=" + i.getLongDesc());
            }

            repository.saveAll(items);
        };
    }

    // ==========================
    // STEP
    // ==========================
    @Bean
    public Step step1(
            ExcelItemReader reader,
            ItemProcessor<Map<String, String>, BatchTemplate> processor,
            ItemWriter<BatchTemplate> writer) {

        return ((SimpleStepBuilder<Map<String, String>, BatchTemplate>) stepBuilderFactory
                .get("step1")
                .<Map<String, String>, BatchTemplate>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener((StepExecutionListener) counterListener))
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    // ==========================
    // JOB
    // ==========================
    @Bean
    public Job importJob(Step step1) {

        return jobBuilderFactory.get("importJob")
                .incrementer(new RunIdIncrementer())
                .listener((JobExecutionListener) counterListener)
                .start(step1)
                .build();
    }

    // ==========================
    // FIELD MAPPING CLASS
    // ==========================
    public static class FieldMapping {

        private String sourceField;
        private String targetField;

        public String getSourceField() {
            return sourceField;
        }

        public void setSourceField(String sourceField) {
            this.sourceField = sourceField;
        }

        public String getTargetField() {
            return targetField;
        }

        public void setTargetField(String targetField) {
            this.targetField = targetField;
        }
    }
}