package com.vezeau.alex.stl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vezeau.alex.stl.util.HTTPUtil;

public class FragmentRightSTL extends Fragment {
	private static final String tag = "STLRIGHTFRAGMENT";

	ListView lv;
	public static List<DataBusPoint> busPointListCache;

	public static List<DataVehicle> vehicleListCache;
	public static String busLine;

	public static String minLat;
	public static String maxLat;
	public static String minLon;
	public static String maxLon;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(tag, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(tag, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(tag, "onCreateView");

		View view = inflater.inflate(R.layout.stlrightfragment, container,
				false);

		lv = (ListView) view.findViewById(R.id.busStopListView);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d(tag, "onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(tag, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(tag, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(tag, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(tag, "onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(tag, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(tag, "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(tag, "onDetach");
	}

	private class STLRequest extends AsyncTask<String, Void, List<DataBusStop>> {

		@Override
		protected List<DataBusStop> doInBackground(String... busLine) {
			final String restURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=stl&r="
					+ busLine[0];

			Log.d("REST_URL", restURL);

			ServiceDataHandlerBusStop dataHandler = new ServiceDataHandlerBusStop();

			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<DataBusStop> dataList = null;

			try {

				HTTPUtil.setProxyInfo(httpclient, getActivity());

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
						parseBusStopXML(dataHandler, responseString);

					} catch (ParserConfigurationException pce) {
						Log.e("SAX XML", "sax parse error", pce);
					} catch (SAXException se) {
						Log.e("SAX XML", "sax error", se);
					} catch (IOException ioe) {
						Log.e("SAX XML", "sax parse io error", ioe);
					}

					dataList = dataHandler.getDataList();
					busPointListCache = dataHandler.getBusPointList();
					maxLat = dataHandler.getMaxLat();
					maxLon = dataHandler.getMaxLon();
					minLat = dataHandler.getMinLat();
					minLon = dataHandler.getMinLon();

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Toast.makeText(getView().getContext(), "Response Not OK: ",
							Toast.LENGTH_LONG).show();
				}

			} catch (ClientProtocolException e) {
				Toast.makeText(getView().getContext(),
						"ClientProtocolException :" + e, Toast.LENGTH_LONG)
						.show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(getView().getContext(), "IOException :" + e,
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (Exception e) {
				Toast.makeText(getView().getContext(), "Exception :" + e,
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} finally {
				httpclient.getConnectionManager().shutdown();
			}

			return dataList;

		}

		private void parseBusStopXML(ServiceDataHandlerBusStop dataHandler,
				String responseString) throws ParserConfigurationException,
				SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();

			xr.setContentHandler(dataHandler);

			xr.parse(new InputSource(new StringReader(responseString)));
		}

	}

	private class STLRequestStopInfo extends
			AsyncTask<String, Void, List<DataPrediction>> {

		@Override
		protected List<DataPrediction> doInBackground(String... stopId) {
			final String restURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=stl&stopId="
					+ stopId[0] + "&routeTag=" + stopId[1];

			Log.d("REST_URL", restURL);

			ServiceDataHandlerPrediction dataHandler = new ServiceDataHandlerPrediction();

			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<DataPrediction> dataList = null;

			try {

				final SharedPreferences prefs = new ObscuredSharedPreferences(
						getActivity().getBaseContext(),
						getActivity().getSharedPreferences(
								ObscuredSharedPreferences.MY_PREFS_FILE_NAME,
								Context.MODE_PRIVATE));

				String username = prefs.getString("username", "");
				String password = prefs.getString("password", "");
				String proxyUrl = prefs.getString("proxy", "");
				int port = prefs.getInt("port", 80);
				String useProxy = prefs.getString("useproxy", "");

				if (ActivityPassword.USE_PROXY.equals(useProxy)) {
					HttpHost proxy = new HttpHost(proxyUrl, port, "http");
					httpclient.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);

					httpclient.getCredentialsProvider()
							.setCredentials(
									new AuthScope(proxyUrl, port),
									new UsernamePasswordCredentials(username,
											password));
				}

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
						SAXParserFactory spf = SAXParserFactory.newInstance();
						SAXParser sp = spf.newSAXParser();

						XMLReader xr = sp.getXMLReader();

						xr.setContentHandler(dataHandler);

						xr.parse(new InputSource(new StringReader(
								responseString)));

					} catch (ParserConfigurationException pce) {
						Log.e("SAX XML", "sax parse error", pce);
					} catch (SAXException se) {
						Log.e("SAX XML", "sax error", se);
					} catch (IOException ioe) {
						Log.e("SAX XML", "sax parse io error", ioe);
					}

					dataList = ((ServiceDataHandlerPrediction) dataHandler)
							.getDataList();

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Toast.makeText(getView().getContext(), "Response Not OK: ",
							Toast.LENGTH_LONG).show();
				}

			} catch (ClientProtocolException e) {
				Toast.makeText(getView().getContext(),
						"ClientProtocolException :" + e, Toast.LENGTH_LONG)
						.show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(getView().getContext(), "IOException :" + e,
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (Exception e) {
				Toast.makeText(getView().getContext(), "Exception :" + e,
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} finally {
				httpclient.getConnectionManager().shutdown();
			}

			return dataList;

		}
	}

	public void displayDetails(final String busLine) {

		final List<DataBusStop> populatedDataList = populateBusStopData(busLine);

		addClickListenerToBusStopList(busLine, populatedDataList);

		ImageButton ibShowRoute = (ImageButton) getView().findViewById(
				R.id.ibShowRoute);
		TextView tvShowRoute = (TextView) getView().findViewById(
				R.id.tvShowRoute);

		addClickListenerToShowRoteButton(ibShowRoute, busLine);

		ibShowRoute.setVisibility(View.VISIBLE);
		tvShowRoute.setVisibility(View.VISIBLE);

	}

	private void addClickListenerToShowRoteButton(ImageButton ibShowRoute,
			final String busLine) {

		ibShowRoute.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				FragmentRightSTL.busLine = busLine;

				Intent i = new Intent(getActivity(),
						MapActivityRouteVehicles.class);
				startActivity(i);

			}
		});

	}

	private List<DataBusStop> populateBusStopData(final String busLine) {

		STLRequest requestTask = new STLRequest();
		List<DataBusStop> dataList = null;

		try {

			if (HTTPUtil.isNetworkAvailable(getActivity())) {
				dataList = requestTask.execute(busLine).get();
			} else {
				HTTPUtil.showNoNetworkToast(getActivity());
			}

		} catch (InterruptedException e) {
			Toast.makeText(getView().getContext(),
					"InterruptedException :" + e, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (ExecutionException e) {
			Toast.makeText(getView().getContext(), "ExecutionException :" + e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		List<String> strList = new ArrayList<String>();

		for (DataBusStop bsd : dataList) {
			if (bsd == null) {
				Log.d("ERROR", "FOUND ONE NULL VALUE");
			}
		}

		final List<DataBusStop> populatedDataList = populateDataList(dataList);

		for (DataBusStop busStopData : populatedDataList) {
			strList.add(busStopData.title);
		}

		ArrayAdapterBusStop aa = new ArrayAdapterBusStop(
				getView().getContext(), strList.toArray(new String[strList
						.size()]));
		lv.setAdapter(aa);
		return populatedDataList;
	}

	private void addClickListenerToBusStopList(final String busLine,
			final List<DataBusStop> populatedDataList) {
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				DataBusStop busStopData = populatedDataList.get(position);

				STLRequestStopInfo request = new STLRequestStopInfo();

				try {
					List<DataPrediction> predictions = null;

					if (HTTPUtil.isNetworkAvailable(getActivity())) {
						predictions = request.execute(busStopData.stopId,
								busLine).get();
					} else {
						HTTPUtil.showNoNetworkToast(getActivity());
					}

					String message = constructNextBusMessage(predictions);

					// ---obtain a reference to the ShowMap fragment---
					startBusStopMapIntent(busStopData, message);

				} catch (InterruptedException e) {
					Toast.makeText(getView().getContext(),
							"InterruptedException :" + e, Toast.LENGTH_LONG)
							.show();
					e.printStackTrace();
				} catch (ExecutionException e) {
					Toast.makeText(getView().getContext(),
							"ExecutionException :" + e, Toast.LENGTH_LONG)
							.show();
					e.printStackTrace();
				}

			}

			private void startBusStopMapIntent(DataBusStop busStopData,
					String message) {
				Intent i = new Intent(getActivity().getBaseContext(),
						MapActivityBusStop.class);
				i.putExtra("lat", busStopData.latitude);
				i.putExtra("long", busStopData.longitude);
				i.putExtra("times", message);
				startActivity(i);
			}

			private String constructNextBusMessage(
					List<DataPrediction> predictions) {
				String message = null;

				if (predictions.size() >= 3) {
					message = String.format(
							"Next bus in %s, %s and %s minutes",
							predictions.get(0).minutes,
							predictions.get(1).minutes,
							predictions.get(2).minutes);

				} else {

					switch (predictions.size()) {
					case 1:
						message = String.format("Next bus in %s minutes",
								predictions.get(0).minutes);
						break;

					case 2:
						message = String.format(
								"Next bus in %s and %s minutes",
								predictions.get(0).minutes,
								predictions.get(1).minutes);
						break;

					default:
						message = "Please select next stop to get predictions";
						break;
					}
				}
				return message;
			}
		});
	}

	private List<DataBusStop> populateDataList(List<DataBusStop> dataList) {
		return (dataList == null) ? new ArrayList<DataBusStop>() : dataList;
	}
}
