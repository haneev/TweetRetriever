package classifiers;

abstract public class BayesFeature {

	protected String name;
	
	protected boolean isRemovable = false;
	
	public BayesFeature(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setIsRemovable(boolean to) {
		this.isRemovable = to;
	}

	public boolean getIsRemovable() {
		return this.isRemovable;
	}
	
	abstract public String getValue(BayesDocument doc);	
}
