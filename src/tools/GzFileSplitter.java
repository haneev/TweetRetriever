package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzFileSplitter {

	public static final int SPLIT_COUNT = 1000;
	
	public static final String SPLIT_SUFFIX = "_part";
	
	private String file;
	
	public GzFileSplitter(String inputFile) {
		this.file = inputFile;
	}
	
	public Writer getNewWriter(String file) {
		try {
			return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void split(String outputDir, String outputFile) {
		
		int splitCount = 0;
		
		System.out.println("Start splitting "+file);
		
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(file))), "UTF-8"));

			String filename = outputDir + "/" + outputFile+SPLIT_SUFFIX+splitCount+".gz";
			Writer writer = getNewWriter(filename);
			
			String line;
			int i = 0;
			while((line = read.readLine()) != null) {
				
				if(i < SPLIT_COUNT) {
					writer.write(line+"\n");
				} else {
					splitCount++;
					System.out.println("Splitted to "+splitCount);
					
					writer.close();
					writer = getNewWriter(outputDir + "/" + outputFile+SPLIT_SUFFIX+splitCount+".gz");
					
					i = 0;
				}
				
				i++;
			}
			
			System.out.println("End splitting "+file);
			
			read.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
