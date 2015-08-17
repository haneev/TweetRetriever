package datagrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesFeature;
import classifiers.features.TokenizerFeature;
import datagrant.parsers.TweetTextLearnerParser;
import datagrant.possibleWords.PossibleWords;
import datagrant.sources.DataSource;
import datagrant.sources.FileDataSource;

public class ClassifierDocFreq extends ClassifierCountry implements PossibleWords {
	
	private String className = "testset";
	
	public ClassifierDocFreq() {
		this.classifier = new BayesClassifier();
		this.getClassifier().addFeature(new TokenizerFeature());
		this.getClassifier().setThreshold(5);
	}
	
	public void trainFile(String filename, int offset, int length) {
		this.trainSource(new FileDataSource(filename), offset, length);
	}
	
	public void trainSource(DataSource source, int offset, int length) {
		TweetTextLearnerParser parser = new TweetTextLearnerParser(classifier);
		parser.setActiveClass(className);
		
		this.read(source, offset, length, parser);
		
		System.out.println("training done for " + source);
	}	

	private List<String> sortPossibleWords(Map<String, Double> inputList) {
		
		List<String> returningWords = new ArrayList<String>();
		
		// sort the new list
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>( inputList.entrySet() );
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
            {
                return - o1.getValue().compareTo(o2.getValue());
            }
        });
		
		int i = 0;
		
		for(Map.Entry<String, Double> word : list) {
			
			System.out.println("tf word "+word.getKey() + " = "+word.getValue());
			
			returningWords.add(word.getKey());
		}
		
		return returningWords;
	}
	
	/**
	 * Get sorted list of possible additional words
	 * 
	 * @require Trained Classifier in order to find the possible words
	 * @require the two classes must be `match` and `normal`
	 * 
	 * @param top How many should be returned
	 * 
	 */
	@Override
	public List<String> getPossibleWords(int top) {
		
		BayesClass cls = getClassifier().getClass(className);
		
		Map<String, Double> scores = new HashMap<String, Double>();
		
		for(Entry<BayesFeature, Map<String, Map<BayesClass, Long>>> entry : getClassifier().getCounts().entrySet()) {

			String word = entry.getKey().getValue(null);
			
			if(entry.getValue().get(word) != null && entry.getValue().get(word).get(cls) != null) {
				scores.put(word, new Double(entry.getValue().get(word).get(cls)));
			}			
			
		}
		
		List<String> list = sortPossibleWords(scores);
		
		if(list.size() < top) {
			return list;
		} else
			return list.subList(0, top);
	}

	@Override
	public double getPossibleWordScore(String term) {
		double result = 0.0;
		
		BayesClass cls = getClassifier().getClass(className);
		
		for(Entry<BayesFeature, Map<String, Map<BayesClass, Long>>> entry : getClassifier().getCounts().entrySet()) {

			String word = entry.getKey().getValue(null);
			
			if(word.equals(term)) {
			
				if(entry.getValue().get(word) != null && entry.getValue().get(word).get(cls) != null) {
					result = new Double(entry.getValue().get(word).get(cls));
				}
				
				break;
			}
			
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		
		ClassifierDocFreq f = new ClassifierDocFreq();
		f.trainFile("../data/out_movember_small.json", 0, 20000);
		
		System.out.println("Additional Words");
		
		for(String w : f.getPossibleWords(100)) {
			System.out.println(w);
		}
		
	}

}
