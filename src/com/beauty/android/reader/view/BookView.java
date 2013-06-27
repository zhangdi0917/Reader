package com.beauty.android.reader.view;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Scroller;
import android.widget.Toast;

/**
 * 支持分页、翻页动画
 * 
 * @author zhangdi
 * 
 */
public class BookView extends View {

    private BookViewOption mBookViewOption = new BookViewOption();

    private Paint mTextPaint;

    private Paint mTitlePaint;

    private Paint mPaint;

    private int mLineCount = 0;

    private BookModel mBookModel;

    private Book mBook;

    private boolean mWaitPaging = false;

    private int mStartOffset = 0;

    private List<Chapter> mChapterList;

    private int mBookSize = 0;

    private static final int READ_BUFFER_LENGTH = 20480;

    private List<Page> mPageList = new ArrayList<Page>();

    private int mPageIndex = 0;

    private BookViewListener mBookViewListener;

    private Bitmap mCurrBitmap;
    private Bitmap mNextBitmap;

    private Canvas mCurrCanvas;
    private Canvas mNextCanvas;

    private int[] mBackShadowColors;
    private int[] mFrontShadowColors;
    private GradientDrawable mBackShadowDrawableLR;
    private GradientDrawable mBackShadowDrawableRL;
    private GradientDrawable mFolderShadowDrawableLR;
    private GradientDrawable mFolderShadowDrawableRL;

    private GradientDrawable mFrontShadowDrawableHBT;
    private GradientDrawable mFrontShadowDrawableHTB;
    private GradientDrawable mFrontShadowDrawableVLR;
    private GradientDrawable mFrontShadowDrawableVRL;

