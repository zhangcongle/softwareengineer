package audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Mp3chunk {
	public static void main(String[] args)
			throws UnsupportedAudioFileException, IOException {
		File file = new File("C:/Users/IBM_ADMIN/Desktop/ESLPod806.mp3");
		AudioInputStream in = AudioSystem.getAudioInputStream(file);
		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
				16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(), false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);
	}
}
