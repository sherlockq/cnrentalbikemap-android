package com.wukongzou.chinarentalbicyclemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this FragmentActivity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the FragmentActivity may not have been completely destroyed during this
	 * process (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * current position marker
	 */
	private Marker positionMarker;

	/**
	 * rental stations, should mostly in current screen
	 */
	private Marker[] stationMarkers;

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
			@Override
			public void onMyLocationChange(Location location) {
				Log.d("LOCATION", "location changed: " + location);
				if (location != null) {
					if (location.getAccuracy() < 100) {
						LatLng currentPosition = new LatLng(location
								.getLatitude(), location.getLongitude());
						CameraPosition position = CameraPosition
								.fromLatLngZoom(currentPosition, 15);

						positionMarker = mMap.addMarker(new MarkerOptions()
								.position(currentPosition)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
								.title("I'm Here~"));
						mMap.moveCamera(CameraUpdateFactory
								.newCameraPosition(position));
						// just shut location tracking down
						mMap.setMyLocationEnabled(false);
					} else {
						Log.d("LOCATION",
								"location accuracy not sufficient now: "
										+ location.getAccuracy());
					}
				}
			}
		});

		loadStationMarkers();
		// mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
		//
		// @Override
		// public View getInfoWindow(Marker marker) {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public View getInfoContents(Marker marker) {
		// // TODO Auto-generated method stub
		// return null;
		// }
		// });
		final Context context = this;
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.getSnippet() != null) {
					String snip = marker.getSnippet();
					String stationId = snip.substring(snip.indexOf("|") + 1);
					StationState state = readStationState(stationId);
					if (state != null) {
						Toast.makeText(
								context,
								"bycycle: " + state.bycycles + ", ports: "
										+ state.ports, Toast.LENGTH_LONG)
								.show();
					}
				}
				return false;
			}
		});
	}

	protected void loadStationMarkers() {

		InputStream inputStream = getResources().openRawResource(
				R.raw.station_list);
		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;

		try {
			while ((line = buffreader.readLine()) != null) {
				String[] info = line.split("\\|");
				LatLng latLng = new LatLng(Double.valueOf(info[0]),
						Double.valueOf(info[1]));
				mMap.addMarker(new MarkerOptions().position(latLng)
						.title(info[2]).snippet(info[3] + "|" + info[4]));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final String STATION_STATE_URL = "http://self.chinarmb.com/stationinfo.aspx?snumber=";

	protected static final Pattern PATTERN_STATION_STATE_BICYCLE = Pattern
			.compile("车(\\d+)");
	protected static final Pattern PATTERN_STATION_STATE_PORT = Pattern
			.compile("位(\\d+)");

	protected StationState readStationState(String stationId) {
		String url = STATION_STATE_URL + stationId;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);

			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line);
			}
			in.close();
			// parse it
			CharSequence text = str.subSequence(0, str.length());
			boolean hasInfo = false;

			Matcher m = PATTERN_STATION_STATE_BICYCLE.matcher(text);
			StationState state = new StationState();
			if (m.find()) {
				state.bycycles = Integer.valueOf(m.group(1));
				hasInfo = true;
			}
			m = PATTERN_STATION_STATE_PORT.matcher(text);
			if (m.find()) {
				state.ports = Integer.valueOf(m.group(1));
				hasInfo = true;
			}
			if (!hasInfo) {
				Log.w("state_fetch", "not recognizable state: " + str);
				return null;
			}
			return state;

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "station state load failed",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	public class StationState {
		public int bycycles;
		public int ports;
	}
}
