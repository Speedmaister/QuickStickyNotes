package activities;

import java.util.ArrayList;

import models.Contact;
import models.ContentTypes;
import models.ImageContent;
import models.StickyNote;
import models.StickyNoteContent;
import models.TextContent;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
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
import com.parse.integratingfacebooktutorial.R;

import datapersister.StickyNotesPersister;

public class StickyNoteDetailedActivity extends Activity implements
		ILogoutMenuItem {

	// private String stickyNoteId;
	private StickyNote stickyNote;
	private int position;
	private String facebookId;
	private TextView stickyNoteTitleHolder;
	private LinearLayout stickyNoteContentHolder;
	private ProgressDialog progressDialog;
	private ActionBar actionBar;
	private StickyNoteContent cachedStickyNoteContent;
	private ArrayList<Contact> contactsInserted= new ArrayList<Contact>();;

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
		facebookId = selectedStickyNoteBundle.getString("facebookId");
		stickyNote = StickyNotesPersister.getNote(position);

		stickyNoteTitleHolder.setText(stickyNote.getTitle());
		cachedStickyNoteContent = stickyNote.getContent();
		displayStickyNoteContent();
		// Fill FIELDS
	}

	private void displayStickyNoteContent() {
		ArrayList<Pair<ContentTypes, Object>> contents = cachedStickyNoteContent
				.getContents();
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
		stickyNoteBundle.putString("facebookId", facebookId);

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
		StickyNotesPersister.deleteStickyNote(stickyNote.getObjectId(),
				dismissDialog);
	}

	private void startUserListOfNotesActivity() {
		Intent userListOfNotesIntent = new Intent(this,
				UserListOfNotesActivity.class);
		// userListOfNotesIntent.putExtra("refreshStickyNotesList", true);
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

}
