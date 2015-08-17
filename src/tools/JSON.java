package tools;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSON {

	public static JSONObject parse(String json) throws JSONException {
		JSONTokener token = new JSONTokener(json);
		JSONObject object = new JSONObject(token);
		return object;
	}
	
	public static JSONObject parseFile(String file) throws JSONException, FileNotFoundException {
		FileReader fileReader = new FileReader(file);		
		JSONTokener token = new JSONTokener(fileReader);
		JSONObject object = new JSONObject(token);
		return object;
	}
	
}
