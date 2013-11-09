package models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import datapersister.SqliteHelper;

@ParseClassName("StickyNote")
public class StickyNote extends ParseObject {
	public String getTitle() {
		return getString("title");
	}

	public void setTitle(String title) {
		put("title", title);
	}

	public ParseUser getAuthor() {
		return (ParseUser) get("author");
	}

	public void setAuthor(ParseUser author) {
		put("author", author);
	}

	public StickyNoteContent getContent() {
		Gson gson = new Gson();
		String contentJson = getString("content");

		Type type = new TypeToken<ArrayList<Pair<ContentTypes, String>>>() {
		}.getType();
		Type typeOfTextContent = new TypeToken<TextContent>() {
		}.getType();
		Type typeOfImageContent = new TypeToken<ImageContent>() {
		}.getType();

		ArrayList<Pair<ContentTypes, String>> elements = gson.fromJson(
				contentJson, type);

		StickyNoteContent content = new StickyNoteContent();
		int size = elements.size();
		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, String> element = elements.get(i);
			switch (element.first) {
			case Text:
				TextContent text = gson.fromJson(element.second,
						typeOfTextContent);
				content.insertTextChild(text);
				break;
			case Contact:
				// TODO
				break;
			case Image:
				 ImageContent image = gson.fromJson(element.second, typeOfImageContent);
				 content.insertImageChild(image);
				break;
			case Map:
				// TODO
				break;
			}
		}

		return content;
	}

	public void setContent(StickyNoteContent content) {
		Gson gson = new Gson();
		ArrayList<Pair<ContentTypes, String>> elements = new ArrayList<Pair<ContentTypes, String>>();
		ArrayList<Pair<ContentTypes, Object>> contents = content.getContents();
		int size = contents.size();
		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, Object> element = contents.get(i);
			switch (element.first) {
			case Text:
				elements.add(new Pair<ContentTypes, String>(ContentTypes.Text,
						gson.toJson(element.second)));
				break;
			case Contact:
				// TODO
				break;
			case Image:
				String jsonImage = gson.toJson(((ImageContent)element.second).getImage());
				elements.add(new Pair<ContentTypes, String>(ContentTypes.Image,
						jsonImage));
				break;
			case Map:
				// TODO
				break;
			}
		}
		Type type = new TypeToken<ArrayList<Pair<ContentTypes, String>>>() {
		}.getType();
		String contentJson = gson.toJson(elements, type);
		put("content", contentJson);
	}

	public Date getDateCreated() {
		return getCreatedAt();
	}
}
