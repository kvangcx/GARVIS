import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;


public class AudioIn {

	private static boolean stopped = true;
	
	public static void main(String[] args) {
		
		System.out.println("HELLO TESTING");
		AudioFormat format = new AudioFormat(8000.0f, 16, 2, true, true);
		TargetDataLine line;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		
		if(!AudioSystem.isLineSupported(info)) {
			// Handle the error ...
		}
		// Obtain and open the line. 
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			stopped = false;
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int numBytesRead;
			byte[] data = new byte[line.getBufferSize() / 5];
			
			// Begin audio capture.
			line.start();
			
			// Here, stopped is a global boolean set by another thread. 
			while(!stopped) {
				// Read the next chunk of data from the TargetDataLine.
				numBytesRead = line.read(data, 0, data.length);
				// Save this chunk of data. 
				out.write(data, 0, numBytesRead);
			} // end of while loop 
			
			line.close();
		} catch (LineUnavailableException e) {
			// Handle the error ... 
		}
	}
}

