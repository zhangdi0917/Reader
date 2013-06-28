/**
 * ViewUtils.java
 */
package com.beauty.android.reader.utils;

import java.lang.reflect.Method;

import android.graphics.Paint;
import android.view.View;

/**
 * @author Shaofeng Wang 2013-4-13下午1:01:46
 */
public class ViewUtils {

	private static final String METHOD_SET_LAYER_TYPE = "setLayerType";
	private static final String METHOD_GET_LAYER_TYPE = "getLayerType";

	public static final int LAYER_TYPE_NONE = 0;
	
	public static final int LAYER_TYPE_SOFTWARE = 1;

	public static final int LAYER_TYPE_HARDWARE = 2;

	public static void setLayerType(View view, int layerType, Paint paint) {

		if (view == null) {
			return;
		}

		Method methodSet;
		Method methodGet;

		try {
			methodGet = View.class.getDeclaredMethod(METHOD_GET_LAYER_TYPE);
			if (methodGet == null) {
				return;
			}

			int type = (Integer) methodGet.invoke(view);
			if (type == layerType) {
				return;
			}

			methodSet = View.class.getDeclaredMethod(METHOD_SET_LAYER_TYPE, int.class, Paint.class);
			if (methodSet != null) {
				methodSet.invoke(view, layerType, paint);
			}
		} catch (Exception e) {
		}
	}

}
