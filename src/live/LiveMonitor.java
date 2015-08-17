package live;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Monitors all threads and is able to start and stop all threads
 * @author Han
 *
 */
public class LiveMonitor implements Runnable, Stoppable {

	private static final Logger logger = LogManager.getLogger("LiveContentMonitor");
	
	private List<Stoppable> threads;
	
	private long runningTime = 60;
	
	private boolean running = true;
	
	private Thread thisThread;
	
	public synchronized void setRunning(boolean running) {
		this.running = running;
	}
	public synchronized boolean getRunning() {
		return this.running;
	}
	
	public LiveMonitor(Long maxRunningTime) {
		this.threads = new ArrayList<Stoppable>();
		
		if(maxRunningTime < 10 || maxRunningTime == null) {
			maxRunningTime = new Long(10);
		}
		
		this.runningTime = maxRunningTime;
	}
	
	public void addThread(Stoppable thread) {
		this.threads.add(thread);
	}
	
	public void start() {
		
		logger.info("Starting threads");
		
		for (Stoppable thread : threads) {
			if (thread instanceof Runnable) {
				new Thread((Runnable) thread).start();
			}
		}
		
		logger.info("Starting monitor");
		
		this.thisThread = new Thread(this);
		this.thisThread.start();
	}
	
	public void stop() {
		setRunning(false);
		stopThreads();
	}
	
	public void reset() {
		this.threads.clear();
	}
	
	public void waitForFinish(){
	    try {
			this.thisThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void stopThreads() {
		
		for(Stoppable s : threads) {
			logger.info("Stopping "+s.getClass().getName());
			s.stop();
		}
		
	}
	
	public void run() {
		
		logger.trace("Started monitor");
		
		long maxRunningTime = runningTime*60*1000;
		
		long startTime = System.currentTimeMillis();
		
		while(getRunning()) {
			try {
				Thread.sleep(1000);				
			} catch (InterruptedException e) {}
			
			if((startTime + maxRunningTime) < System.currentTimeMillis()) {
				
				this.stop();
				
				logger.info("Content grabber max execution time reached");
				
			}
		}
		
		logger.trace("Stopped monitor");
	}

	
	
}
