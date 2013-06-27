package com.beauty.android.reader.view;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    public int id;

    public String name;

    public String author;

    public int coverRes;

    public String description;

    public String filename;

    public String charset = "GBK";

    public Book() {

    }

    public Book(int id, String name, String author, int coverRes, String description, String filename, String charset) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.coverRes = coverRes;
        this.description = description;
        this.filename = filename;
        this.charset = charset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(author);
        dest.writeInt(coverRes);
        dest.writeString(description);
        dest.writeString(filename);
        dest.writeString(charset);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            Book book = new Book();
            book.id = source.readInt();
            book.name = source.readString();
            book.author = source.readString();
            book.coverRes = source.readInt();
            book.description = source.readString();
            book.filename = source.readString();
            book.charset = source.readString();
            return book;
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

}
