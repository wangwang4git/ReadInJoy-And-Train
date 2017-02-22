package com.example.leopeng.recyclerviewdemo;


import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
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
    private List<String> stringTagsList;

    public static class Rating {
        public String max;
        public String numRaters;
        public String average;
        public String min;
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

    public void setRating(String max, String min, String numRaters, String average) {
        this.rating = new Rating();
        this.rating.max = max;
        this.rating.min = min;
        this.rating.average = average;
        this.rating.numRaters = numRaters;
    }

    public List<Tag> getTags() {
        return this.tags;
    }

    public List<String> getStringTags() {
        if (stringTagsList == null) {
            stringTagsList = new ArrayList<>(this.tags.size());
        }

        stringTagsList.clear();

        int maxNum = Math.min(this.tags.size(), 10);
        for (int i = 0; i < maxNum; i++) {
            stringTagsList.add(tags.get(i).tagName + " " + this.tags.get(i).count);
        }

        return this.stringTagsList;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
