package com.ai.hub.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private Map<Long, Map<String, Object>> progressMap = new ConcurrentHashMap<>();

    public void update(Long jobId, int processed) {
        progressMap.put(jobId, Map.of("processed", processed));
    }

    public Map<String, Object> get(Long jobId) {
        return progressMap.getOrDefault(jobId, Map.of("processed", 0));
    }
}
