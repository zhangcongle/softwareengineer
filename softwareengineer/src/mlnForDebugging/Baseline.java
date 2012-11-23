package mlnForDebugging;

import java.io.File;

import com.google.common.collect.TreeMultimap;

import javatools.administrative.D;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DR;

public class Baseline {
	static TreeMultimap<Integer, Integer> map = TreeMultimap.create();
	static double[] topkAcc;

	public static void loadAnswer(String input_answer) {
		DR dr = new DR(input_answer);
		String[] l;
		while ((l = dr.read()) != null) {
			String vInStr = l[0];
			int v = Integer.parseInt(vInStr.toLowerCase().replace("v", ""));
			String[] ab = l[1].split(" ");
			for (String x : ab) {
				map.put(v, Integer.parseInt(x));
			}
		}
		dr.close();
	}

	public static void baseline(String dir, int covNum, int MAX) {
		String input_coverage = dir + "/v" + covNum + "_cov.txt";
		if (!new File(input_coverage).exists())
			return;
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
		int[] idx = QuickSort.quicksort(score, true);
		StringBuilder sb = new StringBuilder();
		int k = 0;
		for (; k < idx.length; k++) {
			if (map.containsEntry(covNum, idx[k])) {
				topkAcc[k]++;
				break;
			}
		}
		for (k = k + 1; k < idx.length; k++) {
			topkAcc[k] += 1;
		}
		//D.p(sb.toString());
		dr.close();
	}

	public static void main(String[] args) {
		String dir = args[0];
		int NUMTESTCASE = Integer.parseInt(args[1]);
		int NUMSTATEMENT = Integer.parseInt(args[2]);
		topkAcc = new double[NUMSTATEMENT];
		loadAnswer(dir + "/answer.txt");
		for (int i = 0; i < NUMTESTCASE; i++) {
			baseline(dir, i, NUMSTATEMENT);
		}
		for (int i = 0; i < topkAcc.length; i++) {
			topkAcc[i] /= topkAcc[topkAcc.length - 1];
		}
		D.p(topkAcc);
	}
}
