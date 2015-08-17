package live;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import tools.JSON;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * This class calls the Twitter streaming api
 * @author Han
 *
 */
public class LiveContentGrabber implements Runnable, Stoppable, OutputQueue<JSONObject> {
	
	private static final Logger logger = LogManager.getLogger("LiveContentGrabber");
		
	private TwitterStream twitterStream;
	
	public static long tweetCounter = 0;
	
	private int missedTweets;
	
	private Queue<JSONObject> output_queue;
	
	private List<String> words;
	
	public LiveContentGrabber(List<String> words) {
		this.output_queue = new ConcurrentLinkedQueue<JSONObject>();
		this.words = words;
	}
	
	public int getMissedTweets() {
		return missedTweets;
	}
	
	public Queue<JSONObject> getOutputQueue() {
		return output_queue;
	}
	
	private void processTweet(Status status) {
    	String line = TwitterObjectFactory.getRawJSON(status);
    	
    	output_queue.offer(JSON.parse(line));
    	
		if(++tweetCounter % 100 == 0) {
			logger.info("Write buffer {}", tweetCounter);
		}
	}
	
	private void startStreaming(List<String> words) {	
		
		StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	processTweet(status);
	        }
	        
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	        	missedTweets = numberOfLimitedStatuses;
	        }
	        public void onException(Exception ex) {
	            logger.info("TwitterStream {}", ex.getMessage());
	        }
			public void onScrubGeo(long arg0, long arg1) {
				
			}
			public void onStallWarning(StallWarning arg0) {
				logger.info("TwitterStream stall warning");
			}
	    };
	    
	    twitterStream = new TwitterStreamFactory().getInstance();
	    
	    logger.info("Start streaming with {}", words);
	    
	    FilterQuery query = new FilterQuery();
	    query.track(words.toArray(new String[words.size()]));
	    
	    twitterStream.addListener(listener);
	    twitterStream.filter(query);	
	}
	
	public void stop() {
		
		if(twitterStream != null)
			twitterStream.shutdown();
		
		logger.info("Shutting down");
	}
	
	public void run() {
		logger.trace("Start LiveContentGrabber");
		
		startStreaming(words);
		
		logger.trace("End LiveContentGrabber");
	}
	
}
