package datagrant.parsers;

import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import classifiers.features.LatLonLocationFeature;

public class TweetMatcherParser extends TweetParser {
	
	private long total = 0;
	private long match = 0;
	
	private LatLonLocationFeature latlon;
	
	public TweetMatcherParser(BayesClassifier classifier, LatLonLocationFeature latlon) {
		super(classifier);
		this.latlon = latlon;
	}

	@Override
	public void parse(BayesDocument doc) {
		List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
		
		String countryMatch = latlon.getValue(doc);
		
		if(countryMatch == null) {
			System.out.println("Match: " + doc.getJson().getString("text")+" " + countryMatch + " = "+results.get(0).getKey().getName());
		}
		
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
