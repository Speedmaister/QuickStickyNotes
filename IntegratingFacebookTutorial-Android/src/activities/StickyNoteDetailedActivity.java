package activities;

import java.util.ArrayList;

import models.ContentTypes;
import models.ImageContent;
import models.StickyNote;
import models.StickyNoteContent;
import models.TextContent;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import datapersister.StickyNotesPersister;

public class StickyNoteDetailedActivity extends Activity implements
		ILogoutMenuItem {

	// private String stickyNoteId;
	private StickyNote stickyNote;
	private int position;
	private TextView stickyNoteTitleHolder;
	private LinearLayout stickyNoteContentHolder;
	private ProgressDialog progressDialog;
	private ActionBar actionBar;
	private StickyNoteContent cachedStickyNoteContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stickynote);

		getActionBar();

		stickyNoteTitleHolder = (TextView) findViewById(R.id.stickyNoteTitle);
		stickyNoteContentHolder = (LinearLayout) findViewById(R.id.stickyNoteContent);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle selectedStickyNoteBundle = intent.getExtras();
		position = selectedStickyNoteBundle.getInt("position");
		stickyNote = StickyNotesPersister.getNote(position);

		stickyNoteTitleHolder.setText(stickyNote.getTitle());
		cachedStickyNoteContent = stickyNote.getContent();
		displayStickyNoteContent();
		// Fill FIELDS
	}

	private void displayStickyNoteContent() {
		ArrayList<Pair<ContentTypes, Object>> contents = cachedStickyNoteContent.getContents();
		int size = contents.size();
		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, Object> element = contents.get(i);
			switch (element.first) {
			case Text:
				TextContent text = (TextContent) element.second;
				TextView textHolder = new TextView(this);
				textHolder.setText(text.getText());
				stickyNoteContentHolder.addView(textHolder);
				break;
			case Contact:
				// TODO
				break;
			case Image:
				ImageContent imageCont = (ImageContent) element.second;
				ImageView imageHolder = new ImageView(this);
				imageHolder.setImageBitmap(imageCont.getImage());
				stickyNoteContentHolder.addView(imageHolder);
				break;
			case Map:
				// TODO
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		MenuItem search = menu.findItem(R.id.searchMenuItem);
		search.setVisible(false);

		MenuItem newStickyNote = menu.findItem(R.id.createNewMenuItem);
		newStickyNote.setVisible(false);

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

		stickyNoteBundle.putInt("position", position);

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
		StickyNotesPersister.deleteStickyNote(stickyNote.getObjectId(), dismissDialog);
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
