package edu.mit.d54.plugins.erl30;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

/**
 * This plugin was developed by a member of the EAPS department to commemorate
 * the 30th anniversary of the Earth Resources Lab.  It was shown on the
 * display on May 30, 2012.
 */
public class Erl30Plugin extends DisplayPlugin
{
	private final double dt;
	private int loopNum;
	private double time;

	private static final BufferedImage erlScrollingText;

	static
	{
		try
		{
			InputStream stream=Erl30Plugin.class.getResourceAsStream("/images/erl30/erltext.png");
			erlScrollingText=ImageIO.read(stream);
			stream.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final BufferedImage[][] waveimg = new BufferedImage[3][75];

	static
	{
		for (int j = 1; j < 4; j++)
		{
			for (int i = 0; i < 75; i++)
			{
				try
				{
					InputStream stream=Erl30Plugin.class.getResourceAsStream(String.format("/images/erl30/snap_%d_%02dg.png", j, i));
					waveimg[j-1][i]=ImageIO.read(stream);
					stream.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}


	public Erl30Plugin(Display2D display, double framerate) {
		super(display, framerate);
		dt=1./framerate;
		loopNum = 0;
		time=0;
	}

	@Override
	protected void loop() {
		Display2D display=getDisplay();
		time+=dt;
		Graphics2D g=display.getGraphics();

		float timeInLoop;
		float timeInSegment;
		int segment1Length = 5;
		int segment2Length = 10;
		int segment3Length = (int) Math.ceil((255 + 2*17)*dt);
		int transitionLength = 1;
		int totalLength = segment1Length + segment2Length + segment3Length;
		float maxBrightness;



	    int[][] ErlCoords = {
	  	      {0,0}, {0,1}, {0,2},
	  	      {1,0},
	  	      {2,0}, {2,1},
	  	      {3,0},
	  	      {4,0}, {4,1}, {4,2},
	  	
	  	      {6,0}, {6,1}, {6,2},
	  	      {7,0},        {7,2},
	  	      {8,0}, {8,1},
	  	      {9,0},        {9,2},
	  	      {10,0},       {10,2},
	  	
	  	      {12,0},
	  	      {13,0},
	  	      {14,0},
	  	      {15,0},
	  	      {16,0}, {16,1}, {16,2}
	  	    };
	  	
	    int[][] ThirtyCoords = {
                     {1,5}, {1,6}, {1,7},
	  	      {2,4},                      {2,8},
	  	                                  {3,8},
	  	                           {4,7},
	  	                                  {5,8},
	  	      {6,4},                      {6,8},
	  	             {7,5}, {7,6}, {7,7},
	  	
	  	             {9,5}, {9,6}, {9,7},
	  	      {10,4},                     {10,8},
	  	      {11,4},                     {11,8},
	  	      {12,4},                     {12,8},
	  	      {13,4},                     {13,8},
	  	      {14,4},                     {14,8},
	  	             {15,5},{15,6},{15,7}
	  	    };
	    float brightness = 0f;

	    timeInLoop = (float) time % totalLength;

	    if (timeInLoop < 0.5*dt)
	    {
	    	loopNum = (loopNum + 1) % 3;
	    }

	    // Segment 1
	    if (timeInLoop >=0 && timeInLoop < segment1Length)
	    {
	    	timeInSegment = timeInLoop;
	    	int framesInSegment = (int) Math.round(timeInSegment / dt);
	    	maxBrightness = 1f;

	    	g.drawImage(waveimg[loopNum][framesInSegment], 0, 0, null);
	    }


	    // Segment 2
	    else if (timeInLoop >= segment1Length && timeInLoop < segment1Length + segment2Length)
	    {
	    	timeInSegment = timeInLoop - segment1Length;
	    	if (timeInSegment <= transitionLength)
	    	{
	    		maxBrightness = timeInSegment / transitionLength;
	    	}
	    	else if (segment2Length - timeInSegment  <= transitionLength)
	    	{
	    		maxBrightness = (segment2Length - timeInSegment) / transitionLength;
	    	}
	    	else
	    	{
	    		maxBrightness = 1f;
	    	}
	    	for (int idx=0; idx<ErlCoords.length; idx++)
	    	{
	    		int y=ErlCoords[idx][0];
	    		int x=ErlCoords[idx][1];
	    		if (y<=5)
	    			display.setPixelHSB(x, y, 0f, 0.5f, 1f * maxBrightness);
	    		else if (y<=10)
	    			display.setPixelHSB(x, y, 0f, 0.67f, 0.7f * maxBrightness);
	    		else
	    			display.setPixelHSB(x, y, 0f, 0.85f, 0.3f * maxBrightness);
	    	}
	    	for (int idx=0; idx<ThirtyCoords.length; idx++)
	    	{
	    		int y=ThirtyCoords[idx][0];
	    		int x=ThirtyCoords[idx][1];
	    		if ((int)(Math.random() * (5)) == 0)
	    		{
	    			brightness = 1f;
	    		}
	    		else
	    		{
	    			brightness = 0.5f;
	    		}
	    		display.setPixelHSB(x, y, 1f, 0f, brightness * maxBrightness);
	    	}

	    }

	    // Segment 3
	    else if (timeInLoop >= segment1Length  + segment2Length && timeInLoop < totalLength)
	    {
	    	timeInSegment = timeInLoop - segment1Length - segment2Length;
	    	g.drawImage(erlScrollingText, 0, -(255 + 17) + (int) Math.round(timeInSegment/dt), null);
	    }

	}

}
