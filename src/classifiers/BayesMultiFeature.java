package classifiers;

import java.util.List;

abstract public class BayesMultiFeature extends BayesFeature {

	public BayesMultiFeature(String name) {
		super(name);
	}
	
	public String getValue(BayesDocument doc) {
		return null;
	}

	abstract public List<BayesFeature> getMultiValue(BayesDocument doc);
}
