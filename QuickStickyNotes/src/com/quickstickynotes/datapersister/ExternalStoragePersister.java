package com.quickstickynotes.datapersister;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class ExternalStoragePersister {
	public static boolean checkIfExtStorageIsAvailableForWriting() {
		boolean isExternalStorageAvailable = false;
		boolean isExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			isExternalStorageAvailable = true;
			isExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			isExternalStorageAvailable = true;
			isExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			isExternalStorageAvailable = false;
			isExternalStorageWriteable = false;
		}

		boolean isStorageReadyForWriting = false;
		if (isExternalStorageAvailable && isExternalStorageWriteable) {
			isStorageReadyForWriting = true;
		}

		return isStorageReadyForWriting;
	}

	public static boolean checkIfExtStorageIsAvailableForReading() {
		boolean isExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			isExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			isExternalStorageAvailable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			isExternalStorageAvailable = false;
		}

		boolean isStorageReadyForReading = false;
		if (isExternalStorageAvailable) {
			isStorageReadyForReading = true;
		}

		return isStorageReadyForReading;
	}

	public static boolean saveImageToExtStorage(String filename, byte[] image) {
		File mainDir = Environment.getExternalStorageDirectory();
		File imageFile = new File(mainDir, filename);
		if (imageFile.exists()) {
			imageFile.delete();
			imageFile = new File(mainDir, filename);
		}

		imageFile.setWritable(true);
		BufferedOutputStream writer = null;
		try {
			writer = new BufferedOutputStream(new FileOutputStream(imageFile));
			writer.write(image, 0, image.length);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public static byte[] getImageFromExtStorage(String filename) {
		File mainDir = Environment.getExternalStorageDirectory();
		File imageFile = new File(mainDir, filename);

		byte[] image = new byte[(int) imageFile.length()];
		readBytes(imageFile, image);

		return image;
	}

	private static void readBytes(File imageFile, byte[] image) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		if (imageFile.exists()) {
			try {
				fis = new FileInputStream(imageFile);
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				
				if (dis.available() > 0) {
					dis.readFully(image, 0, image.length);
				}				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					dis.close();
					bis.close();
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
