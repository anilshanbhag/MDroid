package com.exzalt.mdroid;
//Importing required libraries

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//(re)-defining our Activity's class
@SuppressLint("NewApi")
public class MainActivity extends BaseActivity {

	/** Called when the activity is first created. */
	private static final int REQUEST_CODE = 10;
	String htmldataString = "";
	ProgressDialog loginDialog;
	Dialog firstTimeDialog;
	SchemeRegistry schreg = new SchemeRegistry();
	TryAsyncLogin task = null;
	
	public static DefaultHttpClient httpclient;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		appPrefs = new AppPreferences(getApplicationContext());

		/*
		 * Initialize the Http Client
		 */
		httpClientBootstrap();

		/*
		 * Setting up the login dialog
		 */
		loginDialog = new ProgressDialog(MainActivity.this);
		loginDialog.setMessage("Logging In ..");
		loginDialog.setIndeterminate(true);
		loginDialog.setCancelable(true);
		loginDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
			public void onCancel(DialogInterface dialog) {
				if(task != null) task.cancel(true);
			}
	    });

		/*
		 * Login button listener
		 */
		Button loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(loginButtonListener);
		
		/*
		 * Off line button listener
		 */
		Button offlineButton = (Button) findViewById(R.id.userOfflineButton);
		offlineButton.setOnClickListener(userOfflineButtonListener);

		/*
		 * Loading Preferences
		 */
		preferencesBootstrap();
	}
	
	/*
	 * Loading user preferences onto login page
	 * TODO : Do something for first time
	 */
	private void preferencesBootstrap() {
		SharedPreferences prefsGet = getPreferences(Context.MODE_PRIVATE);
		String userNamePref = prefsGet.getString("username", "");
		String passwordPref = prefsGet.getString("password", "");
		serverAddress = prefsGet.getString("serverAddress", "");
		
		Boolean firstTime = prefsGet.getBoolean("firstTime", true);
		
		if (firstTime) firstTimeHelp();

		EditText usernameRaw = (EditText) findViewById(R.id.usernameEditText);
		EditText passwordRaw = (EditText) findViewById(R.id.passwordEditText);
		usernameRaw.setText(userNamePref);
		passwordRaw.setText(passwordPref);
	}
	
	/*
	 * Bootstrap the HTTP client
	 */
	private void httpClientBootstrap() {
		HttpParams httpParams = new BasicHttpParams();
		int timeoutConnection = 30000;
		HttpConnectionParams.setSoTimeout(httpParams, timeoutConnection);

		schreg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schreg.register(new Scheme("https", PlainSocketFactory
				.getSocketFactory(), 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(
				httpParams, schreg);
		httpclient = new DefaultHttpClient(cm, httpParams);
	}
	/*
	 * Shows a help dialog for first time visitors
	 */
	public void firstTimeHelp() {
		firstTimeDialog = new Dialog(MainActivity.this);
		firstTimeDialog.setContentView(R.layout.firsttimehelp);
		firstTimeDialog.setTitle("Getting Started!");

		Button dismissButton = (Button) firstTimeDialog
				.findViewById(R.id.firstTimeHelpDialogDismiss);

		dismissButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("firstTime", false);
				editor.commit();
				firstTimeDialog.dismiss();
			}
		});
		
		firstTimeDialog.show();
	}

	/*
	 * TODO : delete passwordPref
	 */
	private OnClickListener loginButtonListener = new OnClickListener() {
		public void onClick(View v) {
			// calling tryAsyncLogin function
			// Getting userInputs into Java variables
			EditText usernameField = (EditText) findViewById(R.id.usernameEditText);
			EditText passwordField = (EditText) findViewById(R.id.passwordEditText);

			String username = usernameField.getText().toString();
			String password = passwordField.getText().toString();

			// Saving user-name password preferences...
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("username", username);
			editor.putString("password", password);
			editor.commit();

			if ((username.compareTo("") != 0) && (password.compareTo("") != 0)) {
				task = new TryAsyncLogin();
				task.execute(username, password);
			} else {
				Toast.makeText(getBaseContext(), "Username/Password cannot be empty",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener userOfflineButtonListener = new OnClickListener() {
		public void onClick(View v) {
			// check if file exists!
			String file = android.os.Environment.getExternalStorageDirectory()
					.getPath() + "/MDroid";
			File f = new File(file);
			if (f.exists()) {
				userOfflineIntentOpen();
			} else {
				Toast.makeText(getBaseContext(),
						"Maybe you should browse some courses first",
						Toast.LENGTH_LONG).show();
			}
		}
	};

	/* 
	 * AsycTask Thread 
	 * TODO : Improve Error
	 */
	private class TryAsyncLogin extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... credentials) {
			long ret = 0;
			try {
				ret = loginPOST(credentials[0], credentials[1]);
			} catch (ClientProtocolException e) {
				ret = 3;
				e.printStackTrace();
			} catch (IOException e) {
				ret = 4;
				e.printStackTrace();
			}
			return ret;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loginDialog.show();
		}

		protected void onPostExecute(Long ret) {
			try {
				loginDialog.dismiss();
			} catch (Exception e) {
				// nothing
			}
			
			if (ret == 0) changeLoginandChangeIntent();
			else {
				// 1 -> login failed
				// 2 -> bad Internet
				Toast.makeText(getBaseContext(),
						"OUCH! Login Failed",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void userOfflineIntentOpen() {
		Intent j = new Intent(this, UserOfflineFolderListing.class);
		startActivityForResult(j, 11);
	}
	
	/*
	 * TODO : Check Authentication Successful
	 */
	public int loginPOST(String username, String password)
			throws ClientProtocolException, IOException {
				
		// TODO : Send a get request ?? WHY ??
		int ret = 0;
		Log.w("MDroid", serverAddress + "/login/index.php");
		HttpGet httpGet = new HttpGet(serverAddress + "/login/index.php");

		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			Log.w("MDroid1", serverAddress + "/login/index.php");
			entity.consumeContent();
		}
		Log.w("MDroid2", serverAddress + "/login/index.php");
		
		HttpPost httpost = new HttpPost(serverAddress + "/login/index.php");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("password", password));

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		
		response = httpclient.execute(httpost);
		entity = response.getEntity();

		String out = inputStreamToString(entity.getContent());

		if (out != null) {
			int index = htmldataString.indexOf(
					"<div class=\"logininfo\">You are logged in as", 0);
			if (index == -1) ret = 1;
		} else {
			ret = 2;
		}
		
		if (entity != null) {
			entity.consumeContent();
		}
		
		return ret;
	}

	private String inputStreamToString(InputStream is) throws IOException {
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null) {
			total.append(line);
		}

		htmldataString = total.toString();
		
		return htmldataString;
	}

	public void changeLoginandChangeIntent() {
		Intent i = new Intent(this, CourseListing.class);
		i.putExtra("htmlData", htmldataString);
		startActivityForResult(i, REQUEST_CODE);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
}