package com.ai.hub.batch.config;



import com.ai.hub.repository.BatchTemplate;
import com.ai.hub.repository.UserFileImportRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired private JobBuilderFactory        jobBuilderFactory;
    @Autowired private StepBuilderFactory       stepBuilderFactory;
    @Autowired private UserFileImportRepository repository;
    @Autowired private BatchCounterListener     counterListener;

    // ── Reader ────────────────────────────────────────────────────────────
    @Bean
    @StepScope
    public ListItemReader<BatchTemplate> reader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        List<BatchTemplate> records = Collections.emptyList();

        try (Reader fileReader = Files.newBufferedReader(
                Paths.get(filePath), StandardCharsets.UTF_8)) {

            CsvToBean<BatchTemplate> csvToBean =
                new CsvToBeanBuilder<BatchTemplate>(fileReader)
                    .withType(BatchTemplate.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .withSeparator('\t')
                    .build();

            records = csvToBean.parse();

        } catch (Exception e) {
            System.err.println("Failed to parse file: " + filePath
                               + " — " + e.getMessage());
        }

        return new ListItemReader<>(records);
    }

    // ── Processor (batchId injected from job parameter) ───────────────────
    @Bean
    @StepScope
    public ItemProcessor<BatchTemplate, BatchTemplate> processor(
            @Value("#{jobParameters['batchId']}") String batchId) {  // ← NEW

        return dto -> {
            if (dto == null || dto.getId() == null || dto.getId().isBlank()) {
                return null; // skip invalid rows
            }

            BatchTemplate entity = new BatchTemplate();
            entity.setId(dto.getId().trim());
            entity.setClassName(dto.getClassName());
            entity.setShortDesc(dto.getShortDesc());
            entity.setLongDesc(dto.getLongDesc());
            entity.setMaterialType(dto.getMaterialType());
            entity.setUnspc(dto.getUnspc());

            // ── Audit fields ─────────────────────────────────────────────
            entity.setBatchId(batchId);                  // ← set per upload
            entity.setCreatedBy("system");
            entity.setCreatedDate(LocalDateTime.now());

            return entity;
        };
    }

    // ── Writer ────────────────────────────────────────────────────────────
    @Bean
    public ItemWriter<BatchTemplate> writer() {
        return items -> repository.saveAll(items);
    }

    // ── Step ──────────────────────────────────────────────────────────────
    @Bean
    public Step step1(
            ItemReader<BatchTemplate> reader,
            ItemProcessor<BatchTemplate, BatchTemplate> processor,
            ItemWriter<BatchTemplate> writer) {

        return ((SimpleStepBuilder<BatchTemplate, BatchTemplate>) stepBuilderFactory.get("step1")
                .<BatchTemplate, BatchTemplate>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener((StepExecutionListener) counterListener))
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    // ── Job ───────────────────────────────────────────────────────────────
    @Bean
    public Job importJob(Step step1) {
        return jobBuilderFactory.get("importJob")
                .incrementer(new RunIdIncrementer())
                .listener((JobExecutionListener) counterListener)
                .start(step1)
                .build();
    }
}
