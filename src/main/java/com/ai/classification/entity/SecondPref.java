package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mirrors the "Second_Pref" sheet of CLASS_CLEAN_Item_Details_NIIC.xlsx.
 * Python: check1 = pd.read_excel(check_df, 'Second_Pref')  # RULE2
 * Columns: DESCRIPTION | Class
 * DESCRIPTION holds a '#'-delimited chain, e.g. "OBJECT#QUALIFIER1#QUALIFIER2"
 */
@Entity
@Table(name = "second_pref")
public class SecondPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "class", length = 500)
    private String className;

    public SecondPref() {
    }

    public SecondPref(String description, String className) {
        this.description = description;
        this.className = className;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
