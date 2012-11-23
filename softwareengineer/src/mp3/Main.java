package mp3;

import javatools.administrative.D;
import javatools.filehandlers.DR;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import org.tritonus.share.sampled.AudioSystemShadow;
import org.tritonus.share.sampled.file.AudioOutputStream;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	public static void main(String[] args) throws IOException {
		// testPlay(args[0]);
		// testWrite(args[0], args[0] + ".wav", 80, 160);
		HashMap<Integer, Integer> starts = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> ends = new HashMap<Integer, Integer>();
		getStartEndTime("C:/Users/congle/Downloads/eslpod/a.txt", starts,
				ends);
		extractSlowDialog(
				"C:/Users/congle/Music/iTunes/iTunes Media/Podcasts/English as a Second Language Podcast",
				"C:/Users/congle/Downloads/eslpod/chunk", starts, ends);

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
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
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
