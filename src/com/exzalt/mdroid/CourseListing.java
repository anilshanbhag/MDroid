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
		ArrayList<Integer> courseIDs = new ArrayList<Integer>();
		ArrayList<String> courseNames = new ArrayList<String>();
		// Setting title of the Course Activity...
		setTitle("MDroid");

		while (true) {
			prevIndex = htmlData.indexOf("/course/view.php?id=", prevIndex) + 20;
			if (prevIndex == -1)
				break;
			endIndex = htmlData.indexOf('\"', prevIndex);

			int tempId;
			try {
				tempId = Integer.parseInt(htmlData.substring(prevIndex,
						endIndex));
			} catch (NumberFormatException e) {
				continue;
			}

			if (!courseIDs.contains(tempId)) {
				courseIDs.add(tempId);
				prevIndex = endIndex + 2;
				endIndex = htmlData.indexOf("</a>", prevIndex);

				courseNames.add(htmlData.substring(prevIndex, endIndex));
			} else
				break;
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
					Log.e("TravellerLog :: ",
							"Problem creating course folder for "
									+ tempCourseName);
					Toast.makeText(getBaseContext(),
							"failed to create folder " + file,
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		LayoutInflater li = LayoutInflater.from(this);
		LinearLayout parent = (LinearLayout) this.findViewById(R.id.myCourses);

		for (int i = 0; i < courseNames.size(); i++) {
			View rowView = li.inflate(R.layout.courselistviewlayout, null);

			final TextView textView = (TextView) rowView
					.findViewById(R.id.title);
			textView.setText(courseNames.get(i));
			textView.setHint(Integer.toString(courseIDs.get(i)));

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
			}
		}
	}
}
