package com.wukongzou.chinarentalbicyclemap.http.baidufix;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.JsonHttpResponseHandler;

public abstract class BaiduApiFixHttpResponseHandler extends
		JsonHttpResponseHandler {

	protected static final String KEY_ERROR = "error";
	protected static final String KEY_X = "x";
	protected static final String KEY_Y = "y";

	@Override
	public void onSuccess(JSONObject object) {
		try {
			if (object != null && object.getInt(KEY_ERROR) == 0) {
				String x = new String(Base64.decode(object.getString(KEY_X),
						Base64.DEFAULT));
				String y = new String(Base64.decode(object.getString(KEY_Y),
						Base64.DEFAULT));
				onSuccess(new LatLng(Double.valueOf(y), Double.valueOf(x)));
			} else {
				onFailure(new IllegalArgumentException("wrong response"),
						object);
			}
		} catch (JSONException e) {
			Log.e("baidu_api", "handle baidu resposne error: " + e);
		} catch (Exception e) {
			onFailure(e, object);
		}
	}

	/**
	 * will return a offsetlatlng if success
	 * 
	 * @param offsetLatlng
	 */
	public abstract void onSuccess(LatLng offsetLatlng);

}