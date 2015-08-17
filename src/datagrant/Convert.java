package datagrant;

import tools.Weka;

public class Convert {

	public static void main(String[] args) {
		
		Weka.convert("../data/out_movember_small.json", "../data/out_movember.arff");

	}

}
