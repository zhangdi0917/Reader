package com.beauty.android.reader.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class BookPageFactory {

    private int mPageWidth = 0;
    private int mPageHeight = 0;

    private int marginWidth = 0;
    private int marginHeight = 0;

    private int mVisibleWidth = 0;
    private int mVisibleHeight = 0;

    private int lineCount = 0;

    private int lineSpace = 0;

    private float density = 0;

    private int fontSize = 0;

    private int textColor = Color.rgb(28, 28, 28);
    private int bgColor = 0xffffffff;
    private String charsetName = "GBK";

    private FileChannel mFileChannel;
    private MappedByteBuffer mMappedByteBuffer;
    private long bufferedLength = 0;

    private int startIndex = 0;
    private int endIndex = 0;

    private boolean isFirstPage = false;
    private boolean isLastPage = false;

    private List<String> pageLines = new ArrayList<String>();

    private Paint mPaint;

    public BookPageFactory(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Align.LEFT);

        density = context.getResources().getDisplayMetrics().density;
        fontSize = (int) (density * 15 + 0.5);
        lineSpace = (int) (density * 3 + 0.5);

        marginWidth = (int) (density * 8 + 0.5);
        marginHeight = (int) (density * 8 + 0.5);

    }

    public void setPageSize(int width, int height) {
        mPageWidth = width;
        mPageHeight = height;

        mVisibleWidth = mPageWidth - 2 * marginWidth;
        mVisibleHeight = mPageHeight - 2 * marginHeight;

        lineCount = mVisibleHeight / (fontSize + lineSpace) - 1;
    }

    public void setFontSize(int size) {
        fontSize = (int) (density * size + 0.5);
        lineCount = mVisibleHeight / (fontSize + lineSpace) - 1;
    }

    public void setTextColor(int color) {
        textColor = color;
    }

    public void setBackgroundColor(int color) {
        bgColor = color;
    }

    public void setStartIndex(int index) {
        startIndex = index;
        endIndex = index;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getFirstLineText() {
        return pageLines.size() > 0 ? pageLines.get(0) : "";
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    @SuppressWarnings("resource")
    public void openBook(String path, int offset) {
        try {
            File file = new File(path);
            bufferedLength = file.length();
            mFileChannel = new RandomAccessFile(file, "r").getChannel();
            mMappedByteBuffer = mFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, bufferedLength);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (offset >= 0) {
            startIndex = offset;
            endIndex = offset;
        }
    }

    public void closeBook() {
        try {
            if (mMappedByteBuffer != null) {
                mMappedByteBuffer.clear();
                mMappedByteBuffer = null;
            }
            if (mFileChannel != null) {
                mFileChannel.close();
                mFileChannel = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        mPaint.setTextSize(fontSize);
        mPaint.setColor(textColor);

        if (pageLines.size() == 0) {
            pageLines = pageDown();
        }

        canvas.drawColor(bgColor);

        int y = marginHeight;
        for (String strLine : pageLines) {
            y += fontSize;
            canvas.drawText(strLine, marginWidth, y, mPaint);
            y += lineSpace;
        }

        float fPercent = (float) (startIndex * 1.0 / bufferedLength);
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";
        int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
        canvas.drawText(strPercent, mPageWidth - nPercentWidth, mPageHeight - 10, mPaint);
    }

    /**
     * 向前翻页
     */
    public void previewPage() {
        if (startIndex <= 0) {
            startIndex = 0;
            isFirstPage = true;
            return;
        } else {
            isFirstPage = false;
        }
        pageLines.clear();
        pageUp();
        pageLines = pageDown();
    }

    public void currentPage() {
        pageLines.clear();
        pageLines = pageDown();
    }

    /**
     * 向后翻页
     */
    public void nextPage() {
        if (endIndex >= bufferedLength) {
            isLastPage = true;
            return;
        } else {
            isLastPage = false;
        }
        pageLines.clear();
        startIndex = endIndex;
        pageLines = pageDown();
    }

    /**
     * 获取下一页的数据
     * 
     * @return
     */
    private List<String> pageDown() {
        mPaint.setTextSize(fontSize);
        mPaint.setColor(textColor);
        String strParagraph = "";
        List<String> lines = new ArrayList<String>();
        while (lines.size() < lineCount && endIndex < bufferedLength) {
            byte[] paraBuf = readParagraphForward(endIndex);
            endIndex += paraBuf.length;
            try {
                strParagraph = new String(paraBuf, charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String strReturn = "";
            if (strParagraph.indexOf("\r\n") != -1) {
                strReturn = "\r\n";
                strParagraph = strParagraph.replaceAll("\r\n", "");
            } else if (strParagraph.indexOf("\n") != -1) {
                strReturn = "\n";
                strParagraph = strParagraph.replaceAll("\n", "");
            }

            if (strParagraph.length() == 0) {
                lines.add(strParagraph);
            } else {
                while (strParagraph.length() > 0) {
                    int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                    if (strParagraph.length() < nSize) {
                        nSize = strParagraph.length();
                    }
                    lines.add(strParagraph.substring(0, nSize));
                    strParagraph = strParagraph.substring(nSize);

                    if (lines.size() >= lineCount) {
                        break;
                    }
                }

                if (strParagraph.length() != 0) {
                    try {
                        endIndex -= (strParagraph + strReturn).getBytes(charsetName).length;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return lines;
    }

    private void pageUp() {
        if (startIndex < 0) {
            startIndex = 0;
        }

        mPaint.setTextSize(fontSize);
        mPaint.setColor(textColor);

        List<String> lines = new ArrayList<String>();
        String strParagraph = "";

        while (lines.size() < lineCount && startIndex > 0) {
            List<String> paraLines = new ArrayList<String>();
            byte[] paraBuf = readParagraphBack(startIndex);
            startIndex -= paraBuf.length;
            try {
                strParagraph = new String(paraBuf, charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n", "");
            strParagraph = strParagraph.replaceAll("\n", "");

            if (strParagraph.length() == 0) {
                paraLines.add(strParagraph);
            }
            while (strParagraph.length() > 0) {
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                if (nSize > strParagraph.length()) {
                    nSize = strParagraph.length();
                }
                paraLines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
            }
            lines.addAll(0, paraLines);
        }

        while (lines.size() > lineCount) {
            try {
                startIndex += lines.get(0).getBytes(charsetName).length;
                lines.remove(0);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        endIndex = startIndex;
    }

    /**
     * 向后读一个段落
     * 
     * @param fromPos
     * @return
     */
    protected byte[] readParagraphBack(int fromPos) {
        int end = fromPos;
        int i;
        byte b0, b1;
        if (charsetName.equals("UTF-16LE")) {
            i = end - 2;
            while (i > 0) {
                b0 = mMappedByteBuffer.get(i);
                b1 = mMappedByteBuffer.get(i + 1);
                if (b0 == 0x0a && b1 == 0x00 && i != end - 2) {
                    i += 2;
                    break;
                }
                i--;
            }

        } else if (charsetName.equals("UTF-16BE")) {
            i = end - 2;
            while (i > 0) {
                b0 = mMappedByteBuffer.get(i);
                b1 = mMappedByteBuffer.get(i + 1);
                if (b0 == 0x00 && b1 == 0x0a && i != end - 2) {
                    i += 2;
                    break;
                }
                i--;
            }
        } else {
            i = end - 1;
            while (i > 0) {
                b0 = mMappedByteBuffer.get(i);
                if (b0 == 0x0a && i != end - 1) {
                    i++;
                    break;
                }
                i--;
            }
        }
        if (i < 0)
            i = 0;
        int size = end - i;
        int j;
        byte[] buf = new byte[size];
        for (j = 0; j < size; j++) {
            buf[j] = mMappedByteBuffer.get(i + j);
        }
        return buf;
    }

    /**
     * 向前读一个段落
     * 
     * @param fromPos
     * @return
     */
    protected byte[] readParagraphForward(int fromPos) {
        int start = fromPos;
        int i = start;
        byte b0, b1;
        if (charsetName.equals("UTF-16LE")) {
            while (i < bufferedLength - 1) {
                b0 = mMappedByteBuffer.get(i++);
                b1 = mMappedByteBuffer.get(i++);
                if (b0 == 0x0a && b1 == 0x00) {
                    break;
                }
            }
        } else if (charsetName.equals("UTF-16BE")) {
            while (i < bufferedLength - 1) {
                b0 = mMappedByteBuffer.get(i++);
                b1 = mMappedByteBuffer.get(i++);
                if (b0 == 0x00 && b1 == 0x0a) {
                    break;
                }
            }
        } else {
            while (i < bufferedLength) {
                b0 = mMappedByteBuffer.get(i++);
                if (b0 == 0x0a) {
                    break;
                }
            }
        }
        int size = i - start;
        byte[] buf = new byte[size];
        for (i = 0; i < size; i++) {
            buf[i] = mMappedByteBuffer.get(start + i);
        }
        return buf;
    }

}
