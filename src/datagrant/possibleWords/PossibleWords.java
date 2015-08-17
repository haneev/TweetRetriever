package datagrant.possibleWords;

import java.util.List;

/**
 * Possible additional keywords interface
 * @author Han
 *
 */
public interface PossibleWords {

	public List<String> getPossibleWords(int top);
	
	public double getPossibleWordScore(String term);
	
}
