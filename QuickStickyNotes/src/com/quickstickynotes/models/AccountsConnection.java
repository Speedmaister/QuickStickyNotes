package com.quickstickynotes.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("AccountsConnection")
public class AccountsConnection extends ParseObject{
	public String getGoogleName(){
		return this.getString("googleName");
	}
	
	public void setGoogleName(String name){
		this.put("googleName", name);
	}
	
	public String getFacebookId(){
		return this.getString("facebookId");
	}
	
	public void setFacebookId(String facebookId){
		this.put("facebookId",facebookId);
	}
}
