package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

abstract public class ClassifierReader implements Iterator<ClassifierDocument>, Iterable<ClassifierDocument> {

	protected String file;
	
	protected FileInputStream fileStream;
	protected InputStreamReader reader;
	protected BufferedReader read;
	
	public ClassifierReader(String file) {
		this.file = file;
	}
	
	public void init() {
		try {
			fileStream = new FileInputStream(new File(file));
			reader = new InputStreamReader(fileStream, "UTF-8");
			read = new BufferedReader(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(UnsupportedEncodingException e) {
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
	
}
