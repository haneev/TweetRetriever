package tools;

import java.io.FileNotFoundException;

import org.json.JSONObject;

public class Config {

	private static Config instance;
	
	private static String filename = "config.json";
	
	private JSONObject configObject;
	
	public static Config getInstance() {
		try {
			if(instance == null)
				instance = new Config();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return instance;
	}
	

	public Config() throws FileNotFoundException {
		this.configObject = JSON.parseFile(filename);
	}	
	
	public String get(String key) {
		return this.configObject.getString(key);
	}
}
