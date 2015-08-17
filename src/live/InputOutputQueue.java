package live;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class that handles the input and output queue
 * @author Han
 *
 * @param <T>
 */
public class InputOutputQueue<T> implements InputQueue<T>, OutputQueue<T> {

	protected Queue<T> out;
	protected Queue<T> in;
	
	public InputOutputQueue(Queue<T> in) {
		this.in = in;
		this.out = new ConcurrentLinkedQueue<T>();
	}
	
	public Queue<T> getInputQueue() {
		return this.in;
	}
	
	@Override
	public Queue<T> getOutputQueue() {
		return this.out;
	}

	@Override
	public void setInputQueue(Queue<T> q) {
		this.in = q;
	}

}
