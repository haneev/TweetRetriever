package tools;

import org.json.JSONObject;

public class ClassifierDocument {

	private String cls;
	
	private String text;
	
	private String extra;
	
	private JSONObject json;
	
	public ClassifierDocument(String text) {
		this.text = text;
	}
	
	public ClassifierDocument(String cls, String text) {
		this.cls = cls;
		this.text = text;
	}
	
	public ClassifierDocument(String cls, String text, String extra) {
		this.cls = cls;
		this.text = text;
		this.extra = extra;
	}
	
	public void setJson(JSONObject json) {
		this.json = json;
	}
	
	public JSONObject getJson() {
		return this.json;
	}
	
	public String getMatchClass() {
		return this.cls;
	}
	
	public String getText() {
		return this.text;
	}
	
	public String toString() {
		return this.cls + " " + this.text + " [ "+this.extra+" ] ";
	}

}
