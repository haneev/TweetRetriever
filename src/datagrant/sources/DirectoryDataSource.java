package datagrant.sources;

import java.io.File;
import java.util.Iterator;
import java.util.Stack;

import org.json.JSONObject;

/**
 * Read tweets from a file into this datasource
 * @author Han
 */
public class DirectoryDataSource extends DataSource implements Iterator<JSONObject> {

	private int fileCounter = 0;
	
	private String directory;
	
	private DataSource activeDataSource;
	
	private Stack<File> filesToBeParsed;
	
	public DirectoryDataSource(String directory) {
		this.directory = directory;
		this.filesToBeParsed = this.walk(directory);
		if (!filesToBeParsed.empty()) {
			this.init();
		}
	}
	
	private void init() {
		String firstFile = filesToBeParsed.pop().getAbsolutePath();
		System.out.println("#"+fileCounter+" start next file "+firstFile);
		activeDataSource = new FileDataSource(firstFile);
	}
	
	public String toString() {
		return this.getClass().getName() + " using files in "+directory;
	}
	
	private Stack<File> walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        Stack<File> filesList = new Stack<File>();
        
        if (list == null) 
        	return filesList;

        for (File f : list ) {
            if (f.isDirectory() ) {
                filesList.addAll(walk(f.getAbsolutePath()));
            } else {
            	filesList.add(f.getAbsoluteFile());
            }
        }
        return filesList;
    }
	
	public void close() {
		if (activeDataSource != null) {
			activeDataSource.close();
		}
	}

	public boolean hasNext() {
		return activeDataSource != null && activeDataSource.hasNext();
	}

	public JSONObject next() {
		JSONObject returnObject = null;
		if (activeDataSource != null) {
			returnObject = activeDataSource.next();
		}
		
		if (!activeDataSource.hasNext() && !this.filesToBeParsed.empty()) {
			fileCounter++;
			activeDataSource.close();
			String nextFile = filesToBeParsed.pop().getAbsolutePath();
			System.out.println("#"+fileCounter+" take next file "+nextFile);
			activeDataSource = new FileDataSource(nextFile);
			
			if (activeDataSource.hasNext() && returnObject == null) {
				returnObject = activeDataSource.next();
			}
		}
		
		return returnObject;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
