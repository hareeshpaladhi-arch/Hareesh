package com.example.NewProject.Entity;

import com.example.NewProject.Repository.DictRepo;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class DictResponse {

//    private String description;
//    private String noun;
//    private String modifier;
//    private String nounmodifier;
//
//    public DictResponse(){}
//
//    public DictResponse(String description, String noun, String modifier, String nounmodifier) {
//        this.description = description;
//        this.noun = noun;
//        this.modifier = modifier;
//        this.nounmodifier = nounmodifier;
//    }

    private String description;
    private List<DictItemResponse> items;

    public DictResponse(String description, List<DictItemResponse> items) {
        this.description = description;
        this.items = items;
    }
}
