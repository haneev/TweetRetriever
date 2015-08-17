package live;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

public class DummyTweetRateGenerator implements Runnable, Stoppable, OutputQueue<JSONObject> {

	private Queue<JSONObject> out;
	
	private int delay = 100; // in ms
	
	private volatile boolean running = true;
	
	public DummyTweetRateGenerator(int delay) {
		this.out = new ConcurrentLinkedQueue<JSONObject>();
		this.delay = delay;
	}
	
	@Override
	public Queue<JSONObject> getOutputQueue() {
		return out;
	}

	@Override
	public void stop() {
		this.running = false;
	}

	@Override
	public void run() {
		
		while(running) {
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			out.add(new JSONObject());
		}
		
	}

}
