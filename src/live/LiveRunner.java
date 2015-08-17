package live;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import validate.OverlapPossibleWords;
import datagrant.ClassifierWords;
import datagrant.sources.DataSource;
import datagrant.sources.FileDataSource;
import datagrant.sources.TweetSearchDataSource;

public class LiveRunner {

	private static final Logger logger = LogManager.getLogger("LiveRunner");
	
	private static int trainingTweetsCount = 3000;
	private static int notTweetsCount = 25000;
	
	public static List<String> getWords(int top, String query, DataSource mf, DataSource nf) {
		
		LiveAdditionalWords w = new LiveAdditionalWords();
		
		//ClassifierDocFreq tf = new ClassifierDocFreq();
		//tf.trainSource(twitterStream, 0, trainingTweetsCount);
		//w.addParser(tf);
		
		ClassifierWords prior = new ClassifierWords();
		prior.trainSource("match", mf, 0, trainingTweetsCount);
		prior.trainSource("normal", nf, 0, notTweetsCount);
		
		// w.addParser(prior);
		
		List<String> wordsToRankSecondary = LiveAdditionalWords.filterWords(prior.getPossibleWords(80));
		
		logger.info("Starting list for overlap {}", wordsToRankSecondary);
		
		OverlapPossibleWords overlap = new OverlapPossibleWords( query, wordsToRankSecondary);
		w.addParser(overlap);	
		
		return w.getWords(top);
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		
//		List<Stoppable> threads = new ArrayList<Stoppable>();
//		
//		logger.info("Started");
//		
//		/**
//		 * Arguments
//		 * 
//		 * -top 20					Use only top 20 of words for streaming
//		 * -mf "file"		
//		 * -nf "file"
//		 * -out "outfile"
//		 * -reevaluate 				Use live reavaluate with new tweets
//		 * -nostreaming
//		 */
//		
//		Options options = new Options();
//		options.addOption(OptionBuilder.withArgName("int").hasArg().withDescription("top n matching words for each method").create("top"));
//	    options.addOption(OptionBuilder.withArgName("path").hasArg().withLongOpt("matchingfile").withDescription("data file with matching tweets").create("mf"));
//	    options.addOption(OptionBuilder.withArgName("path").hasArg().withLongOpt("normalfile").withDescription("data file with normal tweets").create("nf"));
//	    options.addOption(OptionBuilder.withArgName("str").hasArg().withLongOpt("twitterkeywords").withDescription("use twitter keywords").create("twk"));
//	    options.addOption(OptionBuilder.withArgName("path").hasArg().withDescription("matching output file").create("out"));
//	    options.addOption(OptionBuilder.withArgName("path").hasArg().withDescription("not output file").create("nout"));
//	    options.addOption(OptionBuilder.withArgName("int").hasArg().withLongOpt("duration").withDescription("running time in m").create("d"));
//	    
//	    //options.addOption("noc", "noclassifier", false, "disable classification of tweets");
//
//	    CommandLine cmdline = null;
//	    CommandLineParser parser = new GnuParser();
//	    try {
//	      cmdline = parser.parse(options, args);
//	    } catch (ParseException exp) {
//	      logger.error("Error parsing command line: " + exp.getMessage());
//	      System.exit(-1);
//	    }
//	    
//	    if (!cmdline.hasOption("top") || !cmdline.hasOption("nf") || !cmdline.hasOption("out")) {
//	    	HelpFormatter formatter = new HelpFormatter();
//	    	formatter.printHelp(LiveContentGrabber.class.getName(), options);
//	    	System.exit(-1);
//	    }
//
//	    
//	    String normalFile = cmdline.getOptionValue("nf");
//	    
//	    String top = cmdline.getOptionValue("top");
//	    String duration = cmdline.getOptionValue("duration");
//	    
//	    String keyword = cmdline.getOptionValue("twk");
//	    
//	    /* SOURCES */
//	    DataSource nf = new FileDataSource(normalFile);
//	    DataSource mf;
//	    if(cmdline.hasOption("twk")) {
//	    	mf = new TweetSearchDataSource(keyword);
//	    } else {
//	    	mf = new FileDataSource(cmdline.getOptionValue("mf"));
//	    }
//	    
//	    logger.info("Getting matching set and not set");
//	    
//	    DataSource matchingAndRelevantTweets = mf.toArray(trainingTweetsCount);
//	    DataSource notRelevantTweets = nf.toArray(notTweetsCount);
//	    
//	    logger.info("Getting top words");
//	    
//	    List<String> topWords = getWords(Integer.parseInt(top), keyword, matchingAndRelevantTweets, notRelevantTweets);
//	    
//	    /* THREADS */
//	    
//	    LiveContentGrabber liveContent = new LiveContentGrabber(topWords);
//	    threads.add(liveContent);
//		
//	    logger.info("Train live classifier");
//	    
//	    LiveClassifier liveClassifier = new LiveClassifier(liveContent.getQueue(), keyword, topWords);
//	    liveClassifier.trainBySource(matchingAndRelevantTweets, notRelevantTweets);
//	    threads.add(liveClassifier);
//	
//	    logger.info("Set outputs");
//	    
//	    /* Catch not output */
//	    liveClassifier.setCollectNotCases(cmdline.hasOption("nout"));
//	    if(cmdline.hasOption("nout")) {
//	    	LiveWriter notOutputWriter = new LiveWriter(liveClassifier.getNotQueue(), cmdline.getOptionValue("nout"));
//	    	threads.add(notOutputWriter);
//	    }
//	    
//	    /* Catch matching output */
//	    if(cmdline.hasOption("out")) {
//	    	LiveWriter notOutputWriter = new LiveWriter(liveClassifier.getMatchQueue(), cmdline.getOptionValue("out"));
//	    	threads.add(notOutputWriter);
//	    }
//	    
//	    logger.info("Starting threads");
//	
//	    for(Stoppable thread : threads) {
//	    	if(thread instanceof Runnable) {
//	    		new Thread( (Runnable) thread ).start();
//	    	}
//	    }
//	    
//	    logger.info("Starting monitor");
//	    
//	    Thread monitor = new Thread(new LiveContentMonitor(threads, Long.parseLong(duration)));
//	    monitor.start();
//	    
//	    try {
//			monitor.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}	    
//	    
//	    logger.info("Stopped");
	}
	
}

