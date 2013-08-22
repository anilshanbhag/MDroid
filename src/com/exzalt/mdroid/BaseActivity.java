package com.exzalt.mdroid;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BaseActivity extends SherlockActivity {
	public AppPreferences appPrefs;
	public static String serverAddress;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater =	getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.AboutMDroid:
			showDialog(0); break;
		case R.id.ChangeServer:
			showDialog(1); break;
		case R.id.Rating:
			showDialog(2); break;
		}
		return true;
	}

	public Dialog onCreateDialog(int id) {
		final Dialog dialog=new Dialog(this);
		/*
		 * TODO: Currently settings - copied wrong code :-/
		 */
		switch (id) {
		case 0:
			dialog.setContentView(R.layout.aboutmdroid);
			dialog.setTitle("About");
			break;
			
		case 1:
			dialog.setContentView(R.layout.settings);
			dialog.setTitle("Settings");

			final EditText changedServerEditText = (EditText) dialog
					.findViewById(R.id.changeServerEditText);
			final TextView errors = (TextView) dialog.findViewById(R.id.settingsError);
			
			changedServerEditText.setText(serverAddress);

			Button changeServerValueButton = (Button) dialog
					.findViewById(R.id.changeServerValue);
			Button cancelChangeButton = (Button) dialog
					.findViewById(R.id.resetServerValue);

			changeServerValueButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String newServerName = changedServerEditText.getText()
							.toString();
					errors.setVisibility(View.GONE);
					// Check validity of user entry
					if (newServerName.lastIndexOf("/") == newServerName.length() - 1) {
						newServerName = newServerName.substring(0, newServerName.length() - 1);
					}
					
					if (newServerName == ""){
						errors.setVisibility(View.VISIBLE);
						errors.setText("Server URL cannot be empty");
					} else {
						// Saving in preferences..
						SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("serverAddress", newServerName);
						editor.commit();
						serverAddress = newServerName;
						dialog.dismiss();
	
						Toast.makeText(getBaseContext(),
								"Server Preference Saved\n" + serverAddress,
								Toast.LENGTH_SHORT).show();	
						System.out.println("Server Preference Saved\n" + serverAddress);
					}
				}
			});

			cancelChangeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			break;
			
		case 2:
			dialog.setContentView(R.layout.rating);
			dialog.setTitle("Rate Me");

			Button submitRatingButton = (Button) dialog
					.findViewById(R.id.submitRating);

			submitRatingButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					RatingBar mBar = (RatingBar) dialog
							.findViewById(R.id.ratingBar);
					int[] i = new int[] { (int) mBar.getRating() };

					// Saving prefs
					appPrefs.saveIntPrefs("rated", 1);

					if (i[0] <= 3) {
						dialog.setContentView(R.layout.mdroidhelp);
						dialog.setTitle("Help");
					} else {
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri
								.parse("market://details?id=com.exzalt.mdroid"));
						startActivity(intent);
					}
				}
			});
			break;
		}
		return dialog;
	}
}
