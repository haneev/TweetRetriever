package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.json.JSONObject;

import tools.JSON;

public class TweetReader extends ClassifierReader {

	private String currentLine;	
	
	public static int missedId = 0;
	
	public TweetReader(String file) {
		super(file);
		
		init();
	}
	
	@Override
	public void init() {
		try {
			
			fileStream = new FileInputStream(new File(file));
			GZIPInputStream gzip = new GZIPInputStream(fileStream, 65536*2);
			reader = new InputStreamReader(gzip, "UTF-8");
			read = new BufferedReader(reader);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		try {
			this.currentLine = read.readLine();
			
			return this.currentLine != null;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public ClassifierDocument next() {
		
		if(this.currentLine == null)
			return null;
		
		JSONObject json = JSON.parse(this.currentLine);
		String text = json.optString("text");
		ClassifierDocument doc = new ClassifierDocument(text);
		doc.setJson(json);
		
		return doc;
	}

	@Override
	public void remove() {}

	@Override
	public Iterator<ClassifierDocument> iterator() {
		return this;
	}
	
	public static void main(String[] args) {
	
		TweetReader read = new TweetReader("../data/trec/tweets2011.trec.gz");
		
		int i = 0;
		while(read.hasNext()) {
			
			ClassifierDocument doc = read.next();
			
			if(doc == null) {
				continue;
			}
			
			System.out.println(doc);
			
			i++;
			
			if(i > 2000)
				break;
		}	
		
	}
}
