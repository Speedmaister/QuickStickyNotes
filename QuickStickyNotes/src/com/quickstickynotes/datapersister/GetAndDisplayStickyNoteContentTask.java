package com.quickstickynotes.datapersister;

import com.quickstickynotes.activities.IDisplayContent;
import com.quickstickynotes.models.StickyNote;
import com.quickstickynotes.models.StickyNoteContent;

import android.os.AsyncTask;

public class GetAndDisplayStickyNoteContentTask extends AsyncTask<StickyNote, Integer, StickyNoteContent> {
	private IDisplayContent contentDisplayer;
	
	public GetAndDisplayStickyNoteContentTask(IDisplayContent contentDisplayer){
		this.contentDisplayer = contentDisplayer;
	}
	
	@Override
	protected void onPostExecute(StickyNoteContent result) {
		this.contentDisplayer.displayStickyNoteContent(result);
	}

	@Override
	protected StickyNoteContent doInBackground(StickyNote... params) {
		StickyNoteContent cachedStickyNoteContent;
		cachedStickyNoteContent = params[0].getContent();
		return cachedStickyNoteContent;
	}
	
	
}