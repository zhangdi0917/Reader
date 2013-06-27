package com.beauty.android.reader.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingManager {

    private static SettingManager gSettingManager;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences.Editor mEditor;

    private Context mContext;

    public static synchronized SettingManager getInstance() {
        if (gSettingManager == null) {
            gSettingManager = new SettingManager();
        }
        return gSettingManager;
    }

    private SettingManager() {

    }

    public void init(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    }

    public int getLastReadIndex(int bookId) {
        return mSharedPreferences.getInt(getLastReadKey(bookId), 0);
    }

    public void setLastReadIndex(int bookId, int index) {
        mEditor.putInt(getLastReadKey(bookId), index);
        mEditor.commit();
    }

    private static final String BOOK_KEY_BASE = "last_read_key";

    private String getLastReadKey(int bookId) {
        return BOOK_KEY_BASE + bookId;
    }

}
