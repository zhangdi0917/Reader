package com.beauty.android.reader;

import com.beauty.android.reader.setting.SettingManager;

import android.app.Application;

public class ReadApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SettingManager.getInstance().init(this);
    }

}
