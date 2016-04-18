package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class FileCache extends Cache {
	
	private Map<String, String> cache;
	
	public FileCache(String file) {
		this.cache = new HashMap<String, String>();
		this.loadCache(file);
	}
	
	private void loadCache(String file)  {
		FileInputStream fileStream = null;
		InputStreamReader reader = null;
		BufferedReader read = null;
		try {
			fileStream = new FileInputStream(new File(file));
			reader = new InputStreamReader(fileStream, "UTF-8");
			read = new BufferedReader(reader);
			
			String line;
			int i = 0;
			while((line = read.readLine()) != null) {
				i++;
				
				if (i % 1000 == 0) {
					System.out.println("Loading geo cache "+i);
				}
				// format = Coordinate (lat,lon);Country
				
				String[] parts = line.split(";");
				
				if (parts.length == 2 && parts[0].length() > 2 && parts[1].length() > 2 && parts[1] != "\""+Cache.NOT_FOUND+"\"") {
					String coordinate = parts[0].substring(1, parts[0].length() - 1);
					String country = parts[1].substring(1, parts[1].length() - 1);
					if (CoordinateConvert.isCoordinate(coordinate) && !country.equals(Cache.NOT_FOUND)) {
						cache.put(CoordinateConvert.toStrippedCoordinates(coordinate), CountryCode.getCountry(country));
					}
				}
				
				
			}
			
			fileStream.close();
			reader.close();
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void put(String key, String value) {
		cache.put(CoordinateConvert.toStrippedCoordinates(key), value);
	}

	@Override
	public String get(String key) {
		return cache.get(CoordinateConvert.toStrippedCoordinates(key));
	}
	
	public void writeNewCacheFile(String file) {
		try {
			FileOutputStream fileStream = new FileOutputStream(new File(file));
			OutputStreamWriter reader = new OutputStreamWriter(fileStream, "UTF-8");
			BufferedWriter writer = new BufferedWriter(reader);
			
			for(Map.Entry<String, String> entry : cache.entrySet()) {
				writer.write("\""+entry.getKey()+"\";\""+entry.getValue()+"\"\n");
			}
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		FileCache cache = new FileCache("../data/utwente2015-cache/coordinate-cache.csv");
		System.out.println(cache.get("-6.91084175%2c107.56422175"));
		System.out.println(cache.get("55.92856744N%2C4.03762701W"));
		System.out.println(cache.get("57.11234N,12.31234E"));
		
		cache.writeNewCacheFile("../data/utwente2015-cache/parsed-3-coordinate-cache.csv");
	}
	
}
