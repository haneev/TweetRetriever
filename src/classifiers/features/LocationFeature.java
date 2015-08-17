package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class LocationFeature extends BayesFeature {
	
	public LocationFeature() {
		super("location");
	}

	@Override
	public String getValue(BayesDocument doc) {
		return doc.getJson().getJSONObject("user").optString("location", null);
	}	
	
}
