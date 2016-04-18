package datagrant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import classifiers.BayesFeature;
import classifiers.features.GeoLocationFeature;
import classifiers.features.LatLonLocationFeature;
import classifiers.features.LocationFeature;
import classifiers.features.TimezoneFeature;
import classifiers.features.TweetLanguageFeature;
import datagrant.parsers.PurgingTweetLearnerParser;
import datagrant.parsers.TweetCountryDividerParser;
import datagrant.parsers.TweetLearnerParser;
import datagrant.parsers.TweetParser;
import datagrant.sources.DataSource;
import datagrant.sources.DirectoryDataSource;
import tools.FileCache;

public class DataGrantClassifierPerCountry extends ClassifierCountry {
	
	private String originalFile;
	
	public DataGrantClassifierPerCountry(String file, String cacheFile) {
		super();
		
		this.originalFile = file;
		
		this.classifier = new BayesClassifier();
		this.classifier.setThreshold(0);
		
		this.cache = new FileCache(cacheFile);
		this.latlon = new LatLonLocationFeature(this.cache);
	}

	public void test(DataSource datasource, int offset, int length) {
		TweetParser parser = new DataGrantClassifierPerCountry.TweetPerCountryParser(classifier, latlon);
		read(datasource, offset, length, parser);
	}
	
	public void trainOriginalFile() {
		// train original file also
		this.train(new datagrant.sources.FileDataSource(originalFile), 0, Integer.MAX_VALUE);
	}
	
	public void train(DataSource datasource, int offset, int length) {	
		TweetParser parser = new PurgingTweetLearnerParser(classifier, latlon);
		read(datasource, offset, length, parser);
	}
	
	public class TweetPerCountryParser extends TweetParser {
		
		private Map<String, Long> totals;
		private Map<String, Long> matches;
		
		public LatLonLocationFeature latlon;
		
		private int counter = 0;
		
		public TweetPerCountryParser(BayesClassifier classifier, LatLonLocationFeature latlon) {
			super(classifier);
			
			this.totals = new HashMap<String, Long>();
			this.matches = new HashMap<String, Long>();
			
			this.latlon = latlon;
		}
		
		@Override
		public void end() {
			
			System.out.println("Per Country Analysis");
			long totalTweets = 0;
			for(String country : totals.keySet()) {
				long total = totals.containsKey(country) ? totals.get(country) : new Long(1);
				long match = matches.containsKey(country) ? matches.get(country) : new Long(0);
				double per = new Double(match) / new Double(total) * 100.0;
				System.out.println(country + " ; " + match + " ; " + total + " ; " + per);
				
				totalTweets += total;
			}
			
			System.out.println("Tweet Total with geolocation;;"+totalTweets);
			System.out.println("Tweet Total;;"+counter);

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
			counter++;
			List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
			String countryMatch = latlon.getValue(doc);
			
			if (counter % 10000 == 0) {
				String now = (new Date()).toString();
				System.out.println(now+ " progress ... "+counter);
			}
			
			if(countryMatch != null)
				count(countryMatch, countryMatch.equals(results.get(0).getKey().getName()) );
		}
		
	}
	
	public static void main(String[] args) {
		
		if (args.length != 3) {
			System.out.println("useage is <train file> <cache file> <input dir>");
		} else {
			
			String trainFile = args[0]; // "../data/out_movember_small.json.gz"
			String cacheFile = args[1]; // "../data/utwente2015-cache/parsed-3-coordinate-cache.csv"
			String inputDir = args[2]; // "../data/utwente2015"
		
			DataGrantClassifierPerCountry c = new DataGrantClassifierPerCountry(
				trainFile,
				cacheFile
			);
			
			BayesClassifier classifier = c.getClassifier();
			
			BayesFeature location = new LocationFeature();
			location.setIsRemovable(true);
			classifier.addFeature(location);
			classifier.addFeature(new TimezoneFeature());
			//classifier.addFeature(new TweetLanguageFeature());
			//classifier.addFeature(new GeoLocationFeature(c.cache));
			//classifier.addFeature(new UTCFeature());
			//classifier.addFeature(new UserLanguageFeature());
			
			
	
			System.out.println("Start training");
			
			DataSource source = new DirectoryDataSource(inputDir);
			
			c.trainOriginalFile();
			classifier.setFeatureCountThreshold(10);
			
			int totalTweets = Integer.MAX_VALUE;
			c.train(source, 0, totalTweets);		
			
			System.out.println("End training");
			
			System.out.println("Start per country");
			
			DataSource source2 = new DirectoryDataSource(inputDir);
			c.test(source2, 0, totalTweets);
			
			System.out.println("End per country");
					
			printStats();
			c.close();
		}
	}
	
}
