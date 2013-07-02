package com.beauty.android.reader;

import java.text.DecimalFormat;
import java.util.HashMap;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotManager;

import com.beauty.android.reader.setting.SettingManager;
import com.beauty.android.reader.utils.NetworkManager;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ReadActivity extends Activity implements OnClickListener {

    public static final String INTENT_EXTRA_BOOK = "extra_book";

    public static final int SHOW_SPOT_AD_DELAY = 5 * 60 * 1000;

    private BookView mBookView;

    private BookViewOption mBookViewOption;

    private Book mBook;

    private View mSettingView;

    private View mBackView;

    private ImageView mDayNightView;

    private View mLoadingView;

    private ProgressBar mProgressBar;

    private SeekBar mSeekBar;

    private TextView mSeekHintText;

    private int mCurrProgress = 0;

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

        // 实例化广告条
        AdView adView = new AdView(this, AdSize.SIZE_320x50);
        // 获取要嵌入广告条的布局
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
        // 将广告条加入到布局中
        adLayout.addView(adView);

        // 展示插播广告
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SpotManager.getInstance(getApplicationContext()).showSpotAds(ReadActivity.this);
            }
        }, SHOW_SPOT_AD_DELAY);

        mSettingView = findViewById(R.id.setting);
        mSettingView.setOnClickListener(this);

        mBackView = findViewById(R.id.back);
        mBackView.setOnClickListener(this);
        mDayNightView = (ImageView) findViewById(R.id.day_or_night);
        mDayNightView.setOnClickListener(this);

        mLoadingView = findViewById(R.id.loading_rl);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        mSeekHintText = (TextView) findViewById(R.id.progress_hint);

        mBook = getIntent().getParcelableExtra(INTENT_EXTRA_BOOK);

        if (mBook == null) {
            finish();
        }

        boolean hasNetwork = NetworkManager.isConnectionAvailable(this);
        HashMap<String, String> extras = new HashMap<String, String>();
        extras.put("book", mBook.name);
        extras.put("network", hasNetwork ? "network" : "none");
        MobclickAgent.onEvent(this, "read", extras);

        int start = SettingManager.getInstance().getLastReadIndex(mBook.id);
        mBookView = (BookView) findViewById(R.id.book_view);
        mBookView.setOnLoadPageListener(mOnLoadPageListener);
        mBookView.setOnCenterClickListener(mOnCenterClickListener);

        mBookViewOption = mBookView.getBookViewOption();
        float density = getResources().getDisplayMetrics().density;
        mBookViewOption.transToPixel(density);

        mLightBgBm = BitmapFactory.decodeResource(getResources(), R.drawable.background_1);
        mLight = SettingManager.getInstance().isLightTheme();
        changeTheme(mLight);

        mBookView.openBook(mBook, start);

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
            mSeekHintText.setVisibility(View.INVISIBLE);
            mSeekBar.setVisibility(View.VISIBLE);
            int progress = mBookView.getCurrReadIndex() * mSeekBar.getMax() / mBookView.getBookSize();
            mSeekBar.setProgress(progress);
        }
    };

    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mCurrProgress = progress;

            float fPercent = (float) (progress * 1.0 / seekBar.getMax());
            DecimalFormat df = new DecimalFormat("#0.0");
            String strPercent = "位于全书的" + df.format(fPercent * 100) + "%处";
            mSeekHintText.setText(strPercent);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            float fPercent = (float) (seekBar.getProgress() * 1.0 / seekBar.getMax());
            DecimalFormat df = new DecimalFormat("#0.0");
            String strPercent = "位于全书的" + df.format(fPercent * 100) + "%处";
            mSeekHintText.setVisibility(View.VISIBLE);
            mSeekHintText.setText(strPercent);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int offset = mBookView.getBookSize() * mCurrProgress / seekBar.getMax();
            if (offset >= mBookView.getBookSize()) {
                offset = mBookView.getBookSize() - 1;
            }
            mBookView.seekTo(offset);

            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mSeekHintText != null) {
                        mSeekHintText.setVisibility(View.INVISIBLE);
                    }
                }
            }, 1000);
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

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.day_or_night:
            if (mLight) {
                mLight = false;
            } else {
                mLight = true;
            }
            changeTheme(mLight);
            mBookView.redraw();
            SettingManager.getInstance().setIsLightTheme(mLight);
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

    private void changeTheme(boolean light) {
        if (!light) {
            mBookViewOption.bgColor = mDarkBgColor;
            mBookViewOption.bgBm = null;
            mBookViewOption.textColor = mDarkTextColor;
            mBookViewOption.titleTextColor = mDarkTitleColor;

            setScreenBrightness(50);
            mDayNightView.setImageResource(R.drawable.menu_day);
        } else {
            mBookViewOption.bgColor = mLightBgColor;
            mBookViewOption.bgBm = mLightBgBm;
            mBookViewOption.textColor = mLightTextColor;
            mBookViewOption.titleTextColor = mLightTitleColor;

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
