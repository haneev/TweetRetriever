package tweet;

import classifiers.BayesDocument;

public class TweetDocument extends BayesDocument {

	private ClassifyableTweet tweet;
	
	public TweetDocument(ClassifyableTweet tweet) {
		super(tweet.getJson());
		this.tweet = tweet;
	}
	
	public ClassifyableTweet getTweet() {
		return tweet;
	}
	
}
