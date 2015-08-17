package live;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.json.JSONObject;

public class TweetOutputAnalyzer extends InputOutputQueue<JSONObject> implements Runnable, Stoppable {

	private CircularFifoBuffer buffer;
	
	private volatile boolean running = true;
	
	public TweetOutputAnalyzer(Queue<JSONObject> in) {
		super(in);
		this.buffer = new CircularFifoBuffer(32);
	}
	
	public List<JSONObject> getBuffer() {
		List<JSONObject> list = new ArrayList<JSONObject>();
		
		for(Object obj : buffer) {
			list.add( (JSONObject) obj);
		}
		
		return list;
	}

	@Override
	public void stop() {
		this.running = false;
		
		for(Object obj : this.buffer) {
			this.out.add((JSONObject) obj);
		}
		
		this.buffer.clear();
	}

	@Override
	public void run() {

		while(running) {
			
			JSONObject current = this.getInputQueue().poll();			
			if(current != null) {
				
				this.buffer.add(current);
				
				if(this.buffer.isFull()) {
					this.out.offer( (JSONObject) this.buffer.get());
					
					if(!this.buffer.isEmpty())
						this.buffer.remove();
				}
				
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
