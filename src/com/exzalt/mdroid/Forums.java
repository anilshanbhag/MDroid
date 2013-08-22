package com.exzalt.mdroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Forums extends BaseActivity {

	String courseName = "";
	String courseID = "";
	String discussionCount = "0";
	ArrayList<String> DiscussionIDs = new ArrayList<String>();
	ArrayList<String> DiscussionSubject = new ArrayList<String>();
	ArrayList<String> DiscussionAuthor = new ArrayList<String>();
	ArrayList<String> DiscussionRepliesCount = new ArrayList<String>();
	ArrayList<String> DiscussionLastPostTime = new ArrayList<String>();

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.forums);
		// For getting Preferences
		appPrefs = new AppPreferences(getApplicationContext());

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		courseID = extras.getString("courseID");
		courseName = extras.getString("courseName");

		// Setting title of FileListing activity..
		setTitle("Forums (" + discussionCount + "): " + courseName);

		new getForumsPageContent().execute(courseID);
	}

	/* AsycTask Thread */
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
			// Changing title of FileListing activity..
			setTitle("Forums (" + discussionCount + "): " + courseName);

			listFilesInListView(DiscussionIDs, DiscussionSubject,
					DiscussionAuthor, DiscussionRepliesCount,
					DiscussionLastPostTime);

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

		extractFileDetailsForForumsOne(total.toString());
	}

	public void extractFileDetailsForForumsOne(String htmlDataString) {

		int prevIndex = 0;
		int endIndex = 0;
		String forumViewID = "";

		while (true) {
			prevIndex = htmlDataString.indexOf("<a href=\"view.php?f=",
					prevIndex);

			if (prevIndex == -1)
				break;

			prevIndex += 20;
			endIndex = htmlDataString.indexOf("\"", prevIndex);
			forumViewID = (htmlDataString.substring(prevIndex, endIndex));
		}

		try {
			getPageContentForumsTwo(forumViewID);
		} catch (ClientProtocolException e) {

		} catch (IOException e) {

		}

	}

	public void getPageContentForumsTwo(String forumViewID)
			throws ClientProtocolException, IOException {

		DefaultHttpClient httpclient = MainActivity.httpclient;

		HttpGet httpgetCourse = new HttpGet(serverAddress
				+ "/mod/forum/view.php?f=" + forumViewID);

		HttpResponse responseCourse = httpclient.execute(httpgetCourse);
		HttpEntity entityCourse = responseCourse.getEntity();

		try {
			inputStreamToStringForumsTwo(responseCourse.getEntity()
					.getContent());
		} catch (IOException e) {

		}

		if (entityCourse != null) {
			entityCourse.consumeContent();
		}
	}

	private void inputStreamToStringForumsTwo(InputStream is)
			throws IOException {
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null) {
			total.append(line);
		}

		extractFileDetailsForForumsTwo(total.toString());
	}

	// Call to ListFilesInListView for Forum files made here..

	public void extractFileDetailsForForumsTwo(String htmlDataString) {

		int prevIndex = 0;
		int endIndex = 0;

		while (true) {
			prevIndex = htmlDataString.indexOf(
					"class=\"topic starter\"><a href=\"", prevIndex);

			if (prevIndex == -1)
				break;

			// for Post ID
			prevIndex += 31;
			endIndex = htmlDataString.indexOf("\"", prevIndex);
			DiscussionIDs.add(htmlDataString.substring(prevIndex, endIndex));

			// for post subject
			prevIndex = endIndex + 2;
			endIndex = htmlDataString.indexOf("</a>", prevIndex);
			String textConvertedhtmlDataString = htmlDataString.substring(
					prevIndex, endIndex);
			textConvertedhtmlDataString = android.text.Html.fromHtml(
					textConvertedhtmlDataString).toString();
			DiscussionSubject.add(textConvertedhtmlDataString);

			// for post Author
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf(
					"<td class=\"author\"><a href=\"", prevIndex) + 28;
			prevIndex = htmlDataString.indexOf("\">", prevIndex) + 2;
			endIndex = htmlDataString.indexOf("</a>", prevIndex);
			DiscussionAuthor.add(htmlDataString.substring(prevIndex, endIndex));

			// for post replies count
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf(
					"<td class=\"replies\"><a href=\"", prevIndex) + 29;
			prevIndex = htmlDataString.indexOf("\">", prevIndex) + 2;
			endIndex = htmlDataString.indexOf("</a>", prevIndex);
			DiscussionRepliesCount.add(htmlDataString.substring(prevIndex,
					endIndex));

			// for post last reply time
			prevIndex = endIndex;
			prevIndex = htmlDataString.indexOf(
					"<td class=\"lastpost\"><a href=\"", prevIndex) + 30;
			prevIndex = htmlDataString.indexOf("\">", prevIndex) + 2;
			prevIndex = htmlDataString.indexOf("\">", prevIndex) + 2;
			prevIndex = htmlDataString.indexOf(",", prevIndex) + 2;
			endIndex = htmlDataString.indexOf("</a>", prevIndex);
			DiscussionLastPostTime.add(htmlDataString.substring(prevIndex,
					endIndex));

		}

		discussionCount = DiscussionIDs.size() + "";

	}

	public void listFilesInListView(ArrayList<String> DiscussionIDs,
			ArrayList<String> DiscussionSubject,
			ArrayList<String> DiscussionAuthor,
			ArrayList<String> DiscussionRepliesCount,
			ArrayList<String> DiscussionLastPostTime) {
		if(DiscussionSubject.size() == 0){
			((TextView)this.findViewById(R.id.forumTitle)).setText("Forum Empty");
			((ProgressBar)this.findViewById(R.id.progressBarForum)).setVisibility(View.GONE);
			return;
		}
		this.findViewById(R.id.forum_loading_card).setVisibility(View.GONE);
		
		ListView forumsList = (ListView) findViewById(R.id.forumListView);

		ForumCardAdapter cardAdapter = new ForumCardAdapter(this);
		
		for(int i=0; i<DiscussionSubject.size(); i++){
			cardAdapter.add(new ForumCard(DiscussionIDs.get(i),
					DiscussionSubject.get(i),
					DiscussionAuthor.get(i),
					DiscussionRepliesCount.get(i),
					DiscussionLastPostTime.get(i)));
		}
		forumsList.setAdapter(cardAdapter);
		ListHelper.getListViewSize(forumsList);
	}

	public class ForumCardAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<ForumCard> items;
		private boolean isChanged = false;
	    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
	    private int mAccentColor;
	    private int mPopupMenu = -1;
	    private boolean mCardsClickable = true;
	    private int mLayout = R.layout.forumslistviewlayout;

	    public ForumCardAdapter(Context context) {
	        this.context = context;
	        this.items = new ArrayList<ForumCard>();
	        mAccentColor = context.getResources().getColor(android.R.color.black);
	    }
	    @Override
	    public boolean isEnabled(int position) {
	        ForumCard item = getItem(position);
	        if (!mCardsClickable) 
	        	return false;
	        else
	        	return true;
	        
	    }
	    public final ForumCardAdapter setAccentColor(int color) {
	        mAccentColor = color;
	        return this;
	    }
	    public final ForumCardAdapter setAccentColorRes(int colorRes) {
	        setAccentColor(getContext().getResources().getColor(colorRes));
	        return this;
	    }
	    public final ForumCardAdapter setCardLayout(int layoutRes) {
	        mLayout = layoutRes;
	        return this;
	    }
	    public int getLayout(int index, int type) {
	        return getItem(index).getLayout();
	    }
	    private void invalidatePadding(int index, View view) {
	        int top = index == 0 ? R.dimen.card_outer_padding_firstlast : R.dimen.card_outer_padding_top;
	        int bottom = index == (getCount() - 1) ? R.dimen.card_outer_padding_firstlast : R.dimen.card_outer_padding_top;
	        view.setPadding(view.getPaddingLeft(),
	                getContext().getResources().getDimensionPixelSize(top),
	                view.getPaddingRight(),
	                getContext().getResources().getDimensionPixelSize(bottom));
	    }
	    @Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
	    public ForumCard getItem(int i) {
	        return items.get(i);
	    }

		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
	            convertView = LayoutInflater.from(context).inflate(R.layout.forumslistviewlayout, null);
	        }
			invalidatePadding(position, convertView);
			ForumCard item = new ForumCard(getItem(position));
			final TextView subjectTextView = (TextView) convertView
					.findViewById(R.id.forumThreadSubject);
			subjectTextView.setText(item.getSubject());
			subjectTextView.setHint(item.getId());

			final TextView repliesCountTextView = (TextView) convertView
					.findViewById(R.id.forumThreadRepliesCount);
			repliesCountTextView.setText("("
					+ item.getRepliesCount() + ")");

			final TextView authorTextView = (TextView) convertView
					.findViewById(R.id.forumThreadAuthor);
			authorTextView.setText(item.getAuthor());

			final TextView timeTextView = (TextView) convertView
					.findViewById(R.id.forumThreadStartTime);
			timeTextView.setText(item.getLastPostTime());

			convertView.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					final int REQUEST_CODE = 16;
					Intent i = new Intent(context, ForumDiscussThread.class);
					i.putExtra("discussID", subjectTextView.getHint()
							+ "&mode=1");
					i.putExtra("discussSubject", subjectTextView.getText());
					startActivityForResult(i, REQUEST_CODE);
				}
			});

			return convertView;
		}
		public final Context getContext() {
			return context;
		}
		//functions below from Silk Adapter
		public void add(int index, ForumCard toAdd) {
	        isChanged = true;
	        this.items.add(index, toAdd);
	        notifyDataSetChanged();
	    }
		/**
	     * Adds a single item to the adapter and notifies the attached ListView.
	     */
	    public void add(ForumCard toAdd) {
	        isChanged = true;
	        this.items.add(toAdd);
	        //notifyDataSetChanged();

	    }
	    /**
	     * Updates a single item in the adapter using isSame() from SilkComparable. Once the filter finds the item, the loop is broken
	     * so you cannot update multiple items with a single call.
	     * <p/>
	     * If the item is not found, it will be added to the adapter.
	     *
	     * @return True if the item was updated.
	     */
	    public boolean update(ForumCard toUpdate) {
	        return update(toUpdate, true);
	    }

	    /**
	     * Updates a single item in the adapter using isSame() from SilkComparable. Once the filter finds the item, the loop is broken
	     * so you cannot update multiple items with a single call.
	     *
	     * @param addIfNotFound Whether or not the item will be added if it's not found.
	     * @return True if the item was updated or added.
	     */
	    public boolean update(ForumCard toUpdate, boolean addIfNotFound) {
	        boolean found = false;
	        for (int i = 0; i < items.size(); i++) {
	            if (toUpdate.isSameAs(items.get(i))) {
	                items.set(i, toUpdate);
	                found = true;
	                break;
	            }
	        }
	        if (found) return true;
	        else if (addIfNotFound && !found) {
	            add(toUpdate);
	            return true;
	        }
	        return false;
	    }


	}
}
