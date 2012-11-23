package gradehw2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class NBClassifier {

    /** 10-fold cross-validation */
    private static final int NUM_RUNS = 10;
    
    /** Stopwords filename */
    private static final String STOPWORDS_FILENAME = "stopwords";
    
    /** Removes stop words (includes punctuation) if true */
    private boolean extraCredit;
    
    /** Mapping from class name to documents */
    private Map<String, Set<File>> classToDocs;
    
    /** Dictionary of all stop words */
    private Set<String> stopwords = new HashSet<String>();
    
    /** Dictionary of all words found in documents */
    private Set<String> vocab = new HashSet<String>();
    
    /** Prior of each class */
    private Map<String, Double> prior = new HashMap<String, Double>();
    
    /** P(Word|Class) */
    private Map<String, Map<String, Double>> condProb = new HashMap<String, Map<String, Double>>();
    
    /** Constructs and starts training a Naive Bayes classifier */
    public NBClassifier(String trainData, boolean extraCredit) throws FileNotFoundException {
        this.extraCredit = extraCredit;
        if (extraCredit) {
            // setup stopwords dictionary
            File stopwordsFile = new File(STOPWORDS_FILENAME);
            Scanner s = new Scanner(stopwordsFile);
            while (s.hasNext()) {
                // add all words into stopwords dictionary
                stopwords.add(s.next().toLowerCase());
            }
        }
        classToDocs = new HashMap<String, Set<File>>();
        File trainDataRoot = new File(trainData);
        for (File f : trainDataRoot.listFiles()) {
            // add each dir name as a class
            String className = f.getName();
            File[] fChildren = f.listFiles();
            for (File fChild : fChildren) {
                if (fChild.getName().equals("train")) {
                    Set<File> docs = new HashSet<File>();
                    for (File doc : fChild.listFiles()) {
                        docs.add(doc);
                        Scanner s = new Scanner(doc);
                        while (s.hasNext()) {
                            String token = s.next().toLowerCase();
                            if (extraCredit) {
                                // filter out punctuation
                                if (token.length() == 1 && !Character.isLetterOrDigit(token.charAt(0))) {
                                    continue;
                                }
                                // filter out stopwords
                                if (stopwords.contains(token)) {
                                    continue;
                                }
                            }
                            // add all tokens into dictionary otherwise
                            vocab.add(token);
                        }
                    }
                    classToDocs.put(className, docs);
                }
            }
        }       
        trainMultinomial();
    }
    
    /** Trains classifier using the classes to document mapping from classToDocs */
    public void trainMultinomial() throws FileNotFoundException {
        int n = 0;
        for (Set<File> docs : classToDocs.values()) {
            n += docs.size();
        }
        for (String className : classToDocs.keySet()) {
            int n_c = classToDocs.get(className).size();
            prior.put(className, (double) n_c / n);
            Map<String, Integer> tokenCount = new HashMap<String, Integer>();
            for (File doc : classToDocs.get(className)) {
                Scanner s = new Scanner(doc);
                while (s.hasNext()) {
                    String token = s.next().toLowerCase();
                    if (tokenCount.containsKey(token)) {
                        tokenCount.put(token, tokenCount.get(token) + 1);
                    } else {
                        tokenCount.put(token, 1);
                    }
                }
            }
            int tokenCountTotal = 0;
            for (int i : tokenCount.values()) {
                tokenCountTotal += i;
            }
            for (String text : vocab) {
                int t_ct = 0;
                if (tokenCount.containsKey(text)) {
                    t_ct = tokenCount.get(text);
                }
                int top = t_ct + 1;
                int bottom = vocab.size() + tokenCountTotal;
                double condprob_tc = (double) top / bottom;
                if (condProb.containsKey(text)) {
                    Map<String, Double> classToProb = condProb.get(text);
                    classToProb.put(className, condprob_tc);
                } else {
                    Map<String, Double> classToProb = new HashMap<String, Double>();
                    classToProb.put(className, condprob_tc);
                    condProb.put(text, classToProb);
                }   
            }
        }
    }
    
    /** Classifies a list of documents and outputs the results */
    public void classifyTestData(List<File> docs, String outputFilename) throws IOException {
        FileWriter out = new FileWriter(outputFilename);
        for (File doc : docs) {
            out.write(doc.getName() + "|" + applyMultinomial(doc).toUpperCase() + "\n");
        }
        out.flush();
        out.close();
        System.out.println("Wrote test results to " + outputFilename);
    }
    
    /** Classifies test data and outputs the results */
    public void classifyTestData(String testData, String outputFilename) throws IOException {
        if (extraCredit) {
            outputFilename += ".extra";
        }
        File testDataRoot = new File(testData);
        classifyTestData(Arrays.asList(testDataRoot.listFiles()), outputFilename);
    }
    
    /** Classifies a given document */
    public String applyMultinomial(File doc) throws FileNotFoundException {
        List<String> words = new LinkedList<String>();
        Scanner s = new Scanner(doc);
        while (s.hasNext()) {
            words.add(s.next().toLowerCase());
        }
        Map<String, Double> score = new HashMap<String, Double>();
        for (String className : classToDocs.keySet()) {
            double currScore = Math.log(prior.get(className));          
            for (String word : words) {
                if (condProb.containsKey(word)) {
                    currScore += Math.log(condProb.get(word).get(className));
                }             
            }
            score.put(className, currScore);
        }
        double bestProb = Double.NEGATIVE_INFINITY;
        String bestClass = null;
        for (String className : score.keySet()) {
            double currentProb = score.get(className);
            if (currentProb > bestProb) {
                bestProb = currentProb;
                bestClass = className;
            }
        }      
        return bestClass;
    }
    
    /** Runs NUM_RUNS-fold ross-validation on training data */
    public void crossValidate(String data) throws FileNotFoundException {
        System.out.println("Starting cross-validation...");
        Map<String, List<ResultPair>> results = new HashMap<String, List<ResultPair>>();
        for (int i = 0; i < NUM_RUNS; i++) {
            Map<String, ResultPair> results_i = crossValidate(data, i);
            for (String className : results_i.keySet()) {
                ResultPair pair = results_i.get(className);
                if (results.containsKey(className)) {
                    results.get(className).add(pair);
                } else {
                    List<ResultPair> resultList = new LinkedList<ResultPair>();
                    resultList.add(pair);
                    results.put(className, resultList);
                }
            }
        }
        double precisionAvgAll = 0.0;
        double recallAvgAll = 0.0;
        System.out.println("AVERAGE:");
        for (String className : results.keySet()) {
            double precisionAvg = 0.0;
            double recallAvg = 0.0;
            for (ResultPair pair : results.get(className)) {
                precisionAvg += pair.precision;
                recallAvg += pair.recall;
            }
            precisionAvg /= results.get(className).size();
            recallAvg /= results.get(className).size();
            precisionAvgAll += precisionAvg;
            recallAvgAll += recallAvg;
            System.out.println("Class: " + className.toUpperCase());
            System.out.println("\tPrecision: " + precisionAvg);
            System.out.println("\tRecall: " + recallAvg);
        }
        precisionAvgAll /= results.keySet().size();
        recallAvgAll /= results.keySet().size();
        System.out.println("Average of All Runs:");
        System.out.println("\tPrecision: " + precisionAvgAll);
        System.out.println("\tRecall: " + recallAvgAll);
    }
    
    /** Runs one-fold cross-validation */
    public Map<String, ResultPair> crossValidate(String data, int run) throws FileNotFoundException {
        System.out.println("RUN " + run + ":");
        classToDocs = new HashMap<String, Set<File>>();
        Map<File, String> testSet = new HashMap<File, String>();
        File dataRoot = new File(data);
        for (File f : dataRoot.listFiles()) {
            // add each dir name as a class
            String className = f.getName();
            File[] fChildren = f.listFiles();
            for (File fChild : fChildren) {
                if (fChild.getName().equals("train")) {
                    Set<File> docs = new HashSet<File>();
                    File[] files = fChild.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (i >= run * files.length / NUM_RUNS && i < (run + 1) * files.length / NUM_RUNS) {
                            testSet.put(files[i], className);
                        } else {
                            docs.add(files[i]);
                        }
                    }
                    classToDocs.put(className, docs);
                }
            }
        }
        trainMultinomial();
        // calculate precision and recall
        Map<String, Integer> tp = new HashMap<String, Integer>();
        Map<String, Integer> fp = new HashMap<String, Integer>();
        Map<String, Integer> fn = new HashMap<String, Integer>();
        for (File doc : testSet.keySet()) {
            String result = applyMultinomial(doc);
            String actual = testSet.get(doc);
            if (result.equals(actual)) {
                // record true positive
                if (tp.containsKey(actual)) {
                    tp.put(actual, tp.get(actual) + 1);
                } else {
                    tp.put(actual, 1);
                }
            } else {
                // record false positive
                if (fp.containsKey(result)) {
                    fp.put(result, fp.get(result) + 1);
                } else {
                    fp.put(result, 1);
                }
                // record false negative
                if (fn.containsKey(actual)) {
                    fn.put(actual, fn.get(actual) + 1);
                } else {
                    fn.put(actual, 1);
                }
            }
        }
        // take care of zero values
        for (String className : classToDocs.keySet()) {
            if (!tp.containsKey(className)) {
                tp.put(className, 0);
            }
            if (!fp.containsKey(className)) {
                fp.put(className, 0);
            }
            if (!fn.containsKey(className)) {
                fn.put(className, 0);
            }
        }
        Map<String, ResultPair> results = new HashMap<String, ResultPair>();
        for (String className : classToDocs.keySet()) {
            int tp_i = tp.get(className);
            int fp_i = fp.get(className);
            int fn_i = fn.get(className);
            double precision_i = (double) tp_i / (tp_i + fp_i);
            double recall_i = (double) tp_i / (tp_i + fn_i);
            results.put(className, new ResultPair(precision_i, recall_i));
            System.out.println("Class: " + className.toUpperCase());
            System.out.println("\tPrecision: " + precision_i);
            System.out.println("\tRecall: " + recall_i);
        }
        return results;
    }
    
    /** 
     * Constructs a Naive Bayes classifier using the training data,
     * classifies the test data, and runs cross-validation on the
     * training data.
     */
    public static void main(String[] args) {
        
        long start, end;
        start = System.currentTimeMillis();
        
        if (args.length < 3) {
            System.err.println("Usage: training_data test_data test_output [-x]");
            return;
        }
        
        boolean extraCredit = false;
        if (args.length == 4) {
            if (args[3].equals("-x")) {
                extraCredit = true;
            }
        }        
        
        NBClassifier classifier = null;
        try {
            classifier = new NBClassifier(args[0], extraCredit);
        } catch (FileNotFoundException e) {
            System.err.println("Error occured during training!");
            e.printStackTrace();
        }
        
        end = System.currentTimeMillis();
        System.out.println("Training took " + (double) (end - start) / 1000 + "s");
        start = System.currentTimeMillis();    
        
        try {
            classifier.classifyTestData(args[1], args[2]);
        } catch (IOException e) {
            System.err.println("Error occured during testing!");
            e.printStackTrace();
        }
        
        
        end = System.currentTimeMillis();
        System.out.println("Testing took " + (double) (end - start) / 1000 + "s");
        start = System.currentTimeMillis();        
        
        try {
            classifier.crossValidate(args[0]);
        } catch (FileNotFoundException e) {
            System.err.println("Error occured during cross-validation!");
            e.printStackTrace();
        } 
        
        end = System.currentTimeMillis();
        System.out.println("Cross-validation took " + (double) (end - start) / 1000 + "s");
    }
}
