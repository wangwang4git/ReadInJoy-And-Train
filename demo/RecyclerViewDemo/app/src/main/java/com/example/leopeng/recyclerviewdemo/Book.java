package com.example.leopeng.recyclerviewdemo;

import android.app.ListActivity;

import java.util.List;

import static java.lang.Math.min;

/**
 * Created by leopeng on 10/02/2017.
 */

public class Book {
    private String bookName;
    private String author;
    private String bookDescription;
    private String status;
    private String imageURL;
    private int statusColor;
    private Rating rating;
    private List<Tag> tags;

    public static class Rating {
        public double max;
        public int numRaters;
        public double average;
        public double min;
    }

    public static class Tag {
        public int count;
        public String tagName;
    }

    public Book(String bookName, String author, String bookDescription, String status, String imageURL) {
        this.bookName = bookName;
        this.author = author;
        this.bookDescription = bookDescription;
        this.status = status;
        this.imageURL = imageURL;
    }

    public String getBookName() {
        return this.bookName;
    }

    public String getAuthor() {
        return this.author;
    }

    public  String getBookDescription() {
        return this.bookDescription;
    }

    public String getShortBookDescription() {
        return (this.bookDescription.length() > 0)  ?
                this.bookDescription.substring(0, min(this.bookDescription.length() - 1, 70)) + "..." :
                this.bookDescription;
    }

    public String getStatus() {
        String res = "";

        switch (this.status) {
            case "wish":
                res = "想读";
                statusColor = 0xFFF0B27A;
                break;
            case "reading":
                res = "在读";
                statusColor = 0xFF5DADE2;
                break;
            case "read":
                res = "读过";
                statusColor = 0xFF58D68D;
                break;
            default:
                res = "";
        }

        return res;
    }

    public String getImageURL() {
        return this.imageURL;
    }

    public int getStatusColor() {
        return this.statusColor;
    }

    public Rating getRating() {
        return this.rating;
    }

    public void setRating(double max, double min, int numRaters, double average) {
        this.rating = new Rating();
        this.rating.max = max;
        this.rating.min = min;
        this.rating.average = average;
        this.rating.numRaters = numRaters;
    }

    public List<Tag> getTags() {
        return this.tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
