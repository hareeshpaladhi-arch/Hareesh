package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mirrors the "Third_Pref" sheet of CLASS_CLEAN_Item_Details_NIIC.xlsx.
 * Python: class_check = pd.read_excel(check_df, 'Third_Pref')
 * Columns: Class | TERM(OBJECT) | TERM(Qualifier)
 */
@Entity
@Table(name = "third_pref")
public class ThirdPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", length = 500)
    private String className;

    @Column(name = "term_object", length = 1000)
    private String termObject;

    @Column(name = "term_qualifier", length = 1000)
    private String termQualifier;

    public ThirdPref() {
    }

    public ThirdPref(String className, String termObject, String termQualifier) {
        this.className = className;
        this.termObject = termObject;
        this.termQualifier = termQualifier;
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

    public String getTermObject() {
        return termObject;
    }

    public void setTermObject(String termObject) {
        this.termObject = termObject;
    }

    public String getTermQualifier() {
        return termQualifier;
    }

    public void setTermQualifier(String termQualifier) {
        this.termQualifier = termQualifier;
    }
}
