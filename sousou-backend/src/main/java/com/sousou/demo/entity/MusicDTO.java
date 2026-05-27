package com.sousou.demo.entity;

import java.util.List;

public class MusicDTO {
    private int id;
    private String title;
    private String artist;
    private String coverBase64; 
    private String src;         
    private List<LyricLine> lyrics; 

    public MusicDTO() {}

    public MusicDTO(int id, String title, String artist, String coverBase64, String src, List<LyricLine> lyrics) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverBase64 = coverBase64;
        this.src = src;
        this.lyrics = lyrics;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCoverBase64() {
        return coverBase64;
    }

    public void setCoverBase64(String coverBase64) {
        this.coverBase64 = coverBase64;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<LyricLine> getLyrics() {
        return lyrics;
    }

    public void setLyrics(List<LyricLine> lyrics) {
        this.lyrics = lyrics;
    }
}