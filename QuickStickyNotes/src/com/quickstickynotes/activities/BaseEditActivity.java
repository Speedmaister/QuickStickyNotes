package com.quickstickynotes.activities;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.quickstickynotes.R;
import com.quickstickynotes.models.Contact;
import com.quickstickynotes.models.ImageContent;
import com.quickstickynotes.models.StickyNoteContent;
import com.quickstickynotes.models.TextContent;

public class BaseEditActivity extends Activity implements ILogoutMenuItem {

	protected static final int REQUEST_PICK_CONTACT = 3;
	protected static final int SELECT_PHOTO = 100;
	protected static final int IMAGE_SIZE = 400;

	protected ArrayList<Contact> contactsInserted = new ArrayList<Contact>();
	protected String facebookId;

	protected EditText editTitleHolder;
	protected EditText editContentHolder;
	protected ActionBar actionBar;
	protected LinearLayout contentHolder;

	protected void onInsertPictureMenuItemClicked() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				displayImage(data);
			}

			break;
		case REQUEST_PICK_CONTACT:
			if (resultCode == RESULT_OK) {
				displayContact(data);
			}

			break;
		}
	}

	private void displayContact(Intent data) {
		Uri contactUri = data.getData();
		String[] projection = { Phone.NUMBER, Contacts.DISPLAY_NAME };
		Cursor cursor = getContentResolver().query(contactUri,
				projection, null, null, null);
		cursor.moveToFirst();

		int nameColumn = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
		int numberColumn = cursor.getColumnIndex(Phone.NUMBER);
		final String number = cursor.getString(numberColumn);
		String name = cursor.getString(nameColumn);
		Contact contact = new Contact(name, number);
		contactsInserted.add(contact);
		createContactView(number, name);
	}

	private void displayImage(Intent data) {
		Uri selectedImageUri = data.getData();
		try {
			Bitmap selectedImage = getAndResizeImage(selectedImageUri);
			ImageView imageViewer = new ImageView(this);
			imageViewer.setImageBitmap(selectedImage);
			setImageViewLayout(imageViewer);

			contentHolder.addView(imageViewer);
		} catch (FileNotFoundException e) {
			// File was pick by the user so this exception is only needed by the IDE
		}
	}

	private void createContactView(final String number, String name) {
		LinearLayout contactHolder = (LinearLayout) LayoutInflater.from(this)
				.inflate(R.layout.contact_view, null);
		Button callButton = (Button) contactHolder
				.findViewById(R.id.callButton);
		callButton.setText("X");
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Button button = (Button) v;
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
		});

		TextView nameHolder = (TextView) contactHolder
				.findViewById(R.id.nameHolder);
		nameHolder.setText(name);
		contentHolder.addView(contactHolder, contactsInserted.size());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.create_edit_menu, menu);

		return true;
	}

	private Bitmap getAndResizeImage(Uri selectedImage) throws FileNotFoundException {

		// Decode image size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null,
				options);

		// Find the correct scale value. It should be the power of 2.
		int width = options.outWidth, height = options.outHeight;
		int scale = 1;
		while (true) {
			if (width / 2 < IMAGE_SIZE || height / 2 < IMAGE_SIZE) {
				break;
			}

			width /= 2;
			height /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options finalOptions = new BitmapFactory.Options();
		finalOptions.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null,
				finalOptions);
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

	protected void startUserListOfNotesActivity() {
		Intent userListOfNotesIntent = new Intent(this,
				UserListOfNotesActivity.class);
		startActivity(userListOfNotesIntent);
	}

	protected void showPopup(String title, String message) {
		final AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						alert.cancel();
					}
				});

		alert.show();
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

	protected boolean setStickyNoteContent(StickyNoteContent content) {
		int contactPosition = 0;
		int childrenCount = contentHolder.getChildCount();
		if (childrenCount == 0) {
			return false;
		}

		for (int i = 0; i < childrenCount; i++) {
			View child = contentHolder.getChildAt(i);
			if (child.getClass().equals(EditText.class)) {
				EditText textContainer = (EditText) child;
				String text = textContainer.getText().toString();
				content.insertTextChild(new TextContent(text));
			} else if (child.getClass().equals(ImageView.class)) {
				ImageView imageContainer = (ImageView) child;
				Bitmap image = ((BitmapDrawable) imageContainer.getDrawable())
						.getBitmap();
				content.insertImageChild(new ImageContent(image));
			} else if (child.getClass().equals(LinearLayout.class)) {
				Contact contact = contactsInserted.get(contactPosition);
				content.insertContactChild(contact);
				contactPosition++;
			}
		}

		return true;
	}

	protected void onInsertContactMenuItemClicked() {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
				Uri.parse("content://contacts"));
		pickContactIntent
				.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(pickContactIntent, REQUEST_PICK_CONTACT);
	}
}
