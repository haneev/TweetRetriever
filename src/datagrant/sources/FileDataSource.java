package datagrant.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import tools.JSON;

/**
 * Read tweets from a file into this datasource
 * @author Han
 *
 */
public class FileDataSource extends DataSource implements Iterator<JSONObject> {

	private String file;
	
	private FileInputStream fileStream;
	private InputStreamReader reader;
	private BufferedReader read;
	
	private String currentLine;
	
	private boolean compressed = false;
	
	public FileDataSource(String file) {
		this.file = file;
		
		compressed = file.endsWith(".gz");
		
		this.init();
	}
	
	public String toString() {
		return this.getClass().getName() + " using "+file;
	}
	
	private void init() {
		try {			
			fileStream = new FileInputStream(new File(file));
			
			InputStream stream;
			
			if(compressed)
				stream = new GZIPInputStream(fileStream);
			else
				stream = fileStream;
			
			reader = new InputStreamReader(stream, "UTF-8");
			read = new BufferedReader(reader);
			
			this.currentLine = read.readLine();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			fileStream.close();
			reader.close();
			read.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasNext() {
		return this.currentLine != null;
	}

	public JSONObject next() {
		
		JSONObject returnObject = null;
		
		try {
			if (!this.currentLine.isEmpty()) {
				returnObject = JSON.parse(this.currentLine);
			}
			
			this.currentLine = read.readLine();
		} catch (JSONException e) {
			
		} catch (IOException e) {
			
		}
		
		return returnObject;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
