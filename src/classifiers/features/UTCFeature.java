package classifiers.features;

import org.json.JSONObject;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import tweet.TweetDocument;

public class UTCFeature extends BayesFeature {
	
	public UTCFeature() {
		super("utc");
	}

	@Override
	public String getValue(BayesDocument doc) {
		
		if (doc instanceof TweetDocument) {
			return ((TweetDocument) doc).getTweet().getUTCTimeZone();
		}
		
		JSONObject user = doc.getJson().optJSONObject("user");
		return user == null ? null : user.optString("utc_offset", null);
	}	
	
}
