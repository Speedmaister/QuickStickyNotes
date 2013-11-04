package activities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import models.StickyNote;

import org.json.JSONException;
import org.json.JSONObject;

import adapters.StickyNotesAdapter;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import datapersister.DownloadFacebookProfilePictureTask;
import datapersister.StickyNotesPersister;

public class UserListOfNotesActivity extends ListActivity implements
		ILogoutMenuItem {

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		MenuItem accept = menu.findItem(R.id.acceptMenuItem);
		accept.setVisible(false);

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

	private void onCreateNewMenuItemClicked() {
		Intent createNewStickyNoteIntent = new Intent(this,
				CreateNewStickyNoteActivity.class);
		startActivity(createNewStickyNoteIntent);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		StickyNote stickyNoteSelected = userNotes.get(position);
		Intent stickyNoteDetailedIntent = new Intent(
				UserListOfNotesActivity.this, StickyNoteDetailedActivity.class);
		Bundle stickyNoteSelectedBundle = new Bundle();
		stickyNoteSelectedBundle.putString("title",
				stickyNoteSelected.getTitle());
		stickyNoteSelectedBundle.putString("content",
				stickyNoteSelected.getContent());
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
			userProfile.put("facebookId", user.getId());
			userProfile.put("name", user.getName());

			// Save the user profile info in a user property
			ParseUser currentUser = ParseUser.getCurrentUser();
			currentUser.put("profile", userProfile);
			currentUser.saveInBackground();

			// Show the user info
			updateViewsWithProfileInfo();
		} catch (JSONException e) {
			Log.d(ParseInitializer.TAG, "Error parsing returned user data.");
		}
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
		userNotes = StickyNotesPersister.GetNotes();
		userNotes = StickyNotesPersister.LoadNotesFromParse(facebookId,
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

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Resources appResources = this.getResources();
		BitmapDrawable drawableProfilePicture = new BitmapDrawable(
				appResources, bitmapProfilePicture);
		return drawableProfilePicture;
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
