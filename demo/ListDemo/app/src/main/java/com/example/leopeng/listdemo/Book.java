package com.example.leopeng.listdemo;

/**
 * Created by leopeng on 10/02/2017.
 */

// 既然book需要添加进list里面，对应就会有从list删除等涉及book比较的操作，对于java的对象比较方法，hash方法就需要重写了
// 这两个方法的坑与重写的合理方式，参考 effective java相关章节
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

        // 这个语法糖印象中是jdk7才用，但是手Q构建虽然是jdk7，但构建设置采用jdk6语法构建，所以这个语法后面不能在手Q里面出现哈
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
