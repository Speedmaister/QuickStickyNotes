package activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import datapersister.StickyNotesPersister;

public class StickyNoteDetailedActivity extends Activity implements
		ILogoutMenuItem {

	private String stickyNoteId;
	private String stickyNoteTitle;
	private String stickyNoteContent;
	private TextView stickyNoteTitleHolder;
	private TextView stickyNoteContentHolder;
	private ProgressDialog progressDialog;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stickynote);

		getActionBar();

		stickyNoteTitleHolder = (TextView) findViewById(R.id.stickyNoteTitle);
		stickyNoteContentHolder = (TextView) findViewById(R.id.stickyNoteContent);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle selectedStickyNoteBundle = intent.getExtras();

		stickyNoteTitle = selectedStickyNoteBundle.getString("title");
		stickyNoteContent = selectedStickyNoteBundle.getString("content");
		stickyNoteId = selectedStickyNoteBundle.getString("objectId");
		selectedStickyNoteBundle.getInt("position");

		stickyNoteTitleHolder.setText(stickyNoteTitle);
		stickyNoteContentHolder.setText(stickyNoteContent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		MenuItem search = menu.findItem(R.id.searchMenuItem);
		search.setVisible(false);

		MenuItem newStickyNote = menu.findItem(R.id.createNewMenuItem);
		newStickyNote.setVisible(false);

		MenuItem accept = menu.findItem(R.id.acceptMenuItem);
		accept.setVisible(false);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		case R.id.editMenuItem:
			startEditStickyNoteActivity();
			return true;
		case R.id.discardMenuItem:
			deleteStickyNote();
			return true;
		case R.id.logoutMenuItem:
			onLogoutMenuItemClicked();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void startEditStickyNoteActivity() {
		Intent editStickyNoteIntent = new Intent(this,
				EditStickyNoteActivity.class);
		Bundle stickyNoteBundle = new Bundle();

		stickyNoteBundle.putString("title", stickyNoteTitle);
		stickyNoteBundle.putString("content", stickyNoteContent);
		stickyNoteBundle.putString("objectId", stickyNoteId);

		editStickyNoteIntent.putExtras(stickyNoteBundle);
		startActivity(editStickyNoteIntent);
	}

	private void deleteStickyNote() {
		Callback dismissDialog = new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				progressDialog.dismiss();
				startUserListOfNotesActivity();
				return false;
			}

		};

		progressDialog = ProgressDialog.show(this, "Delete sticky note.",
				"Deleting...");
		StickyNotesPersister.deleteStickyNote(stickyNoteId, dismissDialog);
	}

	private void startUserListOfNotesActivity() {
		Intent userListOfNotesIntent = new Intent(this,
				UserListOfNotesActivity.class);
		// userListOfNotesIntent.putExtra("refreshStickyNotesList", true);
		startActivity(userListOfNotesIntent);
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
