package com.example.NewProject.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;

import java.util.Map;
import java.util.Set;


@Entity
@Getter
@Setter
@Table(name = "Dictionary")
public class DictModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String noun;
    private String modifier;
    private String nounmodifier;

    @Transient
    private Map<String, Set<String>> nounToModifiers;


}