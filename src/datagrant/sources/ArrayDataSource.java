package datagrant.sources;

import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

/**
 * Converts a List of JSONObjects into a datasource
 * @author Han
 *
 */
public class ArrayDataSource extends DataSource {

	private List<JSONObject> list;
	
	private int count = 0;
	
	public ArrayDataSource(List<JSONObject> list) {
		this.list = list;
	}
	
	@Override 
	public Iterator<JSONObject> iterator() {
		count = 0;
		return super.iterator();
	}
	
	@Override 
	public void rewind() {
		count = 0;
	}
	
	public boolean hasNext() {
		return list.size() > (count + 1) && list.get(count + 1) != null;
	}

	public JSONObject next() {
		return list.get(++count);
	}

	public void remove() {
	}

	@Override
	public void close() {
	}
	
}