    private ColorMatrixColorFilter mColorMatrixFilter;
    private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };
    private Matrix mMatrix;

    private PointF mTouch = new PointF();

    private int mCornerX = 1;
    private int mCornerY = 1;

    private Path mPath0;
    private Path mPath1;

    private float mMiddleX;
    private float mMiddleY;
    private float mDegrees;
    private float mTouchToCornerDis;

    private PointF mBezierStart1 = new PointF();
    private PointF mBezierControl1 = new PointF();
    private PointF mBeziervertex1 = new PointF();
    private PointF mBezierEnd1 = new PointF();

    private PointF mBezierStart2 = new PointF();
    private PointF mBezierControl2 = new PointF();
    private PointF mBeziervertex2 = new PointF();
    private PointF mBezierEnd2 = new PointF();

    boolean mIsRTandLB;

    private Scroller mScroller;

    private static final int PAGE_ANIM_DELAY = 1000;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ViewTreeObserver mObserver;

    public BookView(Context context) {
        this(context, null);
    }

    public BookView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath0 = new Path();
        mPath1 = new Path();

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Align.LEFT);
        mPaint = new Paint();
        mTitlePaint = new Paint();
        mTitlePaint.setAntiAlias(true);

        mTouch.set(-1, -1);
        mScroller = new Scroller(getContext());

        createDrawable();

        ColorMatrix cm = new ColorMatrix();
        float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0, 0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0 };
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);

        mMatrix = new Matrix();

        mObserver = getViewTreeObserver();
        mObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mBookViewOption.width = getWidth();
                mBookViewOption.height = getHeight();

                if (mCurrCanvas == null) {
                    mCurrBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    mCurrCanvas = new Canvas(mCurrBitmap);
                }
                if (mNextCanvas == null) {
                    mNextBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    mNextCanvas = new Canvas(mNextBitmap);
                }
            }
        });

        // 关闭硬件加速
        ViewUtils.setLayerType(this, ViewUtils.LAYER_TYPE_SOFTWARE, null);
    }

    public BookViewOption getBookViewOption() {
        return mBookViewOption;
    }

    public List<Chapter> getChapters() {
        return mChapterList;
    }

    public void openBook(Book book, int offset) throws IOException {
        mBook = book;
        mBookModel = new BookModel(getContext(), book);
        mStartOffset = offset;
        mWaitPaging = true;

        invalidate();

    }

    public static interface BookViewListener {
        public void onLoad();

        public void loadOver();
    }

    public void setBookViewListener(BookViewListener listener) {
        mBookViewListener = listener;
    }

    public int getCurrReadIndex() {
        if (mPageList == null || mPageList.size() == 0) {
            return mStartOffset;
        }
        if (mPageIndex < 0) {
            return mPageList.get(0).start;
        } else if (mPageIndex >= mPageList.size()) {
            return mPageList.get(mPageList.size() - 1).start;
        } else {
            return mPageList.get(mPageIndex).start;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBookModel == null) {
            canvas.drawColor(mBookViewOption.bgColor);
            return;
        }

        if (mWaitPaging) {
            mWaitPaging = false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBookViewListener != null) {
                        mBookViewListener.onLoad();
                    }
                    mChapterList = mBookModel.getChapters();
                    mBookSize = mBookModel.getBookSize();

                    if (mStartOffset >= mBookSize) {
                        mStartOffset = mBookSize - 1;
                    }

                    int length = READ_BUFFER_LENGTH;
                    if (mStartOffset + length > mBookSize) {
                        length = mBookSize - mStartOffset;
                    }
                    String buffer = null;
                    try {
                        buffer = mBookModel.read(mStartOffset, length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final List<Page> list = paging(buffer, mStartOffset);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPageList.clear();
                            if (list != null) {
                                mPageList.addAll(list);
                            }
                            mPageIndex = 0;
                            drawCurrPage();

                            invalidate();

                            if (mBookViewListener != null) {
                                mBookViewListener.loadOver();
                            }
                        }
                    });
                }
            }).start();
            return;
        }

        calcPoints();
        drawCurrentPageArea(canvas, mCurrBitmap, mPath0);
        drawNextPageAreaAndShadow(canvas, mNextBitmap);
        drawCurrentPageShadow(canvas);
        drawCurrentBackArea(canvas, mCurrBitmap);

    }

    private void drawPage(Canvas canvas, Page page) {
        canvas.drawColor(mBookViewOption.bgColor);

        if (page == null) {
            return;
        }

        mTextPaint.setTextSize(mBookViewOption.textSize);
        mTextPaint.setColor(mBookViewOption.textColor);

        mTitlePaint.setTextSize(mBookViewOption.titleTextSize);
        mTitlePaint.setColor(mBookViewOption.titleTextColor);

        // draw title
        int y = mBookViewOption.topPadding + mBookViewOption.titleTextSize;
        if (mBook != null && mBook.name != null) {
            int x = (int) (mBookViewOption.leftPadding + (getWidth() - mBookViewOption.leftPadding
                    - mBookViewOption.rightPadding - mTitlePaint.measureText(mBook.name)) / 2);
            canvas.drawText(mBook.name, x, y, mTitlePaint);
        }

        // draw text
        y += mBookViewOption.lineSpace;
        for (String str : page.lines) {
            y += mBookViewOption.textSize;
            canvas.drawText(str, mBookViewOption.leftPadding, y, mTextPaint);
            y += mBookViewOption.lineSpace;
        }

        // draw time and percent
        y = getHeight() - mBookViewOption.bottomPadding;
        canvas.drawText(getCurrentTime(), mBookViewOption.leftPadding, y, mTitlePaint);

        float fPercent = (float) (page.start * 1.0 / mBookSize);
        DecimalFormat df = new DecimalFormat("#0.0");
        String strPercent = df.format(fPercent * 100) + "%";
        int nPercentWidth = (int) mTitlePaint.measureText("99.9%") + 1;
        canvas.drawText(strPercent, getWidth() - mBookViewOption.rightPadding - nPercentWidth, y, mTitlePaint);
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm", Locale.getDefault());
        return formatter.format(new Date());
    }

    private void drawCurrPage() {
        if (mPageIndex < 0) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (mBookViewListener != null) {
                        mBookViewListener.onLoad();
                    }
                    final List<Page> list = readPrePages();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (list != null && list.size() > 0) {
                                mPageIndex = list.size() - 1;
                                mPageList.addAll(0, list);
                                releaseUnnecessaryPages();
                            } else {
                                mPageIndex = 0;
                            }

                            if (mPageIndex >= 0 && mPageIndex < mPageList.size()) {
                                drawPage(mCurrCanvas, mPageList.get(mPageIndex));
                            }

                            invalidate();

                            if (mBookViewListener != null) {
                                mBookViewListener.loadOver();
                            }
                        }
                    });

                }
            }).start();
        } else if (mPageIndex > mPageList.size() - 1) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (mBookViewListener != null) {
                        mBookViewListener.onLoad();
                    }
                    final List<Page> list = readNextPages();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (list != null) {
                                mPageList.addAll(list);
                                releaseUnnecessaryPages();
                            }

                            if (mPageIndex >= 0 && mPageIndex < mPageList.size()) {
                                drawPage(mCurrCanvas, mPageList.get(mPageIndex));
                            }

                            invalidate();

                            if (mBookViewListener != null) {
                                mBookViewListener.loadOver();
                            }
                        }
                    });

                }
            }).start();
        }

        if (mPageIndex >= 0 && mPageIndex < mPageList.size()) {
            drawPage(mCurrCanvas, mPageList.get(mPageIndex));
        } else {
            drawPage(mCurrCanvas, null);
        }
    }

    private void drawNextPage() {
        if (mPageIndex >= mPageList.size() - 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBookViewListener != null) {
                        mBookViewListener.onLoad();
                    }
                    final List<Page> list = readNextPages();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (list != null && list.size() > 0) {
                                mPageList.addAll(list);
                                releaseUnnecessaryPages();
                            }

                            if (mPageIndex + 1 >= 0 && mPageIndex + 1 < mPageList.size()) {
                                drawPage(mNextCanvas, mPageList.get(mPageIndex + 1));
                            }

                            invalidate();

                            if (mBookViewListener != null) {
                                mBookViewListener.loadOver();
                            }
                        }
                    });

                }
            }).start();
        }
        if (mPageIndex + 1 >= 0 && mPageIndex + 1 < mPageList.size()) {
            drawPage(mNextCanvas, mPageList.get(mPageIndex + 1));
        } else {
            drawPage(mNextCanvas, null);
        }
    }

    private void drawPrePage() {
        if (mPageIndex <= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBookViewListener != null) {
                        mBookViewListener.onLoad();
                    }
                    final List<Page> list = readPrePages();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (list != null && list.size() > 0) {
                                mPageIndex = list.size() - 1;
                                mPageList.addAll(0, list);
                                releaseUnnecessaryPages();
                            }

                            if (mPageIndex - 1 >= 0 && mPageIndex - 1 < mPageList.size()) {
                                drawPage(mNextCanvas, mPageList.get(mPageIndex - 1));
                            }

                            invalidate();

                            if (mBookViewListener != null) {
                                mBookViewListener.loadOver();
                            }
                        }
                    });

                }
            }).start();
        }

        if (mPageIndex - 1 >= 0 && mPageIndex - 1 < mPageList.size()) {
            drawPage(mNextCanvas, mPageList.get(mPageIndex - 1));
        } else {
            drawPage(mNextCanvas, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBookModel == null) {
            return super.onTouchEvent(event);
        }

        float x = 0;
        float y = 0;

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            x = event.getX();
            y = event.getY();

            abortAnimation();

            mTouch.set(x, y);
            calcCornerXY(x, y);

            drawCurrPage();
            if (mCornerX == 0) {
                if (isFirstPage()) {
                    mTouch.set(-1, -1);
                    Toast.makeText(getContext(), "已经是第一页", Toast.LENGTH_SHORT).show();
                    return false;
                }
                drawPrePage();
            } else if (mCornerX == getWidth()) {
                if (isLastPage()) {
                    mTouch.set(-1, -1);
                    Toast.makeText(getContext(), "已经是最后一页", Toast.LENGTH_SHORT).show();
                    return false;
                }
                drawNextPage();
            }
            break;

        case MotionEvent.ACTION_MOVE:
            mTouch.set(event.getX(), event.getY());
            invalidate();
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            x = event.getX();
            y = event.getY();

            int startX = (int) mTouch.x;
            int startY = (int) mTouch.y;
            int dx = 0;
            int dy = 0;

            if (mCornerX == getWidth()) {
                dx = -(int) (getWidth() - 1 + startX);
                mPageIndex++;
            } else if (mCornerX == 0) {
                dx = (int) (2 * getWidth() - 1 - startX);
                mPageIndex--;
            }
            if (mCornerY == getHeight()) {
                dy = (int) (getHeight() - 1 - startY);
            } else if (mCornerY == 0) {
                dy = 1 - startY;
            }

            mScroller.startScroll(startX, startY, dx, dy, PAGE_ANIM_DELAY);
            invalidate();
            break;

        }
        return true;
    }

    private boolean isFirstPage() {
        if (mPageList == null || mPageList.size() == 0) {
            return true;
        } else if (mPageIndex == 0 && mPageList.get(0).start <= 0) {
            return true;
        }
        return false;
    }

    private boolean isLastPage() {
        if (mPageList != null && mPageList.size() > 0 && mPageIndex >= mPageList.size() - 1) {
            Page page = mPageList.get(mPageList.size() - 1);
            int end = page.start;
            for (String str : page.lines) {
                if (str != null) {
                    end += str.length();
                }
            }
            if (end >= mBookSize) {
                return true;
            }
        }
        return false;
    }

    private void calcCornerXY(float x, float y) {
        if (x >= getWidth() / 2) {
            mCornerX = getWidth();
        } else {
            mCornerX = 0;
        }

        if (y >= getHeight() / 2) {
            mCornerY = getHeight();
        } else {
            mCornerY = 0;
        }

        if ((mCornerX == 0 && mCornerY == getHeight()) || (mCornerX == getWidth() && mCornerY == 0))
            mIsRTandLB = true;
        else
            mIsRTandLB = false;
    }

    private void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mTouch.x = mScroller.getCurrX();
            mTouch.y = mScroller.getCurrY();
            postInvalidate();
        }
    }

    private void createDrawable() {
        int[] color = { 0x333333, 0xb0333333 };
        mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[] { 0xff111111, 0x111111 };
        mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[] { 0x80111111, 0x111111 };
        mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    private PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();

        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    private void calcPoints() {
        mMiddleX = (mTouch.x + mCornerX) / 2;
        mMiddleY = (mTouch.y + mCornerY) / 2;

        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;
        mBezierControl2.x = mCornerX;
        mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
        mBezierStart1.y = mCornerY;

        if (mTouch.x > 0 && mTouch.x < getWidth()) {
            if (mBezierStart1.x < 0 || mBezierStart1.x > getWidth()) {
                if (mBezierStart1.x < 0)
                    mBezierStart1.x = getWidth() - mBezierStart1.x;

                float f1 = Math.abs(mCornerX - mTouch.x);
                float f2 = getWidth() * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);

                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

                mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2;
            }
        }
        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2;

        mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

        mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
        mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);

        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
        mPath0.lineTo(mTouch.x, mTouch.y);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.restore();
    }

    private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
        int leftx;
        int rightx;
        GradientDrawable mBackShadowDrawable;
        if (mIsRTandLB) {
            leftx = (int) (mBezierStart1.x);
            rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightx = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        float maxLength = (float) Math.hypot(getWidth(), getHeight());
        mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx, (int) (maxLength + mBezierStart1.y));
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRTandLB) {
            degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
        } else {
            degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
        }
        float maxLength = (float) Math.hypot(getWidth(), getHeight());

        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouch.x + d1);
        float y;
        if (mIsRTandLB) {
            y = (float) (mTouch.y + d2);
        } else {
            y = (float) (mTouch.y - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();

        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        int leftx;
        int rightx;
        GradientDrawable mCurrentPageShadow;
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl1.x);
            rightx = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftx = (int) (mBezierControl1.x - 25);
            rightx = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(leftx, (int) (mBezierControl1.y - maxLength), rightx, (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.close();
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl2.y);
            rightx = (int) (mBezierControl2.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftx = (int) (mBezierControl2.y - 25);
            rightx = (int) (mBezierControl2.y + 1);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
        float temp;
        if (mBezierControl2.y < 0)
            temp = mBezierControl2.y - getHeight();
        else
            temp = mBezierControl2.y;

        int hmg = (int) Math.hypot(mBezierControl2.x, temp);
        if (hmg > maxLength)
            mCurrentPageShadow.setBounds((int) (mBezierControl2.x - 25) - hmg, leftx,
                    (int) (mBezierControl2.x + maxLength) - hmg, rightx);
        else
            mCurrentPageShadow.setBounds((int) (mBezierControl2.x - maxLength), leftx, (int) (mBezierControl2.x),
                    rightx);

        mCurrentPageShadow.draw(canvas);
        canvas.restore();
    }

    private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        float f1 = Math.abs(i - mBezierControl1.x);
        int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
        float f2 = Math.abs(i1 - mBezierControl2.y);
        float f3 = Math.min(f1, f2);
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRTandLB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left = (int) (mBezierStart1.x - f3 - 1);
            right = (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);

        mPaint.setColorFilter(mColorMatrixFilter);

        float dis = (float) Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
        float f8 = (mCornerX - mBezierControl1.x) / dis;
        float f9 = (mBezierControl2.y - mCornerY) / dis;
        mMatrixArray[0] = 1 - 2 * f9 * f9;
        mMatrixArray[1] = 2 * f8 * f9;
        mMatrixArray[3] = mMatrixArray[1];
        mMatrixArray[4] = 1 - 2 * f8 * f8;
        mMatrix.reset();
        mMatrix.setValues(mMatrixArray);
        mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
        mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
        canvas.drawBitmap(bitmap, mMatrix, mPaint);

        mPaint.setColorFilter(null);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        float maxLength = (float) Math.hypot(getWidth(), getHeight());
        mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right, (int) (mBezierStart1.y + maxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }

    private void releaseUnnecessaryPages() {
        if (mPageList != null && mPageList.size() > 500) {
            if (mPageIndex <= 0) {
                mPageList = mPageList.subList(0, 500);
            } else if (mPageIndex >= mPageList.size() - 1) {
                mPageList = mPageList.subList(mPageList.size() - 500, mPageList.size());
            } else {
                int start = 0;
                int end = mPageList.size();
                if (mPageList.size() - mPageIndex >= 400) {
                    end = mPageIndex + 400;
                }
                if (mPageIndex > 100) {
                    start = mPageIndex - 100;
                }
                mPageList = mPageList.subList(start, end);
                mPageIndex = mPageIndex - start;
            }
        }
    }

    // 向前读若干页
    private List<Page> readPrePages() {
        if (mPageList == null || mPageList.size() == 0) {
            return null;
        }
        int end = mPageList.get(0).start;
        int length = READ_BUFFER_LENGTH;
        int start = end - length;
        if (start < 0) {
            start = 0;
            length = end;
        }

        String buffer = null;
        try {
            buffer = mBookModel.read(start, length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paging(buffer, start);
    }

    // 向后读若干页
    private List<Page> readNextPages() {
        int start = 0;
        if (mPageList != null && mPageList.size() > 0) {
            Page lastPage = mPageList.get(mPageList.size() - 1);
            start = lastPage.start;
            for (String str : lastPage.lines) {
                if (!TextUtils.isEmpty(str)) {
                    start += str.length();
                }
            }
        }
        int length = READ_BUFFER_LENGTH;
        if (start + length > mBookSize) {
            length = mBookSize - start;
        }

        String buffer = null;
        try {
            buffer = mBookModel.read(start, length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paging(buffer, start);
    }

    /**
     * 分页
     * 
     * @param buffer
     * @return
     */
    private List<Page> paging(String buffer, int startIndex) {
        List<Page> pages = new ArrayList<Page>();

        if (TextUtils.isEmpty(buffer)) {
            return pages;
        }

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            return pages;
        }

        mTextPaint.setTextSize(mBookViewOption.textSize);
        mLineCount = (height - mBookViewOption.topPadding - mBookViewOption.bottomPadding - mBookViewOption.titleTextSize * 2)
                / (mBookViewOption.textSize + mBookViewOption.lineSpace);

        int length = buffer.length();
        int index = 0;
        // int start = startIndex;

        Page page = new Page();
        page.start = startIndex;
        List<String> lines = new ArrayList<String>();

        while (index < length) {
            String paragraph = readParagraph(buffer, index);
            if (TextUtils.isEmpty(paragraph)) {
                break;
            }

            index += paragraph.length();

            // if (paragraph.indexOf("\r\n") != -1) {
            // paragraph.replaceAll("\r\n", "");
            // } else if (paragraph.indexOf("\n") != -1) {
            // paragraph.replaceAll("\n", "");
            // }

            /*
             * if (TextUtils.isEmpty(paragraph)) { lines.add(paragraph); } else
             * {
             */
            int i = 0;
            while (i < paragraph.length()) {
                int size = mTextPaint.breakText(paragraph, i, paragraph.length(), true, (width
                        - mBookViewOption.leftPadding - mBookViewOption.rightPadding), null);
                lines.add(paragraph.substring(i, i + size));
                i += size;

                if (lines.size() >= mLineCount) {
                    page.lines = lines;
                    pages.add(page);

                    int start = page.start;
                    for (String str : lines) {
                        if (str != null) {
                            start += str.length();
                        }
                    }

                    page = new Page();
                    page.start = start;
                    lines = new ArrayList<String>();
                }
            }

        }
        // if (lines.size() >= mLineCount) {
        // Page page = new Page();
        // page.lines = lines;
        // pages.add(page);
        // lines = new ArrayList<String>();
        // }
        // }

        if (lines.size() > 0) {
            page.lines = lines;
            pages.add(page);
        }

        return pages;
    }

    /**
     * 读一个段落
     * 
     * @param str
     * @param start
     * @return
     */
    private String readParagraph(String str, int start) {
        int i = str.indexOf("\n\r", start);
        if (i >= 0) {
            return str.substring(start, i + 2);
        } else {
            i = str.indexOf("\n", start);
            if (i >= 0) {
                return str.substring(start, i + 1);
            }
        }
        return str.substring(start);
    }

}
