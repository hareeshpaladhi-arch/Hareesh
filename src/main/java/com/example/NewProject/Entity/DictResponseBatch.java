package com.example.NewProject.Entity;

import java.util.List;

public class DictResponseBatch {
    private List<DictResponse> results;

    public DictResponseBatch(List<DictResponse> results) {
        this.results = results;
    }

    public List<DictResponse> getResults() {
        return results;
    }

    public void setResults(List<DictResponse> results) {
        this.results = results;
    }
}
