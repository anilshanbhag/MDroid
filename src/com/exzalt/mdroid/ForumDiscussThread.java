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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ForumDiscussThread extends BaseActivity {
	String discussID;
	String discussSubject;
	String courseName;
	ArrayList<ArrayList<String>> ForumFileIDs = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> ForumFileNames = new ArrayList<ArrayList<String>>();
	ArrayList<String> discussThreadReplySubject = new ArrayList<String>();
	ArrayList<String> discussThreadReplyTime = new ArrayList<String>();
	ArrayList<String> discussThreadReplyPerson = new ArrayList<String>();
	ArrayList<String> discussThreadReplyContent = new ArrayList<String>();

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.forumdiscussthread);
		// For getting Preferences
		appPrefs = new AppPreferences(getApplicationContext());

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		discussID = extras.getString("discussID");
		discussSubject = extras.getString("discussSubject");
		courseName = extras.getString("courseName");

		// Setting title of discussThread activity..
		setTitle(discussSubject);

		new getForumsPageContent().execute(discussID);
	}

	/* AsycTask Thread */

	private class getForumsPageContent extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... discussID) {

			try {
				getPageContentForumsOne(discussID[0]);
			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			}

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Long result) {

			listFilesInListView(discussThreadReplySubject,
					discussThreadReplyPerson, discussThreadReplyTime,
					discussThreadReplyContent,ForumFileIDs,
					ForumFileNames);
			hideLoadingMessageLayout();

		}

	}

	public void getPageContentForumsOne(String courseID)
			throws ClientProtocolException, IOException {

		DefaultHttpClient httpclient = MainActivity.httpclient;

		HttpGet httpgetCourse = new HttpGet(discussID);

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

		extractFileDetailsForForumsOne(total.toString());
	}

	public void extractFileDetailsForForumsOne(String htmlDataString) {

		int prevIndex = 0;
		int endIndex = 0;

		while (true) {
			prevIndex = htmlDataString
					.indexOf(" class=\"subject\">", prevIndex);

			if (prevIndex == -1)
				break;

			// for reply subject
			prevIndex += 17;
			endIndex = htmlDataString.indexOf("</div>", prevIndex);
			discussThreadReplySubject.add(htmlDataString.substring(prevIndex,
					endIndex));

			// for reply person name
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf("<div class=\"author\">",
					prevIndex) + 22;
			prevIndex = htmlDataString.indexOf("\">", prevIndex) + 2;
			endIndex = htmlDataString.indexOf("</a>", prevIndex);
			discussThreadReplyPerson.add(htmlDataString.substring(prevIndex,
					endIndex));

			// for reply time
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf(", ", prevIndex) + 3;
			endIndex = htmlDataString.indexOf("</div>", prevIndex);
			discussThreadReplyTime.add(htmlDataString.substring(prevIndex,
					endIndex));
			
			//for attached files
			int prevIndex1 = endIndex,endIndex1;
			ArrayList<String> tempForumFileIDs = new ArrayList<String>();
			ArrayList<String> tempForumFileNames = new ArrayList<String>();
			while(true){
				prevIndex1 = htmlDataString.indexOf(
                        "<div class=\"attachments\"><a href=\"", prevIndex1);
				if (prevIndex1 == -1)
                    break;

				prevIndex1 += 34;
				endIndex1 = htmlDataString.indexOf("\"", prevIndex1);
            
				if(endIndex1== -1)
                    break;
				 tempForumFileIDs.add(htmlDataString.substring(prevIndex1, endIndex1));
                 
                 prevIndex1 = endIndex1 + 3;
                 prevIndex1 = htmlDataString.indexOf("\">", prevIndex1);
                 prevIndex1 += 2;
                 endIndex1 = htmlDataString.indexOf(
                                 "</a>", prevIndex1);
                 String textConvertedhtmlDataString = htmlDataString.substring(
                                 prevIndex1, endIndex1);
                 textConvertedhtmlDataString = android.text.Html.fromHtml(
                                 textConvertedhtmlDataString).toString();
                 tempForumFileNames.add(textConvertedhtmlDataString);
                 prevIndex1 = endIndex1;
			}
			ForumFileIDs.add(tempForumFileIDs);
			ForumFileNames.add(tempForumFileNames);
			// for reply content
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf("<div class=\"posting",
					prevIndex) + 19;
			prevIndex = htmlDataString.indexOf("\">",prevIndex)+2;
			endIndex = htmlDataString.indexOf("</div><div class=\"", prevIndex);
			String tempdiscussThreadReplyContent = (htmlDataString.substring(
					prevIndex, endIndex));
			tempdiscussThreadReplyContent = android.text.Html.fromHtml(
					tempdiscussThreadReplyContent).toString();
			discussThreadReplyContent.add(tempdiscussThreadReplyContent);

		}

	}

	public void hideLoadingMessageLayout() {
		LinearLayout mainLayout = (LinearLayout) this
				.findViewById(R.id.linearLayoutLoadingDiscussThread);
		mainLayout.setVisibility(LinearLayout.GONE);
	}

	public void listFilesInListView(
			ArrayList<String> discussThreadReplySubject,
			ArrayList<String> discussThreadReplyPerson,
			ArrayList<String> discussThreadReplyTime,
			ArrayList<String> discussThreadReplyContent,
			ArrayList<ArrayList<String>> discussForumFileIDs,
			ArrayList<ArrayList<String>> discussForumFileNames) {
		ListView forumsListView = (ListView) findViewById(R.id.forumsDiscussThread);

		MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this,
				discussThreadReplySubject, discussThreadReplyPerson,
				discussThreadReplyTime, discussThreadReplyContent,
				discussForumFileIDs,discussForumFileNames);
		// Assign adapter to ListView
		
		forumsListView.setAdapter(adapter);
	}

	public class MySimpleArrayAdapter extends BaseAdapter {
		private final Context context;
		private final ArrayList<String> discussThreadReplySubjectListView;
		private final ArrayList<String> discussThreadReplyPersonListView;
		private final ArrayList<String> discussThreadReplyTimeListView;
		private final ArrayList<String> discussThreadReplyContentListView;
		private final ArrayList<ArrayList<String>> discussForumFileIDsListView;
		private final ArrayList<ArrayList<String>> discussForumFileNamesListView;

		public MySimpleArrayAdapter(Context context,
				ArrayList<String> discussThreadReplySubjectListView,
				ArrayList<String> discussThreadReplyPersonListView,
				ArrayList<String> discussThreadReplyTimeListView,
				ArrayList<String> discussThreadReplyContentListView,
				ArrayList<ArrayList<String>> discussForumFileIDsListView,
				ArrayList<ArrayList<String>> discussForumFileNamesListView) {
			this.context = context;
			this.discussThreadReplySubjectListView = discussThreadReplySubjectListView;
			this.discussThreadReplyPersonListView = discussThreadReplyPersonListView;
			this.discussThreadReplyTimeListView = discussThreadReplyTimeListView;
			this.discussThreadReplyContentListView = discussThreadReplyContentListView;
			this.discussForumFileIDsListView = discussForumFileIDsListView;
			this.discussForumFileNamesListView = discussForumFileNamesListView;
			
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			
            convertView = LayoutInflater.from(context)
	            		.inflate(R.layout.forumdiscussthreadlistviewlayout, null);
			final TextView subjectTextView = (TextView) convertView
					.findViewById(R.id.forumDiscussThreadSubject);
			subjectTextView.setText(discussThreadReplySubjectListView
					.get(position));

			final TextView authorTextView = (TextView) convertView
					.findViewById(R.id.forumDiscussThreadAuthor);
			authorTextView.setText(discussThreadReplyPersonListView
					.get(position));

			final TextView timeTextView = (TextView) convertView
					.findViewById(R.id.forumDiscussThreadReplyTime);
			timeTextView.setText(discussThreadReplyTimeListView.get(position));

			final TextView contentTextView = (TextView) convertView
					.findViewById(R.id.forumDiscussThreadContent);
			contentTextView.setText(discussThreadReplyContentListView
					.get(position));
			
			ArrayList<String> fileIDs = discussForumFileIDsListView.get(position);
	        ArrayList<String> fileNames = discussForumFileNamesListView.get(position);
	        
	        if(fileNames.size()==0){
	        	System.out.println(" :():");
	        }
	        else{
	        	((LinearLayout) convertView.findViewById(R.id.attachmentContainer)).setVisibility(View.VISIBLE);
	        
	        	LinearLayout fileList = (LinearLayout) convertView.findViewById(R.id.attachments);
	        	fileList.removeAllViews();
	        	
	        
	        	FileListAdapter adapter = new FileListAdapter(this.context,fileNames,fileIDs,position);
	        	int i;
	        	for(i=0;i<fileNames.size();i++){
	        		View view = adapter.getView(i, null, null);
	        		fileList.addView(view);
	        	}
	        	
	        }
	        return convertView;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return discussThreadReplySubjectListView.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	public class FileListAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final ArrayList<String> filenames;
		private final ArrayList<String> fileids;
		private int discussionNo;

		public FileListAdapter(Context context,
				ArrayList<String> fileNames, ArrayList<String> fileIDs,int discussionNo) {
			super(context, R.layout.filelistviewlayout, fileNames);
			this.context = context;
			this.filenames = fileNames;
			this.fileids = fileIDs;
			this.discussionNo = discussionNo;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/*
			 * This function of adapter sets the list entry
			 * For entry at position check if already exists in filesystem
			 * And take appropriate action
			 */
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.filelistviewlayout,
					parent, false);

			final TextView textView = (TextView) rowView
					.findViewById(R.id.myFileName);
			// Set Name
			System.out.println("getView - "+filenames.get(position));
			textView.setText(filenames.get(position));

			final TextView textViewDownload = (TextView) rowView
					.findViewById(R.id.myFileDownloadStatus);

			final Button fileOperationButton = (Button) rowView
					.findViewById(R.id.myFileButton);
			
			/*
			 * TODO: Whats happening Here ?? 
			 */
			//TODO - added by Ayush - have to modify this i think so
			textView.setTag(-position - discussionNo*100);
			textViewDownload.setId(-position - discussionNo*100);
			fileOperationButton.setId(-position - discussionNo*1000);
			
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

			if (match == 1) {
				// f.exists()
				// Saving file extension in Hint for existing file..
				textView.setHint(extension);

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
				textView.setHint(fileids.get(position));
				fileOperationButton.setText("Download");
			}

			fileOperationButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String fOperationButtonID = fileOperationButton.getId() + "";
					if (fileOperationButton.getText() == "Download") {
						fileOperationButton.setEnabled(false);
						fileOperationButton
								.setBackgroundResource(R.drawable.black_btm);
						
						/*
						 * Disable button and start download
						 * This is done via ASync Task
						 */
						DownloadFile downloadFile = new DownloadFile();
						downloadFile.execute(textView.getText().toString(),
								textView.getHint().toString(), textView
										.getTag().toString(),
								fOperationButtonID);
					} else {
						String file = textView.getText().toString() + "."
								+ textView.getHint();
						String fileUrl = android.os.Environment
								.getExternalStorageDirectory().getPath()
								+ "/MDroid/" + courseName + "/" + file;
						
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
	public class DownloadFile extends AsyncTask<String, Integer, String> {
		int resID;
		int resIDButton;
		String fileSizeString = "";

		@Override
		protected String doInBackground(String... fileDetails) {
			try {
				// Extracting FileName and FileUrl from FileData....
				String fileName = fileDetails[0];
				String filePath = fileDetails[1];
				String DownloadStatusTextViewID = fileDetails[2];
				String fOperationButtonID = fileDetails[3];
				
				resID = getResources().getIdentifier(DownloadStatusTextViewID,
						"id", getPackageName());
				resIDButton = getResources().getIdentifier(fOperationButtonID,
						"tag", getPackageName());

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
				// only for files hosted on Moodle server..//
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
				prevIndex = fileNameInServer.indexOf("filename=\"", prevIndex) + 11;
				endIndex = fileNameInServer.indexOf("\"", prevIndex);
				fileNameInServer = fileNameInServer.substring(prevIndex,
						endIndex);
				prevIndex = fileNameInServer.lastIndexOf(".") + 1;
				endIndex = fileNameInServer.length();
				String fileExtension = fileNameInServer.substring(prevIndex,
						endIndex);

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

			TextView downloadStatusTextView = (TextView) findViewById(resID);
			Button fileOperationButton = (Button) findViewById(resIDButton);
			if (downloadStatusTextView != null) {
				downloadStatusTextView.setText(fileSizeString + ", "
						+ progress[0] + "% downloaded");
				fileOperationButton.setText("Downloading..");
				fileOperationButton.setEnabled(false);
				fileOperationButton.setBackgroundResource(R.drawable.black_btm);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			TextView downloadStatusTextView = (TextView) findViewById(resID);
			Button fileOperationButton = (Button) findViewById(resIDButton);
			if (downloadStatusTextView != null) {
				downloadStatusTextView.setText(fileSizeString
						+ ", download complete");
				fileOperationButton.setText("Open");
				fileOperationButton.setBackgroundResource(R.color.buttonGreen);
			}
			else{
				System.out.println("downloadStatusTextView = null");
			}
		}
	}
	public String getPageContentForIITBDownload(String filePath)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = MainActivity.httpclient;
		String fileDownloadAddress = "";

		HttpGet httpgetCourse = new HttpGet(filePath);

		HttpResponse responseCourse = httpclient.execute(httpgetCourse);
		HttpEntity entityCourse = responseCourse.getEntity();

		String buffer = inputStreamToString(responseCourse.getEntity().getContent());
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
	private String inputStreamToString(InputStream is)
			throws IOException {
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
}
