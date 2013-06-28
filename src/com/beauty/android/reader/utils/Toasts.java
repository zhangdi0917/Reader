/**
 * Toasts.java
 */
package com.beauty.android.reader.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author Di Zhang Oct 16, 20123:52:20 PM
 */
public class Toasts {

    private static Toasts gToasts;
    private Context mContext;
    private Toast mToast;

    public synchronized static Toasts getInstance(Context context) {
        if (gToasts == null) {
            gToasts = new Toasts(context);
        }
        return gToasts;
    }

    private Toasts(Context context) {
        mContext = context.getApplicationContext();
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
    }

    public void show(int resId, int duration) {
        mToast.setText(resId);
        mToast.setDuration(duration);
        mToast.show();
    }

    public void show(CharSequence text, int duration) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        mToast.setText(text);
        mToast.setDuration(duration);
        mToast.show();
    }

}
