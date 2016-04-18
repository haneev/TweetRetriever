package datagrant.parsers;

import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import classifiers.features.LatLonLocationFeature;

public class PurgingTweetLearnerParser extends TweetLearnerParser {
	
	private long count = 0;
	
	public PurgingTweetLearnerParser(BayesClassifier classifier, LatLonLocationFeature latlon) {
		super(classifier, latlon);
	}
	
	@Override
	public void parse(BayesDocument doc) {
		
		count++;
		
		super.parse(doc);
		
		if (count % 100000 == 0) {
			System.out.println("Optimizing tweet features " +count);
			this.classifier.trainClassifier();
		}
	}

}
