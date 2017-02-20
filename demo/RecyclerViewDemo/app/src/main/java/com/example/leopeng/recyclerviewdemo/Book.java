package com.example.leopeng.recyclerviewdemo;

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
}
