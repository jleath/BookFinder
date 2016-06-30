package com.example.android.bookfinder;

/**
 * A class to represent a book retrieved from the Google Books API.
 */
public class Book {

    private String mAuthor;
    private String mTitle;

    public Book(String author, String title) {
        mAuthor = author;
        mTitle = title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }
}
