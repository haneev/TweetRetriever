package classifiers;

public class BayesClass {

	private String name;
	
	private long classCount = 0;
	private double classPrior = 0.0;

	public BayesClass(String className) {
		this.name = className;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void countClass() {
		this.classCount++;
	}
	
	public long getClassCount() {
		return this.classCount;
	}
	
	public double getClassPrior() {
		return this.classPrior;
	}
	
	public void reset() {
		this.classPrior = 0.0;
		this.classCount = 0;
	}
	
	public void train(long totalDocuments) {
		this.classPrior = new Double(this.classCount) / new Double(totalDocuments);
	}

	public String toString() {
		return "[BayesClass " + this.getName()+"] (c "+this.classCount+" p "+this.classPrior+")";
	}
	
}
