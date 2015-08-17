package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class KVFeature extends BayesFeature {

	private String value;
	
	public KVFeature() {
		super("word");
	}
	
	public KVFeature(String name, String value) {
		super(name);
		this.value = value;
	}

	@Override
	public String getValue(BayesDocument doc) {
		return value;
	}
	
	

}
