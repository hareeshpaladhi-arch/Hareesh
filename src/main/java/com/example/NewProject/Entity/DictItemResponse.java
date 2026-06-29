package com.example.NewProject.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictItemResponse {

    private String noun;
    private String modifier;
    private String nounModifier;

    public DictItemResponse(String noun, String modifier, String nounModifier) {
        this.noun = noun;
        this.modifier = modifier;
        this.nounModifier = nounModifier;
    }

    // getters & setters
}