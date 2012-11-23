package youdao;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javatools.administrative.D;
import javatools.filehandlers.DR;
import javatools.filehandlers.DW;

public class URLReader {
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			dir = args[0];
		}
		// htmls();
		parseHtml();
		// parseHtmlFromWordlist();
	}

	public static void main2(String[] args) throws Exception {

		URL oracle = new URL(
				"http://dict.youdao.com/search?q=venue&keyfrom=dict.index#q%3Dvenue%26keyfrom%3Ddict.index");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				oracle.openStream(), "UTF-8"));

		String a = "C:/Users/congle/Downloads/a.html";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(a), "utf-8"));
		String inputLine;
		StringBuilder sb = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine + "\n");
			// bw.write(inputLine + "\n");
		}
		String collins = takeCollins(sb.toString());
		bw.write(collins);
		in.close();
		bw.close();
	}

	static String dir = "C:/Users/congle/Downloads/youdao";

	public static void removeChineseExp(String input, String output)
			throws IOException {
		class Pair {
			int start;
			int end;
			String newstr;

			public Pair(int start, int end, String newstr) {
				this.start = start;
				this.end = end;
				this.newstr = newstr;
			}
		}
		StringBuilder sb = new StringBuilder();
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(input), "utf-8"));
			String l;

			while ((l = in.readLine()) != null) {
				sb.append(l);
			}
		}
		{
			List<Pair> pairs = new ArrayList<Pair>();
			String s = sb.toString();
			Pattern p = Pattern.compile("</span>(.*?)</p>");
			Matcher m = p.matcher(s);

			while ((m.find())) {
				String a = m.group(0);
				String b = a.replaceAll("[^\\x00-\\x7F]", "");
				Pair pair = new Pair(m.start(), m.end(), b);
				pairs.add(pair);
			}
			Collections.sort(pairs, new Comparator<Pair>() {

				@Override
				public int compare(Pair p0, Pair p1) {
					// TODO Auto-generated method stub
					return p1.start - p0.start;
				}
			});
			for (int k = 0; k < pairs.size(); k++) {
				Pair pair = pairs.get(k);
				sb = sb.replace(pair.start, pair.end, pair.newstr);
			}
		}
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output), "utf-8"));
			bw.write(sb.toString());
			bw.close();
		}
	}

	public static String removeChineseExp(String raw) throws IOException {
		class Pair {
			int start;
			int end;
			String newstr;

			public Pair(int start, int end, String newstr) {
				this.start = start;
				this.end = end;
				this.newstr = newstr;
			}
		}
		StringBuilder sb = new StringBuilder(raw);
		{
			List<Pair> pairs = new ArrayList<Pair>();
			String s = sb.toString();
			Pattern p = Pattern.compile("</span>(.*?)</p>");
			Matcher m = p.matcher(s);

			while ((m.find())) {
				String a = m.group(0);
				String b = a.replaceAll("[^\\x00-\\x7F]", "");
				Pair pair = new Pair(m.start(), m.end(), b);
				pairs.add(pair);
			}
			Collections.sort(pairs, new Comparator<Pair>() {

				@Override
				public int compare(Pair p0, Pair p1) {
					// TODO Auto-generated method stub
					return p1.start - p0.start;
				}
			});
			for (int k = 0; k < pairs.size(); k++) {
				Pair pair = pairs.get(k);
				sb = sb.replace(pair.start, pair.end, pair.newstr);
			}
		}
		return sb.toString();
	}

	public static void htmls() throws Exception {
		DR dr = new DR(dir + "/wordlist");
		String outputdir = dir + "/htmls";
		HashSet<String> crawledwords = new HashSet<String>();
		{
			File f = new File(outputdir);
			String[] list = f.list();
			for (String w : list) {
				w = w.replace(".html", "");
				crawledwords.add(w);
			}
		}
		String[] l;
		while ((l = dr.read()) != null) {
			String w = l[0].trim();
			if (!crawledwords.contains(w)) {
				System.out.println(w);
				try {
					crawlOneWord(w, dir + "/htmls");
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.sleep(1000);
			}
		}
		dr.close();
	}

	public static void parseHtmlFromWordlist() throws Exception {
		DR dr = new DR(dir + "/wordlist");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(dir + "/look.html"), "utf-8"));
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dir + "/head"), "utf-8"));
			String l;
			StringBuilder sb = new StringBuilder();
			while ((l = in.readLine()) != null) {
				sb.append(l + "\n");
			}
			bw.write(sb.toString());
			in.close();
		}
		String[] ll;
		while ((ll = dr.read()) != null) {
			String w = ll[0].trim();
			String file = dir + "/htmls/" + w + ".html";
			if ((new File(file)).exists()) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), "utf-8"));
				String l;
				StringBuilder sb = new StringBuilder();
				while ((l = in.readLine()) != null) {
					sb.append(l);
				}
				String s = takeCollins(sb.toString());
				if (s.length() < 10) {
					bw.write("<h3>" + w + "</h3>");
				} else {
					bw.write(s);
				}
			} else {
				bw.write("<h3>" + w + "</h3>");
			}
		}
		dr.close();
		bw.close();
	}

	public static void parseHtml() throws Exception {
		String inputdir = dir + "/htmls";
		File f = new File(inputdir);
		String[] list = f.list();
		StringBuilder head = new StringBuilder();
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(dir + "/head"), "utf-8"));
			String l;

			while ((l = in.readLine()) != null) {
				head.append(l + "\n");
			}
			in.close();
		}

		int k = 0;
		BufferedWriter bw = null;
		for (String w : list) {
			if (++k % 2000 == 0 || bw == null) {
				if (bw != null)
					bw.close();
				bw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(dir + "/look" + k + ".html"),
						"utf-8"));
				bw.write(head.toString() + "\n");
				System.out.println(k);
			}
			String file = inputdir + "/" + w;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "utf-8"));
			String l;
			StringBuilder sb = new StringBuilder();
			while ((l = in.readLine()) != null) {
				sb.append(l);
			}
			String s = takeCollins(sb.toString());
			s = removeChineseExp(s);
			bw.write(s + "\n");
		}
		if (bw != null) {
			bw.close();
		}
	}

	public static void crawlOneWord(String w, String outputdir)
			throws Exception {
		String url = "http://dict.youdao.com/search?q=" + w
				+ "&keyfrom=dict.index#q%3D" + w + "%26keyfrom%3Ddict.index";
		URL oracle = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				oracle.openStream(), "UTF-8"));

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputdir + "/" + w + ".html"), "utf-8"));
		String inputLine;
		StringBuilder sb = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine + "\n");
			// bw.write(inputLine + "\n");
		}
		// String collins = takeCollins(sb.toString());
		bw.write(sb.toString());
		in.close();
		bw.close();
	}

	public static String takeCollins(String s) {
		// find <div id="collins"
		// find all <div
		// find all </div>
		class Pair {
			int start;
			int type;

			public Pair(int start, int type) {
				this.start = start;
				this.type = type;
			}
		}

		String result = "";
		int collinsstart = s.indexOf("<div id=\"collins\"");
		if (collinsstart < 0) {
			return "";
		} else {
			String ss = s.substring(collinsstart);
			List<Pair> pairs = new ArrayList<Pair>();
			Pattern p1 = Pattern.compile("<div");
			Matcher m1 = p1.matcher(ss);
			while (m1.find()) {
				int start = m1.start();
				pairs.add(new Pair(start, 1));
			}
			Pattern p2 = Pattern.compile("</div>");
			Matcher m2 = p2.matcher(ss);
			while (m2.find()) {
				int end = m2.end();
				pairs.add(new Pair(end, 2));
			}
			Collections.sort(pairs, new Comparator<Pair>() {

				@Override
				public int compare(Pair arg0, Pair arg1) {
					// TODO Auto-generated method stub
					return arg0.start - arg1.start;
				}

			});
			int flag = 0;
			for (int i = 0; i < pairs.size(); i++) {
				Pair p = pairs.get(i);
				if (p.type == 1) {
					flag++;
				} else {
					flag--;
				}
				if (flag == 0 && p.start != 0) {
					result = ss.substring(0, p.start);
					return result;
				}
			}
		}
		return "";
	}
}
