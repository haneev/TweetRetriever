package live;

import java.util.Queue;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.json.JSONObject;

/**
 * Analyzes a stream and returns the amount of tweets per second
 * @author Han
 *
 */
public class PerSecAnalyzer extends InputOutputQueue<JSONObject> implements Stoppable, Runnable {

	private CircularFifoBuffer timings;
	
	private int capacity = 32;
	
	private volatile boolean running = true;
	
	private long number_of_tweets_passed = 0;
	
	private Long lastAdded;
	
	public PerSecAnalyzer(Queue<JSONObject> in) {
		super(in);
		this.timings = new CircularFifoBuffer(capacity);
	}
	
	public double getTweetsPerSec() {
		
		if(this.timings.isEmpty()) 
			return 0.0;
		
		Long firstTiming = (Long) this.timings.get();
		
		if(firstTiming != null) {
			return 1000 / (new Double(lastAdded - firstTiming) / new Double(this.timings.size()));
		} else
			return 0.0;
		
	}
	
	public long getPassedTweetsCount() {
		return number_of_tweets_passed;
	}
	
	@Override
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		
		while(running) {
			
			JSONObject current = this.in.poll();			
			if(current != null) {
				this.lastAdded = new Long(System.currentTimeMillis());
				this.timings.add(new Long(System.currentTimeMillis()));
				this.out.offer(current);
				
				this.number_of_tweets_passed++;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		DummyTweetRateGenerator out = new DummyTweetRateGenerator(10); // 5 per sec
		PerSecAnalyzer an = new PerSecAnalyzer(out.getOutputQueue());
		
		new Thread(out).start();
		new Thread(an).start();
		
	}

}
