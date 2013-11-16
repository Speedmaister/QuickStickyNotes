package com.quickstickynotes.models;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageContent{

	private byte[] image;
	
	public ImageContent(Bitmap image){
		this.image = getBytes(image);
	}
	
	public ImageContent(byte[] image){
		this.image = image;
	}
	
	private byte[] getBytes(Bitmap image) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 0, byteStream);
        byte imageBytes[] = byteStream.toByteArray();
		return imageBytes;
	}
	
	public byte[] getData(){
		return this.image;
	}

	public Bitmap getImage() {
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
}
