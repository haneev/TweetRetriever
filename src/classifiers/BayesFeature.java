package classifiers;

abstract public class BayesFeature {

	protected String name;
	
	public BayesFeature(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	abstract public String getValue(BayesDocument doc);	
}
