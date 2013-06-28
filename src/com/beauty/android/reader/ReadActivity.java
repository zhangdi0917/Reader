package com.beauty.android.reader;

import java.io.IOException;

import com.beauty.android.reader.setting.SettingManager;
import com.beauty.android.reader.view.Book;
import com.beauty.android.reader.view.BookView;
import com.beauty.android.reader.view.BookView.OnCenterClickListener;
import com.beauty.android.reader.view.BookView.OnLoadPageListener;
import com.beauty.android.reader.view.BookViewOption;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ReadActivity extends Activity implements OnClickListener {

    public static final String INTENT_EXTRA_BOOK = "extra_book";

    private BookView mBookView;

    private BookViewOption mBookViewOption;

    private Book mBook;

    private View mSettingView;

    private View mBackView;

    private ImageView mDayNightView;

    private View mLoadingView;

    private ProgressBar mProgressBar;

    private boolean mLight = true;

    private Bitmap mLightBgBm;
    private int mLightBgColor = Color.rgb(200, 200, 200);
    private int mLightTextColor = Color.rgb(28, 28, 28);
    private int mLightTitleColor = Color.rgb(40, 40, 40);

    private int mDarkBgColor = Color.rgb(28, 28, 28);
    private int mDarkTextColor = Color.rgb(200, 200, 200);
    private int mDarkTitleColor = Color.rgb(170, 170, 170);

    private WakeLock mWakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_read);

        mSettingView = findViewById(R.id.setting);
        mSettingView.setOnClickListener(this);

        mBackView = findViewById(R.id.back);
        mBackView.setOnClickListener(this);
        mDayNightView = (ImageView) findViewById(R.id.day_or_night);
        mDayNightView.setOnClickListener(this);

        mLoadingView = findViewById(R.id.loading_rl);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mBook = getIntent().getParcelableExtra(INTENT_EXTRA_BOOK);

        if (mBook == null) {
            finish();
        }

        MobclickAgent.onEvent(this, "read", mBook.name);

        int start = SettingManager.getInstance().getLastReadIndex(mBook.id);
        mBookView = (BookView) findViewById(R.id.book_view);
        mBookView.setOnLoadPageListener(mOnLoadPageListener);
        mBookView.setOnCenterClickListener(mOnCenterClickListener);

        mBookViewOption = mBookView.getBookViewOption();
        float density = getResources().getDisplayMetrics().density;
        mBookViewOption.transToPixel(density);

        mLight = true;
        mLightBgBm = BitmapFactory.decodeResource(getResources(), R.drawable.background_1);
        mBookViewOption.bgBm = mLightBgBm;
        mBookViewOption.textColor = mLightTextColor;
        mBookViewOption.bgColor = mLightBgColor;
        setScreenBrightness(255);

        try {
            mBookView.openBook(mBook, start);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");

    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private OnLoadPageListener mOnLoadPageListener = new OnLoadPageListener() {

        @Override
        public void onLoad() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoadingView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void loadOver() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    mLoadingView.setVisibility(View.GONE);
                }
            });
        }

    };

    private OnCenterClickListener mOnCenterClickListener = new OnCenterClickListener() {

        @Override
        public void onCenterClick() {
            mSettingView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBook != null) {
            SettingManager.getInstance().setLastReadIndex(mBook.id, mBookView.getCurrReadIndex());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.day_or_night:
            changeDayOrNight();
            break;
        case R.id.back:
            finish();
            break;
        case R.id.setting:
            if (mSettingView.getVisibility() == View.VISIBLE) {
                mSettingView.setVisibility(View.GONE);
            }
            break;
        }
    }

    private void changeDayOrNight() {
        if (mLight) {
            mLight = false;
            mBookViewOption.bgColor = mDarkBgColor;
            mBookViewOption.bgBm = null;
            mBookViewOption.textColor = mDarkTextColor;
            mBookViewOption.titleTextColor = mDarkTitleColor;
            mBookView.redraw();

            setScreenBrightness(50);

            mDayNightView.setImageResource(R.drawable.menu_day);
        } else {
            mLight = true;
            mBookViewOption.bgColor = mLightBgColor;
            mBookViewOption.bgBm = mLightBgBm;
            mBookViewOption.textColor = mLightTextColor;
            mBookViewOption.titleTextColor = mLightTitleColor;
            mBookView.redraw();

            setScreenBrightness(255);

            mDayNightView.setImageResource(R.drawable.menu_night);
        }
    }

    private void setScreenBrightness(int brightness) {
        if (brightness < 1) {
            brightness = 1;
        }
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = (float) (brightness / 255.0);
        getWindow().setAttributes(lp);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_in_from_left, R.anim.activity_slide_out_to_right);
    }

}
