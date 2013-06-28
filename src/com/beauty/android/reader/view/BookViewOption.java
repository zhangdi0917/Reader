package com.beauty.android.reader.view;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BookViewOption {

    public int width = 0;

    public int height = 0;

    public int textSize = 18;

    public int textColor = Color.rgb(28, 28, 28);

    public int leftPadding = 6;

    public int rightPadding = 6;

    public int topPadding = 6;

    public int bottomPadding = 6;

    public int lineSpace = 10;

    public int bgColor = Color.rgb(255, 255, 255);

    public Bitmap bgBm = null;

    public int titleTextSize = 14;
    public int titleTextColor = Color.rgb(66, 66, 66);

    public void transToPixel(float density) {
        this.textSize *= density;
        this.leftPadding *= density;
        this.rightPadding *= density;
        this.topPadding *= density;
        this.bottomPadding *= density;
        this.lineSpace *= density;
        this.titleTextSize *= density;
    }

}
