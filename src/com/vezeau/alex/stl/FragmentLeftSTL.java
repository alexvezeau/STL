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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.vezeau.alex.stl.util.HTTPUtil;

public class FragmentLeftSTL extends Fragment {
	private static final String tag = "STLLEFTFRAGMENT";

	Button btnStlLeft;

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

		View view = inflater
				.inflate(R.layout.stlleftfragment, container, false);

		Button next = (Button) view.findViewById(R.id.btnPrevious);
		next.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(tag, "Previous Button clicked");
				Intent intent = new Intent(getView().getContext(),
						ActivitySTLMain.class);
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
			}
		});

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

		Spinner spinner = (Spinner) getView().findViewById(R.id.spinnerStl);
		spinner.setSelected(false);

		setDataList(spinner);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {

				String value = (String) parent.getItemAtPosition(pos);
				if (!"SELECT".equalsIgnoreCase(value)) {
					FragmentRightSTL frag = (FragmentRightSTL) getFragmentManager()
							.findFragmentById(R.id.stlrightfragment);
					frag.displayDetails(value);
				}

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

	}

	private class STLRequest extends AsyncTask<String, Void, List<DataBusLine>> {

		@Override
		protected void onPostExecute(List<DataBusLine> result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}

		private ProgressDialog dialog;
		private Activity activity;

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Progress start");
			this.dialog.show();
		}

		public STLRequest(Activity activity) {
			this.activity = activity;
			dialog = new ProgressDialog(activity);
		}

		@Override
		protected List<DataBusLine> doInBackground(String... url) {
			final String restURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=stl";

			Log.d("REST_URL", restURL);

			DefaultHandler dataHandler = new ServiceDataHandlerBusLine();

			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<DataBusLine> dataList = null;

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
										
					//response.getEntity().writeTo(out);
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

					ServiceDataHandlerBusLine busLineDataHandler = (ServiceDataHandlerBusLine) dataHandler;
					dataList = busLineDataHandler.getDataList();

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Toast.makeText(getView().getContext(), "Response Not OK: ",
							Toast.LENGTH_LONG).show();
				}

			} catch (ClientProtocolException e) {
				Log.d("ERROR", e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("ERROR", e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				Log.d("ERROR", e.toString());
				e.printStackTrace();
			} finally {
				httpclient.getConnectionManager().shutdown();
			}

			return dataList;

		}

	}

	private void setDataList(Spinner spinner) {

		STLRequest requestTask = new STLRequest(getActivity());

		List<DataBusLine> dataList = new ArrayList<DataBusLine>();
		try {
			if (HTTPUtil.isNetworkAvailable(getActivity())) {
				dataList = requestTask.execute(new String[0]).get();
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
		List<String> busLineList = new ArrayList<String>();

		busLineList.add("SELECT");

		for (DataBusLine data : dataList) {
			busLineList.add(data.busNumber);

		}

		String[] array = busLineList.toArray(new String[busLineList.size()]);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				getView().getContext(), android.R.layout.simple_spinner_item,
				array);

		spinner.setAdapter(spinnerArrayAdapter);

		spinner.setSelection(0);

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
}
