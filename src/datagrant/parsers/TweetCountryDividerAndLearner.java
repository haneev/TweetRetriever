package datagrant.parsers;

import java.util.Date;

import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import tools.TweetWriter;
import tweet.TweetDocument;

public class TweetCountryDividerAndLearner extends TweetCountryDividerParser {
	
	private int trainCount = 0;
	
	public TweetCountryDividerAndLearner(BayesClassifier classifier) {
		super(classifier);
	}

	@Override
	public void parse(BayesDocument doc) {
		
		boolean isHandled = false;
		
		if (doc instanceof TweetDocument) {
			
			String country = ((TweetDocument) doc).getTweet().getCountry();
			
			if (country != null) {				
				doc.setIsTrained(true);
				doc.setBayesClass(classifier.getClass(country));
				classifier.trainDocument(doc);
				trainCount++;
				
				// train per 100000 Tweet
				if (trainCount % 50000 == 0) {
					String now = (new Date()).toString();
					System.out.println(now + " Training in progress.... " + trainCount);
					classifier.trainClassifier();
				}
				
				isHandled = true;
				
				// write tweet to correct stream
				TweetWriter stream = getStream(country);
				stream.write(doc.getJson());
			}
			
		}
		
		if (!isHandled) {		
			super.parse(doc);
			isHandled = true;
		}
	
	}
}
