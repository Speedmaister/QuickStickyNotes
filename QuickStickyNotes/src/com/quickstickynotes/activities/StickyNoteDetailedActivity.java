package com.quickstickynotes.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.quickstickynotes.R;
import com.quickstickynotes.datapersister.GetAndDisplayStickyNoteContentTask;
import com.quickstickynotes.datapersister.GoogleDrivePersister;
import com.quickstickynotes.datapersister.StickyNotesPersister;
import com.quickstickynotes.models.Contact;
import com.quickstickynotes.models.ContentTypes;
import com.quickstickynotes.models.ImageContent;
import com.quickstickynotes.models.StickyNote;
import com.quickstickynotes.models.StickyNoteContent;
import com.quickstickynotes.models.TextContent;

public class StickyNoteDetailedActivity extends Activity implements
		ILogoutMenuItem, IDisplayContent {

	private StickyNote stickyNote;
	private int position;
	private String facebookId;
	private TextView stickyNoteTitleHolder;
	private LinearLayout stickyNoteContentHolder;
	private ProgressDialog progressDialog;
	private ActionBar actionBar;
	private ArrayList<Contact> contactsInserted = new ArrayList<Contact>();
	private ArrayList<Pair<ContentTypes, Object>> contents;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stickynote);
		progressDialog = ProgressDialog.show(this, "Loading sticky note.",
				"Loading...");
		getActionBar();

		stickyNoteTitleHolder = (TextView) findViewById(R.id.stickyNoteTitle);
		stickyNoteContentHolder = (LinearLayout) findViewById(R.id.stickyNoteContent);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		Bundle selectedStickyNoteBundle = intent.getExtras();
		position = selectedStickyNoteBundle.getInt("position");
		facebookId = selectedStickyNoteBundle.getString("facebookId");
		stickyNote = StickyNotesPersister.getNote(position);

		stickyNoteTitleHolder.setText(stickyNote.getTitle());
		GetAndDisplayStickyNoteContentTask stickyNoteContentTask = 
								new GetAndDisplayStickyNoteContentTask(this);
		stickyNoteContentTask.execute(stickyNote);
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


	private void handleContactContent(Pair<ContentTypes, Object> element) {
		LinearLayout contactHolder = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.contact_view, null);
		Contact contact = (Contact) element.second;
		Button callButton = (Button) contactHolder
				.findViewById(R.id.callButton);
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View eventTarget) {
				makeCall(eventTarget);
			}
		});

		TextView nameHolder = (TextView) contactHolder
				.findViewById(R.id.nameHolder);
		nameHolder.setText(contact.getName());

		stickyNoteContentHolder.addView(contactHolder,
				contactsInserted.size() + 1);
		contactsInserted.add(contact);
	}

	private void makeCall(View eventTarget) {
		Button button = (Button) eventTarget;
		LinearLayout contactHolder = (LinearLayout) button.getParent();
		TextView nameHolder = (TextView) contactHolder
				.findViewById(R.id.nameHolder);
		String contactName = nameHolder.getText().toString();
		Contact contact = null;
		int contactsCount = contactsInserted.size();
		for (int j = 0; j < contactsCount; j++) {
			Contact currentContact = contactsInserted.get(j);
			if (currentContact.getName().equals(contactName)) {
				contact = currentContact;
				break;
			}
		}

		String uri = "tel:" + contact.getNumber();
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse(uri));
		startActivity(intent);
	}
	
	private void startEditStickyNoteActivity() {
		Intent editStickyNoteIntent = new Intent(this,
				EditStickyNoteActivity.class);
		Bundle stickyNoteBundle = new Bundle();

		stickyNoteBundle.putInt("position", position);
		stickyNoteBundle.putString("facebookId", facebookId);

		editStickyNoteIntent.putExtras(stickyNoteBundle);
		startActivity(editStickyNoteIntent);
	}

	private void deleteStickyNote() {
		progressDialog = ProgressDialog.show(this, "Delete sticky note.",
				"Deleting...");
		int contentsSize = contents.size();
		int imageCounter = 0;
		for (int i = 0; i < contentsSize; i++) {
			ContentTypes type = contents.get(i).first;
			if (type.equals(ContentTypes.Image)) {
				imageCounter++;
			}
		}

		String stickyNoteTitle = stickyNote.getTitle();

		deleteStickyNoteImagesFromDrive(stickyNoteTitle, imageCounter);
		stickyNote.deleteInBackground();
		progressDialog.dismiss();
		startUserListOfNotesActivity();
	}

	private void deleteStickyNoteImagesFromDrive(final String stickyNoteTitle,
			final int imagesCount) {
		Thread deleteFilesThread = new Thread(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < imagesCount; i++) {
					String filename = formatTitleForFilename(stickyNoteTitle,
							i + 1);
					GoogleDrivePersister.deleteFile(filename);
				}
			}
		});

		deleteFilesThread.start();
	}

	private String formatTitleForFilename(String title, int imageCounter) {
		String[] splittedTitle = title.split("\\s+");
		StringBuilder filenameBuilder = new StringBuilder();
		for (int i = 0; i < splittedTitle.length; i++) {
			filenameBuilder.append(splittedTitle[i]);
		}

		filenameBuilder.append("_");
		filenameBuilder.append(imageCounter);
		filenameBuilder.append(".jpg");

		return filenameBuilder.toString();
	}

	private void startUserListOfNotesActivity() {
		Intent userListOfNotesIntent = new Intent(this,
				UserListOfNotesActivity.class);
		startActivity(userListOfNotesIntent);
	}

	private void setImageViewLayout(ImageView imageViewer) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int horizontalMargin = (int) getResources().getDimension(
				R.dimen.childview_horizontal_margin);
		int verticalMargin = (int) getResources().getDimension(
				R.dimen.childview_vertical_margin);
		lp.setMargins(horizontalMargin, verticalMargin, horizontalMargin,
				verticalMargin);
		lp.gravity = Gravity.LEFT;
		imageViewer.setLayoutParams(lp);
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

	@Override
	public void displayStickyNoteContent(StickyNoteContent content) {
		contents = content.getContents();
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
				handleContactContent(element);
				break;
			case Image:
				ImageContent imageCont = (ImageContent) element.second;
				ImageView imageHolder = new ImageView(this);
				imageHolder.setImageBitmap(imageCont.getImage());
				setImageViewLayout(imageHolder);
				stickyNoteContentHolder.addView(imageHolder);
				break;
			case Map:
				// TODO
				break;
			}
		}

		progressDialog.dismiss();
	}

}
