package com.example.hikehub;

public class Post {
    private int id;
    private String title;
    private String picturePath;

    public Post(int id, String title, String picturePath) {
        this.id = id;
        this.title = title;
        this.picturePath = picturePath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPicturePath() {
        return picturePath;
    }
}