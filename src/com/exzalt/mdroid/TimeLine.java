package com.exzalt.mdroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TimeLine extends BaseActivity {

	public ArrayList<String> resourcesFileIDs = new ArrayList<String>();
	public ArrayList<String> resourcesFileNames = new ArrayList<String>();
	String courseName = "";
	String courseID = "";
	ArrayList<String> forumViewIDs = new ArrayList<String>();
	ArrayList<String> forumNames = new ArrayList<String>();
	boolean fileComplete = false;
	boolean forumComplete = false;

	LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		inflater = getLayoutInflater();
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		courseID = extras.getString("courseID");
		courseName = extras.getString("courseName");
		setTitle(courseName);
		new getPageContent().execute(courseID);
		new getForumsPageContent().execute(courseID);
	}

	/* AsycTask Thread */

	private class getPageContent extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... courseID) {

			try {
				buildCoursePageFiles(courseID[0]);
			} catch (ClientProtocolException e) {
				Toast.makeText(
						getBaseContext(),
						"ClientProtocolException " + e
								+ " while trying to use postData();",
						Toast.LENGTH_SHORT).show();
				Log.e("error", "ClientProtocolException " + e
						+ " while trying to use postData();");
			} catch (IOException e) {
				Toast.makeText(
						getBaseContext(),
						"IOException " + e + " while trying to use postData();",
						Toast.LENGTH_SHORT).show();
				Log.e("error", "IOException " + e
						+ " while trying to use postData();");
			}
			return null;
		}

		// Purely for publish progress call..as this can't be done from
		// outside!!
		public void doProgress(int value) {
			publishProgress(value);
		}

		protected void onPostExecute(Long result) {
			listFilesInTimeLine();
			fileComplete = true;
			removeLoading();
		}
	}

	private class getForumsPageContent extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... courseID) {

			try {
				getPageContentForumsOne(courseID[0]);
			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Long result) {
			listForumsInTimeLine();
			forumComplete = true;
			removeLoading();
		}
	}

	public void getPageContentForumsOne(String courseID)
			throws ClientProtocolException, IOException {

		DefaultHttpClient httpclient = MainActivity.httpclient;

		HttpGet httpgetCourse = new HttpGet(serverAddress
				+ "/mod/forum/index.php?id=" + courseID);

		HttpResponse responseCourse = httpclient.execute(httpgetCourse);
		HttpEntity entityCourse = responseCourse.getEntity();

		try {
			inputStreamToStringForumsOne(responseCourse.getEntity()
					.getContent());
		} catch (IOException e) {

		}

		if (entityCourse != null) {
			entityCourse.consumeContent();
		}
	}

	private void inputStreamToStringForumsOne(InputStream is)
			throws IOException {
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null) {
			total.append(line);
		}

		extractForums(total.toString());
	}

	public void extractForums(String htmlDataString) {

		int prevIndex = 0;
		int endIndex = 0;
		boolean error = false;
		while (true) {
			prevIndex = htmlDataString.indexOf("<a href=\"view.php?f=",
					prevIndex);

			if (prevIndex == -1)
				break;

			prevIndex += 20;
			endIndex = htmlDataString.indexOf("\"", prevIndex);
			if (endIndex == -1) {
				error = true;
				continue;
			}
			forumViewIDs.add((htmlDataString.substring(prevIndex, endIndex)));
			prevIndex = htmlDataString.indexOf(">", prevIndex);
			if (prevIndex == -1) {
				error = true;
				continue;
			}
			prevIndex += 1;
			endIndex = htmlDataString.indexOf("<", prevIndex);
			if (endIndex == -1) {
				error = true;
				continue;
			}
			forumNames.add((htmlDataString.substring(prevIndex, endIndex)));
			prevIndex = htmlDataString.indexOf("<a href=\"view.php?f=",
					prevIndex) + 5;
		}
		if (error) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(TimeLine.this, "Error in processing data !",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	/*
	 * Resources page specific functions . All functions below are just for
	 * resources page details!
	 */
	public void buildCoursePageFiles(String courseID)
			throws ClientProtocolException, IOException {

		DefaultHttpClient httpclient = MainActivity.httpclient;
		if (httpclient == null) {
			System.out.println("httpclient=null for course id = " + courseID);
		}
		HttpGet httpgetCourse = new HttpGet(serverAddress
				+ "/course/view.php?id=" + courseID);

		HttpResponse responseCourse = httpclient.execute(httpgetCourse);
		HttpEntity entityCourse = responseCourse.getEntity();

		try {
			String result = inputStreamToString(responseCourse.getEntity()
					.getContent());
			extractCoursePageFiles(result);
		} catch (IOException e) {
			Toast.makeText(getBaseContext(),
					"WhileCallingString IOException " + e, Toast.LENGTH_LONG)
					.show();
			Log.e("error", "WhileCallingString IOException");
		}

		if (entityCourse != null) {
			entityCourse.consumeContent();
		}
	}

	public void extractCoursePageFiles(String htmlDataString) {
		int prevIndex = 0;
		int endIndex = 0;
		boolean error = false;

		while (true) {
			prevIndex = htmlDataString.indexOf(serverAddress
					+ "/mod/resource/view.php?id=", prevIndex);

			if (prevIndex == -1)
				break;

			endIndex = htmlDataString.indexOf('\"', prevIndex);
			if (endIndex == -1) {
				error = true;
				continue;
			}
			resourcesFileIDs.add(htmlDataString.substring(prevIndex, endIndex));

			prevIndex = htmlDataString.indexOf("<span", prevIndex) + 5;
			if (prevIndex == -1) {
				error = true;
				continue;
			}
			prevIndex = htmlDataString.indexOf(">", prevIndex) + 1;
			if (prevIndex == -1) {
				error = true;
				continue;
			}
			endIndex = htmlDataString.indexOf("<span class=\"accesshide",
					prevIndex);
			if (endIndex == -1) {
				error = true;
				continue;
			}
			String textConvertedhtmlDataString = htmlDataString.substring(
					prevIndex, endIndex);
			textConvertedhtmlDataString = android.text.Html.fromHtml(
					textConvertedhtmlDataString).toString();
			resourcesFileNames.add(textConvertedhtmlDataString);
		}
		if (error) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(TimeLine.this, "Error in processing data !",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
		// Update the present list on to the UI thread
		getPageContent getpagecontent = new getPageContent();
		getpagecontent.doProgress(1);
	}

	public String getPageContentForIITBDownload(String filePath)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = MainActivity.httpclient;
		String fileDownloadAddress = "";

		HttpGet httpgetCourse = new HttpGet(filePath);

		HttpResponse responseCourse = httpclient.execute(httpgetCourse);
		HttpEntity entityCourse = responseCourse.getEntity();

		String buffer = inputStreamToString(responseCourse.getEntity()
				.getContent());
		fileDownloadAddress = extractFileDetailsForIITBDownload(buffer);

		if (entityCourse != null) {
			entityCourse.consumeContent();
		}
		return fileDownloadAddress;
	}

	public String extractFileDetailsForIITBDownload(String htmlDataString) {
		int prevIndex = 0;
		int endIndex = 0;
		String fileDownloadAddress = "";

		prevIndex = htmlDataString.indexOf("<object", prevIndex) + 7;
		if (prevIndex == 6) {
			fileDownloadAddress = "";
		} else {
			prevIndex = htmlDataString.indexOf("data=\"", prevIndex) + 6;
			endIndex = htmlDataString.indexOf("\"", prevIndex);
			fileDownloadAddress = htmlDataString.substring(prevIndex, endIndex);
		}

		return fileDownloadAddress;
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

		return total.toString();
	}

	public void listFilesInTimeLine() {
		if (resourcesFileIDs.size() == 0)
			return;
		ListView fileList = (ListView) findViewById(R.id.filesListView);
		CardAdapter cardAdapter = new CardAdapter(this);

		cardAdapter.add(new FileCard(resourcesFileIDs, resourcesFileNames));
		fileList.setAdapter(cardAdapter);
		ListHelper.getListViewSize(fileList);

	}

	public void listForumsInTimeLine() {
		ListView forumList = (ListView) findViewById(R.id.forumsListView);
		ForumCardAdapter forumCardAdapter = new ForumCardAdapter(this,
				forumViewIDs, forumNames);
		forumList.setAdapter(forumCardAdapter);
		ListHelper.getListViewSize(forumList);
		System.out.println("cardAdapter set");
	}

	public class DownloadFile extends AsyncTask<Integer, Integer, Void> {
		TextView textView;
		TextView textViewDownload;
		Button downloadButton;
		int resID;
		int resIDButton;
		String fileSizeString = "";
		String fileName;
		String filePath;
		String fileExtension;

		@Override
		protected Void doInBackground(Integer... fileDetails) {
			textView = (TextView) findViewById(fileDetails[0]);
			textViewDownload = (TextView) findViewById(fileDetails[1]);
			downloadButton = (Button) findViewById(fileDetails[2]);

			fileName = textView.getText().toString();
			filePath = downloadButton.getHint().toString();
			try {
				// Extracting FileName and FileUrl from FileData....
				DefaultHttpClient httpclient = MainActivity.httpclient;

				HttpGet httpgetFile;
				if (serverAddress.compareTo("http://moodle.iitb.ac.in") == 0) {
					String fileDownloadAddress = getPageContentForIITBDownload(filePath);
					if (fileDownloadAddress.compareTo("") == 0) {
						httpgetFile = new HttpGet(filePath);
					} else {
						httpgetFile = new HttpGet(fileDownloadAddress);
					}
				} else {
					httpgetFile = new HttpGet(filePath);
				}

				HttpResponse responseFile = httpclient.execute(httpgetFile);
				HttpEntity entityFile = responseFile.getEntity();

				// File attributes finding...like extension, size...presently
				// only for files hosted on Moodle server
				// Works for all moodles when the files are hosted on moodle
				Header[] headers = responseFile.getAllHeaders();
				String fileNameInServer = "";
				if (serverAddress.compareTo("http://moodle.iitb.ac.in") == 0) {
					fileNameInServer = headers[6].getValue();
				} else {
					fileNameInServer = headers[7].getValue();
				}
				int fileLength = (int) entityFile.getContentLength();
				// File extension finding --- In general for all moodles
				int prevIndex = 0;
				int endIndex = 0;
				prevIndex = fileNameInServer.indexOf("filename=\"", prevIndex) + 10;
				endIndex = fileNameInServer.indexOf("\"", prevIndex);
				fileNameInServer = fileNameInServer.substring(prevIndex,
						endIndex);
				prevIndex = fileNameInServer.lastIndexOf(".") + 1;
				endIndex = fileNameInServer.length();
				fileExtension = fileNameInServer.substring(prevIndex, endIndex);
				System.out.println("file name in server = " + fileNameInServer);
				System.out.println("file extension in server = "
						+ fileExtension);

				// download the file
				InputStream input = new BufferedInputStream(
						entityFile.getContent());
				File file = new File(Environment.getExternalStorageDirectory(),
						"/MDroid/" + courseName + "/" + fileName + "."
								+ fileExtension);
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / fileLength),
							fileLength / 1024);
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(final Integer... progress) {
			super.onProgressUpdate(progress);
			if (progress[1] > 1024) {
				fileSizeString = (progress[1] / 1024) + " MB";
			} else {
				fileSizeString = (progress[1]) + " KB";
			}
			runOnUiThread(new Runnable() {
				public void run() {
					if (textViewDownload != null) {
						textViewDownload.setText(fileSizeString + ", "
								+ progress[0] + "% downloaded");
						downloadButton.setText("Downloading..");
						downloadButton.setEnabled(false);
						downloadButton
								.setBackgroundResource(R.drawable.black_btm);
					}
				}
			});
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			textView.setHint(fileExtension);
			if (textViewDownload != null) {
				textViewDownload
						.setText(fileSizeString + ", download complete");
				textViewDownload.setHint(null);
				downloadButton.setText("Open");
				downloadButton.setEnabled(true);
				downloadButton.setBackgroundResource(R.color.buttonGreen);
				downloadButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						System.out.println("clicked -!");
						String file = textView.getText().toString() + "."
								+ textView.getHint();
						String fileUrl = android.os.Environment
								.getExternalStorageDirectory().getPath()
								+ "/MDroid/" + courseName + "/" + file;
						System.out.println("file URL = " + fileUrl);

						File fileToBeOpened = new File(fileUrl);
						Intent i = new Intent();
						i.setAction(android.content.Intent.ACTION_VIEW);
						i.setDataAndType(Uri.fromFile(fileToBeOpened),
								"application/" + textView.getHint());
						try {
							startActivity(i);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(
									getBaseContext(),
									"No application found to open file type\n"
											+ textView.getHint(),
									Toast.LENGTH_SHORT).show();

						}
					}
				});
			}
			System.out.println("on post execute completed");
		}
	}

	public class FileListAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final ArrayList<String> filenames;
		private final ArrayList<String> fileids;

		public FileListAdapter(Context context, ArrayList<String> fileNames,
				ArrayList<String> fileIDs) {
			super(context, R.layout.filelistviewlayout, fileNames);
			this.context = context;
			this.filenames = fileNames;
			this.fileids = fileIDs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 * This function of adapter sets the list entry For entry at
			 * position check if already exists in filesystem And take
			 * appropriate action
			 */
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.filelistviewlayout,
					parent, false);

			final TextView textView = (TextView) rowView
					.findViewById(R.id.myFileName);
			textView.setText(filenames.get(position));

			final TextView textViewDownload = (TextView) rowView
					.findViewById(R.id.myFileDownloadStatus);

			final Button fileOperationButton = (Button) rowView
					.findViewById(R.id.myFileButton);

			/*
			 * TODO: Whats happening Here ??
			 */
			// TODO - added by Ayush - have to modify this i think so
			textView.setId(position + 100);
			textViewDownload.setId(position + 500);
			fileOperationButton.setId(position + 1000);

			// check if file exists!
			String courseDir = android.os.Environment
					.getExternalStorageDirectory().getPath()
					+ "/MDroid/"
					+ courseName + "/";

			/*
			 * TODO: Unnecessary looping :-/
			 */
			File dir = new File(courseDir);
			ArrayList<String> fileNames = new ArrayList<String>();
			ArrayList<String> fileExtensions = new ArrayList<String>();
			File[] files = dir.listFiles();
			for (File file : files) {
				if (!(file.isDirectory())) {
					int startIndex;
					int endIndex;
					String fileNameDir = file.toString();
					startIndex = fileNameDir.lastIndexOf("/") + 1;
					endIndex = fileNameDir.length();
					fileNameDir = fileNameDir.substring(startIndex, endIndex);
					startIndex = 0;
					endIndex = fileNameDir.lastIndexOf(".");
					String fileNameNoExtension = fileNameDir.substring(
							startIndex, endIndex);
					startIndex = endIndex + 1;
					endIndex = fileNameDir.length();
					String fileExtension = fileNameDir.substring(startIndex,
							endIndex);
					if (fileNameNoExtension.equals("") == false
							&& fileExtension.equals("") == false) {
						fileNames.add(fileNameNoExtension);
						fileExtensions.add(fileExtension);
					}
				}
			}

			File f = null;
			int match = 0;
			String extension = "";
			// Check if file matches with any of the above files without
			// extensions
			for (int i = 0; i < fileNames.size(); i++) {
				if (filenames.get(position).equals(fileNames.get(i)) == true) {
					f = new File(android.os.Environment
							.getExternalStorageDirectory().getPath()
							+ "/MDroid/"
							+ courseName
							+ "/"
							+ fileNames.get(i)
							+ "." + fileExtensions.get(i));
					match = 1;

					extension = fileExtensions.get(i);

					break;
				}

			}
			textView.setHint(extension);
			if (match == 1) {
				// f.exists()
				// Saving file extension in Hint for existing file..
				long fileSize = f.length();
				Date lastModDate = new Date(f.lastModified());
				SimpleDateFormat format = new SimpleDateFormat(
						"MM/dd/yyyy hh:mm a");
				String lastModDateformatted = format.format(lastModDate);

				String fileSizeInfo;
				if (fileSize > (1024 * 1024))
					fileSizeInfo = fileSize / (1024 * 1024) + "MB";
				else if (fileSize > 1024)
					fileSizeInfo = fileSize / 1024 + "KB";
				else
					fileSizeInfo = fileSize + "Bytes";

				fileOperationButton.setText("Open");
				fileOperationButton.setBackgroundResource(R.color.buttonGreen);
				textViewDownload.setTextColor(Color.parseColor("#3f3f3f"));
				textViewDownload.setText(fileSizeInfo + ", modified: "
						+ lastModDateformatted);

			} else {
				// Saving fileID in Hint for not existing file..
				fileOperationButton.setHint(fileids.get(position));
				fileOperationButton.setText("Download");
			}

			fileOperationButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (fileOperationButton.getText() == "Download") {
						fileOperationButton.setEnabled(false);
						fileOperationButton
								.setBackgroundResource(R.drawable.black_btm);

						/*
						 * Disable button and start download This is done via
						 * ASync Task
						 */
						DownloadFile downloadFile = new DownloadFile();
						downloadFile.execute(textView.getId(),
								textViewDownload.getId(),
								fileOperationButton.getId());
					} else {
						String file = textView.getText().toString() + "."
								+ textView.getHint();
						String fileUrl = android.os.Environment
								.getExternalStorageDirectory().getPath()
								+ "/MDroid/" + courseName + "/" + file;
						System.out.println("file URL = " + fileUrl);

						File fileToBeOpened = new File(fileUrl);
						Intent i = new Intent();
						i.setAction(android.content.Intent.ACTION_VIEW);
						i.setDataAndType(Uri.fromFile(fileToBeOpened),
								"application/" + textView.getHint());
						try {
							startActivity(i);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(
									getBaseContext(),
									"No application found to open file type\n"
											+ textView.getHint(),
									Toast.LENGTH_LONG).show();
						}
					}
				}
			});
			return rowView;
		}
	}

	public class CardAdapter extends BaseAdapter {

		private Context context;
		private FileCard item;
		private boolean isChanged = false;
		private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
		private int mAccentColor;
		private int mPopupMenu = -1;
		private boolean mCardsClickable = false;
		private int mLayout = R.layout.list_item_card;

		public CardAdapter(Context context) {
			this.context = context;
			this.item = new FileCard(null, null);
			mAccentColor = context.getResources().getColor(
					android.R.color.black);
		}

		@Override
		public boolean isEnabled(int position) {
			FileCard item = getItem(position);
			if (!mCardsClickable)
				return false;
			else
				return true;

		}

		/**
		 * Sets the accent color used on card titles and header action buttons.
		 * You <b>should</b> call this method before adding any cards to the
		 * adapter to avoid issues.
		 * 
		 * @param color
		 *            The resolved color to use as an accent.
		 */
		public final CardAdapter setAccentColor(int color) {
			mAccentColor = color;
			return this;
		}

		/**
		 * Sets the accent color resource used on card titles and header action
		 * buttons. You <b>should</b> call this method before adding any cards
		 * to the adapter to avoid issues.
		 * 
		 * @param colorRes
		 *            The color resource ID to use as an accent.
		 */
		public final CardAdapter setAccentColorRes(int colorRes) {
			setAccentColor(getContext().getResources().getColor(colorRes));
			return this;
		}

		/**
		 * Sets a custom layout to be used for all cards (not including headers)
		 * in the adapter. Must be called before adding cards. This <b>does
		 * not</b> override layouts set to individual cards.
		 */
		public final CardAdapter setCardLayout(int layoutRes) {
			mLayout = layoutRes;
			return this;
		}

		public int getLayout(int index, int type) {
			return getItem(index).getLayout();
		}

		private void invalidatePadding(int index, View view) {
			int top = index == 0 ? R.dimen.card_outer_padding_firstlast
					: R.dimen.card_outer_padding_top;
			int bottom = index == (getCount() - 1) ? R.dimen.card_outer_padding_firstlast
					: R.dimen.card_outer_padding_top;
			view.setPadding(view.getPaddingLeft(), getContext().getResources()
					.getDimensionPixelSize(top), view.getPaddingRight(),
					getContext().getResources().getDimensionPixelSize(bottom));
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public FileCard getItem(int i) {
			return item;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			System.out.println("in getView() of CardAdapter" + position);
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.list_item_card, null);
			}
			invalidatePadding(position, convertView);

			LinearLayout fileList = (LinearLayout) convertView
					.findViewById(R.id.fileWeeklyList);
			fileList.removeAllViews();

			ArrayList<String> fileNames = item.getResourcesFileNames();
			ArrayList<String> fileIDs = item.getResourcesFileIDs();

			FileListAdapter adapter = new FileListAdapter(this.context,
					fileNames, fileIDs);
			int i;
			for (i = 0; i < fileNames.size() - 1; i++) {
				View view = adapter.getView(i, null, null);
				fileList.addView(view);
				// TODO - ask why not working

				View divider = inflater.inflate(R.layout.filecard_divider,
						null, false);
				fileList.addView(divider);
			}
			View view = adapter.getView(i, null, null);
			fileList.addView(view);
			// TODO add file names and others

			System.out.println("setupCard complete");
			return convertView;
		}

		public final Context getContext() {
			return context;
		}

		/**
		 * Adds a single item to the adapter and notifies the attached ListView.
		 */
		public void add(FileCard toAdd) {
			isChanged = true;
			this.item = toAdd;
			notifyDataSetChanged();
		}
	}

	public class ForumCardAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<String> forumIDs;
		private ArrayList<String> forumNames;
		private boolean isChanged = false;
		private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
		private int mAccentColor;
		private int mPopupMenu = -1;
		private boolean mCardsClickable = false;
		private int mLayout = R.layout.forum_title_card;

		public ForumCardAdapter(Context context, ArrayList<String> IDs,
				ArrayList<String> names) {
			this.context = context;
			this.forumIDs = IDs;
			this.forumNames = names;
			mAccentColor = context.getResources().getColor(
					android.R.color.black);
		}

		@Override
		public boolean isEnabled(int position) {
			if (!mCardsClickable)
				return false;
			else
				return true;
		}

		/**
		 * Sets the accent color used on card titles and header action buttons.
		 * You <b>should</b> call this method before adding any cards to the
		 * adapter to avoid issues.
		 * 
		 * @param color
		 *            The resolved color to use as an accent.
		 */
		public final ForumCardAdapter setAccentColor(int color) {
			mAccentColor = color;
			return this;
		}

		/**
		 * Sets the accent color resource used on card titles and header action
		 * buttons. You <b>should</b> call this method before adding any cards
		 * to the adapter to avoid issues.
		 * 
		 * @param colorRes
		 *            The color resource ID to use as an accent.
		 */
		public final ForumCardAdapter setAccentColorRes(int colorRes) {
			setAccentColor(getContext().getResources().getColor(colorRes));
			return this;
		}

		/**
		 * Sets a custom layout to be used for all cards (not including headers)
		 * in the adapter. Must be called before adding cards. This <b>does
		 * not</b> override layouts set to individual cards.
		 */
		public final ForumCardAdapter setCardLayout(int layoutRes) {
			mLayout = layoutRes;
			return this;
		}

		private void invalidatePadding(int index, View view) {
			int top = index == 0 ? R.dimen.card_outer_padding_firstlast
					: R.dimen.card_outer_padding_top;
			int bottom = index == (getCount() - 1) ? R.dimen.card_outer_padding_firstlast
					: R.dimen.card_outer_padding_top;
			view.setPadding(view.getPaddingLeft(), getContext().getResources()
					.getDimensionPixelSize(top), view.getPaddingRight(),
					getContext().getResources().getDimensionPixelSize(bottom));
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return forumNames.size();
		}

		@Override
		public String getItem(int i) {
			return forumNames.get(i);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.forum_title_card, null);
			}
			invalidatePadding(position, convertView);
			final int pos = position;
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final int REQUEST_CODE = 14;
					Intent i = new Intent(getBaseContext(), Forums.class);
					i.putExtra("courseID", courseID);
					i.putExtra("courseName", courseName);
					i.putExtra("forumID", forumIDs.get(pos));
					i.putExtra("forumName", forumNames.get(pos));
					startActivityForResult(i, REQUEST_CODE);
				}
			});
			TextView title = (TextView) convertView
					.findViewById(R.id.forumtitle);
			if (title == null)
				throw new RuntimeException(
						"The card layout must contain a TextView with the ID @android:id/title.");

			// TODO add file names and others
			title.setText(forumNames.get(position));

			return convertView;
		}

		public final Context getContext() {
			return context;
		}

		// functions below from Silk Adapter
		public void add(int index, String name, String ID) {
			isChanged = true;
			this.forumNames.add(index, name);
			this.forumIDs.add(index, ID);
			notifyDataSetChanged();
		}

		/**
		 * Adds a single item to the adapter and notifies the attached ListView.
		 */
		public void add(String name, String ID) {
			isChanged = true;
			this.forumNames.add(name);
			notifyDataSetChanged();
		}
	}

	void removeLoading() {
		if (fileComplete && forumComplete)
			this.findViewById(R.id.timelineLoading).setVisibility(View.GONE);
	}

}