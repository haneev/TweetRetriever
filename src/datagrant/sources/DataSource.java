package datagrant.sources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

abstract public class DataSource implements Iterator<JSONObject>, Cloneable, Iterable<JSONObject> {
	abstract public void close();
	
	public static final Logger logger = LogManager.getLogger("DataSource");
	
	public Iterator<JSONObject> iterator() {
		return this;
	}
	
	public ArrayDataSource toArray(int until) {
		
		List<JSONObject> list = new ArrayList<JSONObject>();
		int i = 0;
		
		for(JSONObject doc : this) {
			list.add(doc);
			
			if(i % 500 == 0)
				logger.info("progress {}", i);
			
			if(++i >= until)
				break;
			
		}
		
		return new ArrayDataSource(list);
	}
	
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public void rewind() {
		
	}
}
