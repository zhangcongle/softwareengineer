package mp3;

//import javatools.administrative.D;
import javatools.administrative.D;
import javatools.filehandlers.DR;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import org.tritonus.share.sampled.AudioSystemShadow;
import org.tritonus.share.sampled.file.AudioOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkBySentence {
	static Pattern p = Pattern.compile("\\[(.*?)\\](.*?)");

	public static void lc(String input_lrc, String input_mp3,
			String output_chunks) {
		DR dr = new DR(input_lrc);
		List<Double> starts = new ArrayList<Double>();
		String[] l;
		starts.add(0.0);
		while ((l = dr.read()) != null) {
			String line = l[0];
			int p0 = line.lastIndexOf(']');
			String t = line.substring(1, p0);
			String s = line.substring(p0 + 1);
			// parse the time
			String[] ab = t.split(":");
			try {
				int min = Integer.parseInt(ab[0]);
				double sec = Double.parseDouble(ab[1]);
				double stop = 60 * min + sec;
				char s0 = s.charAt(0);
				if (s0 >= 'A' && s0 <= 'Z') {
					starts.add(stop);
				}
				// D.p(all, s);
			} catch (Exception e) {

			}
		}
		// starts.add(9999.0);
		try {
			chunk(input_mp3, output_chunks, starts);
		} catch (Exception e) {

		}
		dr.close();
	}

	public static void main(String[] args) throws IOException {
		String dir = "/Users/clzhang/Dropbox/read/newconcept4/audio";
		String outputdir = "/Users/clzhang/Downloads/nc4chunks";
		String[] list = (new File(dir)).list();
		for (String l : list) {
			if (l.endsWith(".mp3")) {
				String fmp3 = dir + "/" + l;
				String flc = dir + '/' + l.replace("mp3", "lrc");
				String outputprefix = outputdir + "/" + l.replace(".mp3", "");
				D.p(outputprefix);
				lc(flc, fmp3, outputprefix);
			}
		}
		// String input0 =
		// "/Users/clzhang/Dropbox/read/newconcept4/audio/01.lrc";
		// lc(input0);
	}

	public static void main2(String[] args) throws IOException {
		String inputdir = "C:/Users/congle/Downloads/nc/newconcept";
		String outputdir = "C:/Users/congle/Downloads/nc/newconcept_chunk/";
		// chunk(args[0], args[1]);
		chunk(inputdir + "/41.mp3", outputdir + "/41", new int[] { 0, 18, 27,
				36, 48, 59, 70, 79, 92, 107, 121, 131, 145, 158, 171, 183, 197,
				201, 220, 239, 249, 262 });
		// testPlay(args[0]);
		// // testWrite(args[0], args[0] + ".wav", 80, 160);
		// HashMap<Integer, Integer> starts = new HashMap<Integer, Integer>();
		// HashMap<Integer, Integer> ends = new HashMap<Integer, Integer>();
		// getStartEndTime("/home/clzhang/hhome/data_eslpod/content.txt",
		// starts,
		// ends);
		//
		// extractSlowDialog(
		// "/home/clzhang/hhome/Music/iTunes/iTunes Media/Podcasts/English as a Second Language Podcast",
		// "/home/clzhang/hhome/data_eslpod/chunk", starts, ends);

	}

	public static void chunk(String input, String output, List<Double> seconds) {
		try {
			File file = new File(input);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);

			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			AudioInputStream din = AudioSystem.getAudioInputStream(
					decodedFormat, in);
			for (int i = 1; i < seconds.size(); i++) {
				double startSecond = seconds.get(i - 1);
				double endSecond = seconds.get(i);
				if (endSecond > startSecond) {
					long framesOfAudioToCopy = (long) ((endSecond - startSecond) * decodedFormat
							.getFrameRate());
					AudioInputStream shortenedStream = new AudioInputStream(
							din, decodedFormat, framesOfAudioToCopy);
					AudioSystem.write(shortenedStream,
							AudioFileFormat.Type.WAVE, new File(output + "_"
									+ i + ".wav"));
				}
			}
			long leftframes = din.getFrameLength();
			AudioInputStream shortenedStream = new AudioInputStream(din,
					decodedFormat, leftframes);
			AudioSystem.write(shortenedStream, AudioFileFormat.Type.WAVE,
					new File(output + "_end.wav"));
			din.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void chunk(String input, String output, int[] seconds) {
		try {
			File file = new File(input);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);

			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			AudioInputStream din = AudioSystem.getAudioInputStream(
					decodedFormat, in);
			for (int i = 1; i < seconds.length; i++) {
				int startSecond = seconds[i - 1];
				int endSecond = seconds[i];
				if (endSecond > startSecond) {
					long framesOfAudioToCopy = (endSecond - startSecond)
							* (int) decodedFormat.getFrameRate();
					AudioInputStream shortenedStream = new AudioInputStream(
							din, decodedFormat, framesOfAudioToCopy);
					AudioSystem.write(shortenedStream,
							AudioFileFormat.Type.WAVE, new File(output + "_"
									+ i + ".wav"));
				}
			}
			din.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void chunk(String input, String output) {
		try {
			File file = new File(input);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// output

			int totalByte = 0;
			int framesize = decodedFormat.getFrameSize();
			float framerate = decodedFormat.getFrameRate();

			int bytesPerSecond = (int) (framesize * framerate);
			D.p("bytesPerSecond", bytesPerSecond);
			List<Byte> all = new ArrayList<Byte>();
			byte[] data = new byte[4096];
			int isEnd = 0;
			int howmany = 0;
			do {
				isEnd = din.read(data);
				for (int i = 0; i < isEnd; i++) {
					all.add(data[i]);
				}
				// data.add(x);
				// isEnd = din
				// .read(data, bytesPerSecond * howmany, bytesPerSecond);
				// // isEnd = din.read(data);
				// howmany++;
				// D.p(isEnd);
			} while (isEnd != -1);
			int allbytes = all.size();
			List<Integer> stops = new ArrayList<Integer>();
			stops.add(0);
			int KK = 1;
			int chunkBytes = bytesPerSecond / KK;
			D.p("allbytes", allbytes, allbytes / bytesPerSecond);
			for (int i = 0; i < allbytes / chunkBytes; i++) {
				int off = i * chunkBytes;
				int average = 0;
				for (int k = 0; k < chunkBytes && off + k < all.size(); k++) {
					byte b = all.get(off + k);
					average += Math.abs(b);
				}
				average /= (chunkBytes * 1.0);

				if (average < 15) {
					int sec = i;
					int last = stops.get(stops.size() - 1);
					if (sec - last > 10) {// at least 2 second a chunk
						stops.add(sec);
					}
				}
				// D.p(sec, average);
			}
			din.close();
			in.close();

			in = AudioSystem.getAudioInputStream(file);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			for (int i = 1; i < stops.size(); i++) {
				int startSecond = stops.get(i - 1);
				int endSecond = stops.get(i);
				if (endSecond > startSecond) {
					long framesOfAudioToCopy = (endSecond - startSecond)
							* (int) decodedFormat.getFrameRate() / KK;
					AudioInputStream shortenedStream = new AudioInputStream(
							din, decodedFormat, framesOfAudioToCopy);
					AudioSystem.write(shortenedStream,
							AudioFileFormat.Type.WAVE, new File(output + "_"
									+ i + ".wav"));
				}
			}
			din.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getStartEndTime(String input,
			HashMap<Integer, Integer> starts, HashMap<Integer, Integer> ends)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(input), "utf-8"));
		String l;
		Pattern p1 = Pattern.compile("(\\d+)");
		Pattern p2 = Pattern.compile("(\\d+):(\\d+)");
		int now = -1;
		while ((l = br.readLine()) != null) {
			if (l.startsWith("ESL Pod") || l.startsWith("ESLPod")) {
				Matcher m1 = p1.matcher(l);
				if (m1.find()) {
					String a = m1.group(1);
					now = Integer.parseInt(a);
				}
			}
			if (l.startsWith("Slow dia")) {
				Matcher m2 = p2.matcher(l);
				if (m2.find()) {
					// String a = m2.group();
					int sec = min2sec(m2.group(1), m2.group(2));
					if (!starts.containsKey(now)) {
						starts.put(now, sec);
					}
					// D.p(now, "start", a);
				}
			}
			if (l.startsWith("Explana")) {
				Matcher m2 = p2.matcher(l);
				if (m2.find()) {
					// String a = m2.group();
					int sec = min2sec(m2.group(1), m2.group(2));
					if (!ends.containsKey(now)) {
						ends.put(now, sec);
					}
					// D.p(now, "end", a);
				}
			}
		}
		br.close();
	}

	public static int min2sec(String a1, String a2) {
		int b1 = Integer.parseInt(a1);
		int b2 = Integer.parseInt(a2);
		return b1 * 60 + b2;
	}

	public static void testPlay(String filename) {
		try {
			File file = new File(filename);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// Play now.
			rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e) {
			// Handle exception.
			e.printStackTrace();
		}
	}

	static void extractSlowDialog(String dir, String output_dir,
			HashMap<Integer, Integer> starts, HashMap<Integer, Integer> ends) {
		File in_dir = new File(dir);
		String[] list = in_dir.list();
		Pattern p1 = Pattern.compile("(\\d+)");
		for (String f : list) {
			if (f.startsWith("English Cafe")) {
				continue;
			}
			int episode = -1;
			try {
				Matcher m = p1.matcher(f);
				if (m.find()) {
					episode = Integer.parseInt(m.group());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String input = dir + "/" + f;
			String output = output_dir + "/"
					+ f.replaceAll(" ", "_").replace("mp3", "wav");
			if (starts.containsKey(episode) && ends.containsKey(episode)) {
				int start = starts.get(episode);
				int end = ends.get(episode);
				System.out.println(input + "\t" + output + "\t" + start + "\t"
						+ end);
				submp3(input, output, start, end);
			} else {
				System.err.println(input + "\t" + output);
			}
			// submp3(input, output, start, end);
		}
	}

	public static void submp3(String input, String output, int startSecond,
			int endSecond) {
		try {
			File file = new File(input);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;

			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// output

			int totalByte = 0;
			int framesize = decodedFormat.getFrameSize();
			float framerate = decodedFormat.getFrameRate();
			int bytesPerSecond = (int) (framesize * framerate);
			// skip
			{
				OutputStream f = new ByteArrayOutputStream();
				long framesOfAudioToCopy = startSecond
						* (int) decodedFormat.getFrameRate();

				AudioInputStream shortenedStream = new AudioInputStream(din,
						decodedFormat, framesOfAudioToCopy);
				AudioSystem
						.write(shortenedStream, AudioFileFormat.Type.WAVE, f);
			}
			{

				long framesOfAudioToCopy = (endSecond - startSecond)
						* (int) decodedFormat.getFrameRate();
				AudioInputStream shortenedStream = new AudioInputStream(din,
						decodedFormat, framesOfAudioToCopy);
				AudioSystem.write(shortenedStream, AudioFileFormat.Type.WAVE,
						new File(output));
			}

			// Play now.
			// rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e) {
			// Handle exception.
			e.printStackTrace();
		}
	}

	public static void testWrite(String input, String output, int startSecond,
			int endSecond) {
		try {
			File file = new File(input);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			AudioInputStream din = null;

			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// output
			SourceDataLine line_out = getLine(decodedFormat);

			int totalByte = 0;
			int framesize = decodedFormat.getFrameSize();
			float framerate = decodedFormat.getFrameRate();
			int bytesPerSecond = (int) (framesize * framerate);
			// skip
			{
				OutputStream f = new ByteArrayOutputStream();
				long framesOfAudioToCopy = startSecond
						* (int) decodedFormat.getFrameRate();

				AudioInputStream shortenedStream = new AudioInputStream(din,
						decodedFormat, framesOfAudioToCopy);
				AudioSystem
						.write(shortenedStream, AudioFileFormat.Type.WAVE, f);
			}
			{

				long framesOfAudioToCopy = (endSecond - startSecond)
						* (int) decodedFormat.getFrameRate();
				AudioInputStream shortenedStream = new AudioInputStream(din,
						decodedFormat, framesOfAudioToCopy);
				AudioSystem.write(shortenedStream, AudioFileFormat.Type.WAVE,
						new File(output));
			}

			// Play now.
			// rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e) {
			// Handle exception.
			e.printStackTrace();
		}
	}

	private static void rawplay(AudioFormat targetFormat, AudioInputStream din)
			throws IOException, LineUnavailableException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				// System.out.println(data2string(data));
				if (nBytesRead != -1) {
					nBytesWritten = line.write(data, 0, nBytesRead);
				}
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private static String data2string(byte[] data) {
		int average = 0;
		StringBuilder sb = new StringBuilder();
		for (byte d : data) {
			average += Math.abs(d);
			// sb.append(d + " ");
		}
		average /= data.length;
		return average + "";
	}

	private static SourceDataLine getLine(AudioFormat audioFormat)
			throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}
}
