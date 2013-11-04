package activities;

import com.parse.integratingfacebooktutorial.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class StickyNoteDetailedActivity extends Activity {

	TextView stickyNoteTitleHolder;
	TextView stickyNoteContentHolder;
	ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stickynote);

		actionBar = getActionBar();

		stickyNoteTitleHolder = (TextView) findViewById(R.id.stickyNoteTitle);
		stickyNoteContentHolder = (TextView) findViewById(R.id.stickyNoteContent);

		Intent intent = getIntent();
		Bundle selectedStickyNoteBundle = intent.getExtras();

		String stickyNoteTitle = selectedStickyNoteBundle.getString("title");
		String stickyNoteContent = selectedStickyNoteBundle
				.getString("content");

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

}
