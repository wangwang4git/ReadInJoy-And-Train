package com.example.leopeng.recyclerviewdemo;

/**
 * Created by leopeng on 10/02/2017.
 */

public class Book {
    private String bookName;
    private String author;
    private String bookDescription;
    private String status;

    public Book(String bookName, String author, String bookDescription, String status) {
        this.bookName = bookName;
        this.author = author;
        this.bookDescription = bookDescription;
        this.status = status;
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
                break;
            case "reading":
                res = "在读";
                break;
            case "read":
                res = "读过";
                break;
            default:
                res = "";
        }

        return res;
    }
}
