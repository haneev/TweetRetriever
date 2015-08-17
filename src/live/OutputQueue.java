package live;

import java.util.Queue;

/**
 * Has output queue
 * @author Han
 *
 * @param <T>
 */
public interface OutputQueue<T> {

	public Queue<T> getOutputQueue();
	
}

