package classifiers.features;

import org.json.JSONObject;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import tweet.TweetDocument;

public class TimezoneFeature extends BayesFeature {
	
	public TimezoneFeature() {
		super("timezone");
	}

	@Override
	public String getValue(BayesDocument doc) {
		
		if (doc instanceof TweetDocument) {
			return ((TweetDocument) doc).getTweet().getUserTimezone();
		}
		
		JSONObject user = doc.getJson().optJSONObject("user");
		if (user != null) {
			return user.optString("time_zone", null);
		} else {
			return null;
		}
	}	
	
}
