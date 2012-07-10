package com.vezeau.alex.stl.util;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapUtil {

	private MapUtil() {

	}

	public static void zoomMapFromMaxLatLong(MapController mc,
			LatLongLimit limits) {

		GeoPoint max = new GeoPoint((int) limits.maxLatitude,
				(int) limits.maxLongitude);
		GeoPoint min = new GeoPoint((int) limits.minLatitude,
				(int) limits.minLongitude);

		int maxLatMicro = max.getLatitudeE6();
		int maxLonMicro = max.getLongitudeE6();
		int minLatMicro = min.getLatitudeE6();
		int minLonMicro = min.getLongitudeE6();

		mc.zoomToSpan(maxLatMicro - minLatMicro, maxLonMicro - minLonMicro);


	}

	public static GeoPoint getCenter(LatLongLimit limits) {
		GeoPoint max = new GeoPoint((int) limits.minLatitude,
				(int) limits.maxLongitude);
		GeoPoint min = new GeoPoint((int) limits.minLatitude,
				(int) limits.minLongitude);

		int maxLatMicro = max.getLatitudeE6();
		int maxLonMicro = max.getLongitudeE6();
		int minLatMicro = min.getLatitudeE6();
		int minLonMicro = min.getLongitudeE6();

		GeoPoint center = new GeoPoint((maxLatMicro + minLatMicro) / 2,
				(maxLonMicro + minLonMicro) / 2);

		return center;
	}

	public static void zoomTo(MapController mc, GeoPoint gp, int zoomLevel) {
		mc.setCenter(gp);
		mc.setZoom(zoomLevel);
	}

	public static void clearOverlays(MapView mapView) {
		if (!mapView.getOverlays().isEmpty()) {
			mapView.getOverlays().clear();
			mapView.invalidate();

		}
	}

}
