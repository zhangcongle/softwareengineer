package cse446mlhw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javatools.administrative.D;
import javatools.filehandlers.DW;

public class NbDebug {

	static HashMap<String, Integer> token2id = new HashMap<String, Integer>();
	static HashMap<Integer, String> id2token = new HashMap<Integer, String>();
	static List<String> classId2name = new ArrayList<String>();
	static int FOLD = 10;
	static int M; //number of classes;
	static int V; //number of vocabulary
	static int N; //number of document;
	static List<NbDoc> traindata = new ArrayList<NbDoc>();
	static List<NbDoc> testdata = new ArrayList<NbDoc>();
	static List<String> trainTitle = new ArrayList<String>();
	static List<String> testTitle = new ArrayList<String>();
	/**TO PREDICT: The class of the test cases*/
	static List<String> testClass = new ArrayList<String>();

	public static String normToken(String token) {
		return token.toLowerCase();
	}

	public static List<Integer> text2fts(String line, boolean newtoken) {
		List<Integer> res = new ArrayList<Integer>();
		String[] tokens = line.split(" ");
		for (String t : tokens) {
			t = normToken(t);
			if (token2id.containsKey(t)) {
				int id = token2id.get(t);
				res.add(id);
			} else if (newtoken) {
				token2id.put(t, token2id.size() + 1);
				int id = token2id.get(t);
				id2token.put(id, t);
				res.add(id);
			}
		}
		Collections.sort(res);
		return res;
	}

	public static void loadtrain(String strTraindir) throws IOException {
		File tdir = new File(strTraindir);
		for (String c : tdir.list()) {
			int cid = classId2name.size();
			classId2name.add(c);
			String strClassdir = tdir + File.separator + c + File.separator + "train";
			File cdir = new File(strClassdir);
			List<NbDoc> temp = new ArrayList<NbDoc>();
			for (String a : cdir.list()) {
				String filename = strClassdir + File.separator + a;
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
				String line = br.readLine();
				br.close();
				List<Integer> fts = text2fts(line, true);
				NbDoc nd = new NbDoc();
				nd.label = cid;
				nd.fts = fts;
				nd.title = a;
				temp.add(nd);
			}
			Collections.sort(temp, new Comparator<NbDoc>() {
				@Override
				public int compare(NbDoc arg0, NbDoc arg1) {
					// TODO Auto-generated method stub
					return arg0.title.compareTo(arg1.title);
				}
			});
			for (int i = 0; i < temp.size(); i++) {
				NbDoc nd = temp.get(i);
				//nd.fold will be an int between 0-9; indicate the fold of this document during cross validation
				nd.fold = i * FOLD / temp.size();
				traindata.add(nd);
			}
		}
		M = classId2name.size();
		V = token2id.size() + 1;
		N = traindata.size();
	}

	public static void loadTest(String strTestdir) throws IOException {
		File tdir = new File(strTestdir);
		for (String a : tdir.list()) {
			String filename = tdir + File.separator + a;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
			String line = br.readLine();
			br.close();
			List<Integer> fts = text2fts(line, false);
			NbDoc nd = new NbDoc();
			nd.label = 0;
			nd.fts = fts;
			testdata.add(nd);
			testTitle.add(a);
		}
	}

	public static void trainMultinomialNb(List<NbDoc> train, double prior[], double[][] condprob) {
		for (int i = 0; i < train.size(); i++) {
			int cid = train.get(i).label;
			prior[cid] += 1;
		}
		for (int c = 0; c < prior.length; c++) {
			prior[c] /= N;
		}
		double[] den = new double[M];

		for (int i = 0; i < train.size(); i++) {
			int c = train.get(i).label;
			List<Integer> fts = train.get(i).fts;
			for (int f : fts) {
				condprob[c][f] += 1;
				den[c]++;
			}
		}
		DW dw = new DW("debug");
		for (int c = 0; c < M; c++) {
			for (int j = 0; j < V; j++) {
				if (id2token.containsKey(j) && id2token.get(j).equals("treatment")
						&& classId2name.get(c).equals("company")) {
					//D.p(condprob[c][j], den[c], V);
				}
				double old = condprob[c][j];
				condprob[c][j] = (condprob[c][j] + 1) * 1.0 / (den[c] + V);

				if (id2token.containsKey(j)) {
					//					dw.write("P(W = " + id2token.get(j) + " | " + classId2name.get(c) + ")", condprob[c][j], (old + 2)
					//							* 1.0 / (den[c] + V), old, den[c], V);
					dw.write("P(W = " + id2token.get(j) + " | " + classId2name.get(c) + ")", condprob[c][j]);
				}
			}
		}
		dw.close();
	}

