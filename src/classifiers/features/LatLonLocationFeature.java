package classifiers.features;

import classifiers.BayesDocument;
import tools.Cache;
import tools.CountryCode;

import org.json.JSONObject;

public class LatLonLocationFeature extends GeoLocationFeature {

	public LatLonLocationFeature(Cache cacheLayer) {
		super("latlon_location", cacheLayer);
	}

	@Override
	public String getValue(BayesDocument doc) {
		JSONObject coords = doc.getJson().optJSONObject("coordinates");
		String location = null;
		
		if(coords != null) {
			
			// twitter format is longitude and then latitude
			double longitude = coords.getJSONArray("coordinates").getDouble(0);
			double latitude = coords.getJSONArray("coordinates").getDouble(1); 
			
			location = Math.abs(latitude)+(latitude < 0 ? "S" : "N")+","+Math.abs(longitude)+(longitude < 0 ? "W" : "E");
		}
		
		// try place
		if(location == null) {
			JSONObject place = doc.getJson().optJSONObject("place");
			
			if(place != null && place.getString("country_code") != null) {
				return CountryCode.getCountry(place.getString("country_code").toLowerCase());
				
			} else if(place != null) {
				
				location = place.getString("full_name");
				if(location == null)
					location = place.getString("name");	
				
			}
			
		}
//		System.out.println(location);
		try {
			return getCountry(location);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	
	
}
