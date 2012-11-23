package mlnForDebugging;

import java.io.File;
import java.util.SortedSet;

import com.google.common.collect.TreeMultimap;

import javatools.administrative.D;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DR;
import javatools.filehandlers.DW;

public class WithPrior {
	static TreeMultimap<Integer, Integer> answer = TreeMultimap.create();
	static TreeMultimap<Integer, String> prior = TreeMultimap.create();
	static double[] topkAcc;

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

	public static void loadPrior(String input_prior) {
		DR dr = new DR(input_prior);
		String[] l;
		while ((l = dr.read()) != null) {
			String deptype = l[0];
			int suspicious = Integer.parseInt(l[1]);
			prior.put(suspicious, deptype);
		}
		dr.close();
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

	public static double[] addPrior(double[] score) {
		double[] scorePrior = new double[score.length];
		for (int i = 0; i < score.length; i++) {
			scorePrior[i] = score[i];
			if (prior.containsKey(i)) {
				SortedSet<String> deptypes = prior.get(i);
				scorePrior[i] = score[i] + deptypes.size() * 0.001;
			}
		}
		return scorePrior;
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

	public static void main(String[] args) {
		String dir = args[0];
		int NUMTESTCASE = Integer.parseInt(args[1]);
		int NUMSTATEMENT = Integer.parseInt(args[2]);
		String output = args[3];
		topkAcc = new double[NUMSTATEMENT];
		loadAnswer(dir + "/answer.txt");
		loadPrior(dir + "/prior.txt");
		double[] score = null;
		double[] priorscore = null;
		double[] baselinetopkacc = new double[NUMSTATEMENT];
		double[] priortopkacc = new double[NUMSTATEMENT];
		for (int i = 0; i < NUMTESTCASE; i++) {
			String input_coverage = dir + "/v" + i + "_cov.txt";
			if (!new File(input_coverage).exists())
				continue;
			score = baseline(dir, i, NUMSTATEMENT);
			priorscore = addPrior(score);
			eval(score, baselinetopkacc, i, answer);
			eval(priorscore, priortopkacc, i, answer);
		}
		double divideby = baselinetopkacc[baselinetopkacc.length - 1];
		{
			DW dw = new DW(output);
			for (int i = 0; i < NUMSTATEMENT; i++) {
				baselinetopkacc[i] = baselinetopkacc[i] / divideby;
				priortopkacc[i] = priortopkacc[i] / divideby;
				dw.write(i, baselinetopkacc[i], priortopkacc[i]);
			}
			dw.close();
		}
	}
}