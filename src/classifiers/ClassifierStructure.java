package classifiers;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.SparseInstance;

/**
 * This structure is required by each classifier
 * It is simply a text and class attribute
 * @author Han
 *
 */
public class ClassifierStructure {

	private FastVector structure;
	
	private Attribute classAttr, textAttr;
	
	public ClassifierStructure() {
		structure = getStructure();
	}
	
	public FastVector getStructure() {
		
		if(structure != null)
			return structure;
		
		textAttr = new Attribute("text", (FastVector) null );
		 
		FastVector fvNominalVal = new FastVector(2);
		fvNominalVal.addElement("not");
		fvNominalVal.addElement("match");
		classAttr = new Attribute("@@class@@", fvNominalVal);
		
		FastVector wekaAttributes = new FastVector(2);
		wekaAttributes.addElement(textAttr);
		wekaAttributes.addElement(classAttr);
		
		return wekaAttributes;
	}
	
	public Instance makeInstance(String cls, String text) {		
		Instance in = new Instance(2);
		in.setValue(classAttr, cls);
		in.setValue(textAttr, text);
		return in;
	}
	
}
