package classifiers.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesDocument;
import classifiers.BayesFeature;
import classifiers.BayesMultiFeature;

public class TokenizerFeature extends BayesMultiFeature {

	private Map<String, BayesFeature> wordMap;
	
	public TokenizerFeature() {
		super("tokenizer");
		wordMap = new HashMap<String, BayesFeature>();
	}

	private BayesFeature getFeature(String token) {
		
		if(wordMap.containsKey(token))
			return wordMap.get(token);
		
		BayesFeature feature = new WordFeature(token);
		wordMap.put(token, feature);
		
		return feature;
	}
	
	private String[] getTokens(String doc) {
		return doc.split("\\s|\\n|\\.|,|:|\\?|\\!|;|\\(|\\)|\\[|\\]|\\&|\\t|\\\"|\\'");
	}

	@Override
	public List<BayesFeature> getMultiValue(BayesDocument doc) {
		
		List<BayesFeature> tokens = new ArrayList<BayesFeature>();
		for(String token : getTokens(doc.getJson().getString("text"))) {
			if(!token.isEmpty()) {
				tokens.add(getFeature(token.toLowerCase()));
			}
		}
		
		return tokens;
	}
	
}
