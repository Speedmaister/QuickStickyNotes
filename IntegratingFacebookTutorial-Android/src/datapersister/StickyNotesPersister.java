package datapersister;

import java.util.ArrayList;
import java.util.List;

import models.StickyNote;
import android.os.Handler.Callback;
import android.os.Message;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class StickyNotesPersister {
	private static List<StickyNote> notes = new ArrayList<StickyNote>();

	public static List<StickyNote> getNotes() {
		return notes;
	}

	public static List<StickyNote> loadNotesFromParse(String userId,
			final Callback refreshAdapter) {
		ParseQuery<StickyNote> query = ParseQuery.getQuery("StickyNote");
		query.whereEqualTo("author", userId);
		query.findInBackground(new FindCallback<StickyNote>() {

			@Override
			public void done(List<StickyNote> loadedNotes, ParseException e) {
				if (e == null) {
					notes.clear();
					for (StickyNote note : loadedNotes) {
						notes.add(note);
					}

					refreshAdapter.handleMessage(new Message());
				}
			}
		});

		return notes;
	}

	public static void deleteStickyNote(String stickyNoteId,
			final Callback dismissDialog) {
		ParseQuery<StickyNote> query = ParseQuery.getQuery("StickyNote");
		query.getInBackground(stickyNoteId, new GetCallback<StickyNote>() {

			@Override
			public void done(StickyNote stickyNote, ParseException e) {
				stickyNote.deleteInBackground(new DeleteCallback() {

					@Override
					public void done(ParseException e) {
						dismissDialog.handleMessage(new Message());
					}
				});
			}
		});
	}

	public static StickyNote getNote(int position) {
		return notes.get(position);
	}

}
