package live;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import classifiers.ClassifierStructure;
import datagrant.sources.DataSource;
import tools.ClassifierDocument;
import tools.Data;
import tools.TweetReader;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Classifier that handles Tweets and automatically puts them into their queues
 * Also automatically trains on new Tweets. This is based on a weka implementation of Bayes
 * @author Han
 *
 */
public class LiveClassifier implements Runnable, Stoppable, InputQueue<JSONObject>, OutputQueue<JSONObject> {

	private static final Logger logger = LogManager.getLogger("LiveClassifier");
	
	private NaiveBayesMultinomialUpdateable classifier;
	
	private ClassifierStructure struct = new ClassifierStructure();
	
	private Filter wordVectorFilter;
	
	private Instances dummyFormatInstances;
	
	private Queue<JSONObject> output_queue_match, output_queue_not;
	
	private Queue<JSONObject> input_queue;
	
	private boolean collectNotCases = true;
	
	private volatile boolean running = true;
	
	private boolean doStats = true;
	
	private String keyword;
	
	private Map<String, WordStat> wordStats;
	
	/* Stats */
	private List<String> possible_words;
	private Map<String, Integer> stats_match;
	private Map<String, Integer> stats_not;
	private int tweetCounter = 0;
	
	public LiveClassifier(Queue<JSONObject> input_queue, String keyword, List<String> possible_words) {
		
		this.keyword = keyword;
		this.possible_words = possible_words;
		
		this.stats_match = new HashMap<String, Integer>();
		this.stats_not = new HashMap<String, Integer>();
		
		dummyFormatInstances = new Instances("Rel", struct.getStructure(), 200);
		dummyFormatInstances.setClassIndex(1);
		
		wordVectorFilter = getWordFilter(dummyFormatInstances, struct);
		
		classifier = getBayesMulti();
		
		this.output_queue_match = new ConcurrentLinkedQueue<JSONObject>();
		this.output_queue_not = new ConcurrentLinkedQueue<JSONObject>();
		
		this.input_queue = input_queue;
		
		this.wordStats = new HashMap<String, WordStat>();
	}
	
	public Collection<WordStat> getWordStats() {
		return wordStats.values();
	}
	
	public void setCollectNotCases(boolean b) {
		this.collectNotCases = b;
	}
	
	public Queue<JSONObject> getInputQueue() {
		return input_queue;
	}
	
	public void setInputQueue(Queue<JSONObject> q) {
		this.input_queue = q;
	}
	
	public Queue<JSONObject> getMatchQueue() {
		return output_queue_match;
	}
	
	public Queue<JSONObject> getOutputQueue() {
		return this.getMatchQueue();
	}
	
	public Queue<JSONObject> getNotQueue() {
		return output_queue_not;
	}
	
