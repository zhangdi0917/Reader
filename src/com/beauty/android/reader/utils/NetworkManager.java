package com.beauty.android.reader.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkManager {

	/**
	 * 
	 * @param context
	 * @return 如果wifi可用返回true，否则返回false；
	 */
	public static boolean isWifiAvailable(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean flag = false;
		if ((wifi != null) && (wifi.isAvailable())) {
			if (wifi.isConnected()) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 
	 * @param cotext
	 * @return 如果有可用的网络联接返回true，否则false
	 */
	public static boolean isConnectionAvailable(Context cotext) {
		boolean flag = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) cotext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			NetworkInfo activeNetworkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
				flag = false;
			} else {
				flag = true;
			}
		} else {
			
		}
		return flag;
	}
}
