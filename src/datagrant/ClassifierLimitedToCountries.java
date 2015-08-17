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
import datagrant.parsers.TweetLearnerParser;
import datagrant.parsers.TweetMatcherParser;
import datagrant.parsers.TweetParser;

public class ClassifierLimitedToCountries extends ClassifierCountry {

	private List<String> countries;
	
	public ClassifierLimitedToCountries(String file) {
		super(file);
		
		countries = new ArrayList<String>();
		
		initCountries();
	}
	
	private void initCountries() {
		countries.add("United Kingdom");
		countries.add("Ireland");
		countries.add("Netherlands");
		countries.add("Spain France");
		countries.add("Norway");
		countries.add("Sweden");
		countries.add("Belgium");
		countries.add("Germany");
		countries.add("Finland");
		countries.add("Italy Denmark");
		countries.add("Switzerland");
		countries.add("Czech Republic Latvia");
		countries.add("Portugal Poland Turkey Hungary");
		countries.add("Greece");
		countries.add("Belarus");
		countries.add("Slovakia");
		countries.add("Serbia");
		countries.add("Malta");
		countries.add("Bulgaria");
		countries.add("Honduras");
		countries.add("Northern");
		countries.add("Cyprus");
		countries.add("Croatia");
		countries.add("Luxembourg");
		countries.add("Bosnia and Herzegovina");
		countries.add("Iceland");
		countries.add("Monaco");
		countries.add("Lithuania");
		countries.add("Romania");
	}
	
	public class TweetLearnerParser extends TweetParser {

		private LatLonLocationFeature latlon;
		
		private List<String> countries;
		
		public TweetLearnerParser(BayesClassifier classifier, LatLonLocationFeature latlon, List<String> countries) {
			super(classifier);
			this.latlon = latlon;
			this.countries = countries;
		}

		public void end() {
			classifier.trainClassifier();
		}
		
		private String getCountry(BayesDocument doc) {
			String countryMatch = latlon.getValue(doc);
			
			if(countries.contains(countryMatch)) {
				return countryMatch;
			} else
				return "Other";
		}
		
		@Override
		public void parse(BayesDocument doc) {
			doc.setIsTrained(true);			
			doc.setBayesClass(classifier.getClass(getCountry(doc)));
			classifier.trainDocument(doc);
		}

	}
	
	public class TweetMatcherParser extends TweetParser {
		
		private List<String> countries;
		
		private long total = 0;
		private long match = 0;
		
		private LatLonLocationFeature latlon;
		
		public TweetMatcherParser(BayesClassifier classifier, LatLonLocationFeature latlon, List<String> countries) {
			super(classifier);
			this.latlon = latlon;
			this.countries = countries;
		}
		
		private String getCountry(BayesDocument doc) {
			String countryMatch = latlon.getValue(doc);
			
			if(countries.contains(countryMatch)) {
				return countryMatch;
			} else
				return "Other";
		}
		
		@Override
		public void parse(BayesDocument doc) {
			List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
			
			String countryMatch = getCountry(doc);
			
			total++;
			
			if(countryMatch != null && countryMatch.equals(results.get(0).getKey().getName()))
				match++;
		}
		
		public long getTotal() {
			return total;
		}
		
		public long getMatch() {
			return match;
		}
		
	}
	
	public void train(int offset, int length) {
		TweetParser parser = new ClassifierLimitedToCountries.TweetLearnerParser(classifier, latlon, countries);
		read(file, offset, length, parser);
	}
	
	public long match(int offset, int length) {
		
		TweetMatcherParser parser = new ClassifierLimitedToCountries.TweetMatcherParser(classifier, latlon, countries);
		read(file, offset, length, parser);
		
		return parser.getMatch();
	}
	
	public static void main(String[] args) {
		
		ClassifierLimitedToCountries c = new ClassifierLimitedToCountries("../data/out_movember_small.json");
		
		BayesClassifier classifier = c.getClassifier();
		
		classifier.addFeature(new LocationFeature());
		classifier.addFeature(new TimezoneFeature());
		//classifier.addFeature(new TweetLanguageFeature());
		classifier.addFeature(new GeoLocationFeature(c.cache));

		//classifier.addFeature(new UTCFeature());
		//classifier.addFeature(new UserLanguageFeature());

		System.out.println("Start");
		
		int totalTweets = 39293;
		System.out.println("Total acc " + c.crossValidation(10, totalTweets));
		
		System.out.println("End");
				
		printStats();
		c.close();
	}
	
}
