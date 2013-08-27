package com.exzalt.mdroid;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserOfflineFileListing extends BaseActivity {
	String courseFolderName;
	private ArrayList<String> offlineFileName = new ArrayList<String>();
	private ArrayList<String> offlineFileDateModified = new ArrayList<String>();
	private ArrayList<String> offlineFileSize = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userofflinefilelisting);
		// For getting Preferences
		appPrefs = new AppPreferences(getApplicationContext());
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		courseFolderName = extras.getString("courseFolderName");
		setTitle(courseFolderName);
		File root = new File(android.os.Environment
				.getExternalStorageDirectory().getPath()
				+ "/MDroid/"
				+ courseFolderName + "/");
		ListFiles(root);

	}

	public void ListFiles(File f) {
		File[] files = f.listFiles();
		offlineFileName.clear();
		offlineFileDateModified.clear();
		offlineFileSize.clear();
		for (File file : files) {
			if (!(file.isDirectory())) {
				int startIndex;
				int endIndex;
				String fileName = file.toString();
				startIndex = fileName.lastIndexOf("/") + 1;
				endIndex = fileName.length();
				fileName = fileName.substring(startIndex, endIndex);
				File fileLocationPath = new File(f + "/" + fileName);
				if (fileName != ".android_secure" && fileName != "LOST.DIR") {
					// Getting the size of file
					long fileSize = fileLocationPath.length();
					String fileSizeInfo;
					if (fileSize > (1024 * 1024))
						fileSizeInfo = fileSize / (1024 * 1024) + "MB";
					else if (fileSize > 1024)
						fileSizeInfo = fileSize / 1024 + "KB";
					else
						fileSizeInfo = fileSize + "Bytes";

					// Last modified date of the folder...
					Date lastModDate = new Date(fileLocationPath.lastModified());
					SimpleDateFormat format = new SimpleDateFormat(
							"MM/dd/yyyy hh:mm a");
					String lastModDateformatted = format.format(lastModDate);

					offlineFileName.add(fileName);
					offlineFileDateModified.add(lastModDateformatted);
					offlineFileSize.add(fileSizeInfo);

				}
			}
		}
		listFilesInListView(offlineFileName, offlineFileDateModified,
				offlineFileSize);
	}

	public void listFilesInListView(ArrayList<String> offlineFileName,
			ArrayList<String> offlineFileDateModified,
			ArrayList<String> offlineFileSize) {
		
		System.out.println("list func called");
		if(offlineFileName.size()==0){
			System.out.println("no files");
			((TextView) this.findViewById(R.id.noOfflineFiles)).setVisibility(View.VISIBLE);
			return;
		}
		ListView listView = (ListView) findViewById(R.id.myCourseFilesOffline);

		MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,
				offlineFileName, offlineFileDateModified, offlineFileSize);
		// Assign adapter to ListView
		listView.setAdapter(adapter);
	}

	public class MySimpleArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final ArrayList<String> offlineFileName;
		private final ArrayList<String> offlineFileDateModified;
		private final ArrayList<String> offlineFileSize;

		public MySimpleArrayAdapter(Context context,
				ArrayList<String> offlineFileName,
				ArrayList<String> offlineFileDateModified,
				ArrayList<String> offlineFileSize) {
			super(context, R.layout.userofflinefilelistinglistview,
					offlineFileName);
			this.context = context;
			this.offlineFileName = offlineFileName;
			this.offlineFileDateModified = offlineFileDateModified;
			this.offlineFileSize = offlineFileSize;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(
					R.layout.userofflinefilelistinglistview, parent, false);

			final TextView textViewFileName = (TextView) rowView
					.findViewById(R.id.myOfflineFileName);
			textViewFileName.setText(offlineFileName.get(position));

			final TextView textViewFileDetails = (TextView) rowView
					.findViewById(R.id.myOfflineFileDetails);
			textViewFileDetails.setText(offlineFileSize.get(position) + ", "
					+ offlineFileDateModified.get(position));

			final Button fileOpenButton = (Button) rowView
					.findViewById(R.id.myOfflineFileOpenButton);

			final Button fileDeleteButton = (Button) rowView
					.findViewById(R.id.myOfflineFileDeleteButton);

			fileOpenButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String fileUrl = android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/MDroid/"
							+ courseFolderName
							+ "/"
							+ textViewFileName.getText().toString();
					int startIndex = fileUrl.lastIndexOf(".") + 1;
					int endIndex = fileUrl.length();
					String fileExtension = fileUrl.substring(startIndex,
							endIndex);
					File fileToBeOpened = new File(fileUrl);
					Intent i = new Intent();
					i.setAction(android.content.Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(fileToBeOpened),
							"application/" + fileExtension);
					try {
						startActivity(i);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(
								getBaseContext(),
								"No application found to open file type\n"
										+ fileExtension, Toast.LENGTH_LONG)
								.show();
					}

				}
			});

			rowView.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String fileUrl = android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/MDroid/"
							+ courseFolderName
							+ "/"
							+ textViewFileName.getText().toString();
					int startIndex = fileUrl.lastIndexOf(".") + 1;
					int endIndex = fileUrl.length();
					String fileExtension = fileUrl.substring(startIndex,
							endIndex);
					File fileToBeOpened = new File(fileUrl);
					Intent i = new Intent();
					i.setAction(android.content.Intent.ACTION_VIEW);
					i.setDataAndType(Uri.fromFile(fileToBeOpened),
							"application/" + fileExtension);
					try {
						startActivity(i);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(
								getBaseContext(),
								"No application found to open file type\n"
										+ fileExtension, Toast.LENGTH_LONG)
								.show();
					}

				}
			});

			fileDeleteButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String fileUrl = android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/MDroid/"
							+ courseFolderName
							+ "/"
							+ textViewFileName.getText().toString();
					File fileToBeDeleted = new File(fileUrl);
					boolean deleted = fileToBeDeleted.delete();
					if (deleted)
						Toast.makeText(
								getBaseContext(),
								"File " + textViewFileName.getText().toString()
										+ " deleted!", Toast.LENGTH_SHORT)
								.show();
					offlineFileName.remove(position);
					offlineFileDateModified.remove(position);
					offlineFileSize.remove(position);
					listFilesInListView(offlineFileName,
							offlineFileDateModified, offlineFileSize);

				}
			});

			return rowView;
		}
	}
}
