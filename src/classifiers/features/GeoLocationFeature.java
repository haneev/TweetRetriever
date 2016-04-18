package classifiers.features;

import java.net.URLEncoder;

import tools.Cache;
import tools.Config;
import tools.CountryCode;
import tools.GoogleGeocode;
import tweet.TweetDocument;
import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class GeoLocationFeature extends BayesFeature {
	
	private Cache cacheLayer;
	
	protected GoogleGeocode coder;
	
	public GeoLocationFeature(Cache cacheLayer) {
		super("geo_location");
		this.cacheLayer = cacheLayer;
		this.coder = new GoogleGeocode(Config.getInstance().get("googlekey"));
	}
	
	public GeoLocationFeature(String name, Cache cacheLayer) {
		super(name);
		this.cacheLayer = cacheLayer;
		this.coder = new GoogleGeocode(Config.getInstance().get("googlekey"));
	}

	public String getCountry(String location) throws Exception {
		
		if(location == null)
			return null;
		
		String cacheKey = URLEncoder.encode(location);
		
		String result = this.cacheLayer.get(cacheKey);
		if(result != null && result.equals(Cache.NOT_FOUND))
			return null;
		
		if(result != null)
			return CountryCode.getCountry(result);
		
		String country = coder.read(location);
		
		this.cacheLayer.put(cacheKey, country);
		return CountryCode.getCountry(country);
	}
	
	private String filterLocation(String location) {
		location = java.text.Normalizer.normalize(location, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
		return location.replace("UT:", "");
	}
	
	@Override
	public String getValue(BayesDocument doc) {
		
		String location = null;
		if (doc instanceof TweetDocument) {
			location = ((TweetDocument) doc).getTweet().getUserLocation();
		} else {
			location = doc.getJson().getJSONObject("user").optString("location", null);
		}
		
		if (location != null) {
			location = filterLocation(location);
			
			try {
				return getCountry(location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
		
	}	
	
}
