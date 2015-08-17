package live;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import validate.OverlapPossibleWords;
import datagrant.ClassifierDocFreq;
import datagrant.ClassifierWords;
import datagrant.possibleWords.PossibleWords;
import datagrant.sources.DataSource;
import datagrant.sources.FileDataSource;
import datagrant.sources.TweetSearchDataSource;

public class LiveAdditionalWords {
	
	private static final Logger logger = LogManager.getLogger("LiveAdditionalWords");

	private List<PossibleWords> parsers;
	
	private static List<String> stopwords;
	
	public LiveAdditionalWords() {
		this.parsers = new ArrayList<PossibleWords>();
		
		stopwords = new ArrayList<String>();
		stopwords.add("the");
		stopwords.add("de");
		stopwords.add("het");
		stopwords.add("een");
		stopwords.add("a");
		stopwords.add("http");
		stopwords.add("rt");
		stopwords.add("to");
		stopwords.add("in");
		stopwords.add("is");
		stopwords.add("of");
		stopwords.add("and");
		stopwords.add("am");
		stopwords.add("are");
		stopwords.add("as");
		stopwords.add("at");
		stopwords.add("be");
		stopwords.add("i");
		stopwords.add("he");
		stopwords.add("me");
		stopwords.add("on");
		stopwords.add("en");
		stopwords.add("for");
		stopwords.add("-");
		stopwords.add("le");
		stopwords.add("la");
		stopwords.add("1");
		stopwords.add("2");
		stopwords.add("3");
		stopwords.add("4");
		stopwords.add("5");
		stopwords.add("6");
		stopwords.add("7");
		stopwords.add("8");
		stopwords.add("9");
		
	}
	
	public void addParser(PossibleWords w) {
		this.parsers.add(w);
	}
	
	private static String toAscii(String w) {
		w = Normalizer.normalize(w, Normalizer.Form.NFD);	
		return w.replaceAll("[^\\x00-\\x7F]", "");
	}
	
	public static List<String> filterWords(List<String> unfilteredWords) {
		
		List<String> newWords = new ArrayList<String>();
		
		for(int i = 0; i < unfilteredWords.size(); i++) {
			
			String word = unfilteredWords.get(i);
			
			word = toAscii(word);
			
			if(word.startsWith("@") || word.startsWith("co/") || word.startsWith("?") || word.startsWith("/") || stopwords.contains(word) || word.isEmpty() || word.length() < 2) {
				continue;
			}
			
			newWords.add(word);
		}
		
		return newWords;
	}
	
	public List<String> getWords(int wordsPerParser) {
		
		Set<String> words = new HashSet<String>();
		
		for(PossibleWords p : this.parsers) {
			List<String> beforeFilter = p.getPossibleWords(wordsPerParser*3);
			logger.info("Parser {} before filter {}", p.getClass().getName(), beforeFilter);
			List<String> possible = filterWords(beforeFilter);
			logger.info("Parser {} returns {}", p.getClass().getName(), possible);
			
			List<String> perParser = possible.size() < wordsPerParser ? possible : possible.subList(0, wordsPerParser);
			logger.info("Add to parser {}", perParser);
			words.addAll(perParser);
		}
		
		logger.info("Words to be parsed are {}", words);
		
		return new ArrayList<String>(words);
	}
	
	public static void main(String[] args) {
		
		logger.info("Start");
		
		LiveAdditionalWords w = new LiveAdditionalWords();
		
		String q = "#gameofthrones";
		String q_small = "#gameofthrones";
		//DataSource twitterStream = new FileDataSource("../data/out_movember_small.json");
		//DataSource twitterStream2 = new FileDataSource("../data/out_movember_small.json");
		
		DataSource twitterStream = new TweetSearchDataSource(q);
		DataSource twitterStream2 = new TweetSearchDataSource(q);
		
		ClassifierDocFreq tf = new ClassifierDocFreq();
		tf.trainSource(twitterStream, 0, 5000);
		
		w.addParser(tf);
		
		ClassifierWords prior = new ClassifierWords();
		prior.trainSource("match", twitterStream2, 0, 5000);
		prior.trainFile("normal", "../data/out_en_nl_es_fr_de.json", 0, 15000);
		
		w.addParser(prior);
		
		List<String> wordsToRankSecondary = LiveAdditionalWords.filterWords(prior.getPossibleWords(75));
		
		logger.info("Starting list for overlap {}", wordsToRankSecondary);
		
		OverlapPossibleWords overlap = new OverlapPossibleWords(q_small, wordsToRankSecondary);
		
		w.addParser(overlap);
		
		logger.info("words {}", w.getWords(30));
	}
	
	
}
