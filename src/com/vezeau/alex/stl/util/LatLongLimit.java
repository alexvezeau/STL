package com.vezeau.alex.stl.util;

public class LatLongLimit {

	public double maxLatitude;
	public double minLatitude;
	public double maxLongitude;
	public double minLongitude;

	public LatLongLimit(double minLatitude, double maxLatitude,
			double minLongitude, double maxLongitude) {
		this.maxLatitude = maxLatitude;
		this.minLatitude = minLatitude;
		this.maxLongitude = maxLongitude;
		this.minLongitude = minLongitude;
	}

	public LatLongLimit(String minLatitude, String maxLatitude,
			String minLongitude, String maxLongitude) {
		this.maxLatitude = Double.valueOf(maxLatitude);
		this.minLatitude = Double.valueOf(minLatitude);
		this.maxLongitude = Double.valueOf(maxLongitude);
		this.minLongitude = Double.valueOf(minLongitude);
	}

}
