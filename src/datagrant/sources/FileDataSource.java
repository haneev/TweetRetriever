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
		try {
			this.currentLine = read.readLine();
			
			return this.currentLine != null;
		} catch (IOException e) {
			return false;
		}
	}

	public JSONObject next() {
		
		if(this.currentLine == null) {
			try {
				this.currentLine = read.readLine();
			} catch (IOException e) {}
		}
		try {
			//System.out.println("Read line "+i+" " + file);
			return JSON.parse(this.currentLine);
		} catch(JSONException e) {
			System.err.println("Error in parsing json string");
			return null;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
