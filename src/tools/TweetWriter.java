package tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.json.JSONObject;

public class TweetWriter {

	private String file;
	
	private BufferedWriter writer;
	
	public TweetWriter(String file) {
		this.file = file;
		
		open();
	}
	
	private void open() {
		
		try {
			FileOutputStream fileStream = new FileOutputStream(new File(file));
			GZIPOutputStream gzip = new GZIPOutputStream(fileStream);
			OutputStreamWriter reader = new OutputStreamWriter(gzip, "UTF-8");
			writer = new BufferedWriter(reader);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(JSONObject json) {
		if(writer != null) {
			try {
				writer.write(json.toString()+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		if(writer != null) {
			
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
