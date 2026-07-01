package com.ai.classification.loader;

import com.ai.classification.entity.AlterWord;
import com.ai.classification.entity.Exclusion;
import com.ai.classification.entity.FirstPref;
import com.ai.classification.entity.LastPref;
import com.ai.classification.entity.SecondPref;
import com.ai.classification.entity.ThirdPref;
import com.ai.classification.repository.AlterWordRepository;
import com.ai.classification.repository.ExclusionRepository;
import com.ai.classification.repository.FirstPrefRepository;
import com.ai.classification.repository.LastPrefRepository;
import com.ai.classification.repository.SecondPrefRepository;
import com.ai.classification.repository.ThirdPrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads reference lookup data directly from the database repositories into memory
 * upon application startup, providing a fast lookup cache for ClassificationService.
 */
@Component
public class ExcelDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExcelDataLoader.class);

    private final ThirdPrefRepository thirdPrefRepository;
    private final SecondPrefRepository secondPrefRepository;
    private final FirstPrefRepository firstPrefRepository;
    private final LastPrefRepository lastPrefRepository;
    private final AlterWordRepository alterWordRepository;
    private final ExclusionRepository exclusionRepository;

    // In-memory caches to be consumed by ClassificationService
    private List<Object[]> thirdPrefs = Collections.synchronizedList(new ArrayList<>());
    private List<Object[]> secondPrefs = Collections.synchronizedList(new ArrayList<>());
    private List<Object[]> firstPrefs = Collections.synchronizedList(new ArrayList<>());
    private List<Object[]> lastPrefs = Collections.synchronizedList(new ArrayList<>());
    private Map<String, String> alterWordsMap = Collections.synchronizedMap(Collections.emptyMap());
    private List<Exclusion> exclusions = Collections.synchronizedList(new ArrayList<>());

    public ExcelDataLoader(ThirdPrefRepository thirdPrefRepository,
                               SecondPrefRepository secondPrefRepository,
                               FirstPrefRepository firstPrefRepository,
                               LastPrefRepository lastPrefRepository,
                               AlterWordRepository alterWordRepository,
                               ExclusionRepository exclusionRepository) {
        this.thirdPrefRepository = thirdPrefRepository;
        this.secondPrefRepository = secondPrefRepository;
        this.firstPrefRepository = firstPrefRepository;
        this.lastPrefRepository = lastPrefRepository;
        this.alterWordRepository = alterWordRepository;
        this.exclusionRepository = exclusionRepository;
    }

    @Override
    public void run(String... args) {
        loadAllFromRepositories();
    }

    /**
     * Fetches reference data from the underlying database repositories and caches it in memory.
     */
    public synchronized void loadAllFromRepositories() {
        log.info("Starting lookups migration/load from database repositories...");
        try {
            // 1. Load Preference Tables
            this.thirdPrefs = thirdPrefRepository.findAllData();
            log.info("Cached {} ThirdPref rows from database.", thirdPrefs.size());

            this.secondPrefs = secondPrefRepository.findAllData();
            log.info("Cached {} SecondPref rows from database.", secondPrefs.size());

            this.firstPrefs = firstPrefRepository.findAllData();
            log.info("Cached {} FirstPref rows from database.", firstPrefs.size());

            this.lastPrefs = lastPrefRepository.findAllData();
            log.info("Cached {} LastPref rows from database.", lastPrefs.size());

            // 2. Load Alter Words (optimized directly into a key-value Map for fast lookups)
            List<AlterWord> rawAlterWords = alterWordRepository.findAll();
            this.alterWordsMap = rawAlterWords.stream()
                    .filter(aw -> aw.getWord() != null && aw.getAlterWord() != null)
                    .collect(Collectors.toMap(
                            aw -> aw.getWord().trim().toUpperCase(),
                            aw -> aw.getAlterWord().trim().toUpperCase(),
                            (existing, replacement) -> replacement // Keep latest if duplicates exist
                    ));
            log.info("Cached {} AlterWord pairs into lookup map.", alterWordsMap.size());

            // 3. Load Exclusions
            this.exclusions = exclusionRepository.findAll();
            log.info("Cached {} Exclusion rows from database.", exclusions.size());

            if (thirdPrefs.isEmpty() && secondPrefs.isEmpty() && firstPrefs.isEmpty()) {
                log.warn("Database reference tables appear to be completely empty! " +
                         "Classification fallbacks will trigger until these tables are populated.");
            }

        } catch (Exception e) {
            log.error("Fatal exception encountered while caching reference data from repositories", e);
        }
    }

    // ---- Cache Getters for ClassificationService ----

    public List<Object[]> getThirdPrefs() {
        return thirdPrefs;
    }

    public List<Object[]> getSecondPrefs() {
        return secondPrefs;
    }

    public List<Object[]> getFirstPrefs() {
        return firstPrefs;
    }

    public List<Object[]> getLastPrefs() {
        return lastPrefs;
    }

    public Map<String, String> getAlterWordsMap() {
        return alterWordsMap;
    }

    public List<Exclusion> getExclusions() {
        return exclusions;
    }
}