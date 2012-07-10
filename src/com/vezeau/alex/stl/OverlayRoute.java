package com.vezeau.alex.stl;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class OverlayRoute extends Overlay {

	private GeoPoint gp1;
	private GeoPoint gp2;
	private int color;

	public OverlayRoute(GeoPoint gp1, GeoPoint gp2, int color) {
		this.gp1 = gp1;
		this.gp2 = gp2;
		this.color = color;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	    Projection projection = mapView.getProjection();
	    Point point = new Point();
	    projection.toPixels(gp1, point);
	    
	    Point point2 = new Point();
	    projection.toPixels(gp2, point2);
	    Paint paint = createPaintObj();
	    canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
	    super.draw(canvas, mapView, shadow);
	}

	private Paint createPaintObj() {
		Paint paint = new Paint();
	    paint.setStrokeWidth(7);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);
	    paint.setStrokeCap(Paint.Cap.ROUND);
	    paint.setDither(true);  
	    paint.setAlpha(75);
	    paint.setColor(color);
		return paint;
	}	

}
