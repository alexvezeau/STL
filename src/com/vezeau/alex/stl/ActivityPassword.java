package com.vezeau.alex.stl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityPassword extends Activity {

	public static final String NO_PROXY = "No Proxy";
	public static final String USE_PROXY = "Use Proxy";
	private Button btnSetPassword;
	private Button btnPrevious;

	private final Context context = ActivityPassword.this;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);

		btnSetPassword = (Button) findViewById(R.id.btnSetPassword);
		btnPrevious = (Button) findViewById(R.id.btnPrevious);

		final EditText username = (EditText) findViewById(R.id.etUsername);
		final EditText password = (EditText) findViewById(R.id.etPassword);
		final EditText proxy = (EditText) findViewById(R.id.etProxy);
		final EditText port = (EditText) findViewById(R.id.etPort);
		final ToggleButton useProxy = (ToggleButton) findViewById(R.id.tbUseProxy);

		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(context, ActivitySTLMain.class);
				startActivity(i);

			}
		});

		btnSetPassword.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final SharedPreferences prefs = new ObscuredSharedPreferences(
						context, getSharedPreferences(
								ObscuredSharedPreferences.MY_PREFS_FILE_NAME,
								Context.MODE_PRIVATE));

				v.setEnabled(false);

				prefs.edit()
						.putString("username", username.getText().toString())
						.commit();
				prefs.edit()
						.putString("password", password.getText().toString())
						.commit();

				prefs.edit().putString("proxy", proxy.getText().toString())
						.commit();

				prefs.edit()
						.putInt("port",
								Integer.valueOf(port.getText().toString()))
						.commit();
				prefs.edit()
						.putString("useproxy", useProxy.getText().toString())
						.commit();

				Toast.makeText(context, "Password Changed !",
						Toast.LENGTH_SHORT).show();

				Intent i = new Intent(context, ActivitySTLMain.class);
				startActivity(i);

				v.setEnabled(true);

			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();

		final SharedPreferences prefs = new ObscuredSharedPreferences(context,
				getSharedPreferences(
						ObscuredSharedPreferences.MY_PREFS_FILE_NAME,
						Context.MODE_PRIVATE));

		final EditText username = (EditText) findViewById(R.id.etUsername);
		final EditText password = (EditText) findViewById(R.id.etPassword);
		final EditText proxy = (EditText) findViewById(R.id.etProxy);
		final EditText port = (EditText) findViewById(R.id.etPort);
		final ToggleButton useProxy = (ToggleButton) findViewById(R.id.tbUseProxy);

		username.setText(prefs.getString("username", ""));
		password.setText(prefs.getString("password", ""));
		proxy.setText(prefs.getString("proxy", ""));
		port.setText(String.valueOf(prefs.getInt("port", 80)));
		String useProxyText = prefs.getString("useproxy", NO_PROXY);
		useProxy.setText(prefs.getString("useproxy", NO_PROXY));
		useProxy.setChecked(USE_PROXY.equals(useProxyText));

	}

}