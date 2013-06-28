package com.beauty.android.reader;

import java.io.IOException;

import com.beauty.android.reader.setting.SettingManager;
import com.beauty.android.reader.view.Book;
import com.beauty.android.reader.view.BookView;
import com.beauty.android.reader.view.BookView.BookViewListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ProgressBar;

public class ReadActivity extends Activity implements OnClickListener {

    public static final String INTENT_EXTRA_BOOK = "extra_book";

    private BookView mBookView;

    private Book mBook;

    private View mSettingView;

    private View mProgressView;

    private View mBrightnessView;

    private View mFontSizeView;

    private View mLoadingView;

    private ProgressBar mProgressBar;

    // private boolean mLight = false;
    // private int mLightBgColor = 0xffffffff;
    // private int mDarkBgColor = 0xff000000;
    // private int mLightTextColor = 0xffffff;
    // private int mDarkTextColor = Color.rgb(28, 28, 28);

    private WakeLock mWakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_read);

        mSettingView = findViewById(R.id.setting);
        mSettingView.setOnClickListener(this);

        mProgressView = findViewById(R.id.progress);
        mProgressView.setOnClickListener(this);
        mBrightnessView = findViewById(R.id.brightness);
        mBrightnessView.setOnClickListener(this);
        mFontSizeView = findViewById(R.id.font_size);
        mFontSizeView.setOnClickListener(this);

        mLoadingView = findViewById(R.id.loading_rl);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mBook = getIntent().getParcelableExtra(INTENT_EXTRA_BOOK);

        if (mBook == null) {
            finish();
        }

        int start = SettingManager.getInstance().getLastReadIndex(mBook.id);
        mBookView = (BookView) findViewById(R.id.book_view);
        mBookView.setBookViewListener(mBookViewListener);

        try {
            mBookView.openBook(mBook, start);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");

    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private BookViewListener mBookViewListener = new BookViewListener() {

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

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
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
        case R.id.brightness:
            brightness();
            break;
        case R.id.setting:
            if (mSettingView.getVisibility() == View.VISIBLE) {
                mSettingView.setVisibility(View.GONE);
            }
            break;
        }
    }

    private void brightness() {
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_in_from_left, R.anim.activity_slide_out_to_right);
    }

}
