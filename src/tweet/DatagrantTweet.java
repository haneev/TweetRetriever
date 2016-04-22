package tweet;

import org.json.JSONArray;
import org.json.JSONObject;

import tools.CountryCode;

public class DatagrantTweet implements ClassifyableTweet {

	private JSONObject json;
	
	public DatagrantTweet(JSONObject json) {
		this.json = json;
	}
	
	@Override
	public JSONObject getJson() {
		return this.json;
	}

	@Override
	public String getLanguage() {
		JSONObject gnip = this.json.optJSONObject("gnip");
		if (gnip != null) {
			JSONObject lang = gnip.getJSONObject("language");
			if (lang != null) {
				return lang.has("value") ? lang.optString("value") : null;
			}
		}
		
		return null;
	}

	@Override
	public String getUserLanguage() {
		JSONObject user = this.json.optJSONObject("actor");
		if (user != null) {
			JSONArray location = user.optJSONArray("languages");
			if (location != null) {
				return location.length() > 0 ? location.optString(0) : null;
			}
		}
		return null;
	}

	@Override
	public String getUserLocation() {
		JSONObject user = this.json.optJSONObject("actor");
		if (user != null) {
			JSONObject location = user.optJSONObject("location");
			if (location != null) {
				return location.has("displayName") ? location.optString("displayName") : null;
			}
		}
		return null;
	}

	@Override
	public String getUserTimezone() {
		JSONObject user = this.json.optJSONObject("actor");
		if (user != null) {
			return user.has("twitterTimeZone") ? user.optString("twitterTimeZone") : null;
		}
		return null;
	}

	@Override
	public double[] getCoordinates() {
		JSONObject gnip = this.json.optJSONObject("gnip");
		if (gnip != null) {
			JSONArray loc = gnip.optJSONArray("profileLocations");
			if (loc != null && loc.length() > 0) {
				
				JSONObject firstLocation = loc.getJSONObject(0);
				JSONObject geo = firstLocation.optJSONObject("geo");
				
				if (geo != null) {
					JSONArray coords = geo.getJSONArray("coordinates");
					double[] c = new double[2];
					c[0] = coords.getDouble(1);
					c[1] = coords.getDouble(0);
					return c;
				}
			}
		}
		
		return null;
	}

	@Override
	public String getText() {
		return this.json.getString("body");
	}

	@Override
	public String getUTCTimeZone() {
		JSONObject user = this.json.optJSONObject("actor");
		if (user != null) {
			return user.has("utcOffset") ? user.optString("utcOffset") : null;
		}
		return null;
	}

	@Override
	public String getCountry() {
		JSONObject gnip = this.json.optJSONObject("gnip");
		if (gnip != null) {
			JSONArray loc = gnip.optJSONArray("profileLocations");
			if (loc != null && loc.length() > 0) {
				
				JSONObject firstLocation = loc.getJSONObject(0);
				JSONObject address = firstLocation.optJSONObject("address");
				
				if (address != null) {
					String code = address.optString("countryCode");
					if (code != null) {
						return CountryCode.getCountry(code.toLowerCase());
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public boolean isValidFormatForThisClass() {
		return this.json.has("objectType");
	}

}
