package com.exzalt.mdroid;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CourseListing extends BaseActivity {
	public Context context = this;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.courselisting);
		
		// For getting Preferences
		appPrefs = new AppPreferences(getApplicationContext());
		
		int loginCount = appPrefs.getIntPrefs("logincount");
		int rated = appPrefs.getIntPrefs("rated");

		// Saving prefs
		appPrefs.saveIntPrefs("logincount", loginCount + 1);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		String htmlData = extras.getString("htmlData");

		int prevIndex = 0;
		int endIndex = 0;
		ArrayList<String> courseIDs = new ArrayList<String>();
		ArrayList<String> courseNames = new ArrayList<String>();
		String userName = "";

		prevIndex = htmlData.indexOf("You are logged in as ", prevIndex);
		prevIndex = htmlData.indexOf("\">", prevIndex) + 2;
		endIndex = htmlData.indexOf("</a>", prevIndex);
		userName = (htmlData.substring(prevIndex, endIndex));

		// Setting title of the Course Activity...
		setTitle(userName + "'s courses");

		while (true) {
			prevIndex = htmlData.indexOf(
					"<a title=\"Click to enter this course\" href=\"",
					prevIndex);
			if (prevIndex == -1)
				break;
			prevIndex += 44;
			prevIndex = htmlData.indexOf("/course/view.php?id=", prevIndex) + 20;
			endIndex = htmlData.indexOf('\"', prevIndex);

			courseIDs.add(htmlData.substring(prevIndex, endIndex));

			prevIndex = endIndex + 2;
			endIndex = htmlData.indexOf("</a>", prevIndex);

			courseNames.add(htmlData.substring(prevIndex, endIndex));
		}

		for (int i = 0; i < courseNames.size(); i++) {
			String tempCourseName = "";
			tempCourseName = courseNames.get(i).replaceAll(":", "-");
			tempCourseName = android.text.Html.fromHtml(tempCourseName)
					.toString();

			courseNames.set(i, tempCourseName);
			File file = new File(Environment.getExternalStorageDirectory(),
					"/MDroid/" + tempCourseName + "/");
			if (!file.exists()) {
				if (!file.mkdirs()) {
					Log.e("TravellerLog :: ", "Problem creating course folder for "+tempCourseName);
					Toast.makeText(getBaseContext(),
							"failed to create folder " + file,
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		LayoutInflater li = LayoutInflater.from(this);
		LinearLayout parent = (LinearLayout) this.findViewById(R.id.myCourses);
		
		for(int i=0; i < courseNames.size(); i++)
		{
			View rowView = li.inflate(R.layout.courselistviewlayout, null);
			
			final TextView textView = (TextView) rowView.findViewById(R.id.title);
			textView.setText(courseNames.get(i));
			textView.setHint(courseIDs.get(i));
			//if (position % 2 == 0)
			//	textView.setBackgroundResource(R.drawable.listview_evenitem_color);

			rowView.setClickable(true);
			rowView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					final int REQUEST_CODE = 11;
					Intent i = new Intent(context, TimeLine.class);
					i.putExtra("courseID", textView.getHint());
					i.putExtra("courseName", textView.getText());
					startActivityForResult(i, REQUEST_CODE);
				}
			});
			
			parent.addView(rowView);
			
			if (i != courseNames.size() - 1) {
				View divider = li.inflate(R.layout.divider, null);
				parent.addView(divider);
				Log.w("XYNZ", "Why you not called :(");
			}
		}
		
		if (loginCount % 3 == 0 && loginCount != 0 && rated == 0) {
			showDialog(4);
		}
	}
}
