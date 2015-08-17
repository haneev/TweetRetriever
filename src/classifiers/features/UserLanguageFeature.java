package classifiers.features;

import classifiers.BayesDocument;
import classifiers.BayesFeature;

public class UserLanguageFeature extends BayesFeature {
	
	public UserLanguageFeature() {
		super("user_lang");
	}

	@Override
	public String getValue(BayesDocument doc) {
		return doc.getJson().getJSONObject("user").optString("lang", null);
	}	
	
}
