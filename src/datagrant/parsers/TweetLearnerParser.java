package datagrant.parsers;

import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import classifiers.features.LatLonLocationFeature;

public class TweetLearnerParser extends TweetParser {

	private LatLonLocationFeature latlon;
	
	public TweetLearnerParser(BayesClassifier classifier, LatLonLocationFeature latlon) {
		super(classifier);
		this.latlon = latlon;
	}

	public void end() {
		classifier.trainClassifier();
	}
	
	@Override
	public void parse(BayesDocument doc) {
		
		doc.setIsTrained(true);
		String countryMatch = latlon.getValue(doc);
		
		doc.setBayesClass(classifier.getClass(countryMatch));
		
		classifier.trainDocument(doc);
	}

}
