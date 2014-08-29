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
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.vezeau.alex.stl.util.HTTPUtil;

public class ActivitySTLMain extends Activity {

	private String tag = getClass().getName();

	private Button btnSchedulesRealTime;
	private Button btnSchedulesCedule;
	private Button btnOpus;
	private Button btnSTLCall;
	private Context context = ActivitySTLMain.this;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btnSchedulesRealTime = (Button) findViewById(R.id.btnHoraireRealTime);
		btnSchedulesCedule = (Button) findViewById(R.id.btnHoraireCedule);
		btnSTLCall = (Button) findViewById(R.id.btnAppeler);
		btnOpus = (Button) findViewById(R.id.btnOpus);

		btnSchedulesCedule.setText(Html.fromHtml(getResources().getString(
				R.string.horaires_cedule)));

		addListenersToButtons();

	}

	@Override
	protected void onStart() {
		super.onStart();

		STLMessageRequest messageRequest = new STLMessageRequest(this);
		List<DataMessage> messageList = new ArrayList<DataMessage>();
		try {
			if(HTTPUtil.isNetworkAvailable(this)){
				messageList = messageRequest.execute(new String[0]).get();
			}else{
				HTTPUtil.showNoNetworkToast(context);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		TableLayout tablemessages = (TableLayout) findViewById(R.id.tableMessages);

		for (DataMessage dataMessage : messageList) {
			addRowToTable(tablemessages, dataMessage);

			Log.d(tag, "MESSAGE.tag: " + dataMessage.tag);
			Log.d(tag, "MESSAGE.text: " + dataMessage.text);
			Log.d(tag, "MESSAGE.textEn: " + dataMessage.textEn);
		}

	}

	private void addRowToTable(TableLayout tablemessages,
			DataMessage dataMessage) {
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		TextView tv = new TextView(this);
		tv.setText(dataMessage.tag + ": " + dataMessage.text);
		tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		tr.addView(tv);

		tablemessages.addView(tr, new TableLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private void addListenersToButtons() {
		btnSchedulesRealTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, ActivitySTL.class);
				startActivity(i);

			}
		});

		btnSTLCall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:4506886520"));
					startActivity(callIntent);
				} catch (ActivityNotFoundException e) {
					Log.e("ERROR", "Call failed", e);
				}

			}
		});

		btnSchedulesCedule.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://m.stl.laval.qc.ca";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

			}
		});

		btnOpus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://www.carteopus.info";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menuProxy:
			Intent i = new Intent(context, ActivityPassword.class);
			startActivity(i);

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class STLMessageRequest extends
			AsyncTask<String, Void, List<DataMessage>> {

		@Override
		protected void onPostExecute(List<DataMessage> result) {
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

		public STLMessageRequest(Activity activity) {
			this.activity = activity;
			dialog = new ProgressDialog(activity);
		}

		@Override
		protected List<DataMessage> doInBackground(String... url) {
			final String restURL = "http://webservices.nextbus.com/service/xmlFeed?command=messages&a=stl";
			
			Log.d("REST_URL", restURL);

			DefaultHandler dataHandler = new ServiceDataHandlerMessage();

			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<DataMessage> dataList = null;

			try {

				HTTPUtil.setProxyInfo(httpclient, activity);

				HttpResponse response;

				HttpGet httpGet = new HttpGet(restURL);
				HTTPUtil.setHeadersForCompression(httpGet);

				response = httpclient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();
				Log.d("RESPONSE ENCODING", response.getEntity().getContentEncoding().getValue());
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

					ServiceDataHandlerMessage messageDataHandler = (ServiceDataHandlerMessage) dataHandler;
					dataList = messageDataHandler.getMessageList();

				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Toast.makeText(getBaseContext(), "Response Not OK: ",
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
}