package datapersister;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownloadFacebookProfilePictureTask extends AsyncTask<URL,Integer,Bitmap>{

	@Override
	protected Bitmap doInBackground(URL... urls) {
		URL profilePictureUrl = urls[0];
		Bitmap bitmapProfilePicture = null;
		try {
			bitmapProfilePicture = BitmapFactory.decodeStream(profilePictureUrl.openConnection().getInputStream());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bitmapProfilePicture;
	}
}
