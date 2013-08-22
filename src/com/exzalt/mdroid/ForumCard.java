package com.exzalt.mdroid;

import com.exzalt.mdroid.TimeLine.CardAdapter;

/**
 * Represents a single card displayed in a {@link CardAdapter}.
 *
 * @author Aidan Follestad (afollestad)
 */
public class ForumCard {
	
	private final String discussionID;
	private final String discussionSubject;
	private final String discussionAuthor;
	private final String discussionRepliesCount;
	private final String discussionLastPostTime;
    private int mLayout= R.layout.forumslistviewlayout;
    private boolean isClickable = false;
	private boolean isHeader = false;
	private boolean shouldIgnore = false;
	
    public ForumCard(String discussionID,
    		String discussionSubject,
    		String discussionAuthor,
    		String discussionRepliesCount,
    		String discussionLastPostTime) {
    	this.discussionID = discussionID;
    	this.discussionSubject = discussionSubject;
    	this.discussionAuthor = discussionAuthor;
    	this.discussionRepliesCount = discussionRepliesCount;
    	this.discussionLastPostTime = discussionLastPostTime;
    }
    public ForumCard(ForumCard forumCard) {
    	this.discussionID = forumCard.discussionID;
    	this.discussionSubject = forumCard.discussionSubject;
    	this.discussionAuthor = forumCard.discussionAuthor;
    	this.discussionRepliesCount = forumCard.discussionRepliesCount;
    	this.discussionLastPostTime = forumCard.discussionLastPostTime;
	}
	public String getId() {
		return discussionID;
	}
	public String getSubject() {
		return discussionSubject;
	}
	public String getAuthor() {
		return discussionAuthor;
	}
	public String getRepliesCount() {
		return discussionRepliesCount;
	}
	public String getLastPostTime() {
		return discussionLastPostTime;
	}
	public boolean isClickable() {
		return isClickable;
	}
	public int getLayout() {
		return mLayout;
	}
	public boolean shouldIgnore() {
		
		return shouldIgnore;
	}
	public boolean isSameAs(ForumCard fileCard) {
		// TODO Auto-generated method stub
		return false;
	}
}