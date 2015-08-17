package datagrant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import tools.Config;
import tools.CountryCode;
import tools.DatabaseCache;
import tools.JSON;
import classifiers.*;
import classifiers.features.*;
import datagrant.parsers.TweetLearnerParser;
import datagrant.parsers.TweetMatcherParser;
import datagrant.parsers.TweetParser;
import datagrant.sources.DataSource;

public class ClassifierCountry {

	private static Map<String, Integer> stats = new HashMap<String, Integer>();
	
	protected BayesClassifier classifier;
	protected DatabaseCache cache;
	
	protected LatLonLocationFeature latlon;
	
	protected String file;
	
	public static void stat(String key) {
		Integer c = stats.containsKey(key) ? stats.get(key) : 0;
		stats.put(key, c+1);
	}
	
	public static void printStats() {
		System.out.println("Stats");
		for(Map.Entry<String, Integer> entry : stats.entrySet()) {
			System.out.println("Stat: "+entry.getKey()+" = "+entry.getValue());
		}
	}
	
	public ClassifierCountry() {
		
	}
	
	public ClassifierCountry(String file) {
		this.file = file;
		this.classifier = new BayesClassifier();
		this.classifier.setThreshold(0);
		
		try {
			this.cache = new DatabaseCache(Config.getInstance().get("connection"));
			this.latlon = new LatLonLocationFeature(cache);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		this.cache.close();
	}
	
	public BayesClassifier getClassifier() {
		return this.classifier;
	}
	
	public void read(DataSource source, int offset, int length, TweetParser parser) {
		
		parser.init();
		
		int i = 0;
		source.rewind();
		while(source.hasNext()) {
			
			if(i < offset) 
				continue;
			
			if(i > (offset + length)) 
				break;
			
			parser.parse(new BayesDocument(source.next()));
			
			i++;
		}
		
		parser.end();
		
		source.close();
	}
	
	public void read(String jsonFile, int offset, int length, TweetParser parser) {
		try {
			
			parser.init();
			
			FileInputStream fileStream = new FileInputStream(new File(jsonFile));
			InputStreamReader reader = new InputStreamReader(fileStream, "UTF-8");
			
			BufferedReader read = new BufferedReader(reader);
			
			String line;
			
			try {
				int i = 0;
				while((line = read.readLine()) != null) {
					
					i++;
					
					if(i < offset) 
						continue;
					
					if(i > (offset + length)) {
						break;
					}
					
					try {
						JSONObject json = JSON.parse(line);
						parser.parse(new BayesDocument(json));
					} catch(JSONException e) {
						System.out.println("JSON error skipping document");
					}
					
					if(length > 100000 && i % 100000 == 0) {
						System.out.println("Progress " + i);
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			read.close();
			fileStream.close();
			reader.close();
			
			parser.end();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void train(int offset, int length) {
		TweetParser parser = new TweetLearnerParser(classifier, latlon);
		read(file, offset, length, parser);
	}
	
	public long match(int offset, int length) {
		
		TweetMatcherParser parser = new TweetMatcherParser(classifier, latlon);
		read(file, offset, length, parser);
		
		return parser.getMatch();
	}
	
	public double crossValidation(int folds, int totalTweets) {
		
		int foldCount = ((Double) (new Double(totalTweets) / new Double(folds))).intValue();
		
		List<Double> accuracies = new ArrayList<Double>();
		
		for(int fold = 0; fold < folds; fold++) {
			classifier.reset();
			
			System.out.println(fold + " train " + fold*foldCount + " length : " + foldCount);
			train(fold * foldCount, foldCount);
			
			long matchCount = 0;
			long matchTotal = 0;
			
			if(fold > 0) {
				matchCount++;
				System.out.println(fold + " before match " + 0 + " length : " + foldCount * fold );
				matchCount += match(0, foldCount * fold);
				matchTotal += foldCount * fold;
			}
			
			if(fold < (folds -1)) {
				int startCount = foldCount * fold + foldCount;
				System.out.println(fold + " after match " + (foldCount * fold + foldCount) + " length : " + (totalTweets - startCount) );
				matchCount += match(startCount, totalTweets - startCount);
				matchTotal += totalTweets - startCount;
			}
			
			System.out.println(matchCount + "/" + matchTotal);
			
			accuracies.add(new Double(matchCount) / new Double(matchTotal));
		}
		
		// calculate total
		double accuracySum = 0;
		for(Double acc : accuracies) {
			accuracySum += acc;
		}
		
		return accuracySum / accuracies.size();
	}
	
	public static void main(String[] args) throws SQLException {
		
		int totalTweets = 39293;
		ClassifierCountry c = new ClassifierCountry("../data/out_movember_small.json");
		
		//int totalTweets = 4477;
		//ClassifierCountry c = new ClassifierCountry("../data/dataset2_geolocation.txt");
		
		BayesClassifier classifier = c.getClassifier();
		
		//classifier.addFeature(new LocationFeature());
		classifier.addFeature(new TimezoneFeature());
		//classifier.addFeature(new TweetLanguageFeature());
		classifier.addFeature(new GeoLocationFeature(c.cache));

		//classifier.addFeature(new UTCFeature());
		//classifier.addFeature(new UserLanguageFeature());

		//classifier.addFeature(new TokenizerFeature());

		
		//c.train(0, 5000);
		System.out.println("Total acc " + c.crossValidation(10, totalTweets));
		//System.out.println("Total acc " + c.crossValidation(2, 18100 + 2100 * 4));
		
		//classifier.outPriors(new PrintWriter(System.out));
		
		printStats();
		
		c.close();
	}
	
}
