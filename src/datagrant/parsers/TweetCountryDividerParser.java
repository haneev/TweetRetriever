package datagrant.parsers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesDocument;

public class TweetCountryDividerParser extends TweetParser {

	private Map<String, BufferedWriter> streams;
	
	private String directory = "../data/movember_countries/"; 
	
	private long count = 0;
	
	public TweetCountryDividerParser(BayesClassifier classifier) {
		super(classifier);
		
		streams = new HashMap<String, BufferedWriter>();
	}
	
	private String getFilename(String country) {
		return directory+"country_"+country+".json";
	}
	
	private BufferedWriter getStream(String country) {
		
		if(streams.containsKey(country)) {
			return streams.get(country);
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFilename(country)), "UTF-8"));
			
			streams.put(country, writer);
			
			return writer;
			
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void parse(BayesDocument doc) {
		List<Map.Entry<BayesClass, Double>> results = classifier.match(doc);		
	
		String country = results.get(0).getKey().getName();
		
		count++;
		
		try {
			BufferedWriter stream = getStream(country);
			stream.write(doc.getJson().toString()+"\n");
			
			if(count % 500 == 0)
				stream.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void end() {
		for(BufferedWriter w : streams.values()) {
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
