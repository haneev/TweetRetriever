package classifiers.features;

import org.json.JSONObject;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import tweet.TweetDocument;

public class UserLanguageFeature extends BayesFeature {
	
	public UserLanguageFeature() {
		super("user_lang");
	}

	@Override
	public String getValue(BayesDocument doc) {
		
		if (doc instanceof TweetDocument) {
			return ((TweetDocument) doc).getTweet().getUserLanguage();
		}
		
		JSONObject user = doc.getJson().optJSONObject("user");
		if (user != null) {
			return user.optString("lang", null);
		} else {
			return null;
		}
	}	
	
}
