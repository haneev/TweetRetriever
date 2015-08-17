package datagrant;

import tools.ClassifierDocument;
import tools.Data;
import tools.TweetReader;

public class TweetEval {

	public static void main(String[] args) {
		
		TweetReader reader = new TweetReader(args[0]);
		
		String[] ignoreKeywords = args[1].split(";");
		
		int total = 200;
		
		int i = 0;
		int match = 0;
		for(ClassifierDocument doc : reader) {
			i++;
			
			if(!Data.containsToken(doc.getText(), ignoreKeywords) && Data.containsToken(doc.getText(), args[2])) {
			
				System.out.println(match + ";\""+doc.getText().replaceAll("\"","").replaceAll("\n","")+"\"");
				
				match++;
			}
			
			if(match > total)
				break;
			
			
		}
		System.out.println("total = " + i);
		
	}
	
}
