package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mirrors the "First_Pref(NEW)" sheet of CLASS_CLEAN_Item_Details_NIIC.xlsx.
 * Python: r1_check = pd.read_excel(check_df, 'First_Pref(NEW)')  # RULE1
 * Columns: Class | RULE1
 */
@Entity
@Table(name = "first_pref")
public class FirstPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class", length = 500)
    private String className;

    @Column(name = "rule1", length = 500)
    private String rule1;

    public FirstPref() {
    }

    public FirstPref(String className, String rule1) {
        this.className = className;
        this.rule1 = rule1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRule1() {
        return rule1;
    }

    public void setRule1(String rule1) {
        this.rule1 = rule1;
    }
}
