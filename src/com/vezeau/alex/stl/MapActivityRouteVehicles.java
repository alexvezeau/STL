package com.vezeau.alex.stl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.vezeau.alex.stl.util.HTTPUtil;
import com.vezeau.alex.stl.util.MapUtil;

public class MapActivityRouteVehicles extends MapActivity {

	private MapView mapView;
	private MapController mc;
	private Button btnStartRefresh;
	private Button btnStopRefresh;
	private TextView tvLastUpdated;

	private String tag = "REALTIME";

	private final Context context = MapActivityRouteVehicles.this;
	private Timer timer;

	private OverlayItemizedVehicle vio;

	class UpdateTimeTask extends TimerTask {
		public void run() {
			Log.d(tag, "Calling AutoRefresh");
			runOnUiThread(new Runnable() {
				public void run() {
					refreshMap();
				}
			});

		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		btnStartRefresh.setEnabled(true);
		btnStopRefresh.setEnabled(false);
		Log.d(tag, "Paused:  Stopped timer");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showroute);

		mapView = (MapView) findViewById(R.id.routeMap);
		mc = mapView.getController();
		mapView.setBuiltInZoomControls(true);

		btnStartRefresh = (Button) findViewById(R.id.btnStartRefreshMap);
		btnStopRefresh = (Button) findViewById(R.id.btnStopRefreshMap);
		tvLastUpdated = (TextView) findViewById(R.id.tvLastUpdated);
		btnStopRefresh.setEnabled(false);

		refreshLastUpdatedTime();

		btnStartRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				btnStartRefresh.setEnabled(false);
				btnStopRefresh.setEnabled(true);
				startRefreshMapTimer();
			}
		});

		btnStopRefresh.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				timer.cancel();
				btnStartRefresh.setEnabled(true);
				btnStopRefresh.setEnabled(false);
			}
		});

		drawPath(FragmentRightSTL.busPointListCache, Color.BLUE);

		int mid = (int) FragmentRightSTL.busPointListCache.size() / 2;
		int lat = (int) (Double.valueOf(FragmentRightSTL.busPointListCache
				.get(mid).latitude) * 1E6);
		int lon = (int) (Double.valueOf(FragmentRightSTL.busPointListCache
				.get(mid).longitude) * 1E6);

		GeoPoint gp = new GeoPoint(lat, lon);

		mc.animateTo(gp);
		mc.setZoom(13);

		mapView.invalidate();

	}

	private void refreshLastUpdatedTime() {
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(
				new Date());

		tvLastUpdated.setText("Last updated at: " + currentDateTimeString);
	}

	private void refreshMap() {
		addVehicleOverlay(mapView.getOverlays());
		mapView.invalidate();
		refreshLastUpdatedTime();
	}

	private void startRefreshMapTimer() {

		if (timer != null) {
			timer.cancel();

		}

		timer = new Timer();
		timer.schedule(new UpdateTimeTask(), 1000, 15000);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void drawPath(List<DataBusPoint> busPoints, int color) {
		List<Overlay> overlays = mapView.getOverlays();

		// Clear previous overlays
		MapUtil.clearOverlays(mapView);

		List<List<DataBusPoint>> busPointLists = groupByPathNumber(busPoints);

		for (List<DataBusPoint> subList : busPointLists) {

			LatLonPoint gp1 = null;
			LatLonPoint gp2 = null;

			for (int i = 1; i < subList.size(); i++) {

				DataBusPoint busPoint1 = subList.get(i - 1);
				DataBusPoint busPoint2 = subList.get(i);

				gp1 = new LatLonPoint(busPoint1.latitude, busPoint1.longitude);
				gp2 = new LatLonPoint(busPoint2.latitude, busPoint2.longitude);

				overlays.add(new OverlayRoute(gp1, gp2, color));
			}

		}

		addVehicleOverlay(overlays);
	}

	private void addVehicleOverlay(List<Overlay> overlays) {
		List<DataVehicle> vehicleListCache = populateVehicleData(FragmentRightSTL.busLine);

		overlays.remove(vio);
		vio = new OverlayItemizedVehicle(this.getResources().getDrawable(
				R.drawable.bus), context);

		for (DataVehicle vehicle : vehicleListCache) {
			LatLonPoint gp = new LatLonPoint(vehicle.latitude,
					vehicle.longitude);
			OverlayItem vehicleOverlay = new OverlayItem(gp,
					"Vehicle Coordinates", "Latitude: " + vehicle.latitude
							+ "\n" + "Longitude: " + vehicle.longitude + "\n"
							+ "Heading: " + vehicle.heading + "\n"
							+ "Vehicle Id: " + vehicle.vehicleId);
			vio.addOverlay(vehicleOverlay);
		}

		overlays.add(vio);
	}

	private List<List<DataBusPoint>> groupByPathNumber(
			List<DataBusPoint> busPoints) {
		int current = 1;
		List<List<DataBusPoint>> returnedList = new ArrayList<List<DataBusPoint>>();
		List<DataBusPoint> subList = new ArrayList<DataBusPoint>();

		for (Iterator<DataBusPoint> iterator = busPoints.iterator(); iterator
				.hasNext();) {
			DataBusPoint bp = (DataBusPoint) iterator.next();
			if (bp.pathNumber == current) {
				subList.add(bp);
			} else {
				returnedList.add(subList);
				current = bp.pathNumber;
				subList = new ArrayList<DataBusPoint>();
				subList.add(bp);
			}
		}

		returnedList.add(subList);

		return returnedList;
	}

	private List<DataVehicle> populateVehicleData(final String busLine) {
		STLVehicleRequest requestTask = new STLVehicleRequest();
		List<DataVehicle> dataList = null;

		try {
			if (HTTPUtil.isNetworkAvailable(this)) {
				dataList = requestTask.execute(busLine).get();
			} else {
				HTTPUtil.showNoNetworkToast(this);
			}

		} catch (InterruptedException e) {
			Toast.makeText(context, "InterruptedException :" + e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (ExecutionException e) {
			Toast.makeText(context, "ExecutionException :" + e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		return dataList;
	}

	private class STLVehicleRequest extends
			AsyncTask<String, Void, List<DataVehicle>> {

		@Override
		protected List<DataVehicle> doInBackground(String... busLine) {
			final String restURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=stl&t=0&r="
					+ busLine[0];

			Log.d("REST_URL", restURL);

			ServiceDataHandlerVehicle dataHandler = new ServiceDataHandlerVehicle();

			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<DataVehicle> dataList = null;

			try {

				HTTPUtil.setProxyInfo(httpclient, context);

				HttpResponse response;

				HttpGet httpGet = new HttpGet(restURL);
				HTTPUtil.setHeadersForCompression(httpGet);

				response = httpclient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					
					InputStream instream = response.getEntity().getContent();
					Header contentEncoding = response.getFirstHeader("Content-Encoding");
					if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					    instream = new GZIPInputStream(instream);
					}
	
					byte[] buff = new byte[8000];

			        int bytesRead = 0;
			        
			        while((bytesRead = instream.read(buff)) != -1) {
			             out.write(buff, 0, bytesRead);
			          }					
					
					
					out.close();
					String responseString = out.toString();

					try {
						parseVehicleXML(dataHandler, responseString);

					} catch (ParserConfigurationException pce) {
						Log.e("SAX XML", "sax parse error", pce);
					} catch (SAXException se) {
						Log.e("SAX XML", "sax error", se);
					} catch (IOException ioe) {
						Log.e("SAX XML", "sax parse io error", ioe);
					}

					dataList = dataHandler.getVehicleList();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Toast.makeText(context, "Response Not OK: ",
							Toast.LENGTH_LONG).show();
				}

			} catch (ClientProtocolException e) {
				Toast.makeText(context, "ClientProtocolException :" + e,
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(context, "IOException :" + e, Toast.LENGTH_LONG)
						.show();
				e.printStackTrace();
			} catch (Exception e) {
				Toast.makeText(context, "Exception :" + e, Toast.LENGTH_LONG)
						.show();
				e.printStackTrace();
			} finally {
				httpclient.getConnectionManager().shutdown();
			}

			return dataList;

		}

		private void parseVehicleXML(ServiceDataHandlerVehicle dataHandler,
				String responseString) throws ParserConfigurationException,
				SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			xr.setContentHandler(dataHandler);

			xr.parse(new InputSource(new StringReader(responseString)));
		}

	}

	class StartEndItemizedOverlay extends ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public StartEndItemizedOverlay(Drawable defaultmarker) {
			super(boundCenterBottom(defaultmarker));
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		public void removeItem(int index) {
			mOverlays.remove(index);
			populate();
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

	}
}