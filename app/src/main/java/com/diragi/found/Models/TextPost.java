package com.diragi.found.Models;

/**
 * Created by joe on 4/29/16.
 */
public class TextPost {
    private String author;
    private String content;
    private String score;
    private String time;
    private String postID;

    private String imgLink;

    public TextPost() {

    }

    public String getImgLink() {
        return imgLink;
    }

    public String getPostID() {
        return postID;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getScore() {
        return score;
    }

    public String getTime() {
        return time;
    }
}
