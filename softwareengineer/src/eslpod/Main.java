package eslpod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.filehandlers.DR;
import javatools.filehandlers.DW;

public class Main {

	static String dir = "C:/Users/congle/Downloads/eslpod/new";

	public static void main(String[] args) throws IOException {
		main_step1();
		main_step2();
	}

	public static void main_step1() throws IOException {
		DW dw = new DW(dir + "/issue_id");
		for (int start = 0; start < 20; start += 20) {
			List<Integer> r = analyzeIndexPage("http://www.eslpod.com/website/show_all.php?cat_id=-59456&low_rec="
					+ start);
			for (int x : r) {
				dw.write(x);
			}
			dw.flush();
		}
		dw.close();
	}

	public static void main_step2() throws IOException {
		DR dr = new DR(dir + "/issue_id");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(dir + "/eslpod.html"), "UTF-8"));
		String[] l;
		while ((l = dr.read()) != null) {
			String str_id = l[0];
			String url = "http://www.eslpod.com/website/show_podcast.php?issue_id="
					+ str_id;
			System.out.println(url);
			String sub = analyzeOnePage(url);
			bw.write(sub + "\n");
			bw.flush();
			// break;
		}
		dr.close();
		bw.close();
	}

	public static List<Integer> analyzeIndexPage(String str_url)
			throws IOException {
		List<Integer> results = new ArrayList<Integer>();
		String content = loadurl(str_url);
		Pattern p = Pattern.compile("show_podcast\\.php\\?issue_id=(\\d+)");
		Matcher m = p.matcher(content);
		while ((m.find())) {
			String x = m.group(1);
			results.add(Integer.parseInt(x));
			System.out.println(x);
		}
		return results;
	}

	public static String analyzeOnePage(String urlstr) throws IOException {
		String content = loadurl(urlstr);
		try {

			int t1 = content.indexOf("<b class=\"pod_title\">");
			int t2 = content.indexOf("</b>", t1 + 1);
			String title = content.substring(t1, t2 + "</b>".length());
			int s1 = content
					.indexOf("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"podcast_table_home\">");
			int s2 = content.indexOf("</table>", s1 + 1);
			int s3 = content.indexOf("</table>", s2 + 1);
			String sub = content.substring(s1, s3 + "</table>".length());
			return title + "\n" + sub;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String loadurl(String url) {
		StringBuilder sb = new StringBuilder();
		try {
			URL oracle = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					oracle.openStream()));

			// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			// new FileOutputStream(a), "utf-8"));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine + "\n");
				// bw.write(inputLine + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
