package datagrant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import classifiers.features.GeoLocationFeature;
import classifiers.features.LatLonLocationFeature;
import classifiers.features.LocationFeature;
import classifiers.features.TimezoneFeature;
import classifiers.features.TweetLanguageFeature;
import datagrant.parsers.TweetCountryDividerParser;
import datagrant.parsers.TweetParser;

public class ClassifierPerCountry extends ClassifierCountry {


	public ClassifierPerCountry(String file) {
		super(file);
	}

	public void test(int offset, int length) {
		TweetParser parser = new ClassifierPerCountry.TweetPerCountryParser(classifier, latlon);
		read(file, offset, length, parser);
	}
	
	public class TweetPerCountryParser extends TweetParser {
		
		private Map<String, Long> totals;
		private Map<String, Long> matches;
		
		public LatLonLocationFeature latlon;
		
		public TweetPerCountryParser(BayesClassifier classifier, LatLonLocationFeature latlon) {
			super(classifier);
			
			this.totals = new HashMap<String, Long>();
			this.matches = new HashMap<String, Long>();
			
			this.latlon = latlon;
		}
		
		@Override
		public void end() {
			
			System.out.println("Per Country Analysis");
			for(String country : totals.keySet()) {
				long total = totals.containsKey(country) ? totals.get(country) : new Long(1);
				long match = matches.containsKey(country) ? matches.get(country) : new Long(0);
				double per = new Double(match) / new Double(total) * 100.0;
				System.out.println(country + " ; " + match + " ; " + total + " ; " + per);
			}
		}
		
		private void count(String country, boolean wasMatch) {
			Long total = totals.containsKey(country) ? totals.get(country) : (long) 0;
			totals.put(country, new Long(total + 1));
			
			if(wasMatch) {
				Long match = matches.containsKey(country) ? matches.get(country) :  (long) 0;
				matches.put(country, new Long(match + 1));
			}
		}

		@Override
		public void parse(BayesDocument doc) {
			List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
			String countryMatch = latlon.getValue(doc);
			
			if(countryMatch != null)
				count(countryMatch, countryMatch.equals(results.get(0).getKey().getName()) );
		}
		
	}
	
	public static void main(String[] args) {
		
		ClassifierPerCountry c = new ClassifierPerCountry("../data/out_movember_small.json");
		
		BayesClassifier classifier = c.getClassifier();
		
		classifier.addFeature(new LocationFeature());
		//classifier.addFeature(new TimezoneFeature());
		//classifier.addFeature(new TweetLanguageFeature());
		classifier.addFeature(new GeoLocationFeature(c.cache));

		//classifier.addFeature(new UTCFeature());
		//classifier.addFeature(new UserLanguageFeature());

		System.out.println("Start training");
		
		int totalTweets = 39293;
		c.train(0, totalTweets);		
		
		System.out.println("End training");
		
		System.out.println("Start per country");
		
		c.test(0, 40000);
		
		System.out.println("End per country");
				
		printStats();
		c.close();
	}
	
}
