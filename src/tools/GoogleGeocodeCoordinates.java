package tools;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import datagrant.ClassifierCountry;

public class GoogleGeocodeCoordinates extends GoogleGeocode {
	
	public GoogleGeocodeCoordinates(String key) {
		super(key);
	}

	public String getUrl(String q) {
		return "https://maps.googleapis.com/maps/api/geocode/json?latlng="+URLEncoder.encode(q)+"&quataUser=x&key="+this.key;
	}
	
	public static void main(String[] args) {
		
		GoogleGeocodeCoordinates c = new GoogleGeocodeCoordinates("");
		
		try {
			System.out.println(c.read("10.3468142,123.90963309"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