	public static void printCondProb(double[][] condprob, String file) {
		DW dw = new DW(file);
		for (int c = 0; c < M; c++) {
			for (int j = 0; j < V; j++) {
				if (id2token.containsKey(j)) {
					dw.write("log P(W = " + id2token.get(j) + " | " + classId2name.get(c) + ")",
							Math.log(condprob[c][j]));
				}
			}
		}
		dw.close();
	}

	public static void PR(List<Integer> pred, List<Integer> ans, List<PRResult> prresultlist) {
		int[] right = new int[M];
		int[] pdown = new int[M];
		int[] rdown = new int[M];
		for (int i = 0; i < pred.size(); i++) {
			int p = pred.get(i);
			int a = ans.get(i);
			pdown[p]++;
			rdown[a]++;
			if (p == a) {
				right[p]++;
			}
		}
		double pSum = 0, rSum = 0;
		for (int i = 0; i < M; i++) {
			double precision = right[i] * 1.0 / pdown[i];
			double recall = right[i] * 1.0 / rdown[i];
			pSum += precision;
			rSum += recall;
			prresultlist.add(new PRResult(i, precision, recall));
		}
	}

	public static List<Integer> applyMultinomialNb(List<NbDoc> test, double prior[], double[][] condprob) {
		List<Integer> prediction = new ArrayList<Integer>();
		for (int i = 0; i < test.size(); i++) {
			double score[] = new double[M];
			for (int c = 0; c < M; c++) {
				for (int f : test.get(i).fts) {
					score[c] += Math.log(condprob[c][f]);
				}
			}
			double maxScore = -100000000;
			int bestC = -1;
			for (int c = 0; c < M; c++) {
				if (score[c] > maxScore) {
					maxScore = score[c];
					bestC = c;
				}
			}
			prediction.add(bestC);
		}
		return prediction;
	}

	public static void crossValidation() {
		List<PRResult> results = new ArrayList<PRResult>();
		for (int k = 0; k < FOLD; k++) {
			List<NbDoc> cvtrain = new ArrayList<NbDoc>();
			List<NbDoc> cvtest = new ArrayList<NbDoc>();
			List<Integer> answer = new ArrayList<Integer>();
			for (int i = 0; i < traindata.size(); i++) {
				NbDoc nd = traindata.get(i);
				if (nd.fold == k) {
					cvtest.add(traindata.get(i));
					answer.add(traindata.get(i).label);
				} else {
					cvtrain.add(traindata.get(i));
				}
			}
			double prior[] = new double[M];
			double condprob[][] = new double[M][V];
			trainMultinomialNb(cvtrain, prior, condprob);
			List<Integer> predictions = applyMultinomialNb(cvtest, prior, condprob);
			PR(predictions, answer, results);
		}
		double[] sumprecision = new double[classId2name.size()];
		double[] sumrecall = new double[classId2name.size()];
		for (PRResult prr : results) {
			sumprecision[prr.label] += prr.precision;
			sumrecall[prr.label] += prr.recall;
			System.out.println(classId2name.get(prr.label) + "\tPrecision\t" + prr.precision + "\tRecall\t"
					+ prr.recall);
		}
		System.out.println("=====================================================================");
		for (int i = 0; i < classId2name.size(); i++) {
			System.out.println(classId2name.get(i) + "\tAvgPrecision\t" + (sumprecision[i] / FOLD) + "\tAvgRecall\t"
					+ (sumrecall[i] / FOLD));
		}
	}

	public static void writeTestOutput(String output, List<Integer> prediction) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"));
		for (int i = 0; i < testdata.size(); i++) {
			String title = testTitle.get(i);
			String predict = classId2name.get(prediction.get(i));
			bw.write(title + "|" + predict + "\n");
		}
		bw.close();
	}

	public static void main(String[] args) throws IOException {
		String strOutput = args[2];
		loadtrain(args[0]);
		loadTest(args[1]);
		//crossValidation();
		{
			double prior[] = new double[M];
			double condprob[][] = new double[M][V];
			trainMultinomialNb(traindata, prior, condprob);
			//printCondProb(condprob, "debug");
			List<Integer> predictions = applyMultinomialNb(testdata, prior, condprob);
			writeTestOutput(strOutput, predictions);
		}
	}

}
