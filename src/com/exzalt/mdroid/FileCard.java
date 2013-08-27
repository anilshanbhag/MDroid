package com.exzalt.mdroid;

import java.util.ArrayList;

import com.exzalt.mdroid.TimeLine.CardAdapter;
import com.exzalt.mdroid.model.Week;

/**
 * Represents a single card displayed in a {@link CardAdapter}.
 *
 * @author Aidan Follestad (afollestad)
 */
public class FileCard {
	
	private String title;
    private ArrayList<String> resourcesFileIDs;
    private ArrayList<String> resourcesFileNames;
    private int mLayout= R.layout.list_item_card;
    private boolean isClickable = false;
	private boolean isHeader = false;
	private boolean shouldIgnore = false;
	
    public FileCard(Week week) {
    	title = week.weekSpan;
    	this.resourcesFileIDs = new ArrayList<String>(week.resourcesFileIDs);
    	this.resourcesFileNames = new ArrayList<String>(week.resourcesFileNames);
    }
    public FileCard(FileCard fileCard) {
		this.title = fileCard.title;
		this.resourcesFileIDs = fileCard.resourcesFileIDs;
		this.resourcesFileNames = fileCard.resourcesFileNames;
	}
	public void printDetails(){
    	System.out.println("Week span = "+title);
    	for(int i=0;i<resourcesFileNames.size();i++){
    		System.out.println(resourcesFileNames.get(i));
    	}
    }
	public String getTitle() {
		return title;
	}
	public boolean isClickable() {
		return isClickable;
	}
	public int getLayout() {
		return mLayout;
	}
	public ArrayList<String> getResourcesFileIDs() {
		return resourcesFileIDs;
	}
	public ArrayList<String> getResourcesFileNames() {
		return resourcesFileNames;
	}
	public boolean shouldIgnore() {
		
		return shouldIgnore;
	}
	public boolean isSameAs(FileCard fileCard) {
		// TODO Auto-generated method stub
		return false;
	}
}