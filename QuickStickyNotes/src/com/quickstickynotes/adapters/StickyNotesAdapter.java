package com.quickstickynotes.adapters;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickstickynotes.R;
import com.quickstickynotes.models.StickyNote;

public class StickyNotesAdapter extends BaseAdapter {
	private ListActivity activity;
	private List<StickyNote> data;
	private static LayoutInflater inflater = null;

	public StickyNotesAdapter(ListActivity activity, List<StickyNote> userNotes)
	{
		this.activity = activity;
		this.data = userNotes;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null)
			view = inflater.inflate(R.layout.list_item, null);

		TextView title = (TextView) view.findViewById(R.id.noteTitle); // title

		StickyNote note;
		note = data.get(position);

		// Setting all values in listview
		title.setText(note.getTitle());
		return view;
	}
}
