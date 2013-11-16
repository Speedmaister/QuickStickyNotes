package com.quickstickynotes.models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.quickstickynotes.datapersister.GoogleDrivePersister;

@ParseClassName("StickyNote")
public class StickyNote extends ParseObject {
	public String getTitle() {
		return getString("title");
	}

	public void setTitle(String title) {
		put("title", title);
	}

	public String getAuthor() {
		return getString("author");
	}

	public void setAuthor(String author) {
		put("author", author);
	}

	public StickyNoteContent getContent() {
		Gson gson = new Gson();
		String contentJson = getString("content");

		Type type = new TypeToken<ArrayList<Pair<ContentTypes, String>>>() {
		}.getType();
		Type typeOfTextContent = new TypeToken<TextContent>() {
		}.getType();
		Type typeOfContact = new TypeToken<Contact>() {
		}.getType();
		ArrayList<Pair<ContentTypes, String>> elements = gson.fromJson(
				contentJson, type);
		StickyNoteContent content = new StickyNoteContent();
		int size = elements.size();

		readElements(gson, typeOfTextContent, typeOfContact, elements, content,
				size);

		return content;
	}

	private void readElements(Gson gson, Type typeOfTextContent,
			Type typeOfContact, ArrayList<Pair<ContentTypes, String>> elements,
			final StickyNoteContent content, int size) {
		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, String> element = elements.get(i);
			switch (element.first) {
			case Text:
				TextContent text = gson.fromJson(element.second,
						typeOfTextContent);
				content.insertTextChild(text);
				break;
			case Contact:
				Contact contact = gson.fromJson(element.second, typeOfContact);
				content.insertContactChild(contact);
				break;
			case Image:
				final String fileName = element.second;
				byte[] image = GoogleDrivePersister.loadFileFromDrive(fileName);
				if (image != null) {
					ImageContent imageContent = new ImageContent(image);
					content.insertImageChild(imageContent);
				}
				// byte[] image = null;
				// try {
				// image = (new DownloadStickyNoteImage().execute(fileName))
				// .get();
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (ExecutionException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				//

				/*
				 * if (ExternalStoragePersister
				 * .checkIfExtStorageIsAvailableForReading()) { byte[] image =
				 * ExternalStoragePersister .getImageFromExtStorage(fileName);
				 * ImageContent imageContent = new ImageContent(image);
				 * content.insertImageChild(imageContent); }
				 */
				break;
			case Map:
				// TODO
				break;
			}
		}
	}

	public void setContent(StickyNoteContent content, Activity callingActivity) {
		Gson gson = new Gson();

		ArrayList<Pair<ContentTypes, Object>> contents = content.getContents();
		int size = contents.size();

		int imageCounter = 1;
		ArrayList<Pair<ContentTypes, String>> elements = new ArrayList<Pair<ContentTypes, String>>();

		writeElements(gson, imageCounter, elements, contents, size,
				callingActivity);

		Type type = new TypeToken<ArrayList<Pair<ContentTypes, String>>>() {
		}.getType();
		String contentJson = gson.toJson(elements, type);
		put("content", contentJson);
	}

	private void writeElements(Gson gson, int imageCounter,
			ArrayList<Pair<ContentTypes, String>> elements,
			ArrayList<Pair<ContentTypes, Object>> contents, int size,
			Activity callingActivity) {

		for (int i = 0; i < size; i++) {
			Pair<ContentTypes, Object> element = contents.get(i);
			switch (element.first) {
			case Text:
				elements.add(new Pair<ContentTypes, String>(ContentTypes.Text,
						gson.toJson(element.second)));
				break;
			case Contact:
				elements.add(new Pair<ContentTypes, String>(
						ContentTypes.Contact, gson.toJson(element.second)));
				break;
			case Image:
				String fileName = formatTitleForFilename(getTitle(), imageCounter);
				GoogleDrivePersister.saveFileToDrive(callingActivity, fileName,
						((ImageContent) element.second).getData());
				elements.add(new Pair<ContentTypes, String>(ContentTypes.Image,
						fileName));
				/*
				 * if (ExternalStoragePersister
				 * .checkIfExtStorageIsAvailableForWriting()) { if
				 * (ExternalStoragePersister .saveImageToExtStorage(fileName,
				 * ((ImageContent) element.second).getData())) {
				 * elements.add(new Pair<ContentTypes, String>(
				 * ContentTypes.Image, fileName)); } }
				 */
				break;
			case Map:
				// TODO
				break;
			}
		}
	}

	private String formatTitleForFilename(String title, int imageCounter) {
		String[] splittedTitle = title.split("\\s+");
		StringBuilder filenameBuilder = new StringBuilder();
		for (int i = 0; i < splittedTitle.length; i++) {
			filenameBuilder.append(splittedTitle[i]);
		}

		filenameBuilder.append("_");
		filenameBuilder.append(imageCounter);
		filenameBuilder.append(".jpg");

		return filenameBuilder.toString();
	}

	public Date getDateCreated() {
		return getCreatedAt();
	}
}
