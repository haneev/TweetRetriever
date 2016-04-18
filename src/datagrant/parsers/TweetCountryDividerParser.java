package datagrant.parsers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesDocument;
import tools.TweetWriter;

public class TweetCountryDividerParser extends TweetParser {

	private Map<String, TweetWriter> streams;
	
	public String directory = "../data/movember_countries/";
	
	public int counter = 0;
	
	public TweetCountryDividerParser(BayesClassifier classifier) {
		super(classifier);
		
		streams = new HashMap<String, TweetWriter>();
	}
	
	private String getFilename(String country) {
		return directory+"country_"+country+".json.gz";
	}
	
	protected TweetWriter getStream(String country) {
		
		if(streams.containsKey(country)) {
			return streams.get(country);
		}
	
		String file = getFilename(country);
		System.out.println("Write "+country+" to "+file);
		TweetWriter writer = new TweetWriter(file);
		
		streams.put(country, writer);
		
		return writer;
	}
	
	@Override
	public void parse(BayesDocument doc) {
		List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
	
		String country = results.get(0).getKey().getName();

		TweetWriter stream = getStream(country);
		
		counter++;
		
		if (counter % 10000 == 0) {
			String now = (new Date()).toString();
			System.out.println(now + " Progress .... "+counter);
		}
		
		try {
			stream.write(doc.getJson());
		} catch(Exception e) {
			e.printStackTrace();
		}
	
	}
	
	@Override
	public void end() {
		for(TweetWriter w : streams.values()) {
			w.close();
		}
	}

}
