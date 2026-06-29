package com.example.NewProject.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "NounExemption")
public class DataExemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;
    @Column(columnDefinition = "TEXT")
    public String nouns;
    @Column(columnDefinition = "TEXT")
    public String exemption;

}
