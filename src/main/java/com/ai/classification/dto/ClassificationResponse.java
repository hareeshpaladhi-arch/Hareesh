package com.ai.classification.dto;

public class ClassificationResponse {

    private String description;
    private String normalizedDescription;
    private String predictedClass;

    public ClassificationResponse() {
    }

    public ClassificationResponse(String description, String normalizedDescription, String predictedClass) {
        this.description = description;
        this.normalizedDescription = normalizedDescription;
        this.predictedClass = predictedClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNormalizedDescription() {
        return normalizedDescription;
    }

    public void setNormalizedDescription(String normalizedDescription) {
        this.normalizedDescription = normalizedDescription;
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(String predictedClass) {
        this.predictedClass = predictedClass;
    }
}
