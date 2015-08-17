package datagrant;

import classifiers.BayesClassifier;
import classifiers.features.LocationFeature;
import classifiers.features.TimezoneFeature;
import datagrant.parsers.TweetCountryDividerParser;

public class ClassifierDivider extends ClassifierCountry {

	public ClassifierDivider(String file) {
		super(file);
	}

	public void divider(String filename, int offset, int length) {
		TweetCountryDividerParser parser = new TweetCountryDividerParser(classifier);
		read(filename, offset, length, parser);
	}
	
	public static void main(String[] args) {
		
		ClassifierDivider c = new ClassifierDivider("../data/out_movember_small.json");
		
		BayesClassifier classifier = c.getClassifier();
		
		classifier.addFeature(new LocationFeature());
		classifier.addFeature(new TimezoneFeature());

		System.out.println("Start training");
		
		int totalTweets = 39293;
		c.train(0, totalTweets);		
		
		System.out.println("End training");
		
		System.out.println("Start dividing");
		
		c.divider("../data/all_movember_tweets.json", 0, 2000000);
		
		System.out.println("End dividing");
				
		printStats();
		c.close();
	}
	
}
