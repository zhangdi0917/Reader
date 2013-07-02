package com.beauty.android.reader.vo;

import java.io.IOException;
import java.io.InputStream;

import com.beauty.android.reader.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Book implements Parcelable {
    
    private static final int DEFAULT_ICON_ID = R.drawable.default_cover;

    public int id;

    public String name;

    public String author;

    public String cover;

    public String description;

    public String file;

    public String charset = "GBK";

    public Book() {

    }

    public Book(int id, String name, String author, String cover, String description, String file, String charset) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.cover = cover;
        this.description = description;
        this.file = file;
        this.charset = charset;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    
    public Bitmap getCoverBitmap(Context context) {
        if (!TextUtils.isEmpty(cover)) {
            InputStream is = null;
            try {
                is = context.getAssets().open(cover);
                return BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    is = null;
                }
            }
            return BitmapFactory.decodeResource(context.getResources(), DEFAULT_ICON_ID);
        } else {
            return BitmapFactory.decodeResource(context.getResources(), DEFAULT_ICON_ID);
        }
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
        dest.writeString(cover);
        dest.writeString(description);
        dest.writeString(file);
        dest.writeString(charset);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            Book book = new Book();
            book.id = source.readInt();
            book.name = source.readString();
            book.author = source.readString();
            book.cover = source.readString();
            book.description = source.readString();
            book.file = source.readString();
            book.charset = source.readString();
            return book;
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

}
