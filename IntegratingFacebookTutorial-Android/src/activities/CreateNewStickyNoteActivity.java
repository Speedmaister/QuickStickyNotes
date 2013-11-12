package activities;

import models.StickyNote;
import models.StickyNoteContent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

public class CreateNewStickyNoteActivity extends BaseEditActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_edit_stickynote);

		editTitleHolder = (EditText) findViewById(R.id.newStickyNoteTitle);
		editContentHolder = (EditText) findViewById(R.id.newStickyNoteContent);
		contentHolder = (LinearLayout) findViewById(R.id.contentHolder);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
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
		case R.id.logoutMenuItem:
			onLogoutMenuItemClicked();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void onInsertContactMenuItemClicked() {
		Intent pickContactIntent = new Intent(this,activities.PickContactActivity.class);
		//
		// PROBLEM SPOT
		//
	    startActivityForResult(pickContactIntent, REQUEST_PICK_CONTACT);
	}

	private void createNewStickyNote() {
		// String content = editContentHolder.getText().toString();
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
		newStickyNote.setContent(content);
		newStickyNote.setAuthor(ParseUser.getCurrentUser());
		newStickyNote.saveInBackground();
	}
}
