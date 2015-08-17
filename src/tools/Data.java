package tools;

public class Data {

	public static boolean containsToken(String text, String tokens[]) {
		
		boolean found = false;
		
		for(String token : tokens) {
			found = containsToken(text, token);
			
			if(found)
				break;
		}
		
		return found;
	}
	
	public static boolean containsToken(String text, String token) {
		
		if(!text.toLowerCase().contains(token))
			return false;
		
		String regex = "[\\040|\\r|\\n|\\t|\\.|\\,|\\;|\\:|\\\"|\\'|(|)|\\?|\\!]";
		
		String[] splits = text.toLowerCase().split(regex);
		for(String t : splits) {
			if(t.equals(token)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println(Data.containsToken("hoi ik ben han;fiets", "asdfsd"));
		
	}
	
}
