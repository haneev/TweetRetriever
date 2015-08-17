package datagrant.parsers;

import classifiers.BayesClassifier;
import classifiers.BayesDocument;

public abstract class TweetParser {

	protected BayesClassifier classifier;
	
	public TweetParser(BayesClassifier classifier) {
		this.classifier = classifier;
	}
	
	public void init() {
		
	}
	
	public void end() {
		
	}
	
	public abstract void parse(BayesDocument doc);
}
