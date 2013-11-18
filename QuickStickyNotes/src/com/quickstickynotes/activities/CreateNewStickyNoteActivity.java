package com.quickstickynotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.quickstickynotes.R;
import com.quickstickynotes.models.StickyNote;
import com.quickstickynotes.models.StickyNoteContent;

public class CreateNewStickyNoteActivity extends BaseEditActivity {
	
	static final int REQUEST_AUTHORIZATION = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_edit_stickynote);

		editTitleHolder = (EditText) findViewById(R.id.newStickyNoteTitle);
		editContentHolder = (EditText) findViewById(R.id.newStickyNoteContent);
		contentHolder = (LinearLayout) findViewById(R.id.contentHolder);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		facebookId = extras.getString("facebookId");
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.acceptMenuItem:
			createNewStickyNote();
			return true;
		case R.id.insertPictureMenuItem:
			onInsertPictureMenuItemClicked();
			return true;
		case R.id.insertContactMenuItem:
			onInsertContactMenuItemClicked();
			return true;
		case R.id.logoutMenuItem:
			onLogoutMenuItemClicked();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				createNewStickyNote();
			}
			
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void createNewStickyNote() {
		String title = editTitleHolder.getText().toString();
		StickyNoteContent content = new StickyNoteContent();

		boolean isContentValid = setStickyNoteContent(content);
		boolean isTitleValid = title != null && !title.isEmpty();

		if (isContentValid && isTitleValid) {
			saveNewStickyNote(content, title);
			startUserListOfNotesActivity();
		} else if (!isContentValid) {
			showPopup("Invalid notes", "Please enter some notes.");
		} else if (!isTitleValid) {
			showPopup("Invalid title", "Please enter a title.");
		}
	}

	private void saveNewStickyNote(StickyNoteContent content, String title) {
		StickyNote newStickyNote = new StickyNote();
		newStickyNote.setTitle(title);
		newStickyNote.setContent(content, this);
		newStickyNote.setAuthor(facebookId);
		newStickyNote.saveInBackground();
	}
}
