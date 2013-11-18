package com.quickstickynotes.activities;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.quickstickynotes.R;
import com.quickstickynotes.adapters.StickyNotesAdapter;
import com.quickstickynotes.datapersister.DownloadFacebookProfilePictureTask;
import com.quickstickynotes.datapersister.GoogleDrivePersister;
import com.quickstickynotes.datapersister.StickyNotesPersister;
import com.quickstickynotes.models.AccountsConnection;
import com.quickstickynotes.models.StickyNote;

public class UserListOfNotesActivity extends ListActivity implements
		ILogoutMenuItem {

	private static final int REQUEST_ACCOUNT_PICKER = 1;
	private static final String APP_NAME = "QuickStickyNotes";

	private GoogleAccountCredential credential;

	private String facebookId;
	private String userName;
	private List<StickyNote> userNotes;
	private ActionBar actionBar;
	private StickyNotesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_of_notes);
		actionBar = this.getActionBar();

		credential = GoogleAccountCredential.usingOAuth2(this,
				Arrays.asList(DriveScopes.DRIVE_APPDATA));

		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// Check if the user is currently logged
			// and show any cached content
			updateViewsWithProfileInfo();
			loadUserStickyNotes();
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null
					&& data.getExtras() != null) {
				String googleName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (googleName != null) {
					AccountsConnection connection = new AccountsConnection();
					connection.setFacebookId(facebookId);
					connection.setGoogleName(googleName);
					connection.saveInBackground();
					credential.setSelectedAccountName(googleName);
					Drive service = getDriveService(credential);
					GoogleDrivePersister.createPersister(service);
				}
			}

			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		MenuItem edit = menu.findItem(R.id.editMenuItem);
		edit.setVisible(false);

		MenuItem remove = menu.findItem(R.id.discardMenuItem);
		remove.setVisible(false);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.logoutMenuItem:
			onLogoutMenuItemClicked();
			return true;
		case R.id.createNewMenuItem:
			onCreateNewMenuItemClicked();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent stickyNoteDetailedIntent = new Intent(
				UserListOfNotesActivity.this, StickyNoteDetailedActivity.class);

		Bundle stickyNoteSelectedBundle = new Bundle();
		stickyNoteSelectedBundle.putInt("position", position);
		stickyNoteSelectedBundle.putString("facebookId", facebookId);

		stickyNoteDetailedIntent.putExtras(stickyNoteSelectedBundle);
		startActivity(stickyNoteDetailedIntent);
	}

	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							// Create a JSON object to hold the profile info
							handleGetUserData(user);

						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d(ParseInitializer.TAG,
										"The facebook session was invalidated.");
								onLogoutMenuItemClicked();
							} else {
								Log.d(ParseInitializer.TAG,
										"Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});

		request.executeAsync();
	}

	private void handleGetUserData(GraphUser user) {
		JSONObject userProfile = new JSONObject();
		try {
			// Populate the JSON object
			String userId = user.getId();
			userProfile.put("facebookId", userId);
			userProfile.put("name", user.getName());

			// Save the user profile info in a user property
			ParseUser currentUser = ParseUser.getCurrentUser();
			currentUser.put("profile", userProfile);
			currentUser.saveInBackground();

			getGoogleDriveAccount(userId);

			// Show the user info
			updateViewsWithProfileInfo();
		} catch (JSONException e) {
			Log.d(ParseInitializer.TAG, "Error parsing returned user data.");
		}
	}

	private void getGoogleDriveAccount(String userId) {
		ParseQuery<AccountsConnection> query = ParseQuery
				.getQuery("AccountsConnection");
		query.whereEqualTo("facebookId", userId);
		query.findInBackground(new FindCallback<AccountsConnection>() {

			@Override
			public void done(List<AccountsConnection> connections,
					ParseException e) {
				if (e == null && connections.size() == 1) {
					AccountsConnection connection = connections.get(0);
					credential.setSelectedAccountName(connection
							.getGoogleName());
					Drive service = getDriveService(credential);
					GoogleDrivePersister.createPersister(service);
				} else {
					startActivityForResult(credential.newChooseAccountIntent(),
							REQUEST_ACCOUNT_PICKER);
				}
			}
		});
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).setApplicationName(APP_NAME)
				.build();
	}

	private void updateViewsWithProfileInfo() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser.get("profile") != null) {
			JSONObject userProfile = currentUser.getJSONObject("profile");
			try {
				facebookId = userProfile.get("facebookId").toString();
				userName = userProfile.getString("name");
				BitmapDrawable drawableProfilePicture = getProfilePicture();

				drawableProfilePicture.setTargetDensity(500);
				actionBar.setLogo(drawableProfilePicture);
				actionBar.setTitle(userName);
			} catch (JSONException e) {
				Log.d(ParseInitializer.TAG, "Error parsing saved user data.");
			}

		}
	}

	private void loadUserStickyNotes() {
		Callback refreshAdapter = new Callback() {

			@Override
			public boolean handleMessage(Message message) {
				adapter.notifyDataSetChanged();
				return true;
			}

		};
		userNotes = StickyNotesPersister.getNotes();
		userNotes = StickyNotesPersister.loadNotesFromParse(facebookId,
				refreshAdapter);
		adapter = new StickyNotesAdapter(UserListOfNotesActivity.this,
				userNotes);
		setListAdapter(adapter);
	}

	private BitmapDrawable getProfilePicture() {
		URL profilePictureUrl;
		Bitmap bitmapProfilePicture = null;
		try {
			profilePictureUrl = new URL("http://graph.facebook.com/"
					+ facebookId + "/picture?type=small");
			bitmapProfilePicture = (new DownloadFacebookProfilePictureTask()
					.execute(profilePictureUrl)).get();
		} catch (Exception e) {
			// Set default profile picture
			int defaultPicId = com.facebook.android.R.drawable.com_facebook_profile_default_icon;
			bitmapProfilePicture = BitmapFactory.decodeResource(getResources(),
					defaultPicId);
			e.printStackTrace();
		}

		Resources appResources = this.getResources();
		BitmapDrawable drawableProfilePicture = new BitmapDrawable(
				appResources, bitmapProfilePicture);
		return drawableProfilePicture;
	}

	private void onCreateNewMenuItemClicked() {
		Intent createNewStickyNoteIntent = new Intent(this,
				CreateNewStickyNoteActivity.class);
		Bundle createNewStickyNoteExtras = new Bundle();
		createNewStickyNoteExtras.putString("facebookId", facebookId);
		createNewStickyNoteIntent.putExtras(createNewStickyNoteExtras);
		startActivity(createNewStickyNoteIntent);
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
