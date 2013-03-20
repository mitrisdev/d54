package edu.mit.d54.plugins.audio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

/**
 * The AudioProcessor reads data from the computer's line input to facilitate audio visualization plugins.
 * A plugin should call frameUpdate on every frame to refresh the AudioProcessor with new data.  The raw
 * audio samples as well as FFT magnitude are available via class methods.
 */
public class AudioProcessor {
	
	private static final int FFT_LEN=2048;
	private static final float SAMPLE_RATE=44100;

	private static TargetDataLine line;
	private AudioInputStream input;
	
	private final int fftNumBins;
	private final double fftMaxFreq;
	private final boolean fftBinLog;
	private final double fftScaleDecay;
	private final double freqScalePower;
	
	private byte[] frameRaw=new byte[FFT_LEN*2];
	private int[] frameSamples=new int[FFT_LEN];
	private float[] fftMag;
	private float[] fftMagBinned;
	private float fftMaxValue;
	
	static
	{
		try {
			line=AudioSystem.getTargetDataLine(
					new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,SAMPLE_RATE,16,1,2,SAMPLE_RATE,false));
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a new AudioProcessor around the line in device.
	 * @param fftNumBins Number of FFT bins to provide
	 * @param fftMaxFreq Maximum audio frequency (in Hz) to return through FFT
	 * @param fftBinLog Scale the FFT bins to equal width in log frequency.  Otherwise, FFT bins
	 * will be equal width in frequency.
	 * @param fftScaleDecay The exponential rate (ratio per frame) that the peak FFT amplitude is 
	 * reduced.  This should be a number between 0 and 1.
	 * @param freqScalePower The exponential rate to reduce effective amplitude versus frequency.  
	 * Amplitude is multipled by (freq^^freqScalePower).  Typically this is used to represent the
	 * deemphasize bass versus the middle and high frequencies which are perceived to be louder.
	 */
	public AudioProcessor(int fftNumBins, double fftMaxFreq, boolean fftBinLog, double fftScaleDecay, double freqScalePower)
	{
		this.fftNumBins=fftNumBins;
		this.fftMaxFreq=fftMaxFreq;
		this.fftBinLog=fftBinLog;
		this.fftScaleDecay=fftScaleDecay;
		this.freqScalePower=freqScalePower;
	}
	
	/**
	 * Create a new AudioProcessor around the line in device with generally useful defaults.
	 * fftMaxFreq is set to 3500 Hz, fftBinLog is true, fftScaleDecay is 0.998, and freqScalePower
	 * is 0.125.
	 * @param fftNumBins Number of FFT bins to provide
	 */
	public AudioProcessor(int fftNumBins)
	{
		this(fftNumBins,3500,true,0.998,0.125); //was 0.25
	}
	
	/**
	 * @return the raw audio samples from the current frame.
	 */
	public int[] getFrameSamples()
	{
		return frameSamples;
	}
	
	/**
	 * Get the FFT magnitude bins from the current frame.  The size of this array is determined
	 * by the fftNumBins parameter.  The frequency width and amplitude of the bins is affected by
	 * the fftMaxFreq, fftBinLog, and freqScalePower parameters.
	 * @return the FFT magnitude bins from the current frame.
	 */
	public float[] getFFTMagBins()
	{
		return fftMagBinned;
	}
	
	/**
	 * Get the peak value seen by the FFT in the current frame, or the decaying previous higher peak.
	 * The rate at which the previous peak decays is determined by fftScaleDecay.
	 * @return the FFT peak value
	 */
	public float getFFTMaxValue()
	{
		return fftMaxValue;
	}
	
	/**
	 * Open the audio channel so data can be captured.  This must be called once before frameUpdate or the
	 * data accessors are called.
	 */
	public void openChannel()
	{
		try
		{
			line.open();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		line.start();
		line.drain();
		input=new AudioInputStream(line);
	}
	
	/**
	 * Update the AudioProcessor with new sample data.  This method should be called every time the
	 * DisplayPlugin is updated and before the audio data is accessed.
	 */
	public void frameUpdate()
	{
		try
		{
			while (input.available()>frameRaw.length) //flush the stream
			{
				byte[] flush=new byte[2];
				input.read(flush);
			}
			int avail=input.available();
			// System.out.println("Reading "+avail);
			if (frameRaw.length>avail)
			{
				System.arraycopy(frameRaw, avail, frameRaw, 0, frameRaw.length-avail);
				input.read(frameRaw,frameRaw.length-avail,avail);
			}
			else
			{
				input.read(frameRaw,0,frameRaw.length);
			}
			ByteBuffer bb=ByteBuffer.wrap(frameRaw);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			float[] fft=new float[FFT_LEN];
			for (int i=0; i<fft.length; i++)
			{
				frameSamples[i]=bb.getShort(i*2);
				fft[i]=frameSamples[i];
			}
			FloatFFT_1D fftOp=new FloatFFT_1D(FFT_LEN);
			fftOp.realForward(fft);
			
			//get fft bins
			fftMag=fft2mag(fft);
			if (fftBinLog)
				fftMagBinned=rebinLog(fftMag,fftMaxFreq,fftNumBins,freqScalePower);
			else
				fftMagBinned=rebin(fftMag,fftMaxFreq,fftNumBins,freqScalePower);
			for (int i=0; i<fftMagBinned.length; i++)
			{
				fftMaxValue=Math.max(fftMaxValue,fftMagBinned[i]);
			}
			fftMaxValue*=fftScaleDecay;
			// System.out.println("max="+fftMaxValue);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private static float[] fft2mag(float[] data)
	{
		float[] ret=new float[(data.length/2)-1];
		for (int i=0; i<ret.length; i++)
		{
			float a=data[2*i+2];
			float b=data[2*i+3];
			ret[i]=(float)Math.sqrt(a*a+b*b);
		}
		return ret;
	}
	
	private static float[] rebin(float[] data, double maxFreq, int nbins, double freqScalePwr)
	{
		int count=freqToIndex(maxFreq);
		float[] ret=new float[nbins];
		int high=0;
		int low=0;
		for (int i=0; i<nbins; i++)
		{
			low=high;
			high=(count*(i+1))/nbins;
			float val=0;
			for (int j=low; j<high; j++)
			{
				val+=data[j]*Math.pow(indexToFreq(j), freqScalePwr);
			}
			ret[i]=val/(high-low);
		}
		return ret;
	}
	
	private static float[] rebinLog(float[] data, double maxFreq, int nbins, double freqScalePwr)
	{
		int count=freqToIndex(maxFreq);
		float[] ret=new float[nbins];
		int high=0;
		int low=0;
		for (int i=0; i<nbins; i++)
		{
			low=high;
			high=(int)Math.round(count*Math.expm1((i+1.0)/nbins)/(Math.E-1));
		//	System.out.println("freq bin "+high+" freq "+((high*1.0/FFT_LEN)*SAMPLE_RATE));
			float val=0;
			for (int j=low; j<high; j++)
			{
				val+=data[j]*Math.pow(indexToFreq(j), freqScalePwr);
			}
			ret[i]=val/(high-low);
		}
		return ret;
	}
	
	private static double indexToFreq(int index)
	{
		return (index*1.0/FFT_LEN)*SAMPLE_RATE;
	}
	
	private static int freqToIndex(double freq)
	{
		return (int)Math.round(freq/SAMPLE_RATE*FFT_LEN);
	}
	
	/**
	 * Stop collecting audio data from the line.  After this is called, openChannel must be called before
	 * audio data can be used again.
	 */
	public void closeChannel()
	{
		line.stop();
	}
}