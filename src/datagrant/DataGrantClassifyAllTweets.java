package datagrant;

import java.sql.SQLException;

import classifiers.BayesClassifier;
import classifiers.features.LatLonLocationFeature;
import classifiers.features.LocationFeature;
import classifiers.features.TimezoneFeature;
import datagrant.parsers.TweetCountryDividerAndLearner;
import datagrant.parsers.TweetCountryDividerParser;
import datagrant.sources.DataSource;
import datagrant.sources.DirectoryDataSource;
import tools.Config;
import tools.DatabaseCache;
import tools.FileCache;

public class DataGrantClassifyAllTweets extends ClassifierCountry {

	private String cacheFile;
	
	private String directory;
	
	public DataGrantClassifyAllTweets(String trainFile, String cacheFile, String directory) {
		super();
		
		this.directory = directory;
		this.classifier = new BayesClassifier();
		this.classifier.setThreshold(0);
		this.classifier.setFeatureCountThreshold(0);
		this.file = trainFile;
		this.cacheFile = cacheFile;
		this.cache = new FileCache(cacheFile);
		this.latlon = new LatLonLocationFeature(this.cache);
	}
	
	public void close() {
		if (this.cache instanceof FileCache) {
			System.out.println("Write new cache to "+cacheFile);
			((FileCache) this.cache).writeNewCacheFile(cacheFile);
		}
		super.close();
	}
	
	public void divider(DataSource source, int offset, int length) {
		TweetCountryDividerAndLearner parser = new TweetCountryDividerAndLearner(classifier);
		parser.directory = this.directory;
		read(source, offset, length, parser);
	}
	
	public static void main(String[] args) {
		// train file, cache file, input directory, output directory
		if (args.length != 4) {
			System.out.println("useage is <train file> <cache file> <input dir> <output dir>");
		} else {
			
			String directoryOutput = args[3];
			String trainFile = args[0]; // "../data/out_movember_small.json.gz"
			String cacheFile = args[1]; // "../data/utwente2015-cache/parsed-3-coordinate-cache.csv"
			String inputDir = args[2]; // "../data/utwente2015"
			
			DataGrantClassifyAllTweets c = new DataGrantClassifyAllTweets(trainFile, cacheFile, directoryOutput);
			
			BayesClassifier classifier = c.getClassifier();
			
			classifier.addFeature(new LocationFeature());
			classifier.addFeature(new TimezoneFeature());

			System.out.println("Start training");
			
			int totalTweets = Integer.MAX_VALUE;;
			c.train(0, totalTweets);		
			
			System.out.println("End training");
			
			System.out.println("Start dividing");
			
			classifier.setFeatureCountThreshold(10);
			DataSource source = new DirectoryDataSource(inputDir);
			c.divider(source, 0, totalTweets);
			
			System.out.println("End dividing");
					
			printStats();
			c.close();
		}
		
	}
	
}
