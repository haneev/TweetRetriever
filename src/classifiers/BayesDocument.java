package classifiers;

import org.json.JSONObject;

import tools.JSON;

public class BayesDocument {
	
	private BayesClass bayesClass;
	private boolean isTrained = false;
	
	private JSONObject json;
	
	public BayesDocument(JSONObject json) {
		this.json = json;
	}
	
	public BayesDocument(String json) {
		this.json = JSON.parse(json);
	}
	
	public JSONObject getJson() {
		return json;
	}
	
	public void setIsTrained(boolean trained) {
		this.isTrained = trained;
	}
	
	public boolean isTrainDocument() {
		return isTrained;
	}
	
	public BayesClass getBayesClass() {
		return this.bayesClass;
	}
	
	public void setBayesClass(BayesClass cls) {
		this.bayesClass = cls;
	}
	
}
