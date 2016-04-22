package tweet;

import org.json.JSONObject;

public interface ClassifyableTweet {

	public boolean isValidFormatForThisClass();
	
	public JSONObject getJson();
	
	public String getLanguage();
	
	public String getUserLanguage();
	
	public String getUserLocation();
	
	public String getUserTimezone();
	
	public double[] getCoordinates();
	
	/**
	 * @return string of country, full country parsed by CountryCode
	 */
	public String getCountry();
	
	public String getText();
	
	public String getUTCTimeZone();
}
