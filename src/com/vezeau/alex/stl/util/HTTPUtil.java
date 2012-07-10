package com.vezeau.alex.stl.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.vezeau.alex.stl.ActivityPassword;
import com.vezeau.alex.stl.ObscuredSharedPreferences;

public class HTTPUtil {

	private HTTPUtil() {

	}

	public static void setProxyInfo(DefaultHttpClient httpclient,
			Context context) {
		final SharedPreferences prefs = new ObscuredSharedPreferences(context,
				context.getSharedPreferences(
						ObscuredSharedPreferences.MY_PREFS_FILE_NAME,
						Context.MODE_PRIVATE));

		String username = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String proxyUrl = prefs.getString("proxy", "");
		int port = prefs.getInt("port", 80);
		String useProxy = prefs.getString("useproxy", "");

		if (ActivityPassword.USE_PROXY.equals(useProxy)) {
			HttpHost proxy = new HttpHost(proxyUrl, port, "http");
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);

			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(proxyUrl, port),
					new UsernamePasswordCredentials(username, password));
		}
	}

	public static void setHeadersForCompression(HttpGet httpGet) {
		httpGet.setHeader("Accept-Encoding", "gzip, deflate");
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void showNoNetworkToast(Context context) {
		Toast.makeText(context, "Network not available", Toast.LENGTH_SHORT)
				.show();

	}

}
