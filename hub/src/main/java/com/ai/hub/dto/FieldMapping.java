package com.ai.hub.dto;

public class FieldMapping {

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

    @Override
    public String toString() {
        return "FieldMapping{" +
                "sourceField='" + sourceField + '\'' +
                ", targetField='" + targetField + '\'' +
                '}';
    }
}
