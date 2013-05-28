package com.wukongzou.chinarentalbicyclemap.http.cmbstate;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Static way to get station state
 * 
 * @author Sherlock
 * 
 */
public class ChinarmbStateClient {
	public static final String STATION_STATE_URL = "http://self.chinarmb.com/stationinfo.aspx?snumber=";

	private static AsyncHttpClient client;

	public static void get(Context context, String stationId,
			StationStateHttpResponseHandler responseHandler) {
		getClient().get(context, getAbsoluteUrl(stationId), responseHandler);
	}

	protected static String getAbsoluteUrl(String stationId) {
		return STATION_STATE_URL + stationId;
	}

	/**
	 * Get client and init once
	 * 
	 * @return
	 */
	protected static AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient();
			// default timeout to 30 seconds
			client.setTimeout(30 * 1000);
		}
		return client;
	}
}