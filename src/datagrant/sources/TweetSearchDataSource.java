package datagrant.sources;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import tools.JSON;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;

/**
 * Search for Tweets using Twitter4J
 * This is resulted in a datasource that can read directly from Twitter
 * 
 * @author Han
 *
 */
public class TweetSearchDataSource extends DataSource {

	private String keywords;
	
	private Twitter twitter;
	
	private int i = 0;
	
	private QueryResult result;
	
	private List<Status> activeResultSet;
	
	private int tweetBatchCount = 100;
	
	private int sleepTime = 1000;
	
	private boolean finished = false;
	
	public TweetSearchDataSource(String keywords) {
		this.keywords = keywords;
		twitter = TwitterFactory.getSingleton();
	}
	
	public String getQuery() {
		return keywords;
	}
	
	public String toString() {
		return this.getClass().getName() + " using "+keywords;
	}
	
	public void doNext() {
		try {
			if(result == null) {
				
				Query query = new Query(this.keywords);
			    query.setCount(tweetBatchCount);
			    
			    try {
			    	Thread.sleep(sleepTime);
			    } catch(InterruptedException e) {}
			    
				result = twitter.search(query);			
				
				i = 0;
			} else if (i >= tweetBatchCount - 1) {
				Query nextResult = result.nextQuery();
				
				if(nextResult == null) {
					finished = true;
				} else { 
					result = twitter.search(nextResult);
				
					try {
				    	Thread.sleep(sleepTime);
				    } catch(InterruptedException e) {
				    	
				    }
					
					i = 0;
				}
			}
			
			activeResultSet = result.getTweets();
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNext() {
		
		if(finished) 
			return false;
		
		doNext();
		
		++i;
		
		try {
			return activeResultSet != null && activeResultSet.size() > i && activeResultSet.get(i) != null;
		} catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
	}

	public JSONObject next() {
					
		try {
			if(activeResultSet != null) {
				String json = TwitterObjectFactory.getRawJSON(activeResultSet.get(i));				
				
				return JSON.parse(json);
			}
		} catch(IndexOutOfBoundsException e) {
			return null;
		} catch(JSONException e) {
			return null;
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
	
	}

	public static void main(String[] args) {
		
		DataSource src = new TweetSearchDataSource("movember");
		
		int k = 0;
		while(src.hasNext()) {
			
			
			if(k > 95) {
				break;
			}
			
			JSONObject obj = src.next();
		
			System.out.println("Tweet "+ k);
		
			k++;
		}
		
	}
	
	public Object clone() throws CloneNotSupportedException {
        return new TweetSearchDataSource(this.keywords);
    }
	
}
