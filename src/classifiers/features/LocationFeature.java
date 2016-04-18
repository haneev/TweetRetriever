package classifiers.features;

import org.json.JSONObject;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import tweet.TweetDocument;

public class LocationFeature extends BayesFeature {
	
	public LocationFeature() {
		super("location");
	}

	@Override
	public String getValue(BayesDocument doc) {
		
		if (doc instanceof TweetDocument) {
			return ((TweetDocument) doc).getTweet().getUserLocation();
		}
		
		JSONObject user = doc.getJson().optJSONObject("user");
		if (user != null) {
			return user.optString("location", null);
		} else {
			return null;
		}
		
	}	
	
}
