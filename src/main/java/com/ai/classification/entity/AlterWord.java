package com.ai.classification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Mirrors IS_ALTER_WORD_ABB.xlsx.
 * Python: alter_df = pd.read_excel(...)  # WORD | ALTER_WORD
 * Rows with a null WORD or ALTER_WORD are dropped on import, matching:
 *   alter_df.drop(alter_df[alter_df['ALTER_WORD'].isnull()].index, inplace=True)
 *   alter_df.drop(alter_df[alter_df['WORD'].isnull()].index, inplace=True)
 */
@Entity
@Table(name = "alter_word")
public class AlterWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word", length = 500)
    private String word;

    @Column(name = "alter_word", length = 500)
    private String alterWord;

    public AlterWord() {
    }

    public AlterWord(String word, String alterWord) {
        this.word = word;
        this.alterWord = alterWord;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAlterWord() {
        return alterWord;
    }

    public void setAlterWord(String alterWord) {
        this.alterWord = alterWord;
    }
}
