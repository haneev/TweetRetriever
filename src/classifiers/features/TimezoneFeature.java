package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class TimezoneFeature extends BayesFeature {
	
	public TimezoneFeature() {
		super("timezone");
	}

	@Override
	public String getValue(BayesDocument doc) {
		return doc.getJson().getJSONObject("user").optString("time_zone", null);
	}	
	
}
