package com.quickstickynotes.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickstickynotes.R;
import com.quickstickynotes.datapersister.GetAndDisplayStickyNoteContentTask;
import com.quickstickynotes.datapersister.StickyNotesPersister;
import com.quickstickynotes.models.Contact;
import com.quickstickynotes.models.ContentTypes;
import com.quickstickynotes.models.ImageContent;
import com.quickstickynotes.models.StickyNote;
import com.quickstickynotes.models.StickyNoteContent;
import com.quickstickynotes.models.TextContent;

public class EditStickyNoteActivity extends BaseEditActivity implements
		IDisplayContent {

	private StickyNote stickyNote;
	private int position;

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
		Bundle selectedStickyNoteBundle = intent.getExtras();
		position = selectedStickyNoteBundle.getInt("position");
		stickyNote = StickyNotesPersister.getNote(position);
		editTitleHolder.setText(stickyNote.getTitle());
		facebookId = selectedStickyNoteBundle.getString("facebookId");
		GetAndDisplayStickyNoteContentTask stickyNoteContentTask = 
										new GetAndDisplayStickyNoteContentTask(this);
		stickyNoteContentTask.execute(stickyNote);
	}

	private void handleContactContent(Pair<ContentTypes, Object> element) {
		LinearLayout contactHolder = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.contact_view, null);
		Contact contact = (Contact) element.second;
		Button callButton = (Button) contactHolder
				.findViewById(R.id.callButton);
		callButton.setText("X");
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View eventTarget) {
				removeContact(eventTarget);
			}
		});

		TextView nameHolder = (TextView) contactHolder
				.findViewById(R.id.nameHolder);
		nameHolder.setText(contact.getName());

		contentHolder.addView(contactHolder, contactsInserted.size() + 1);
		contactsInserted.add(contact);
	}

	private void removeContact(View eventTarget) {
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

		contentHolder.removeView(contactHolder);
		contactsInserted.remove(contact);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.acceptMenuItem:
			saveEdittedStickyNote();
			startStickyNoteDetailedActivity();
			return true;
		case R.id.insertPictureMenuItem:
			onInsertPictureMenuItemClicked();
			return true;
		case R.id.insertContactMenuItem:
			onInsertContactMenuItemClicked();
			return true;
		case android.R.id.home:
			startStickyNoteDetailedActivity();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void saveEdittedStickyNote() {
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
		stickyNote.setTitle(title);
		stickyNote.setContent(content, this);
		String stickyNoteId = stickyNote.getObjectId();
		StickyNotesPersister.clearCachedStickyNoteContent(stickyNoteId);
		StickyNotesPersister.cacheStickyNoteContent(stickyNoteId, content);
		stickyNote.saveInBackground();
	}

	private void startStickyNoteDetailedActivity() {
		Intent stickyNoteDetailedIntent = new Intent(this,
				StickyNoteDetailedActivity.class);
		Bundle stickyNodeBundle = new Bundle();

		stickyNodeBundle.putInt("position", position);

		stickyNoteDetailedIntent.putExtras(stickyNodeBundle);
		startActivity(stickyNoteDetailedIntent);
	}

	@Override
	public void displayStickyNoteContent(StickyNoteContent content) {
		ArrayList<Pair<ContentTypes, Object>> contents = content.getContents();
		int size = contents.size();
		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, Object> element = contents.get(i);
			switch (element.first) {
			case Text:
				TextContent text = (TextContent) element.second;
				editContentHolder.setText(text.getText());
				break;
			case Contact:
				handleContactContent(element);
				break;
			case Image:
				ImageContent imageCont = (ImageContent) element.second;
				ImageView imageHolder = new ImageView(this);
				imageHolder.setImageBitmap(imageCont.getImage());
				contentHolder.addView(imageHolder);
				break;
			case Map:
				// TODO
				break;
			}
		}
	}
}
