package tools;

abstract public class Cache {

	public final static String NOT_FOUND = "__NOT_FOUND"; 
	
	abstract public void put(String key, String value);
	
	abstract public String get(String key);
	
}
