package com.exzalt.mdroid;

import java.util.ArrayList;

import com.exzalt.mdroid.TimeLine.CardAdapter;

/**
 * Represents a single card displayed in a {@link CardAdapter}.
 * 
 * @author Aidan Follestad (afollestad)
 */
public class FileCard {

	private ArrayList<String> resourcesFileIDs;
	private ArrayList<String> resourcesFileNames;
	private int mLayout = R.layout.list_item_card;
	private boolean isClickable = false;
	private boolean isHeader = false;
	private boolean shouldIgnore = false;

	public FileCard(ArrayList<String> resourcesFileIDs,
			ArrayList<String> resourcesFileNames) {
		this.resourcesFileIDs = resourcesFileIDs;
		this.resourcesFileNames = resourcesFileNames;
	}

	public FileCard(FileCard fileCard) {
		this.resourcesFileIDs = fileCard.resourcesFileIDs;
		this.resourcesFileNames = fileCard.resourcesFileNames;
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