package com.example.hikehub;

public class Post {
    private int id;
    private String title;
    private String picturePath;

    // Method to set the values of the fields
    public Post(int id, String title, String picturePath) {
        this.id = id;
        this.title = title;
        this.picturePath = picturePath;
    }

    // Methods to get the values of the fields
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