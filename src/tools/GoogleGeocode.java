package tools;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import datagrant.ClassifierCountry;

public class GoogleGeocode {

	protected String key;
	
	public GoogleGeocode(String key) {
		this.key = key;
	}
	
	public String getUrl(String q) {
		return "https://maps.googleapis.com/maps/api/geocode/json?address="+URLEncoder.encode(q)+"&key="+this.key;
	}
	
	public String read(String q) throws Exception {
		String country = null;
		try {
			
			// 5 calls per sec
			Thread.sleep(200);
			
			JSONObject json = JsonReader.readJsonFromUrl(getUrl(q));
			
			ClassifierCountry.stat("google geocode call");
			
			JSONObject results = json.getJSONArray("results").optJSONObject(0);
			
			if(results == null) {
				System.err.println("Google results are empty for '"+q+"'");
				return null;
			}
			
			JSONArray addresses = results.getJSONArray("address_components");
			
			for(int i = 0; i < addresses.length(); i++) {
				
				JSONObject addressObject = addresses.getJSONObject(i);
				
				boolean found = false;
				for(int j = 0; !found && j < addressObject.getJSONArray("types").length(); j++) {
					if(addressObject.getJSONArray("types").getString(j).equals("country")) {
						found = true;
						country = addressObject.getString("short_name").toLowerCase();
					}
				}
				
			}
			
			
			
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Google results are empty (json or io)");
			e.printStackTrace();
		}	
		
		System.out.println("Google request '"+q+"' is resolved to '"+country+"'");
		
		return CountryCode.getCountry(country);
	}
	
	public static void main(String[] args) {
		
		GoogleGeocode c = new GoogleGeocode("");
		
		try {
			System.out.println(c.read("10.3468142N,123.90963309E"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
