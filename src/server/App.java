package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.print.attribute.standard.Finishings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import appender.ApiLogAppenderImpl;
import live.Callback;
import live.CallbackExecutor;
import live.LiveMonitor;

/**
 * TweetRetriever core class
 * @author Han
 *
 */
public class App {

	public final static String STOPPED = "stopped";
	public final static String STARTING = "starting";
	public final static String RUNNING = "running";
	public final static String PAUSED = "paused";
	
	public static final Logger logger = LogManager.getLogger("App");
	
	private static final long MAX_DURATION = 120;
	
	private static App app;
	
	private LiveMonitor monitor;
	
	private String status = STOPPED;
	
	private Map<String, Object> stats;
	
	public static App getApp() {
		
		if(app == null)
			app = new App();
		
		return app;
	}
	
	public App() {
		monitor = new LiveMonitor(MAX_DURATION);
		stats = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getStats() {
		return stats;
	}
	
	public List<String> getMessages() {
		
		List<String> messages = new ArrayList<String>();
		
		if(ApiLogAppenderImpl.messageBuffer != null) {
			
			for(Object o : ApiLogAppenderImpl.messageBuffer) {
				messages.add((String) o);
			}
		}
		
		return messages;
		
	}
	
	public LiveMonitor getMonitor() {
		return monitor;
	}
	
	public void start(String keyword, int top, int trainingTweets) {
		
		logger.info("Starting with {}, top {}, training {}", keyword, top, trainingTweets);
		
		this.getStats().put("keyword", keyword);
		
		this.status = STARTING;
		
		logger.info("init app");
		Retriever retrieverConfig = new Retriever(monitor, keyword, top, trainingTweets, new Callback() {
			@Override
			public void callback() {
				logger.info("init end");
				finishInitialization();
			}
		});
		
		retrieverConfig.setFastMode(false);
		
		Executor executor = new CallbackExecutor();
		executor.execute(retrieverConfig);
	}
	
	private void finishInitialization() {
		this.status = App.RUNNING;		
		monitor.start();
	}
	
	public String getStatus() {
		return status;
	}
	
	public void stop() {
		monitor.stop();
		logger.info("app stopped");
		this.status = STOPPED;
		
		monitor.reset();
	}
	
}
