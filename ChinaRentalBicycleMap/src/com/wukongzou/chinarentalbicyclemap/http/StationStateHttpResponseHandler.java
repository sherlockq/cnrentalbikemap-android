package com.wukongzou.chinarentalbicyclemap.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;

import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wukongzou.chinarentalbicyclemap.constant.Constant;

public abstract class StationStateHttpResponseHandler extends
		AsyncHttpResponseHandler {

	protected static final int SUCCESS_JSON_MESSAGE = 100;

	public abstract void onSuccess(StationState response);

	//
	// Pre-processing of messages (executes in background threadpool thread)
	//
	@Override
	protected void sendSuccessMessage(int statusCode, String responseBody) {
		if (statusCode != HttpStatus.SC_NO_CONTENT) {
			try {
				StationState response = readStationState(responseBody);
				if (response == null) {
					sendFailureMessage(new IllegalArgumentException(
							"Response not recognizable"), responseBody);
				} else {
					sendMessage(obtainMessage(SUCCESS_JSON_MESSAGE, response));
				}
			} catch (Exception e) {
				sendFailureMessage(e, responseBody);
			}
		} else {
			sendFailureMessage(new IllegalArgumentException("No response"),
					responseBody);
		}
	}

	//
	// Pre-processing of messages (in original calling thread, typically the UI
	// thread)
	//
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case SUCCESS_JSON_MESSAGE:
			StationState response = (StationState) msg.obj;
			onSuccess(response);
			break;
		default:
			super.handleMessage(msg);
		}
	}

	protected static final Pattern PATTERN_STATION_STATE_BICYCLE = Pattern
			.compile("车(\\d+)");
	protected static final Pattern PATTERN_STATION_STATE_PORT = Pattern
			.compile("位(\\d+)");

	protected StationState readStationState(String responseText) {
		if (responseText == null || responseText.trim().length() == 0) {
			return null;
		}
		Matcher m = PATTERN_STATION_STATE_BICYCLE.matcher(responseText);
		boolean hasInfo = false;
		StationState state = new StationState();
		if (m.find()) {
			state.bicycles = Integer.valueOf(m.group(1));
			hasInfo = true;
		}
		m = PATTERN_STATION_STATE_PORT.matcher(responseText);
		if (m.find()) {
			state.ports = Integer.valueOf(m.group(1));
			hasInfo = true;
		}
		if (!hasInfo) {
			Log.w(Constant.LOG_TAG_APP, "not recognizable state: "
					+ responseText);
			return null;
		}

		return state;

	}

}