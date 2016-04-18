package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import tweet.TweetDocument;

public class TweetLanguageFeature extends BayesFeature {
	
	public TweetLanguageFeature() {
		super("tweet_lang");
	}

	@Override
	public String getValue(BayesDocument doc) {
		if (doc instanceof TweetDocument) {
			return ((TweetDocument) doc).getTweet().getLanguage();
		}
		
		return doc.getJson().optString("lang", null);
	}	
	
}
