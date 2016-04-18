package classifiers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BayesClassifier {

	// Minimal number of occurrences needed for a word or value of a feature (e.g. wordfeature)
	private int min_amount_of_occurrences = 0;
	
	// Minimal number of count that a feature value should have
	private int min_amount_of_feature_counts = 0;
	
	// list of active classes
	private Map<String, BayesClass> classes;
	
	// list of active features
	private List<BayesFeature> features;
	
	// per feature value the count per class
	private Map<BayesFeature, Map<String, Map<BayesClass, Long> > > feature_counts;
	
	// what are the odds when having timezone A that it is class C = P(class C | timezone A)
	private Map<BayesFeature, Map<String, Map<BayesClass, Double> > > feature_priors;
	
	// what are the odds when having timezone A considering class C = P(timezone A | class C)
	private Map<BayesFeature, Map<BayesClass, Map<String, Double>>> feature_priors_itself;
	
	public BayesClassifier() {
		this.features = new ArrayList<BayesFeature>();
		this.feature_counts = new HashMap<BayesFeature, Map<String, Map<BayesClass, Long> > >();
		this.feature_priors = new HashMap<BayesFeature, Map<String, Map<BayesClass, Double> > >();
		this.feature_priors_itself = new HashMap<BayesFeature, Map<BayesClass, Map<String, Double> > >();
		this.classes = new HashMap<String, BayesClass>();
	}
	
	public void reset() {
		this.feature_counts.clear();
		
		for(BayesClass cls : this.classes.values()) {
			cls.reset();
		}
		
		System.gc();
	}
	
	public void setThreshold(int threshold) {
		this.min_amount_of_occurrences = threshold;
	}
	
	public void setFeatureCountThreshold(int threshold) {
		this.min_amount_of_feature_counts = threshold;
	}
	
	public void addFeature(BayesFeature feature) {
		this.features.add(feature);
	}
	
	public List<BayesFeature> getFeatures() {
		return this.features;
	}
	
	public Map<BayesFeature, Map<String, Map<BayesClass, Long> > > getCounts() {
		return this.feature_counts;
	}
	
	public Map<BayesFeature, Map<String, Map<BayesClass, Double>>> getPriors() {
		return this.feature_priors;
	}
	
	public Map<BayesFeature, Map<BayesClass, Map<String, Double>>> getPriorsItself() {
		return this.feature_priors_itself;
	}
	
	public long getDocumentCount() {
		long total = 0;
		
		for(BayesClass cls : classes.values())
			total += cls.getClassCount();
		
		return total;
	}
	
	public BayesClass getClass(String className) {
		BayesClass bayesClass = classes.get(className);
		
		if(bayesClass == null) {
			bayesClass = new BayesClass(className);
			classes.put(className, bayesClass);
		}
		
		return bayesClass;
	}
	
	public void trainDocument(BayesDocument doc) {
		
		if(!doc.isTrainDocument()) {
			return;
		}
		
		BayesClass bayesClass = doc.getBayesClass();
		bayesClass.countClass(); // Add 1 count to the Class 
		
		for(BayesFeature feature : getFeatures()) {
		
			if(feature instanceof BayesMultiFeature) {
				
				for(BayesFeature feat : ((BayesMultiFeature) feature).getMultiValue(doc)) {
					this.countFeature(bayesClass, feat, feat.getValue(doc));
				}
				
			} else {
				// System.out.println("train feature: " + bayesClass.getName() + " "+ feature.getClass().getName() +" " + feature.getValue(doc));
				this.countFeature(bayesClass, feature, feature.getValue(doc));
			}
		}
	}
	
	private void countFeature(BayesClass bayesClass, BayesFeature feature, String value) {
		
		// null to ""
		value = (value == null) ? "" : value;
		
		// get feature counts, create when non existing
		Map<String, Map<BayesClass, Long> > counts = feature_counts.get(feature);
		if(counts == null) {
			counts = new HashMap<String, Map<BayesClass, Long>>();
			feature_counts.put(feature, counts);
		}
		
		// get class counts, create when non existing
		Map<BayesClass, Long> classCounts = counts.get(value);
		if(classCounts == null) {
			classCounts = new HashMap<BayesClass, Long>();
			counts.put(value, classCounts);
		}
		
		Long count = classCounts.containsKey(bayesClass) ? classCounts.get(bayesClass) : new Long(0);
		
		classCounts.put(bayesClass, count + 1);
	}
	
	public void trainClassifier() {
		long totalDocuments = getDocumentCount();
		
		this.cleanFeatures();
		
		for(BayesClass cls : classes.values()) {
			cls.train(totalDocuments);
		}
		
		for(BayesFeature feature : feature_counts.keySet()) {
			this.trainFeature(feature);
			//this.trainFeatureItself(feature);
		}

	}
	
	private void cleanFeatures() {
		List<BayesFeature> features_to_be_ignored = new ArrayList<BayesFeature>();
		
		for(BayesFeature feature : feature_counts.keySet()) {
			boolean useful = this.isUsefulFeature(feature);
			if(!useful) {
				features_to_be_ignored.add(feature);
			}
		}
		
		for(BayesFeature feature : features_to_be_ignored) {
			//System.out.println("Features to be removed = " + feature.getClass().getName());
			feature_counts.remove(feature);
		}
		
		this.cleanFeatureValues();
		
	}
	
	private void cleanFeatureValues() {
		// clean feature values
		for(Map.Entry<BayesFeature,Map<String,Map<BayesClass,Long>>> entry : feature_counts.entrySet()) {
			List<String> values_to_be_removed = new ArrayList<String>();
			
			if (entry.getKey().getIsRemovable()) {
				for(Map.Entry<String, Map<BayesClass, Long>> value : entry.getValue().entrySet()) {
					
					// This is the count of a feature value, for instance location "At the deathstar"
					long total = 0;
					for (Long count : value.getValue().values()) {
						total += count;
					}

					if (total < min_amount_of_feature_counts) {
						values_to_be_removed.add(value.getKey());
					}
				}
				
				// remove all words in this feature
				for(String word : values_to_be_removed) {
					feature_counts.get(entry.getKey()).remove(word);
				}
			}
			
		}
	}
	
	private boolean isUsefulFeature(BayesFeature feature) {
		boolean skipFeature = false;
		
		for(Map.Entry<String, Map<BayesClass, Long>> entry : feature_counts.get(feature).entrySet()) {
			long total = getTotal(entry.getValue());
			if(total < min_amount_of_occurrences) {
				skipFeature = true;
			}
		}
		
		return !skipFeature;
	}
	
	private long getTotal(Map<BayesClass, Long> map) {
		long total = 0;
		
		for(Long count : map.values()) {
			total += count;
		}
		
		return total;
	}
	
	private long getTotalPerClass(BayesClass cls, BayesFeature feature) {
		long total = 0;
		
		for(Map.Entry<BayesFeature,Map<String,Map<BayesClass,Long>>> entry : feature_counts.entrySet()) {
			
			if(!(entry.getKey().getClass().isInstance(feature)))
				continue;
			
			for(Map<BayesClass, Long> item : entry.getValue().values()) {
				if(item.containsKey(cls))
					total += item.get(cls);
			}
		}
		
		return total;
	}
	
	private void trainFeature(BayesFeature feature) {
		
		Map<String, Map<BayesClass, Double>> map = new HashMap<String, Map<BayesClass, Double>>();
		
		for(Map.Entry<String, Map<BayesClass, Long>> entry : feature_counts.get(feature).entrySet()) {
			
			//System.out.println(feature.getClass().getName() + " " +entry.getKey());
			Double total = new Double(getTotal(entry.getValue()));
			
			Map<BayesClass, Double> priorMap = new HashMap<BayesClass, Double>();
			
			for(Map.Entry<BayesClass, Long> countEntry : entry.getValue().entrySet()) {				
				Double prior = new Double(countEntry.getValue()) / total;
				priorMap.put(countEntry.getKey(), prior);
				
				//System.out.println("Train doc for "+countEntry.getKey().getName() + " feature "+feature.getName() + " \t "+entry.getKey()+" \t "+countEntry.getValue()+"/"+total+" = "+prior);
			}
			
			map.put(entry.getKey(), priorMap);
		}
		
		feature_priors.put(feature, map);
	}
	
	private void trainFeatureItself(BayesFeature feature) {

		Map<BayesClass, Map<String, Double>> map_itself = new HashMap<BayesClass, Map<String, Double>>();
		
		for(BayesClass bayesClass : classes.values()) {
			Map<String, Double> prior_map_itself = new HashMap<String, Double>();
		
			Double total = new Double( getTotalPerClass(bayesClass, feature) );
			
			for(Map.Entry<String, Map<BayesClass, Long>> entry : feature_counts.get(feature).entrySet()) {
				
				// to have at least a prior above zero
				Double prior = entry.getValue().containsKey(bayesClass) ? new Double(entry.getValue().get(bayesClass)) / total : 1.0 / total;
				prior_map_itself.put(entry.getKey(), prior);
			}
			
			map_itself.put(bayesClass, prior_map_itself);
		}
		
		feature_priors_itself.put(feature, map_itself);
	}
	

	public Double getPrior(BayesFeature feature, String value, BayesClass bayesClass) {
		
		if(value == null || value.isEmpty())
			return 0.0;
		
		Map<String, Map<BayesClass, Double>> returnMap = feature_priors.get(feature);
		
		if(returnMap == null)
			return 0.0;
		
		Map<BayesClass, Double> returnPriorMap = returnMap.get(value);
		
		if(returnPriorMap == null)
			return 0.0;
		
		Double returnValue = returnPriorMap.get(bayesClass);
		
		if(returnValue == null)
			return 0.0;
		
		return returnValue;
	}
	
	public Double getScore(BayesDocument doc, BayesClass bayesClass) {
		
		Double score = bayesClass.getClassPrior();
		
		for(BayesFeature feature : getFeatures()) {
			
			if(feature instanceof BayesMultiFeature) {
				
				for(BayesFeature feat : ((BayesMultiFeature) feature).getMultiValue(doc)) {
					score += getPrior(feat, getValueFromFeature(feature, doc), bayesClass);
				}
				
			} else 
				score += getPrior(feature, getValueFromFeature(feature, doc), bayesClass);
		}
		
		return score;
	}
	
	private String getValueFromFeature(BayesFeature feature, BayesDocument doc) {
		try {
			return feature.getValue(doc);
		} catch(Exception e) {
			return null;
		}
	}
	
	public List<Map.Entry<BayesClass, Double>> match(BayesDocument doc) {
		
		Map<BayesClass, Double> scores = new HashMap<BayesClass, Double>();
		
		for(BayesClass cls : classes.values()) {
			scores.put(cls, this.getScore(doc, cls));
		}
		
		List<Map.Entry<BayesClass, Double>> list = new ArrayList<Map.Entry<BayesClass, Double>>( scores.entrySet() );
		
		Collections.sort(list, new Comparator<Map.Entry<BayesClass, Double>>() {
            public int compare( Map.Entry<BayesClass, Double> o1, Map.Entry<BayesClass, Double> o2 )
            {
                return - o1.getValue().compareTo(o2.getValue());
            }
        });
		
		return list;
	}
	
	public void outPriors(Writer out) {
		
		try {
			List<BayesClass> classes_columns = new ArrayList<BayesClass>();
			for(BayesClass cls : classes.values())
				classes_columns.add(cls);
			
			String row = ";";
			for(BayesClass cl : classes_columns) {
				row += cl.getName() + ";";
			}
			
			out.write(row+"\n");
			
			for(Map.Entry <BayesFeature, Map<BayesClass, Map<String, Double>>> feature : feature_priors_itself.entrySet()) {
				
				String returnRow = feature.getKey().getName() + ";";
				
				for(String word : feature_priors.get(feature.getKey()).keySet()) {
					
					returnRow += word + ";";
					
					for(BayesClass bayesClass : classes_columns) {
						
						Map<String, Double> entryMap = feature.getValue().get(bayesClass);
						Double prior = (entryMap != null && entryMap.containsKey(word)) ? entryMap.get(word) : 0.0;
						returnRow += prior + ";";
						
					}
					
					out.write(returnRow+"\n");
				}
				
				
			}
			
			out.flush();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
