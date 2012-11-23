package what2remember;

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
import java.util.Date;

public class Main {
	static long currentTime = 0;

	public static double howManyDaysBefore(long time) {
		return (currentTime - time) * 1.0 / 1000 / 60 / 60 / 24;
	}

	public static void dealAFile(String filename) throws IOException {
		File f = new File(filename);
		long time = f.lastModified();
		double d = howManyDaysBefore(time);
		boolean toPrint = false;
		if (d <= 2)
			toPrint = true;
		for (int i = 2; i <= 10; i++) {
			double k = Math.pow(2, i);
			if (d >= k && d <= k + 1) {
				toPrint = true;
				break;
			}
		}
		if (toPrint) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
			String l;
			while ((l = br.readLine()) != null) {
				bw.write(l + "\r\n");
			}
			br.close();
		}
		//		System.out.println(time);
	}

	static BufferedWriter bw;

	public static void main(String[] args) throws IOException {
		currentTime = (new Date()).getTime();
		String output = args[0];
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"));
		//dealAFile("C:/Users/clzhang/Dropbox/paper/acl04-perc.ps");
		File file_dir = new File(".");
		String[] list = file_dir.list();
		for (String onef : list) {
			if (!onef.equals("report") && !onef.endsWith("~")) {
				bw.write("#####" + onef+"\r\n");
				dealAFile(onef);
			}
		}
		bw.close();
	}
}
