package models;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("StickyNote")
public class StickyNote extends ParseObject{
	public String getTitle() {
		return getString("title");
	}
	
	public void setTitle(String title) {
		put("title", title);
	}
	
	public ParseUser getAuthor() {
		return (ParseUser)get("author");
	}
	
	public void setAuthor(ParseUser author) {
		put("author", author);
	}
	
	public String getContent() {
		return getString("content");
	}
	
	public void setContent(String content) {
		put("content",content);
	}
	
	public Date getDateCreated(){
		return getCreatedAt();
	}
}
