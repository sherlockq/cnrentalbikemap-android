package com.wukongzou.chinarentalbicyclemap.http.baidufix;

import java.util.Locale;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;

/**
 * To adjust offset
 * 
 * @author Sherlock
 * 
 */
public class BaiduApiFixClient {
	// http://api.map.baidu.com/ag/coord/convert?from=0&to=2&x=121.590166&y=31.2054856
	public static final String BAIDU_FIX_URL = "http://api.map.baidu.com/ag/coord/convert?from=0&to=2&x=%f&y=%f";

	private static AsyncHttpClient client;

	public static void get(Context context, LatLng latLng,
			BaiduApiFixHttpResponseHandler responseHandler) {
		getClient().get(context, getAbsoluteUrl(latLng), responseHandler);
	}

	protected static String getAbsoluteUrl(LatLng latLng) {
		return String.format(Locale.US, BAIDU_FIX_URL, latLng.longitude, latLng.latitude);
	}

	/**
	 * Get client and init once
	 * 
	 * @return
	 */
	protected static AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
			// default timeout to 10 seconds
			client.setTimeout(10 * 1000);
		}
		return client;
	}
}