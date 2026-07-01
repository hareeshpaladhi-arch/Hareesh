package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mirrors "Exclusions REVISED-08.11.22.xlsx".
 * Python: exmp_df = pd.read_excel(...)  # Object | Exemptions
 * Exemptions is a '|'-delimited list of terms to strip when the Object appears
 * before the exempted term in the description.
 */
@Entity
@Table(name = "exclusion")
public class Exclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object", length = 500)
    private String object;

    @Column(name = "exemptions", length = 1000)
    private String exemptions;

    public Exclusion() {
    }

    public Exclusion(String object, String exemptions) {
        this.object = object;
        this.exemptions = exemptions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getExemptions() {
        return exemptions;
    }

    public void setExemptions(String exemptions) {
        this.exemptions = exemptions;
    }
}
