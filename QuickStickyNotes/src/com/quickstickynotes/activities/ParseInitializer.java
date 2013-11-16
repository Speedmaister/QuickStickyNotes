package com.quickstickynotes.activities;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.quickstickynotes.R;
import com.quickstickynotes.models.AccountsConnection;
import com.quickstickynotes.models.StickyNote;

public class ParseInitializer extends Application {

	static final String TAG = "MyApp";

	@Override
	public void onCreate() {
		super.onCreate();
		
		ParseObject.registerSubclass(StickyNote.class);
		ParseObject.registerSubclass(AccountsConnection.class);
		
		Parse.initialize(this, "XlM63rViW1cl5vAlSrswsrFdBwcOPy5UCsTLINpS",
				"GsRsRC8NOFatjz2Gn2zYC6B5mS963h34cZ1MDuoK");

		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));

	}

}
