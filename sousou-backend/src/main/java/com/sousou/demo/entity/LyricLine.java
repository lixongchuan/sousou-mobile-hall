package com.sousou.demo.entity;

public class LyricLine {
    private double time; 
    private String text; 

    public LyricLine() {}

    public LyricLine(double time, String text) {
        this.time = time;
        this.text = text;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}