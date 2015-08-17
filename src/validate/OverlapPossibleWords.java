package validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datagrant.possibleWords.PossibleWords;

/**
 * Class that implements PossibleWords 
 * And uses TweetCount over the past 6 months
 * 
 * TODO: Months are static now
 * 
 * @author Han
 */
public class OverlapPossibleWords implements PossibleWords {

	private static final Logger logger = LogManager.getLogger("OverlapPossibleWords");
	
	private List<String> words;
	
	private String q;
	
	public OverlapPossibleWords(String originalQuery, List<String> wordsToRank) {
		this.words = wordsToRank;
		this.q = originalQuery;
	}
	
	private long getCount(String query) {
		TweetCount c = new TweetCount(query);
		
		c.setDates(new String[] {
			"2015-02-01=2015-02-03",
			"2015-03-01=2015-03-03",
			"2015-04-01=2015-04-03",
			"2015-05-15=2015-05-17",
			"2015-06-15=2015-06-17",
			"2015-07-15=2015-07-17"}
		);
		
		return c.getEstimatedTweetCount();
	}
	
	private List<String> sortPossibleWords(Map<String, Double> inputList) {
		
		List<String> returningWords = new ArrayList<String>();
		
		// sort the new list
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>( inputList.entrySet() );
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
            {
                return - o1.getValue().compareTo(o2.getValue());
            }
        });
		
		for(Map.Entry<String, Double> word : list) {
			returningWords.add(word.getKey());
		}
		
		return returningWords;
	}
	
	
	@Override
	public List<String> getPossibleWords(int top) {
		
		Map<String, Double> scores = new HashMap<String, Double>();
		
		int noScoreCount = 0;
		for(String word : words) {
			double score = getPossibleWordScore(word);
			if(score > 0)
				scores.put(word, score);
			else
				noScoreCount++;
		}
		
		// more than 50% have no score, then use the original prio ranking
		if(noScoreCount > (words.size() / 2)) {
			return words.size() < top ? words : words.subList(0, top);
		} else {
			List<String> list = sortPossibleWords(scores);
			return list.size() < top ? list : list.subList(0, top);
		}
	}

	@Override
	public double getPossibleWordScore(String term) {
		
		long q_and_w = getCount(this.q + " AND " + term);
		long w = getCount(term);
		
		if(w == 0) 
			w = 1;
		
		logger.info(this.q + " AND "+term + " = " + q_and_w+ "; "+term+" = "+w);	
		
		if(q_and_w > w && w > 0 && q_and_w > 1) {
			return 1.0;
		} else
			return new Double(q_and_w) / new Double(w);
	}

	public static void main(String[] args) {

		List<String> w = new ArrayList<String>();

		w.add("petrucci");
		OverlapPossibleWords p = new OverlapPossibleWords("(#motogp OR #frenchgp)", w);
		
		for(String word : p.getPossibleWords(13)) {
			System.out.println(word);
		}
		
	}

}
