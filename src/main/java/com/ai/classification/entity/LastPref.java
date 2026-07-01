package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "last_pref")
public class LastPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", length = 500)
    private String className;

    @Column(name = "term_object", length = 1000)
    private String termObject;

    public LastPref() {
    }

    public LastPref(String className, String termObject) {
        this.className = className;
        this.termObject = termObject;
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
}
