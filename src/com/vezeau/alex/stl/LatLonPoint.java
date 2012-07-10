package com.vezeau.alex.stl;

import com.google.android.maps.GeoPoint;

public class LatLonPoint extends GeoPoint {

	public LatLonPoint(double latitude, double longitude) {
		super((int) (latitude * 1E6), (int) (longitude * 1E6));
	}

	public LatLonPoint(String latitude, String longitude) {
		this(Double.parseDouble(latitude), Double.parseDouble(longitude));
	}
}
