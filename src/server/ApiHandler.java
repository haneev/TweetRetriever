package server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import live.JSONAble;
import live.LiveClassifier;
import live.LiveContentGrabber;
import live.PerSecAnalyzer;
import live.TweetOutputAnalyzer;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class ApiHandler implements HttpHandler {
	
	public interface ApiCall {
		public JSONObject run(HttpExchange t);
	}
	
	public void apiEndpoints(Map<String, ApiHandler.ApiCall> apiEndpoints) {
		apiEndpoints.put("status", new ApiHandler.StatusCall());
		apiEndpoints.put("log", new ApiHandler.LogCall());
		apiEndpoints.put("twitter", new ApiHandler.TwitterCall());
	}
	
    @Override
    public void handle(HttpExchange t) throws IOException {
    	
    	String requestedUrl = t.getRequestURI().toString().substring(5);
    	t.getResponseHeaders().set("Content-Type", "application/json");
    	
    	Server.logger.info("Serving API call {}", requestedUrl);
    	
    	Map<String, ApiHandler.ApiCall> apis = new HashMap<String, ApiCall>();    	
    	apiEndpoints(apis);
    	
    	if(apis.containsKey(requestedUrl)) {
    		String response = apis.get(requestedUrl).run(t).toString();
    		
    		if(response.isEmpty())
				response = "NULL";
    		
			t.sendResponseHeaders(200, response.getBytes().length);
    								
    		OutputStream os = t.getResponseBody();
    		os.write(response.getBytes());
    		os.close();
    		
    	} else {
    		t.sendResponseHeaders(404, 0);
    		t.getResponseBody().close();
    	}
    	      
    }
    
    
    public static class StatusCall implements ApiHandler.ApiCall {
    	
    	public JSONObject run(HttpExchange t) {
    		
    		JSONObject result = new JSONObject();
    		
    		if(t.getRequestMethod().equals("POST")) {
    			JSONObject json = new JSONObject(new JSONTokener(t.getRequestBody()));
    			
    			if(json.has("command") && json.getString("command").equals("start")) {
    				
    				App.getApp().start(json.getString("keyword"), json.optInt("top", 10), json.optInt("training", 2500));
    				
    			} else if(json.has("command") && json.getString("command").equals("stop")) {
    				
    				App.getApp().stop();    
    				
    			}
    			
    			result.put("status", App.getApp().getStatus());
    			
    		} else {
    			result = constructStatus(result);
    		}
    	
    		return result;
    	}
    	
    	private List<JSONObject> convertListToJSON(Collection<? extends JSONAble> in) {
    		List<JSONObject> list = new ArrayList<JSONObject>();
    		
    		for(JSONAble obj : in) {
    			list.add(obj.toJSON());
    		}
    			
    		return list;
    	}
    	
    	private JSONObject constructStatus(JSONObject result) {
    		result.put("status", App.getApp().getStatus());
    		
    		if(App.getApp().getStats().get("liveClassifier") == null) {
    			Server.logger.info("not started yet");
    		} else {
    			
    			result.put("keyword", (String) App.getApp().getStats().get("keyword"));
    			
	    		result.put("wordStats", convertListToJSON( ((LiveClassifier) App.getApp().getStats().get("liveClassifier")).getWordStats()) );
	    		
	    		result.put("inputTweetsPerSec", ( (PerSecAnalyzer) App.getApp().getStats().get("inputTweets")).getTweetsPerSec() );
	    		result.put("inputTweetsCount", ( (PerSecAnalyzer) App.getApp().getStats().get("inputTweets")).getPassedTweetsCount() );
	    		
	    		result.put("notQueueTweetsPerSec", ( (PerSecAnalyzer) App.getApp().getStats().get("notQueueAnalyzer")).getTweetsPerSec() );
	    		result.put("notQueueTweetsCount", ( (PerSecAnalyzer) App.getApp().getStats().get("notQueueAnalyzer")).getPassedTweetsCount() );
	    		
	    		result.put("matchQueueTweetsPerSec", ( (PerSecAnalyzer) App.getApp().getStats().get("matchQueueAnalyzer")).getTweetsPerSec() );
	    		result.put("matchQueueTweetsCount", ( (PerSecAnalyzer) App.getApp().getStats().get("matchQueueAnalyzer")).getPassedTweetsCount() );
	    		
	    		result.put("matchSample", ( (TweetOutputAnalyzer) App.getApp().getStats().get("matchTweetOutputAnalyzer") ).getBuffer() );
	    		result.put("notSample", ( (TweetOutputAnalyzer) App.getApp().getStats().get("notTweetOutputAnalyzer") ).getBuffer() );
	    		
	    		result.put("missingTweets", ( (LiveContentGrabber) App.getApp().getStats().get("liveContent")).getMissedTweets() );
    		}
    		
    		return result;
    	}
    	
    }

	public static class TwitterCall implements ApiHandler.ApiCall {

		@Override
		public JSONObject run(HttpExchange t) {
			
			JSONObject result = new JSONObject();
			
    		Twitter twitter = TwitterFactory.getSingleton();
    		try {
				Trends trends = twitter.getPlaceTrends(23424909); // worldwide
				List<String> twitterTrends = new ArrayList<String>();
				
				for(Trend tr : trends.getTrends()) {
					twitterTrends.add(tr.getName());
				}
				
				result.put("trends", twitterTrends);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
    		return result;
		}
		
	}

    public static class LogCall implements ApiHandler.ApiCall {
    	
    	public JSONObject run(HttpExchange t) {
    		JSONObject result = new JSONObject();
    		result.put("date", new Date().toString());
    		result.put("status", App.getApp().getStatus());
    		result.put("logs", App.getApp().getMessages());
        
    		return result;		
    	}
    	
    }
}
