package com.beauty.android.reader.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beauty.android.reader.vo.Book;

import android.content.Context;
import android.text.TextUtils;

public class BookModel {

    private Context mContext;

    private Book mBook;

    private int mBookSize = 0;

    private List<Chapter> mChapters = new ArrayList<Chapter>();

    public BookModel(Context context, Book book) {
        mContext = context;

        mBook = book;

        if (mBook == null || TextUtils.isEmpty(mBook.file)) {
            mBookSize = 0;
            return;
        }

        BufferedReader br = null;
        char[] buf = new char[4096];
        try {
            br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(mBook.file), mBook.charset));
            int count = 0;
            while ((count = br.read(buf)) > 0) {
                mBookSize += count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // breakChapter(book);
    }

    public List<Chapter> getChapters() {
        return mChapters;
    }

    public String read(int offset, int length) throws IOException {
        if (offset < 0) {
            offset = 0;
        } else if (offset >= mBookSize) {
            return null;
        }
        if (offset + length > mBookSize) {
            length = mBookSize - offset;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(mBook.file), mBook.charset));
        char[] buffer = new char[length];
        br.skip(offset);
        br.read(buffer, 0, length);
        br.close();

        return new String(buffer);
    }

    // /**
    // * 分页完成前读取
    // *
    // * @param start
    // * @param count
    // * @return
    // */
    // public List<Page> getTmpPages(int start, int count) {
    //
    // List<Page> pages = new ArrayList<Page>();
    //
    // BufferedReader br = null;
    // try {
    // br = new BufferedReader(new InputStreamReader(new
    // FileInputStream(mBook.getPath()), mBook.getCharset()));
    // char[] buffer = new char[count];
    // if (br.read(buffer) > 0) {
    // pages.addAll(paging(new String(buffer), start));
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // } finally {
    // if (br != null) {
    // try {
    // br.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    //
    // return pages;
    //
    // }
    //
    // public void startPaging() {
    // mBookView.onStartPage();
    //
    // new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // long startTime = System.currentTimeMillis();
    // System.out.println("startTime = " + startTime);
    //
    // mChapters.clear();
    // mPages.clear();
    //
    // BufferedReader br = null;
    // try {
    // br = new BufferedReader(new InputStreamReader(new
    // FileInputStream(mBook.getPath()),
    // mBook.getCharset()));
    //
    // // 分章节
    // mChapters = breakChapter(mBook);
    //
    // if (mChapters != null || mChapters.size() > 0) {
    // for (int i = 0; i < mChapters.size(); i++) {
    // int length = 0;
    // if (i == mChapters.size() - 1) {
    // length = mBookSize - mChapters.get(i).start;
    // } else {
    // length = mChapters.get(i + 1).start - mChapters.get(i).start;
    // }
    //
    // Chapter chapter = mChapters.get(i);
    // chapter.pageIndex = mPages.size();
    //
    // char[] buffer = new char[length];
    // int count = br.read(buffer, 0, length);
    // if (count > 0) {
    // mPages.addAll(paging(new String(buffer), chapter.start));
    // }
    // System.out.println("page chapter " + i);
    // }
    // } else {
    // int startOffset = 0;
    //
    // char[] buffer = new char[10240];
    // int count = 0;
    // while ((count = br.read(buffer)) > 0) {
    // mPages.addAll(paging(new String(buffer), startOffset));
    // startOffset += count;
    // }
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // mBookView.onPageError();
    // } finally {
    // try {
    // if (br != null) {
    // br.close();
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    //
    // long endTime = System.currentTimeMillis();
    // System.out.println("page time = " + (endTime - startTime));
    // mBookView.onPageOver();
    // }
    // }).start();
    //
    // }
    //
    // private List<Page> paging(String str, int indexOffset) {
    // if (TextUtils.isEmpty(str)) {
    // return null;
    // }
    //
    // List<Page> pages = new ArrayList<Page>();
    //
    // mPaint.setTextSize(mBookViewOption.textSize);
    // int lineCount = (mBookViewOption.height - mBookViewOption.topPadding -
    // mBookViewOption.bottomPadding)
    // / (mBookViewOption.textSize + mBookViewOption.lineSpace);
    //
    // List<String> lines = new ArrayList<String>();
    // int hasPaged = 0;
    // int pageStart = 0;
    // while (hasPaged < str.length()) {
    // int num = mPaint.breakText(str, hasPaged, str.length(), true,
    // mBookViewOption.width
    // - mBookViewOption.leftPadding - mBookViewOption.rightPadding, null);
    // String lineStr = str.substring(hasPaged, hasPaged + num);
    // hasPaged += num;
    //
    // lines.add(lineStr);
    // if (lines.size() >= lineCount) {
    // Page page = new Page();
    // page.start = pageStart + indexOffset;
    // page.end = hasPaged + indexOffset;
    // page.lines = lines;
    // pages.add(page);
    //
    // lines = new ArrayList<String>();
    // pageStart = hasPaged;
    // }
    // }
    //
    // return pages;
    // }

    // /**
    // * 读一个chapter
    // *
    // * @return
    // * @throws IOException
    // */
    // public String readChapter(int chapterId) throws IOException {
    // if (chapterId < 0 || chapterId > mChapters.size() - 1) {
    // return null;
    // }
    //
    // Chapter chapter = mChapters.get(chapterId);
    // int start = chapter.start;
    // int end = 0;
    // if (chapterId == mChapters.size() - 1) {
    // end = getBookSize();
    // } else {
    // end = mChapters.get(chapterId + 1).start;
    // }
    //
    // BufferedReader br = new BufferedReader(new InputStreamReader(new
    // FileInputStream(mBook.getPath()),
    // mBook.getCharset()));
    // char[] buffer = new char[end - start];
    // br.skip(start);
    // br.read(buffer, 0, end - start);
    // String chapterStr = new String(buffer);
    //
    // br.close();
    //
    // return chapterStr;
    // }

    public int getBookSize() {
        return mBookSize;
    }

    private String mRemainStr;
    private static final int REMAIN_LENGTH = 20;
    private int mChapterId = 0;
    private int mHasBreakSize = 0;
    private Pattern mPattern = Pattern.compile("^[0-9一二三四五六七八九十百千 ]+$");
    private String[] mKeyArray = { "章", "回" };

    // private String[] mKeyArray = { "章" };

    /**
     * 按章节分段
     * 
     * @param book
     * @return
     */
    private List<Chapter> breakChapter(Book book) throws IOException {
        if (book == null || book.file == null) {
            return null;
        }

        List<Chapter> chapters = new ArrayList<Chapter>();

        BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(book.file), book.charset));
        char[] buf = new char[4096];
        int count = 0;
        while ((count = br.read(buf)) > 0) {
            mBookSize += count;
            String str = new String(buf, 0, count);
            searchKeyWord(str, chapters);
        }
        searchKeyWord(" ", chapters);
        if (br != null) {
            br.close();
        }

        // if (chapters.size() > 0 && chapters.get(0).start > 0) {
        // Chapter chapter = new Chapter();
        // chapter.start = 0;
        // chapter.title = "前言";
        // chapters.add(0, chapter);
        // }

        return chapters;

    }

    private void searchKeyWord(String str, List<Chapter> chapters) {
        if (str == null || chapters == null) {
            return;
        }

        if (mRemainStr != null) {
            str = str.concat(mRemainStr);
        }

        int index1 = str.indexOf("第");
        if (str.length() > REMAIN_LENGTH && (index1 < 0 || index1 >= str.length() - REMAIN_LENGTH)) {
            mRemainStr = str.substring((str.length() - REMAIN_LENGTH), str.length());
            mHasBreakSize += str.length() - REMAIN_LENGTH - 1;
            return;
        }

        int offset = 0;
        while (index1 >= 0) {
            for (String key : mKeyArray) {
                int index2 = str.indexOf(key, index1);
                if (index2 >= 0) {
                    // 章后面没有空白符舍掉
                    if (index2 < str.length() - 1) {
                        String s = str.substring(index2 + 1, index2 + 2).trim();
                        if (s != null && s.length() != 0) {
                            break;
                        }
                    }

                    // 匹配数字
                    String num = str.substring(index1 + 1, index2);
                    Matcher m = mPattern.matcher(num);
                    if (m.matches()) {
                        int titleEnd = str.indexOf("\n", index2);

                        if (titleEnd < 0) {
                            titleEnd = index2 + 1;
                        } else if (titleEnd > index2 + 20) {
                            titleEnd = index2 + 20;
                        }
                        Chapter chapter = new Chapter();
                        chapter.id = ++mChapterId;
                        chapter.start = mHasBreakSize + index1;
                        chapter.title = str.substring(index1, titleEnd).trim();
                        chapters.add(chapter);

                        offset = titleEnd;
                        break;
                    }
                }

            }

            mRemainStr = null;
            if (offset <= index1) {
                offset = index1 + 1;
            }
            index1 = str.indexOf("第", offset);
        }
        mHasBreakSize += str.length();

    }

}
