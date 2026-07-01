package com.ai.classification.dto;

import javax.validation.constraints.NotBlank;

public class ClassificationRequest {

    @NotBlank(message = "description must not be blank")
    private String description;

    public ClassificationRequest() {
    }

    public ClassificationRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
