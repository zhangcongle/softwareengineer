package mlnForDebugging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class Clause {
	List<String> comment; //e.g. type of the clause
	double weight;
	List<Integer> predicates;

	public Clause() {
		predicates = new ArrayList<Integer>();
	}

	public void addVar(int varInId, boolean positive) {
		if (positive) {
			predicates.add(varInId);
		} else {
			predicates.add(-1 * varInId);
		}
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getSatWeight(HashSet<Integer> truePred) {
		double w = 0;
		for (int i = 0; i < predicates.size(); i++) {
			int signedVarInId = predicates.get(i);
			if (signedVarInId > 0 && truePred.contains(signedVarInId)) {
				w = this.weight;
			} else if (signedVarInId < 0 && truePred.contains(-1 * signedVarInId)) {
				w = this.weight;
			}
		}
		return w;
	}

	public void addComment(String c) {
		this.comment.add(c);
	}
}

public class Cnf {
	List<Clause> clauses = new ArrayList<Clause>();
}
