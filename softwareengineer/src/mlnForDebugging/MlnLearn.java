package mlnForDebugging;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import javatools.administrative.D;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DR;
import javatools.filehandlers.DW;

import com.google.common.collect.TreeMultimap;

public class MlnLearn {
	static TreeMultimap<Integer, Integer> answer = TreeMultimap.create();
	static HashMap<String, Double> priorweight = new HashMap<String, Double>();
	static double defaultWeight = 1;

	//static double[] topkAcc;

	public static void loadAnswer(String input_answer) {
		DR dr = new DR(input_answer);
		String[] l;
		while ((l = dr.read()) != null) {
			String vInStr = l[0];
			int v = Integer.parseInt(vInStr.toLowerCase().replace("v", ""));
			String[] ab = l[1].split(" ");
			for (String x : ab) {
				answer.put(v, Integer.parseInt(x));
			}
		}
		dr.close();
	}

	//	public static void loadPrior(String input_prior) {
	//		DR dr = new DR(input_prior);
	//		String[] l;
	//		while ((l = dr.read()) != null) {
	//			String deptype = l[0];
	//			int suspicious = Integer.parseInt(l[1]);
	//			prior.put(suspicious, deptype);
	//		}
	//		dr.close();
	//	}

	public static void typeWeight() {
		double w0 = defaultWeight;
		priorweight.put("control_dep", w0);
		priorweight.put("data_dep", w0);
		priorweight.put("array", w0);
		priorweight.put("stmt_3_var", w0);
	}

	public static double[] mln(String dir, int covNum, int MAX) {
		Cnf cnf = new Cnf();
		HashSet<Integer> suspect = new HashSet<Integer>();
		{
			//load baseline
			int passCount[] = new int[MAX];
			int failCount[] = new int[MAX];
			String input_coverage = dir + "/v" + covNum + "_cov.txt";
			if (!new File(input_coverage).exists())
				return null;
			DR dr = new DR(input_coverage);
			String[] l;
			while ((l = dr.read()) != null) {
				boolean pass = l[1].equals("Pass") ? true : false;
				l[2] = l[2].trim();
				if (l[2].length() == 0)
					continue;
				String[] execlist = l[2].trim().split(" ");
				for (String exec : execlist) {
					int a = Integer.parseInt(exec);
					if (pass) {
						passCount[a]++;
					} else {
						failCount[a]++;
					}
				}
			}
			for (int i = 0; i < MAX; i++) {
				Clause c = new Clause();
				c.addVar(i, true);
				double f1 = 0;
				if (failCount[i] != 0) {
					f1 = failCount[i] * 1.0 / (passCount[i] + failCount[i]);
					c.setWeight(f1);
					cnf.clauses.add(c);
					//when f1>0, i is an suspect
					suspect.add(i);
				}
			}
		}
		{
			DR dr = new DR(dir + "/prior.txt");
			String[] l;
			while ((l = dr.read()) != null) {
				String deptype = l[0];
				Clause c = new Clause();
				double w = defaultWeight;
				if (priorweight.containsKey(deptype)) {
					w = priorweight.get(deptype);
				}
				c.setWeight(w);
				int suspicious = Integer.parseInt(l[1]);
				c.addVar(suspicious, true);
				if (l.length == 3) {
					int notsuspicious = Integer.parseInt(l[2]);
					c.addVar(notsuspicious, false);
				}
				cnf.clauses.add(c);
			}
			dr.close();
		}
		double[] score = new double[MAX];
		{
			//choose best
			for (int i : suspect) {
				HashSet<Integer> truePred = new HashSet<Integer>();
				truePred.add(i);
				double totalWeight = 0;
				for (Clause c : cnf.clauses) {
					double w0 = c.getSatWeight(truePred);
					totalWeight += w0;
				}
				score[i] = totalWeight;
			}
		}
		return score;

	}

