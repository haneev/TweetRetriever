package validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import tools.JsonReader;

/**
 * Estimate Tweet Count
 * This function is called Amount in the thesis
 * 
 * @author Han
 */
public class TweetCount {

	private String keyword;
	
	private String[] dates;
	
	public TweetCount(String keyword) {
		this.keyword = keyword;
		
		this.dates = new String[] {			
			"2014-03-15=2014-03-17",
		};
	}
	
	public void setDates(String[] dates) {
		this.dates = dates;
	}
	
	private String readPage(URI uri) {
		String finalHTML = "";
		try {
			
			URLConnection spoof = uri.toURL().openConnection();
			spoof.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36" );
			BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));

			String strLine = "";
			
			while ((strLine = in.readLine()) != null){
			   finalHTML += strLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return finalHTML;
	}
	
	/**
	 * Get amount of tweets this keyword gives as a result
	 * @return tweets for keyword
	 */
	public long getEstimatedTweetCount() {
		
		double total_tweets = 0;
		for(String date_range : dates) {
			double density = this.getDensity(this.getTweetCountByDate(date_range.split("\\=")[0], date_range.split("\\=")[1]));
			
			if(density > 0)
				total_tweets += (15.0 * 24.0 * 3600.0) / density;
		}
		
		return new Double(total_tweets).longValue();
	}
	
	public double getDensity(List<Date> tweetDates) {

		double total = 0;
		double total_sum = 0;
		long previous_created_at = 0;
		long start_date = 0;
		
		for(Date created_at : tweetDates) {				
			
			if(previous_created_at != 0)
				total_sum += (previous_created_at - created_at.getTime());
			else
				start_date = created_at.getTime();
			
			previous_created_at = created_at.getTime();
			
			total++;				
		}
		
		double density = 0.0;
		
		if(total_sum == 0 && total > 1)
			density = 60.0 / total; // everthing is in a minute, we assume it is evenly distributed in that minute...
		else if(total_sum == 0 && total == 0)
			density = 0;
		else 
			density = total_sum / total / 1000;
		
		return density;
	}
	
	private Date parseDate(String date) {
		date = date.replace("okt", "oct").replace("mrt", "mar").replace("mei", "may.");
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd MMM. yyyy", Locale.forLanguageTag("nl_NL"));
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Read a page and parse the dates into a list of Posting Tweet dates
	 * 
	 * @param beginDate used in since:
	 * @param endDate used in until:
	 * @return list of dates of tweets 
	 */
	private List<Date> getTweetCountByDate(String beginDate, String endDate) {
		
		List<Date> return_dates = new ArrayList<Date>();
		
		String pattern = "title=\"(\\d+:\\d+\\s+\\-\\s+\\d+\\s+[a-zA-Z]+\\.?\\s+\\d+)\"";
		
		try {
			
			String queryString = "q="+keyword+" since:"+beginDate+" until:"+endDate+"&src=sprv&f=realtime";
			
			URI uri = new URI("https", null, "twitter.com", -1, "/search", queryString, null);
			
			String html = readPage(uri);
			
			Matcher m = Pattern.compile(pattern).matcher(html);
			
			while (m.find()) {
				String result = m.group();
				result = result.substring(7, result.length() - 1);
				
				Date date = parseDate(result);
				if(date != null)
					return_dates.add(date);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return return_dates;
	}
	
	public static void main(String[] args) {
		
		TweetCount tw = new TweetCount("-#moustache AND #movember");
		System.out.println(tw.getEstimatedTweetCount());
		
	}
	
}