	public ClassifierStructure getStructure() {
		return struct;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public String classifyInstance(Instance in) {
		
		try {
			double label = classifier.classifyInstance(filterInstance(in));
			return label > 0.5 ? "match" : "not";
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return "not";
	}
	
	public void trainClassifier(Instances instances) {
		try {
			classifier.buildClassifier(filterInstances(instances));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateClassifier(Instance instance) {
		try {
			classifier.updateClassifier(filterInstance(instance));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Instance filterInstance(Instance instance) {
		
		Instances data = new Instances("Filter", struct.getStructure(), 10);
		data.setClassIndex(1);
		data.add(instance);
		
		try {
			Instances filtered = Filter.useFilter(data, wordVectorFilter);
			return filtered.firstInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Instances filterInstances(Instances instances) {
		
		try {
			return Filter.useFilter(instances, wordVectorFilter);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	private void addOneForStats(Map<String, Integer> stats, String token) {
		
		if(stats.containsKey(token))
			stats.put(token, stats.get(token) + 1);
		else
			stats.put(token, new Integer(1));
		
	}
	
	public void trainBySource(DataSource matchSource, DataSource notSource) {
		
		Instances train = new Instances("train", getStructure().getStructure(), 100000);
		train.setClassIndex(1);
		
		logger.trace("Training");
		
		int i = 0;
		for(JSONObject doc : matchSource) {
			train.add(getStructure().makeInstance("match", doc.optString("text")));
			
			if(++i % 5000 == 0)
				logger.trace("progress "+i);
		}
		
		int j = 0;
		for(JSONObject doc : notSource) {
			train.add(getStructure().makeInstance("not", doc.optString("text")));
			
			if(++j % 5000 == 0)
				logger.trace("progress "+i);
		}
		logger.trace("Done loading instances");
		
		trainClassifier(train);
		
		logger.trace("Done training");
		
	}

	public void stop() {
		running = false;
	}
	
	public void run() {

		logger.trace("Starting");
		int i = 0;
		JSONObject doc;
		while(running) {
			
			doc = this.input_queue.poll();
			
			if(doc != null) {
				
				String cls = this.classifyInstance(struct.makeInstance("match", doc.optString("text")));
				
				tweetCounter++; // add global tweet Count
				
				if(cls.equals("match")) {
					output_queue_match.offer(doc);
				} else if (collectNotCases) {
					output_queue_not.offer(doc);
				}
				
				if(doStats) {
					String text = doc.optString("text");
					for(String token : possible_words) {
						if(Data.containsToken(text, token)) {
							addOneForStats(cls.equals("match") ? stats_match : stats_not, token);
						}
					}						
				}
				
				// update 
				if(Data.containsToken(doc.optString("text"), keyword)) {
					logger.trace("Updating classifier with new tweet");
					this.updateClassifier(struct.makeInstance("match", doc.optString("text")));
				}
				
				// Show summary on the way
				if(i % 100 == 0 && doStats) {
					logger.info("STATS =========== ");
					logger.info("Total Count {}", tweetCounter);
					for(String token : possible_words) {
						
						int not = stats_not.containsKey(token) ? stats_not.get(token) : 0;
						int match =  stats_match.containsKey(token) ? stats_match.get(token) : 0;
						
						wordStats.put(token, new WordStat(token, match, not ));
						
						logger.info("word {} match:{}, not:{}", token, match, not);
					}
					logger.info("END STATS =========== ");
				}
				
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		logger.info("Stopped");
		
	}
	
	public static NaiveBayesMultinomialUpdateable getBayesMulti() {
		NaiveBayesMultinomialUpdateable nb = new NaiveBayesMultinomialUpdateable();
		return nb;
	}
	
	public static StringToWordVector getWordFilter(Instances data, ClassifierStructure struct) {
		StringToWordVector filter = new StringToWordVector();
		
	    try {
			filter.setWordsToKeep(20000000);
			WordTokenizer token = new WordTokenizer();
			filter.setTokenizer(token);
			filter.setInputFormat(data); 
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	    
	    return filter;
	}
	
	public static void main(String[] args) {
		
		logger.info("Starting");
		
		LiveClassifier live = new LiveClassifier(null, null, null);
		
		Instances train = new Instances("train", live.getStructure().getStructure(), 100000);
		train.setClassIndex(1);
		
		logger.info("Train match");
		int i = 1;
		for(ClassifierDocument doc : new TweetReader("../data/arrow-live_compacted.json.gz")) {
			if(Data.containsToken(doc.getText(), "#arrow")) {
				i++;
				train.add(live.getStructure().makeInstance("match", doc.getText()));
			}
			
			if(i % 10000 == 0)
				logger.info("progress "+i);
			
			if(i > 20000)
				break;
		}
		
		logger.info("Train not");
		int j = 0;
		for(ClassifierDocument doc : new TweetReader("../data/tweets_non_related_150615.json.gz")) {
			train.add(live.getStructure().makeInstance("not", doc.getText()));
			
			if(++j % 10000 == 0)
				logger.info("progress "+j);
		}
		
		live.trainClassifier(train);
		
		logger.info("Done training");	
		
	}
	
	/**
	 * Internal class that holds stats for each keyword
	 * @author Han
	 *
	 */
	public class WordStat implements JSONAble {
		public String keyword;
		public int match;
		public int not;
		
		public WordStat(String keyword, int match, int not) {
			this.keyword = keyword;
			this.match = match;
			this.not = not;
		}
		
		public JSONObject toJSON() {
			JSONObject j = new JSONObject();
			j.put("keyword", keyword);
			j.put("match", match);
			j.put("not", not);
			return j;
		}
	}

	
	
}
