package com.wukongzou.chinarentalbicyclemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.InfoWindowAdapter;
import pl.mg6.android.maps.extensions.GoogleMap.OnInfoWindowClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMyLocationChangeListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wukongzou.chinarentalbicyclemap.http.ChinarmbStateClient;
import com.wukongzou.chinarentalbicyclemap.http.StationState;
import com.wukongzou.chinarentalbicyclemap.http.StationStateHttpResponseHandler;
import com.wukongzou.chinarentalbicyclemap.marker.ClusterIconProvider;

public class MainActivity extends SherlockFragmentActivity {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onLocationClick(MenuItem item) {
		mMap.setMyLocationEnabled(true);
	}

	public void onAboutClick(MenuItem item) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
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
					.findFragmentById(R.id.map)).getExtendedMap();
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

	class StationInfoWindowAdapter implements InfoWindowAdapter {

		private final View mContents;

		StationInfoWindowAdapter() {
			mContents = getLayoutInflater().inflate(
					R.layout.include_station_info_contents, null);
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}

		@Override
		public View getInfoContents(Marker marker) {
			if (marker.isCluster()) {
				renderClusterInfo(marker, mContents);
				return mContents;

			} else {
				// for normal station mark
				StationMarkerData data = (StationMarkerData) marker.getData();
				if (data == null) {
					// maybe other type of marker
					return null;
				}
				renderStationInfo(data, mContents);
				return mContents;
			}

		}

		private void renderClusterInfo(Marker marker, View view) {
			// don't care after order, just get any three names
			List<Marker> markers = marker.getMarkers();
			int i = 0;
			StringBuilder text = new StringBuilder();
			for (Iterator<Marker> markerIter = markers.iterator(); markerIter
					.hasNext() && i < 3;) {
				Marker m = markerIter.next();
				String title = m.getTitle();
				if (title == null) {
					continue;
				}
				text.append(title).append("\n");
				i++;
			}
			TextView titleUi = ((TextView) view
					.findViewById(R.id.station_infowindow_title));
			titleUi.setText(R.string.show_cluster_detail);
			TextView snippetUi = ((TextView) view
					.findViewById(R.id.station_infowindow_snippet));
			if (text.length() > 0)
				text.deleteCharAt(text.length() - 1);
			snippetUi.setText(text);
		}

		private void renderStationInfo(StationMarkerData data, View view) {
			String title = data.stationName;
			TextView titleUi = ((TextView) view
					.findViewById(R.id.station_infowindow_title));
			if (title != null) {
				// Spannable string allows us to edit the formatting of the
				// text.
				// SpannableString titleText = new SpannableString(title);
				// titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
				// titleText.length(), 0);
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}

			TextView snippetUi = ((TextView) view
					.findViewById(R.id.station_infowindow_snippet));
			if (data.cachedState == null) {
				snippetUi.setText(R.string.loading_state);
			} else {
				snippetUi.setText(formatStationState(data.cachedState));
			}
		}

	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera. In this case, we just add a marker near Africa.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.setMyLocationEnabled(true);
		mMap.setClustering(new ClusteringSettings().clusterSize(96)
				.addMarkersDynamically(true)
				.iconDataProvider(new ClusterIconProvider(getResources())));
		// set default location
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(31.14106,
				121.35789), 10));
		// init locaiton
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
						if (positionMarker != null) {
							positionMarker.remove();
							positionMarker = null;
						}
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

		mMap.setInfoWindowAdapter(new StationInfoWindowAdapter());

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				if (marker.isCluster()) {
					List<Marker> markers = marker.getMarkers();
					Builder builder = LatLngBounds.builder();
					for (Marker m : markers) {
						builder.include(m.getPosition());
					}
					LatLngBounds bounds = builder.build();
					mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
							bounds,
							getResources().getDimensionPixelSize(
									R.dimen.cluster_zoom_padding)));
				} else {
					// re-request
					requestStationState(marker);
				}
			}
		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(final Marker marker) {
				requestStationState(marker);
				return false;
			}
		});

		loadStationMarkers();
	}

	/**
	 * Will check if marker is a station marker, if not, do nothing.
	 * 
	 * @param marker
	 */
	protected void requestStationState(final Marker marker) {
		// a real station marker
		if (!marker.isCluster()
				&& marker.getData() instanceof StationMarkerData) {
			// show loading
			StationMarkerData data = (StationMarkerData) marker.getData();
			findViewById(R.id.textState).setVisibility(View.VISIBLE);
			ChinarmbStateClient.get(MainActivity.this, data.stationId,
					new StationStateHttpResponseHandler() {

						@Override
						public void onSuccess(StationState response) {
							findViewById(R.id.textState).setVisibility(
									View.INVISIBLE);
							StationMarkerData data = (StationMarkerData) marker
									.getData();
							data.cachedState = response;

							// check if there's a marker current showing and if
							// same
							if (mMap.getMarkerShowingInfoWindow() != null
									&& mMap.getMarkerShowingInfoWindow()
											.equals(marker)) {
								marker.showInfoWindow();
							}
						}

						public void onFailure(Throwable ex, String response) {
							findViewById(R.id.textState).setVisibility(
									View.INVISIBLE);
							Toast.makeText(MainActivity.this,
									"ERROR:" + ex.toString(), Toast.LENGTH_LONG)
									.show();
						};
					});
		}
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
				StationMarkerData data = new StationMarkerData();
				data.stationId = info[4];
				data.stationName = info[2];
				data.stationRegion = info[3];
				LatLng latLng = new LatLng(Double.valueOf(info[0]),
						Double.valueOf(info[1]));
				MarkerOptions marker = new MarkerOptions().position(latLng)
						.title(data.stationName);
				marker.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_cycling));

				mMap.addMarker(marker).setData(data);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffreader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected String formatStationState(StationState state) {
		return getString(R.string.station_state, state.bicycles, state.ports);
	}

	protected class StationMarkerData {
		public String stationName;
		public String stationId;
		public String stationRegion;
		public StationState cachedState;
	}

}
