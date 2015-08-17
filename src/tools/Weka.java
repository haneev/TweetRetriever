package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONObject;

public class Weka {

	public static void write(InputStreamReader in, OutputStreamWriter writer) throws JSONException, IOException {
		String line;
		int i = 0;
		
		BufferedReader reader = new BufferedReader(in);
		
		System.out.println("Start");
		
		while((line = reader.readLine()) != null) {
			try {
				JSONObject json = JSON.parse(line);
				String tweet = json.getString("text").replace("'", "").replace("\n","").replace("\t","").replace("\r","");
				String type = "not";
				writer.write(type+",'"+tweet+"'\n");
				
				System.out.println(i+" Writing " + tweet);
			} catch(JSONException e) {}
			
			i++;
		}
		System.out.println("End");
	}
	
	public static void convert(String jsonFile, String wekaFile) {
		try {
			FileInputStream fileStream = new FileInputStream(new File(jsonFile));
			InputStreamReader reader = new InputStreamReader(fileStream, "UTF-8");
			
			FileOutputStream fileStream2 = new FileOutputStream(new File(wekaFile), true);
	    	OutputStreamWriter writer = new OutputStreamWriter(fileStream2, "UTF-8");
			
			writer.write("@relation 'whatever'\n");
			writer.write("@attribute @@class@@ {movember,not}\n");
			writer.write("@attribute text string\n");
			writer.write("@data\n");
			
			write(reader, writer);
			
			writer.close();
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
