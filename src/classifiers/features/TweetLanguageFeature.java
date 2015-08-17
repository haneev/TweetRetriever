package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class TweetLanguageFeature extends BayesFeature {
	
	public TweetLanguageFeature() {
		super("tweet_lang");
	}

	@Override
	public String getValue(BayesDocument doc) {
		return doc.getJson().optString("lang", null);
	}	
	
}
