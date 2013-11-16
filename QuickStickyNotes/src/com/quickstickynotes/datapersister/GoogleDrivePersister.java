package com.quickstickynotes.datapersister;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

public class GoogleDrivePersister {

	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;

	private static GoogleDrivePersister persister;
	private Drive service;
	private GoogleAccountCredential credential;
	private static StringBuilder query;

	public static void createPersister(Drive service,
			GoogleAccountCredential credential) {
		query = new StringBuilder();
		persister = new GoogleDrivePersister();
		persister.setParams(service, credential);
	}

	private void setParams(Drive service, GoogleAccountCredential credential) {
		this.service = service;
		this.credential = credential;
	}

	public static void saveFileToDrive(final Activity callingActivity,
			final String filename, final byte[] image) {
		Thread saveFileThread = new Thread(new Runnable() {
			@Override
			public void run() {
				java.io.File appDir = callingActivity.getFilesDir();
				java.io.File fileContent = null;
				try {
					fileContent = new java.io.File(appDir, filename);
					persister.writeDataToFile(fileContent, image);
					FileContent mediaContent = new FileContent("image/jpeg",
							fileContent);

					File body = new File();
					body.setTitle(fileContent.getName());
					body.setMimeType("image/jpeg");
					body.setParents(Arrays.asList(new ParentReference()
							.setId("appdata")));

					Insert some = persister.service.files().insert(body,
							mediaContent);
					File file = some.execute();
					if (file != null) {
						Log.d("UPLOAD", "SUCCESSFULL");
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.d("Error", e.getMessage());
				} catch (UserRecoverableAuthIOException e) {
					callingActivity.startActivityForResult(e.getIntent(),
							REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}

				fileContent.delete();
			}
		});

		saveFileThread.start();
	}

	private void writeDataToFile(java.io.File fileContent, final byte[] image) {
		BufferedOutputStream writer = null;
		try {
			writer = new BufferedOutputStream(new FileOutputStream(fileContent));
			writer.write(image, 0, image.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static byte[] loadFileFromDrive(String filename) {
		query.append("title = '");
		query.append(filename);
		query.append("'");

		BufferedInputStream bis = null;
		DataInputStream dis = null;
		byte[] image = null;
		try {
			List<File> result = getFileFromQuery();
			if (!result.isEmpty()) {
				File imageFile = result.get(0);
				if (imageFile.getDownloadUrl() != null
						&& imageFile.getDownloadUrl().length() > 0) {
					HttpResponse resp = persister.service
							.getRequestFactory()
							.buildGetRequest(
									new GenericUrl(imageFile.getDownloadUrl()))
							.execute();
					bis = new BufferedInputStream(resp.getContent());
					dis = new DataInputStream(bis);
					int fileSize = imageFile.getQuotaBytesUsed().intValue();
					image = new byte[fileSize];
					dis.readFully(image, 0, image.length);
				}
			}
		} catch (IOException e) {
			// An error occurred.
			e.printStackTrace();
		} finally {
			closeStreams(bis, dis);
		}

		query.delete(0, query.length());
		return image;
	}

	private static void closeStreams(BufferedInputStream bis,
			DataInputStream dis) {
		try {
			if (dis != null) {
				dis.close();
			}

			if (bis != null) {
				bis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<File> getFileFromQuery() throws IOException {
		Files.List request = persister.service.files().list();
		request.setQ(query.toString());
		List<File> result = new ArrayList<File>();

		do {
			try {
				FileList files = request.execute();
				Log.d("after", "MESSAGE");

				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null
				&& request.getPageToken().length() > 0);
		return result;
	}
	
	public static void deleteFile(String filename){
		query.append("title = '");
		query.append(filename);
		query.append("'");
		
		try {
			List<File> result = getFileFromQuery();
			if (!result.isEmpty()) {
				File imageFile = result.get(0);
				String fileId = imageFile.getId();
				persister.service.files().delete(fileId).execute();
			}
		} catch (IOException e) {
			// An error occurred.
			e.printStackTrace();
		}
	}
}
