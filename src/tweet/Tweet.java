package tweet;

import org.json.JSONObject;

public class Tweet implements ClassifyableTweet {

	private JSONObject json;
	
	public Tweet(JSONObject json) {
		this.json = json;
	}
	
	@Override
	public JSONObject getJson() {
		return this.json;
	}

	@Override
	public String getLanguage() {
		return this.json.optString("lang", null);
	}

	@Override
	public String getUserLanguage() {
		JSONObject user = this.json.optJSONObject("user");
		return user == null ? null : user.optString("lang", null);
	}

	@Override
	public String getUserLocation() {
		JSONObject user = this.json.optJSONObject("user");
		return user == null ? null : user.optString("location", null);
	}

	@Override
	public String getUserTimezone() {
		JSONObject user = this.json.optJSONObject("user");
		return user == null ? null : user.optString("time_zone", null);
	}

	@Override
	public double[] getCoordinates() {
		JSONObject coords = this.json.optJSONObject("coordinates");
		
		if(coords != null) {
			double[] ret = new double[2];
			ret[1] = coords.getJSONArray("coordinates").getDouble(0);
			ret[0] = coords.getJSONArray("coordinates").getDouble(1);
			return ret;
		} else {
			return null;
		}
	}

	@Override
	public String getText() {
		return this.json.getString("text");
	}

	@Override
	public String getUTCTimeZone() {
		JSONObject user = this.json.optJSONObject("user");
		return user == null ? null : user.optString("utc_offset", null);
	}

	@Override
	public String getCountry() { 
		return null; // this is unknown, then use coordinates and cache
	}

	@Override
	public boolean isValidFormatForThisClass() {
		return this.json.has("text");
	}
	
}
