package datagrant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.Twitter;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TwitterHelper {

	public static String outputFile = "../data/tweets_unrelated.json.gz";
	
	public static long counter = 0;
	
	public static void processTweet(Status status) {
		String line = TwitterObjectFactory.getRawJSON(status);	
	    try {
	    	FileOutputStream fileStream = new FileOutputStream(new File(outputFile), true);
	    	GZIPOutputStream gzipOut = new GZIPOutputStream(fileStream);
	    	OutputStreamWriter writer = new OutputStreamWriter(gzipOut, "UTF-8");
	    	
			writer.append(line+"\n");
			writer.close();
			
			System.out.println("Received Tweet "+ (++counter) );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Status> search(String q) throws TwitterException {
		Twitter twitter = TwitterFactory.getSingleton();
	    Query query = new Query("voetbal");
	    query.setCount(100);
	    QueryResult result = twitter.search(query);
	    return result.getTweets();
	}
	
	public static void streamer() throws IOException {
				
	    StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	        	processTweet(status);
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {
	            //ex.printStackTrace();
	        }
			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
			}
	    };
	    
	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	    
	    // track only specific keywords
	    FilterQuery query = new FilterQuery();
	    
	    //query.track(new String[] {"#hpv", "#cdcvax", "#hpvreport","cdc","hpv"});
	    
	    twitterStream.addListener(listener);
	    twitterStream.sample();
	    //twitterStream.sample();
	}
	
	public static void main(String[] args) {
		try {
			TwitterHelper.streamer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
