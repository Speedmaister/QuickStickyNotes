package activities;

import models.StickyNote;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

public class CreateNewStickyNoteActivity extends Activity implements
		ILogoutMenuItem {

	private EditText editTitleHolder;
	private EditText editContentHolder;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_edit_stickynote);

		editTitleHolder = (EditText) findViewById(R.id.newStickyNoteTitle);
		editContentHolder = (EditText) findViewById(R.id.newStickyNoteContent);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		MenuItem search = menu.findItem(R.id.searchMenuItem);
		search.setVisible(false);
		
		MenuItem accept = menu.findItem(R.id.acceptMenuItem);
		accept.setVisible(false);
		
		MenuItem edit = menu.findItem(R.id.editMenuItem);
		edit.setVisible(false);
		
		// TODO Maybe allow discard in creating a new sticky note
		MenuItem remove = menu.findItem(R.id.discardMenuItem);
		remove.setVisible(false);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		case R.id.createNewMenuItem:
			createNewStickyNote();
			return true;
		case R.id.logoutMenuItem:
			onLogoutMenuItemClicked();
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createNewStickyNote() {
		String content = editContentHolder.getText().toString();
		String title = editTitleHolder.getText().toString();
		
		boolean isContentValid = content != null && !content.isEmpty();
		boolean isTitleValid = title != null && !title.isEmpty();
		
		if (isContentValid && isTitleValid) {
			saveNewStickyNote(content, title);
			startUserListOfNotesActivity();
		} else if (!isContentValid) {
			showPopup("Invalid notes","Please enter some notes.");
		} else if (!isTitleValid) {
			showPopup("Invalid title","Please enter a title.");
		}

	}

	private void startUserListOfNotesActivity() {
		Intent userListOfNotesIntent = new Intent(this,UserListOfNotesActivity.class);
		startActivity(userListOfNotesIntent);
	}

	private void showPopup(String title,String message) {
		final AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton(-3,"Ok",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alert.cancel();
			}
		});
		
		alert.show();
	}

	private void saveNewStickyNote(String content, String title) {
		StickyNote newStickyNote = new StickyNote();
		newStickyNote.setContent(content);
		newStickyNote.setTitle(title);
		newStickyNote.setAuthor(ParseUser.getCurrentUser());
		newStickyNote.saveInBackground();
	}

	@Override
	public void onLogoutMenuItemClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	}

	@Override
	public void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}
}
