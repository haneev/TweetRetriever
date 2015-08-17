package datagrant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classifiers.BayesClass;
import classifiers.BayesClassifier;
import classifiers.BayesFeature;
import classifiers.features.TokenizerFeature;
import datagrant.parsers.TweetTextLearnerParser;
import datagrant.possibleWords.PossibleWords;
import datagrant.sources.DataSource;
import datagrant.sources.FileDataSource;
import datagrant.sources.TweetSearchDataSource;

public class ClassifierWords extends ClassifierCountry implements PossibleWords {
	
	public ClassifierWords() {
		this.classifier = new BayesClassifier();
		this.getClassifier().addFeature(new TokenizerFeature());
		this.getClassifier().setThreshold(5); 
	}
	
	public void trainFile(String className, String filename, int offset, int length) {
		this.trainSource(className, new FileDataSource(filename), offset, length);
	}
	
	public void trainSource(String className, DataSource source, int offset, int length) {
		TweetTextLearnerParser parser = new TweetTextLearnerParser(classifier);
		parser.setActiveClass(className);
		
		this.read(source, offset, length, parser);
		
		System.out.println("training done for " + source);
	}
	
	private List<String> getSortedPossibleWords(int toplength, Map<String, Double> scores) {
		List<String> returningWords = new ArrayList<String>();
		
		// sort the new list
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>( scores.entrySet() );
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
            {
                return - o1.getValue().compareTo(o2.getValue());
            }
        });
		
		for(Map.Entry<String, Double> word : list) {
			
			System.out.println("prio word "+word.getKey() + " = "+word.getValue());
			
			returningWords.add(word.getKey());
		}
		
		if(returningWords.size() < toplength) {
			return returningWords;
		} else
			return returningWords.subList(0, toplength);
	}
	
	public List<String> getPossibleWords(int toplength, String matchingClassName, String normalClassName) {
		BayesClass matchingClass = getClassifier().getClass(matchingClassName);
		BayesClass normalClass = getClassifier().getClass(normalClassName);
		
		Map<String, Double> scores = new HashMap<String, Double>();
		
		for(Map.Entry <BayesFeature, Map<BayesClass, Map<String, Double>>> feature : getClassifier().getPriorsItself().entrySet()) {
			String word = feature.getKey().getValue(null);
			
			Double matchingProb = 0.0, normalProb = 1.0;
		
			if(feature.getValue().get(matchingClass) != null && feature.getValue().get(matchingClass).get(word) != null) {
				matchingProb = feature.getValue().get(matchingClass).get(word);
			}
			
			if(feature.getValue().get(normalClass) != null && feature.getValue().get(normalClass).get(word) != null) {
				normalProb = feature.getValue().get(normalClass).get(word);
			}
			 
			scores.put(word, matchingProb / normalProb);
		}
		
		return getSortedPossibleWords(toplength, scores);
	}
	
	public static void main(String[] args) {
		
		System.out.println("Start");
		
		long start = System.currentTimeMillis();
		
		ClassifierWords w = new ClassifierWords();
		
		//w.trainFile("movember", "../data/out_movember_small.json", 0, 1000);
		w.trainSource("movember", new TweetSearchDataSource("silicon valley "), 0, 2000);
		w.trainFile("not", "../data/out_en_nl_es.json", 0, 10000);
		
		for(String word : w.getPossibleWords(100, "movember", "not")) {
			System.out.println(word);
		}
		
		/*try {
			Writer out2 = new FileWriter("../data/priors_cdc_hpv.csv");
			w.getClassifier().outPriors(out2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		System.out.println("Duration: " + ((System.currentTimeMillis() - start) / 1000) + "s");
		
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
		return this.getPossibleWords(top, "match", "normal");
	}

	@Override
	public double getPossibleWordScore(String term) {
		
		BayesClass matchingClass = getClassifier().getClass("match");
		BayesClass normalClass = getClassifier().getClass("normal");
		
		double score = 0.0;
		
		// slow loop, need a hashtable lookup?
		
		for(Map.Entry <BayesFeature, Map<BayesClass, Map<String, Double>>> feature : getClassifier().getPriorsItself().entrySet()) {
			String word = feature.getKey().getValue(null);
			
			if(word.equals(term)) {
			
				Double matchingProb = 0.0, normalProb = 1.0;
		
				if(feature.getValue().get(matchingClass) != null && feature.getValue().get(matchingClass).get(word) != null) {
					matchingProb = feature.getValue().get(matchingClass).get(word);
				}
			
				if(feature.getValue().get(normalClass) != null && feature.getValue().get(normalClass).get(word) != null) {
					normalProb = feature.getValue().get(normalClass).get(word);
				}
				
				score = matchingProb / normalProb;
				
				break;
			}
		}
		
		return score;
	}

}
