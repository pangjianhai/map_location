package cn.com.maplocation;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;

public class MainActivity extends Activity {

	private TextView position_text_view;

	private LocationManager lm;

	private String provider;

	// private BMapManager manager;
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		position_text_view = (TextView) findViewById(R.id.position_text_view);

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providerList = lm.getProviders(true);
		if (providerList.contains(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		} else {
			Toast.makeText(getApplicationContext(), "没有地理位置提供器",
					Toast.LENGTH_SHORT).show();
			return;
		}
		System.out.println("--------------------------->>>>>>>>>>>" + provider);
		Location location = lm.getLastKnownLocation(provider);
		if (location != null) {
			showLocation(location);
		}
		System.out.println("--------------------------->>>>>>>>>>>" + location);
		lm.requestLocationUpdates(provider, 1000, 1, ll);
	}

	LocationListener ll = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			System.out
					.println("onLocationChanged--------------------------->>>>>>>>>>>"
							+ location);
			showLocation(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	};

	private void showLocation(Location l) {
		String lat = l.getLatitude() + "";
		String lon = l.getLongitude() + "";
		String str = lat + "\r\n" + lon;
		position_text_view.setText(str);
	}

	private void init() {
		mapView = (MapView) findViewById(R.id.map_view);
	}

}
