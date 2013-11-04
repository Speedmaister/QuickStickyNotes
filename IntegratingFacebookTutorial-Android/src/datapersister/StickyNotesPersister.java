package datapersister;

import java.util.ArrayList;
import java.util.List;

import models.StickyNote;
import android.os.Handler.Callback;
import android.os.Message;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class StickyNotesPersister {
	private static List<StickyNote> notes = new ArrayList<StickyNote>();

	public static List<StickyNote> GetNotes() {
		return notes;
	}

	public static List<StickyNote> LoadNotesFromParse(final String userId,
			final Callback refreshAdapter) {
		ParseQuery<StickyNote> query = ParseQuery.getQuery("StickyNote");
		ParseUser currentUser = ParseUser.getCurrentUser();
		query.whereEqualTo("author", currentUser);
		query.findInBackground(new FindCallback<StickyNote>() {

			@Override
			public void done(List<StickyNote> loadedNotes, ParseException e) {
				if (e == null && loadedNotes.size() > 0) {
					notes.clear();
					for (StickyNote note : loadedNotes) {
						notes.add(note);
					}
				} else {
					ParseUser user = ParseUser.getCurrentUser();
					StickyNote note = new StickyNote();
					note.setContent("Some example text to show.");
					note.setTitle("Example Sticky Note");
					note.setAuthor(user);
					notes.add(note);
					note.saveInBackground();
				}

				refreshAdapter.handleMessage(new Message());
			}
		});

		return notes;
	}

}
