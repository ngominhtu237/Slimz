package com.tunm.slimz.model;

import java.io.Serializable;

public class Word implements Serializable {
    private int word_id;
    private String wordname;
    private String details;

    public Word() {
    }

    public Word(int word_id, String wordname, String details) {
        this.word_id = word_id;
        this.wordname = wordname;
        this.details = details;
    }

    public int getWord_id() {
        return word_id;
    }

    public void setWord_id(int word_id) {
        this.word_id = word_id;
    }

    public String getWordname() {
        return wordname;
    }

    public void setWordname(String wordname) {
        this.wordname = wordname;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
