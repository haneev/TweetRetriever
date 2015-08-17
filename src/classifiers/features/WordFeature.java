package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class WordFeature extends BayesFeature {

	private String value;
	
	public WordFeature(String value) {
		super("word");
		this.value = value;
	}

	@Override
	public String getValue(BayesDocument doc) {
		return value;
	}

}
