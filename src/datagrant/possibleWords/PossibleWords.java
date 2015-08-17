package datagrant.possibleWords;

import java.util.List;

public interface PossibleWords {

	public List<String> getPossibleWords(int top);
	
	public double getPossibleWordScore(String term);
	
}
