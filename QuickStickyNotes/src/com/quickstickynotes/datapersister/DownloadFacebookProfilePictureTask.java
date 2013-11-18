package com.quickstickynotes.datapersister;

import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownloadFacebookProfilePictureTask extends
		AsyncTask<URL, Integer, Bitmap> {

	@Override
	protected Bitmap doInBackground(URL... urls) {
		URL profilePictureUrl = urls[0];
		Bitmap bitmapProfilePicture = null;
		try {
			bitmapProfilePicture = BitmapFactory.decodeStream(profilePictureUrl
					.openConnection().getInputStream());
		} catch (Exception e) {
			// Failed to retrieve profile picture
			e.printStackTrace();
		} 

		return bitmapProfilePicture;
	}
}
