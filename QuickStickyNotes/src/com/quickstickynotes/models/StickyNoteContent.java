package com.quickstickynotes.models;

import java.util.ArrayList;

import android.util.Pair;

public class StickyNoteContent {
	private ArrayList<Pair<ContentTypes,Object>> contentsOrdered;
	
	public StickyNoteContent(){
		this.contentsOrdered = new ArrayList<Pair<ContentTypes,Object>>();
	}
	
	public void insertTextChild(TextContent text)
	{
		contentsOrdered.add(new Pair<ContentTypes, Object>(ContentTypes.Text,text));
	}
	
	public void insertImageChild(ImageContent image)
	{
		contentsOrdered.add(new Pair<ContentTypes, Object>(ContentTypes.Image, image));
	}
	
	public void insertContactChild(Contact contact)
	{
		contentsOrdered.add(new Pair<ContentTypes, Object>(ContentTypes.Contact, contact));
	}
	
	public ArrayList<Pair<ContentTypes,Object>> getContents(){
		return this.contentsOrdered;
	}
}
