package datagrant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONException;

import classifiers.BayesDocument;
import datagrant.sources.TweetSearchDataSource;
import tools.Data;
import tools.JSON;
import tools.TweetWriter;

public class Run {
	
	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
		Comparator<Map.Entry<K,V>> cmp = new Comparator<Map.Entry<K,V>>() {
            @Override 
            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        };
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(cmp);
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	public Map<String, Double> wordCountRelative = new HashMap<String, Double>();
	
	public Map<String, Integer> wordCount = new TreeMap<String, Integer>();
	
	public void add(String key) {
		if(key.length() > 1) {
			key = key.toLowerCase();
			Integer count = wordCount.containsKey(key) ? wordCount.get(key) : 0;
			count++;
			wordCount.put(key, count);
		}
	}
	
	public void loadRelativeWordCount(String inputFile) throws IOException {
		
		FileInputStream fileStream = new FileInputStream(new File(inputFile));
		InputStreamReader inputStream = new InputStreamReader(fileStream, "UTF-8");
		BufferedReader reader = new BufferedReader(inputStream);		
		
		String line;
		int lineCounter = 0;
		while((line = reader.readLine()) != null) {
			
			if(line.length() < 1)
				continue;
			
			String[] tokens = line.split("\\s+");	
			wordCountRelative.put(tokens[1].trim().toLowerCase(), Double.parseDouble(tokens[2]) / 1000000000.0);
			
			lineCounter++;
			
			if(lineCounter % 1000 == 0)
				System.out.println("relative Process "+lineCounter);
		}
		
	}
	
	public void printWordCounts() {
		System.out.println("Wordcounts");
		
		Double defaultRelativeCount = 0.00001;
		
		try {
			loadRelativeWordCount("../data/wordCounts.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Double> relativedWordCount = new HashMap<String, Double>();
		
		int totalWords = 0;
		for(Integer count : wordCount.values()) {
			totalWords += count;
		}
		
		for(Entry<String, Integer> entry : wordCount.entrySet()) {
			Double tweetRelativeCount = (new Double(entry.getValue())/totalWords);
			Double normalRelativeCount = wordCountRelative.containsKey(entry.getKey()) ? wordCountRelative.get(entry.getKey()) : defaultRelativeCount;
			Double relative = tweetRelativeCount / normalRelativeCount;
							
			relativedWordCount.put(entry.getKey(), relative);
		}
		
		for(Entry<String, Double> entry : entriesSortedByValues(relativedWordCount)) {
			System.out.println(entry.getKey() + "\t"+entry.getValue());
		}
		
		System.out.println("End");
	}
	
	public void parse(String inputFile) throws FileNotFoundException, UnsupportedEncodingException {
		
		try {
			
			FileInputStream fileStream = new FileInputStream(new File(inputFile));
			InputStreamReader inputStream = new InputStreamReader(fileStream, "UTF-8");
			BufferedReader reader = new BufferedReader(inputStream);		
			
			String line;
			int lineCounter = 0;
			while((line = reader.readLine()) != null) {
				String tweetText = JSON.parse(line).getString("text");
				
				tweetText = tweetText.replaceAll("[^\\w\\#\\s]+"," ");
				
				for (String token : tweetText.split("\\s+")) {
					if(!token.equals(""))
						add(token);
				}
				
				lineCounter++;
				
				if(lineCounter % 1000 == 0)
					System.out.println("Process "+lineCounter);
			}
			
			fileStream.close();
			inputStream.close();
			reader.close();
			
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		
		printWordCounts();
		
	}
	
	public static void getNormalTweets() {
	
		TweetWriter writer = new TweetWriter("../data/tweets_non_related_150615.json.gz");
		
		List<TweetSearchDataSource> sources = new ArrayList<TweetSearchDataSource>();
		
		sources.add(new TweetSearchDataSource("lang:nl"));
		sources.add(new TweetSearchDataSource("lang:en"));
		sources.add(new TweetSearchDataSource("lang:es"));
		sources.add(new TweetSearchDataSource("lang:fr"));
		sources.add(new TweetSearchDataSource("lang:de"));
		sources.add(new TweetSearchDataSource("lang:it"));
		
		for(TweetSearchDataSource s : sources) {
			
			int total = 3000;
			
			System.out.println("Source");
			
			int i = 0;
			while(s.hasNext()) {
				i++;
				
				if(i > total) {
					break;
				}
				
				if(i % 100 == 0)
					System.out.println("progress "+i);
				
				writer.write(s.next());
			}
			
		}
		
		writer.close();
		
		
		
	}
	
	public static void main(String[] args) throws IOException  {

		getNormalTweets();
		
		
		
		/*String inputFile = "../data/frenchgp-live_compacted.json.gz";
		
		String hashtag = "#frenchgp";
		
		FileInputStream fileStream = new FileInputStream(new File(inputFile));
		GZIPInputStream gzipStream = new GZIPInputStream(fileStream);
		InputStreamReader inputStream = new InputStreamReader(gzipStream, "UTF-8");
		BufferedReader reader = new BufferedReader(inputStream);		
		
		String line;
		
		int i = 0;
		int tweets = 0;
		while((line = reader.readLine()) != null) {
			
			BayesDocument doc = new BayesDocument(line);
			
			String tweetText = doc.getJson().optString("text").replace("\n", " ");
			
			if(Data.containsToken(tweetText, hashtag) && Math.random() < 0.5) {
			
				System.out.println(";\""+tweetText+"\"");
				
				tweets++;
			}
			
			if(tweets >= 100)
				break;
			
			i++;
		}
		
		reader.close(); */
	
	}
	
}
