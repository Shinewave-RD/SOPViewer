package com.shinewave.sopviewer;

import java.util.List;

/**
 * Created by user on 2015/10/30.
 */
public class PlayListItem {
    public int seq;
    public String localFullFilePath;
    public String strPages;
    public List<Integer> pages;
    public int sec;

    public PlayListItem(int seq, String localFullFilePath, String strPages, List<Integer> pages, int sec) {
        this.setSeq(seq);
        this.setLocalFullFilePath(localFullFilePath);
        this.setStrPages(strPages);
        this.setPages(pages);
        this.setSec(sec);
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getlocalFullFilePath() {
        return localFullFilePath;
    }

    public void setLocalFullFilePath(String localFullFilePath) {
        this.localFullFilePath = localFullFilePath;
    }

    public String getStrPages() {
        return strPages;
    }

    public void setStrPages(String strPages) {
        this.strPages = strPages;
    }

    public List<Integer> getPages() {
        return pages;
    }

    public void setPages(List<Integer> pages) {
        this.pages = pages;
    }


    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;

    }

}
