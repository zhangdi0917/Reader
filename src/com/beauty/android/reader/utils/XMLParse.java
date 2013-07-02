package com.beauty.android.reader.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.beauty.android.reader.vo.Book;

public class XMLParse {
    private static final String TAG_BOOK = "book";
    private static final String TAG_NAME = "name";
    private static final String TAG_DESC = "desc";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_COVER = "cover";
    private static final String TAG_FILE = "file";
    private static final String TAG_CHARSET = "charset";

    public static List<Book> parseBookList(InputStream inputStream) throws Exception {
        List<Book> books = null;
        Book book = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int event = parser.getEventType();// 产生第一个事件
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
            case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
                books = new ArrayList<Book>();// 初始化books集合
                break;
            case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
                if (TAG_BOOK.equalsIgnoreCase(parser.getName())) {// 判断开始标签元素是否是book
                    book = new Book();
                    book.setId(Integer.parseInt(parser.getAttributeValue(0)));// 得到book标签的属性值，并设置book的id
                }
                if (book != null) {
                    if (TAG_NAME.equalsIgnoreCase(parser.getName())) {
                        book.setName(parser.nextText());
                    } else if (TAG_AUTHOR.equalsIgnoreCase(parser.getName())) {
                        book.setAuthor(parser.nextText());
                    } else if (TAG_COVER.equalsIgnoreCase(parser.getName())) {
                        book.setCover(parser.nextText());
                    } else if (TAG_FILE.equalsIgnoreCase(parser.getName())) {
                        book.setFile(parser.nextText());
                    } else if (TAG_DESC.equalsIgnoreCase(parser.getName())) {
                        book.setDescription(parser.nextText());
                    } else if (TAG_CHARSET.equalsIgnoreCase(parser.getName())) {
                        book.setCharset(parser.nextText());
                    }
                }
                break;
            case XmlPullParser.END_TAG:// 判断当前事件是否是标签元素结束事件
                if (TAG_BOOK.equalsIgnoreCase(parser.getName())) {// 判断结束标签元素是否是book
                    books.add(book);// 将book添加到books集合
                    book = null;
                }
                break;
            }
            event = parser.next();// 进入下一个元素并触发相应事件
        }// end while

        return books;

    }

}
