package com.vezeau.alex.stl;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.vezeau.alex.stl.util.MapUtil;

public class MapActivityBusStop extends MapActivity {

	private MapView mapView;
	private MapController mc;
	private GeoPoint p;

	private String latitude;
	private String longitude;
	private String times;
	private Context context = MapActivityBusStop.this;

	class MapOverlay extends com.google.android.maps.Overlay {
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			// ---translate the GeoPoint to screen pixels---
			Point screenPts = new Point();
			mapView.getProjection().toPixels(p, screenPts);

			// ---add the marker---
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.pushpin);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);
			return true;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			// ---when user lifts his finger---
			if (event.getAction() == 1) {
				GeoPoint p = mapView.getProjection().fromPixels(
						(int) event.getX(), (int) event.getY());

				Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
				try {
					List<Address> addresses = geoCoder.getFromLocation(
							p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6,
							1);

					String add = "";
					if (addresses.size() > 0) {
						for (int i = 0; i < addresses.get(0)
								.getMaxAddressLineIndex(); i++)
							add += addresses.get(0).getAddressLine(i) + "\n";
					}
					Toast.makeText(context, add, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			return false;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	public void gotoLocation() {

		p = new LatLonPoint(latitude, longitude);
		mc.animateTo(p);
		mc.setZoom(16);

		// ---Add a location marker---
		MapOverlay mapOverlay = new MapOverlay();
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);

		mapView.invalidate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showmap);

		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.pushpin);
		OverlayItemizedSTL itemizedoverlay = new OverlayItemizedSTL(drawable,
				this);

		Bundle extras = getIntent().getExtras();
		latitude = (String) extras.get("lat");
		longitude = (String) extras.get("long");
		times = (String) extras.get("times");

		GeoPoint point = new LatLonPoint(latitude, longitude);
		OverlayItem overlayitem = new OverlayItem(point, "Next bus at:", times);

		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);

		MapUtil.zoomTo(mapView.getController(), point, 18);

		mapView.invalidate();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}