package live;

import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import tools.TweetWriter;

/**
 * Writes a stream into a file
 * @author Han
 */
public class LiveWriter implements Runnable, Stoppable, InputQueue<JSONObject> {

	private static final Logger logger = LogManager.getLogger("LiveWriter");
	
	private boolean running = true;
	
	private Queue<JSONObject> q;
	
	private TweetWriter writer;
	
	public LiveWriter(Queue<JSONObject> q, String file) {
		this.q = q;
		this.writer = new TweetWriter(file);
	}
	
	public void setInputQueue(Queue<JSONObject> q) {
		this.q = q;
	}
	
	public void stop() {
		running = false;
		writer.close();
	}
	
	public void run() {
		
		logger.trace("Starting");
		
		int i = 0;
		
		JSONObject doc;
		while(running) {
			
			doc = this.q.poll();
			if(doc != null) {
				writer.write(doc);
				
				if(++i % 100 == 0)
					logger.trace("Wrote tweet nr {}", i);
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		logger.trace("Stopped");
	}
}
