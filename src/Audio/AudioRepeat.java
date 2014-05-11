package Audio;

import gnu.getopt.Getopt;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioRepeat extends Thread
{
	/**
	 * Flag for debugging messages.
	 * If true, some messages are dumped to the console
	 * during operation.
	 */
	private static boolean DEBUG;
	private static final int DEFAULT_INTERNAL_BUFSIZ = 40960;
	private static final int DEFAULT_EXTERNAL_BUFSIZ = 40960;
	
	private TargetDataLine m_targetLine;
	private SourceDataLine m_sourceLine;
	private boolean m_bRecording;
	private int m_nExternalBufferSize;
	
	/*
	 * We have to pass an AudioFormat to descrive in which 
	 * format the audio data should be recorded and played.
	 */
	public AudioRepeat(AudioFormat format,
						int nInternalBufferSize,
						int nExternalBufferSize,
						String strMixerName)
		throws LineUnavailableException
	{
		Mixer mixer = null;
		if(strMixerName != null) 
		{
			Mixer.Info mixerInfo = AudioCommon.getMixerInfo(strMixerName);
			if(DEBUG) { System.out.println("AudioLoop.<init>(): mixer info; " + mixerInfo); }
			mixer = AudioSystem.getMixer(mixerInfo);
			if(DEBUG) { System.out.println("AudioLoop.<init>(): mixer: " + mixer);}
		}
		
		/*
		 * We retrieve and open the recording and the playback line.
		 */
		DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,  format, nInternalBufferSize);
		DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format, nInternalBufferSize);
		if(mixer != null)
		{
			m_targetLine = (TargetDataLine) mixer.getLine(targetInfo);
			m_sourceLine = (SourceDataLine) mixer.getLine(sourceInfo);
		}
		else
		{
			m_targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
			m_sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
		}
		if(DEBUG) { System.out.println("AudioLoop.<init>(): SourceDataLine: " + m_sourceLine); }
		if(DEBUG) { System.out.println("AudioLoop.<init>(): TargetDataLine: " + m_targetLine); }
		m_targetLine.open(format, nInternalBufferSize);
		m_sourceLine.open(format, nInternalBufferSize);
		m_nExternalBufferSize = nExternalBufferSize;
	}
	
	public void start()
	{
		m_targetLine.start();
		m_sourceLine.start();
		// start thread
		super.start();
	}
	
	/*
	 * public void stopRecording()
	 * {
	 * m_line.stop()
	 * m_line.close();
	 * m_bRecording = false;
	 * }
	 */
	
	public void run()
	{
		byte[] abBuffer = new byte[m_nExternalBufferSize];
		int nBufferSize = abBuffer.length;
		m_bRecording = true;
		while(m_bRecording)
		{
			if(DEBUG) { System.out.println("Trying to read: " +nBufferSize); }
			
			/*
			 * read a block of data from the recording line.
			 */
			int nBytesRead = m_targetLine.read(abBuffer,  0,  nBufferSize);
			if(DEBUG) { System.out.println("Read: " + nBytesRead); }
			
			/*
			 * And now, we write the block to the playback
			 * line.
			 */
			m_sourceLine.write(abBuffer,  0,  nBytesRead);
		}
	}
	
	public static void main(String[] args)
	{
		String strMixerName = null;
		float fFrameRate = 44100.0F;
		int nInternalBufferSize = DEFAULT_INTERNAL_BUFSIZ;
		int nExternalBufferSize = DEFAULT_EXTERNAL_BUFSIZ;
		
		Getopt g = new Getopt("AudioRepeat", args, "hlr:i:e:M:D");
		int c;
		
		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
			case 'h':
				printUsageAndExit();
			case 'l':
				AudioCommon.listMixersAndExit();
			case 'r':
				fFrameRate = Float.parseFloat(g.getOptarg());
				if (DEBUG) { System.out.println("AudioRepeat.main(): frame rate: " + fFrameRate); }
				break;
			case 'i':
				nInternalBufferSize = Integer.parseInt(g.getOptarg());
				if (DEBUG) { System.out.println("AudioRepeat.main(): mixer name: " + strMixerName); }
				break;
			case 'D':
				DEBUG = true;
				break;
			case '?':
				printUsageAndExit();
			default:
				System.out.println("AudioRepeat.main(): getopt() returned: " + c);
				break;
			}
		}
		
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fFrameRate, 16, 2, 4, fFrameRate, false);
		if (DEBUG) { System.out.println("AudioRepeat.main(): audio format: " + audioFormat); }
		
		AudioRepeat audioRepeat = null;
		try
		{
			audioRepeat = new AudioRepeat(audioFormat,
											nInternalBufferSize,
											nExternalBufferSize,
											strMixerName);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		audioRepeat.start();
	}
	
	private static void printUsageAndExit()
	{
		System.out.println("AudioRepeat: usage:");
		System.out.println("\tjava AudioRepeat -h");
		System.out.println("\tjava AudioRepeat -l");
		System.out.println("\tjava AudioLoop [-D] [-M <mixername>] [-e <buffersize>] [-i <buffersize>]");
		System.exit(1);
	}
}
