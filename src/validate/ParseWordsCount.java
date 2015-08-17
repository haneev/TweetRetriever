package validate;

public class ParseWordsCount {

	public static void main(String[] args) {
		int i = 0;
		for(String keyword : args) {
			i++;
			if(keyword.substring(0, 1).equals("@") || (keyword.length() > 2 && keyword.substring(0, 2).equals("#?")) || (keyword.length() > 3 && keyword.substring(0, 3).equals("co/")) || keyword.substring(0,1).equals("?")) {
				System.out.println(i+";"+keyword+";0");
			} else {
				TweetCount tw = new TweetCount("movember AND "+keyword.trim());
				System.out.println(i+";"+keyword+";"+tw.getEstimatedTweetCount());
			}
		}
	
	}

}
