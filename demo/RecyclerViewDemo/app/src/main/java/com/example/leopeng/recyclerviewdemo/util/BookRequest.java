package com.example.leopeng.recyclerviewdemo.util;

/**
 * Created by leopeng on 28/02/2017.
 */

public class BookRequest {
    public final static String baseURL = "https://api.douban.com/v2/book/";

    public static String getUserCollectionsURL(String username) {
        return (baseURL + "user/" + username + "/collections" + getRangeParasString(20, 0));
    }

    public static String getSearchBooksURL(String keyword) {
        return (baseURL + "search") + getRangeParasString(20, 0) + getSearchParasString(keyword);
    }

    /**
     * 拼接参数字符串
     * @param count default value 20
     * @param start default value 0
     * @return
     */
    private static String getRangeParasString(int count, int start) {
        count = (count <= 0 || count > 100) ? 100 : count;
        start = (start < 0) ? 0 : start;

        return ("?count=" + count + "&start=" + start);
    }

    private static String getSearchParasString(String keyword) {
        return "&q=" + keyword;
    }
}
