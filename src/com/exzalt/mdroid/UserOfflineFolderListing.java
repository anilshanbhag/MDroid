package com.exzalt.mdroid;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class UserOfflineFolderListing extends BaseActivity {
	private ArrayList<String> courseFolder = new ArrayList<String>();
	private ArrayList<String> folderDateModified = new ArrayList<String>();
	private ArrayList<String> folderFilesNumber = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userofflinefolderlisting);
		// For getting Preferences
		appPrefs = new AppPreferences(getApplicationContext());

		File root = new File(android.os.Environment
				.getExternalStorageDirectory().getPath() + "/MDroid/");
		ListCourses(root);
	}

	public void ListCourses(File f) {
		File[] files = f.listFiles();
		courseFolder.clear();
		folderDateModified.clear();
		folderFilesNumber.clear();
		for (File file : files) {
			if (file.isDirectory()) {
				int startIndex;
				int endIndex;
				String fileName = file.toString();
				startIndex = fileName.lastIndexOf("/") + 1;
				endIndex = fileName.length();
				fileName = fileName.substring(startIndex, endIndex);
				File coursefolderPath = new File(f + "/" + fileName);
				if (fileName != ".android_secure" && fileName != "LOST.DIR") {
					// Getting no of files in that folder...
					File filesInCourseFolder = new File(coursefolderPath + "/");
					int filesCountInCourseFolder = filesInCourseFolder
							.listFiles().length;

					// Last modified date of the folder...
					Date lastModDate = new Date(coursefolderPath.lastModified());
					SimpleDateFormat format = new SimpleDateFormat(
							"MM/dd/yyyy hh:mm a");
					String lastModDateformatted = format.format(lastModDate);

					courseFolder.add(fileName);
					folderDateModified.add(lastModDateformatted);
					folderFilesNumber.add(filesCountInCourseFolder
							+ " offline files");

				}
			}
			listFilesInListView(courseFolder, folderDateModified,
					folderFilesNumber);
		}
	}

	public void listFilesInListView(ArrayList<String> courseFolderName,
			ArrayList<String> courseFolderDateModified,
			ArrayList<String> courseFolderFilesCount) {

		ListView listView = (ListView) findViewById(R.id.myCourseFoldersOffline);

		MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,
				courseFolderName, courseFolderDateModified,
				courseFolderFilesCount);
		// Assign adapter to ListView
		listView.setAdapter(adapter);
	}

	public class MySimpleArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final ArrayList<String> courseFolderName;
		private final ArrayList<String> courseFolderDateModified;
		private final ArrayList<String> courseFolderFilesCount;

		public MySimpleArrayAdapter(Context context,
				ArrayList<String> courseFolderName,
				ArrayList<String> courseFolderDateModified,
				ArrayList<String> courseFolderFilesCount) {
			super(context, R.layout.userofflinefolderlistinglistview,
					courseFolderName);
			this.context = context;
			this.courseFolderName = courseFolderName;
			this.courseFolderDateModified = courseFolderDateModified;
			this.courseFolderFilesCount = courseFolderFilesCount;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(
					R.layout.userofflinefolderlistinglistview, parent, false);
			
			if (position % 2 == 0) 
				rowView.setBackgroundResource(R.drawable.listview_evenitem_color);
			
			final TextView textViewFolderName = (TextView) rowView
					.findViewById(R.id.myCourseNameOffline);
			textViewFolderName.setText(courseFolderName.get(position));

			final TextView textViewDateModified = (TextView) rowView
					.findViewById(R.id.courseModifiedDateOffline);
			textViewDateModified
					.setText(courseFolderDateModified.get(position));

			final TextView textViewNoOfFilesOffline = (TextView) rowView
					.findViewById(R.id.noOfFilesOffline);
			textViewNoOfFilesOffline.setText(courseFolderFilesCount
					.get(position));

			rowView.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					final int REQUEST_CODE = 19;
					Intent i = new Intent(context, UserOfflineFileListing.class);
					i.putExtra("courseFolderName", textViewFolderName.getText()
							.toString());
					startActivityForResult(i, REQUEST_CODE);
				}
			});

			return rowView;
		}
	}
}
