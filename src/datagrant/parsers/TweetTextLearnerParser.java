package datagrant.parsers;

import classifiers.BayesClassifier;
import classifiers.BayesDocument;

public class TweetTextLearnerParser extends TweetParser {

	private String activeClass;
	
	public TweetTextLearnerParser(BayesClassifier classifier) {
		super(classifier);
	}

	public void setActiveClass(String cls) {
		this.activeClass = cls;
	}
	
	public void end() {
		classifier.trainClassifier();
	}
	
	@Override
	public void parse(BayesDocument doc) {
		doc.setIsTrained(true);
		doc.setBayesClass(classifier.getClass(activeClass));
		classifier.trainDocument(doc);
	}

}
