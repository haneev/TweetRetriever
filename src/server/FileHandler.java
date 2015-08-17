package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
    	
    	String requestedUrl = t.getRequestURI().toString().substring(7);
    	
    	if(requestedUrl.isEmpty())
    		requestedUrl = "index.html";
    	
    	File requestedFile = new File(Server.WEBROOT + requestedUrl);
    	
    	Server.logger.info("Serving {}", requestedFile);
    	
    	if(requestedFile.exists()) {        		
    		
    		FileInputStream fileStream = new FileInputStream(requestedFile);
    		
    		byte[] buffer = new byte[4096];
    		
            int read = 0;
            
            t.sendResponseHeaders(200, requestedFile.length());
            OutputStream os = t.getResponseBody();
            while ( (read = fileStream.read(buffer)) != -1 ) {
                os.write(buffer, 0, read);
            }
            
            fileStream.close();
            os.close();
            
    	} else {
    		t.sendResponseHeaders(404, 0);
    		t.getResponseBody().close();
    	}
    	      
    }
}
