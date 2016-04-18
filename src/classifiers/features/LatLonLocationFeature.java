package classifiers.features;

import classifiers.BayesDocument;
import tools.Cache;
import tools.Config;
import tools.CountryCode;
import tools.GoogleGeocode;
import tools.GoogleGeocodeCoordinates;
import tweet.TweetDocument;

import org.json.JSONObject;

public class LatLonLocationFeature extends GeoLocationFeature {

	public LatLonLocationFeature(Cache cacheLayer) {
		super("latlon_location", cacheLayer);
		this.coder = new GoogleGeocodeCoordinates(Config.getInstance().get("googlekey"));
	}

	@Override
	public String getValue(BayesDocument doc) {
		
		String location = null;
		
		if (doc instanceof TweetDocument) {
			String country = ((TweetDocument) doc).getTweet().getCountry();
			if (country != null) {
				return country;
			} else {
				double[] coords = ((TweetDocument) doc).getTweet().getCoordinates();
				if (coords != null) {
					location = coords[0]+","+coords[1];
				}				
			}
		} else {
			JSONObject coords = doc.getJson().optJSONObject("coordinates");			
			if(coords != null) {
				
				// twitter format is longitude and then latitude
				double longitude = coords.getJSONArray("coordinates").getDouble(0);
				double latitude = coords.getJSONArray("coordinates").getDouble(1); 
				
				location = latitude+","+longitude;
			}
		}
		
		try {
			if (location != null) {
				return getCountry(location);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}	
	
}