	public static double[] baseline(String dir, int covNum, int MAX) {
		String input_coverage = dir + "/v" + covNum + "_cov.txt";
		if (!new File(input_coverage).exists())
			return null;
		DR dr = new DR(input_coverage);
		String[] l;
		int passCount[] = new int[MAX];
		int failCount[] = new int[MAX];

		while ((l = dr.read()) != null) {
			boolean pass = l[1].equals("Pass") ? true : false;
			l[2] = l[2].trim();
			if (l[2].length() == 0)
				continue;
			String[] execlist = l[2].trim().split(" ");
			for (String exec : execlist) {
				int a = Integer.parseInt(exec);
				if (pass) {
					passCount[a]++;
				} else {
					failCount[a]++;
				}
			}
		}
		double[] score = new double[MAX];
		for (int i = 0; i < score.length; i++) {
			if (failCount[i] == 0 && passCount[i] == 0) {
				score[i] = 0;
			} else {
				score[i] = failCount[i] * 1.0 / (passCount[i] + failCount[i]);
			}
		}
		dr.close();
		return score;
		//		int[] idx = QuickSort.quicksort(score, true);
		//		StringBuilder sb = new StringBuilder();
		//		int k = 0;
		//		for (; k < idx.length; k++) {
		//			if (map.containsEntry(covNum, idx[k])) {
		//				topkAcc[k]++;
		//				break;
		//			}
		//		}
		//		for (k = k + 1; k < idx.length; k++) {
		//			topkAcc[k] += 1;
		//		}
		//D.p(sb.toString());
	}

	public static void eval(double[] score, double[] topkAcc, int covNum, TreeMultimap<Integer, Integer> answer) {
		int[] idx = QuickSort.quicksort(score, true);
		StringBuilder sb = new StringBuilder();
		int k = 0;
		for (; k < idx.length; k++) {
			if (answer.containsEntry(covNum, idx[k])) {
				topkAcc[k]++;
				break;
			}
		}
		for (k = k + 1; k < idx.length; k++) {
			topkAcc[k] += 1;
		}
	}

	public static void mlnlearn(int NUMTESTCASE, int NUMSTATEMENT, String dir, String output) {
		DW dw = new DW(output);
		double WGT[] = new double[] { 0, 0.0001, 0.001, 0.01, 0.1, 1, 10 };
		for (double w : WGT) {
			defaultWeight = w;
			double[] baselinetopkacc = new double[NUMSTATEMENT];
			double[] mlntopkacc = new double[NUMSTATEMENT];
			for (int i = 0; i < NUMTESTCASE; i++) {
				String input_coverage = dir + "/v" + i + "_cov.txt";
				D.p(input_coverage);
				if (!new File(input_coverage).exists())
					continue;
				double[] score = baseline(dir, i, NUMSTATEMENT);
				double[] mlnscore = mln(dir, i, NUMSTATEMENT);
				eval(score, baselinetopkacc, i, answer);
				eval(mlnscore, mlntopkacc, i, answer);
			}
			{

				double reward = 0;
				for (int i = 0; i < NUMSTATEMENT; i++) {
					reward += mlntopkacc[i];
				}
				D.p(defaultWeight, reward);
				dw.write(defaultWeight, reward);
			}
		}
		dw.close();
	}

	public static void main(String[] args) {
		String dir = args[0];
		int NUMTESTCASE = Integer.parseInt(args[1]);
		int NUMSTATEMENT = Integer.parseInt(args[2]);
		String wlearn = dir + "/" + args[3];
		String pred = dir + "/" + args[4];
		double[] baselinetopkacc = new double[NUMSTATEMENT];
		double[] mlntopkacc = new double[NUMSTATEMENT];
		loadAnswer(dir + "/answer.txt");

		//mlnlearn(NUMTESTCASE, NUMSTATEMENT, dir, wlearn);
		{
			//take the best wlearn
			DR dr = new DR(wlearn);
			String[] l;
			double bestEffect = 0;
			while ((l = dr.read()) != null) {
				double weight = Double.parseDouble(l[0]);
				double effect = Double.parseDouble(l[1]);
				if (effect > bestEffect) {
					bestEffect = effect;
					defaultWeight = weight;
				}
			}
			dr.close();
		}
		for (int i = 0; i < NUMTESTCASE; i++) {
			String input_coverage = dir + "/v" + i + "_cov.txt";
			D.p(input_coverage);
			if (!new File(input_coverage).exists())
				continue;
			double[] score = baseline(dir, i, NUMSTATEMENT);
			double[] mlnscore = mln(dir, i, NUMSTATEMENT);
			eval(score, baselinetopkacc, i, answer);
			eval(mlnscore, mlntopkacc, i, answer);
		}
		double divideby = baselinetopkacc[baselinetopkacc.length - 1];
		{
			DW dw = new DW(pred);
			for (int i = 0; i < NUMSTATEMENT; i++) {
				baselinetopkacc[i] = baselinetopkacc[i] / divideby;
				mlntopkacc[i] = mlntopkacc[i] / divideby;
				dw.write(i, baselinetopkacc[i], mlntopkacc[i]);
			}
			dw.close();
		}
	}

}
