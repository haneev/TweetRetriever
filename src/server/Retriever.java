package server;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datagrant.ClassifierWords;
import datagrant.sources.DataSource;
import datagrant.sources.FileDataSource;
import datagrant.sources.TweetSearchDataSource;
import live.Callback;
import live.LiveAdditionalWords;
import live.LiveClassifier;
import live.LiveContentGrabber;
import live.LiveMonitor;
import live.LiveWriter;
import live.PerSecAnalyzer;
import live.TweetOutputAnalyzer;
import validate.OverlapPossibleWords;

public class Retriever implements Callback, Runnable {

	public static final String NORMAL_FILE = "data/not_dataset.json.gz";
	
	public static final String OUTPUT_DIR = "data/";
	
	public static final long MAX_DURATION = 3600;

	public static final Logger logger = LogManager.getLogger("TweetRetrieverServer");
	
	private boolean initializeFast = true;
	
	private String keyword;
	private int top, trainingTweets;
	
	private LiveMonitor monitor;
	
	private Callback callback;
	
	public Retriever(LiveMonitor monitor, String keyword, int top, int trainingTweets, Callback callback2) {
		this.keyword = keyword;
		this.top = top;
		this.trainingTweets = trainingTweets;
		this.monitor = monitor;
		this.callback = callback2;
	}

	/** 
	 * Enable fast mode, when set to false Overlap is used.
	 * Otherwise ProbFact is used
	 * 
	 * @param fast
	 */
	public void setFastMode(boolean fast) {
		this.initializeFast = fast;
	}
	
	private List<String> getWords(DataSource mf, DataSource nf) {
		
		LiveAdditionalWords w = new LiveAdditionalWords();
		
		ClassifierWords prior = new ClassifierWords();
		prior.trainSource("match", mf, 0, trainingTweets);
		prior.trainSource("normal", nf, 0, trainingTweets * 2);
		
		if(initializeFast) {
			w.addParser(prior);
		} else {
			List<String> wordsToRankSecondary = LiveAdditionalWords.filterWords(prior.getPossibleWords(80));

			logger.info("Starting list for overlap {}", wordsToRankSecondary);
			
			OverlapPossibleWords overlap = new OverlapPossibleWords( keyword, wordsToRankSecondary);
			
			w.addParser(overlap);	
		}
		
		return w.getWords(top);
	}
	
	private String getOutputFiles(String type) {
		return OUTPUT_DIR + keyword + "_"+type+".json.gz";
	}
	
	public void run() {
		
	    DataSource nf = new FileDataSource(NORMAL_FILE);
	    DataSource mf = new TweetSearchDataSource(keyword);
	    
	    logger.trace("Get normal and matching dataset");
	    
	    DataSource matchingAndRelevantTweets = mf.toArray(trainingTweets);
	    DataSource notRelevantTweets = nf.toArray(trainingTweets * 2);
	    List<String> topWords = getWords(matchingAndRelevantTweets, notRelevantTweets);
	    
	    /* THREADS */	    
	    
	    LiveContentGrabber liveContent = new LiveContentGrabber(topWords);
	    monitor.addThread(liveContent);
	    App.getApp().getStats().put("liveContent", liveContent);
	    
	    // Amount of Tweets per sec from the live content
	    PerSecAnalyzer inputTweets = new PerSecAnalyzer(liveContent.getOutputQueue());
	    monitor.addThread(inputTweets);
	    App.getApp().getStats().put("inputTweets", inputTweets);
	    
	    // classifier
	    LiveClassifier liveClassifier = new LiveClassifier(inputTweets.getOutputQueue(), keyword, topWords);
	    liveClassifier.trainBySource(matchingAndRelevantTweets, notRelevantTweets);
	    monitor.addThread(liveClassifier);
	    App.getApp().getStats().put("liveClassifier", liveClassifier);
		    
	    logger.trace("Set outputs");
	    
	    // NOT QUEUE Analyzer -> Buffer -> Writer
	    
	    // Not: Per Sec
	    PerSecAnalyzer notQueueAnalyzer = new PerSecAnalyzer(liveClassifier.getNotQueue());
	    monitor.addThread(notQueueAnalyzer);
	    App.getApp().getStats().put("notQueueAnalyzer", notQueueAnalyzer);
	    
	    // Not: Output
	    TweetOutputAnalyzer notTweetOutputAnalyzer = new TweetOutputAnalyzer(notQueueAnalyzer.getOutputQueue());
	    monitor.addThread(notTweetOutputAnalyzer);
	    App.getApp().getStats().put("notTweetOutputAnalyzer", notTweetOutputAnalyzer);
	    
	    // Not: Writer
    	LiveWriter notOutputWriter = new LiveWriter(notTweetOutputAnalyzer.getOutputQueue(), getOutputFiles("not"));
    	monitor.addThread(notOutputWriter);
    	
    	// Match: Analyzer -> Buffer -> Writer
    	
    	// Match: Per Sec
	    PerSecAnalyzer matchQueueAnalyzer = new PerSecAnalyzer(liveClassifier.getOutputQueue());
	    monitor.addThread(matchQueueAnalyzer);
	    App.getApp().getStats().put("matchQueueAnalyzer", matchQueueAnalyzer);
	    
	    // Match: Output
	    TweetOutputAnalyzer matchTweetOutputAnalyzer = new TweetOutputAnalyzer(matchQueueAnalyzer.getOutputQueue());
	    monitor.addThread(matchTweetOutputAnalyzer);
	    App.getApp().getStats().put("matchTweetOutputAnalyzer", matchTweetOutputAnalyzer);
    	
	    // Match: Writer
	    LiveWriter matchOutputWriter = new LiveWriter(matchTweetOutputAnalyzer.getOutputQueue(), getOutputFiles("match"));
	    monitor.addThread(matchOutputWriter);
	    
	    logger.trace("Thread ended");
	}
	
	public void callback() {
		this.callback.callback();
	}
	
}
