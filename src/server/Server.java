package server;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpServer;

import live.LiveMonitor;

public class Server {

	public static final Logger logger = LogManager.getLogger("TweetRetrieverServer");
	
	public final static int PORT = 8080; 
	public final static String WEBROOT = "www/";
	
	public static void main(String[] args) throws Exception {
		
		logger.trace("Start server on port {}", PORT);
		
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/index", new FileHandler());
        server.createContext("/api", new ApiHandler());
        server.setExecutor(null); 
        server.start();
        
        logger.trace("Server started");
	}

	
}
