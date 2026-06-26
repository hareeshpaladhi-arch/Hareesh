package com.example.NewProject.Entity;

import com.example.NewProject.Repository.DictRepo;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DictResponse {

    private String description;
    private String noun;
    private String modifier;
    private String nounmodifier;

    public DictResponse(){}

    public DictResponse(String description, String noun, String modifier, String nounmodifier) {
        this.description = description;
        this.noun = noun;
        this.modifier = modifier;
        this.nounmodifier = nounmodifier;
    }
}
