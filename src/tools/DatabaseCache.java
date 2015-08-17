package tools;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DatabaseCache extends Cache {

	private Connection db;
	
	private PreparedStatement selectCache;
	
	private PreparedStatement putCache;
	
	private Map<String, String> memoryCache;
	
	public DatabaseCache(String connection) throws ClassNotFoundException, SQLException {
		
		// This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      
      // Setup the connection with the DB
      db = DriverManager.getConnection(connection);	
      
      selectCache = db.prepareStatement("SELECT value FROM cache WHERE type = ? AND k = ?");
      putCache = db.prepareStatement("INSERT INTO cache (type, k, value) VALUES (?,?,?)");
      
      memoryCache = new HashMap<String, String>();
	}
	
	public void close() {
		try {
			selectCache.close();
			putCache.close();
			db.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean putCache(String type, String key, String value) {
		try {
			
			putCache.setString(1, type);
			putCache.setString(2, key);

			String storeValue = value == null ? Cache.NOT_FOUND : value;
			
			putCache.setString(3, storeValue);
			
			return putCache.execute();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String readCache(String type, String key) {
		
		try {
			String cacheKey = type+"_"+key;
			
			// get from memory, when available
			if(memoryCache.containsKey(cacheKey)) {
				return memoryCache.get(cacheKey);
			}
			
			selectCache.setString(1, type);
			selectCache.setString(2, key);
			ResultSet result = selectCache.executeQuery();
			
			String cacheResult = null;
			if(result.first()) {
				cacheResult = result.getString("value");
			}
			
			result.close();
			
			// store in memory
			if(cacheResult != null)
				memoryCache.put(cacheKey, cacheResult);
			
			return cacheResult;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void put(String key, String value) {
		// TODO Auto-generated method stub
		this.putCache("cache", key, value);
	}

	@Override
	public String get(String key) {
		return this.readCache("cache", key);
	}

	public static void main(String[] args) {
		
		try {
			
			DatabaseCache cache = new DatabaseCache("jdbc:mysql://192.168.56.104/datagrant?user=han&password=han");
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
