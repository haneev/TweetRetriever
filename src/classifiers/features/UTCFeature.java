package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class UTCFeature extends BayesFeature {
	
	public UTCFeature() {
		super("utc");
	}

	@Override
	public String getValue(BayesDocument doc) {
		return doc.getJson().getJSONObject("user").optString("utc_offset", null);
	}	
	
}
