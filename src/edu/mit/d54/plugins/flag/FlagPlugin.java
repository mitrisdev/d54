package edu.mit.d54.plugins.flag;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

/**
 * Plugin to display a stylized American flag for the July 4th Independence Day display.
 */
public class FlagPlugin extends DisplayPlugin {

	public FlagPlugin(Display2D display, double framerate) {
		super(display, framerate);
		registerKnob("wavePeriodT","10","Wave oscillation period in seconds");
		registerKnob("wavePeriodX","15","Wave oscillation period in x pixels");
		registerKnob("wavePeriodY","10","Wave oscillation period in y pixels");
		registerKnob("waveDepth","0.4","Wave modulation depth");
	}

	@Override
	protected void loop() {
		float wavePeriodT=Float.parseFloat(getKnobValue("wavePeriodT"));
		float wavePeriodX=Float.parseFloat(getKnobValue("wavePeriodX"));
		float wavePeriodY=Float.parseFloat(getKnobValue("wavePeriodY"));
		float waveDepth=Float.parseFloat(getKnobValue("waveDepth"));
		
		Display2D d=getDisplay();
		for (int x=0; x<d.getWidth(); x++)
		{
			for (int y=0; y<d.getHeight(); y++)
			{
				float intensity=(float)((1-(waveDepth/2))+(waveDepth/2)*Math.cos((getTime()/wavePeriodT+x/wavePeriodX+y/wavePeriodY)*2*Math.PI));
				if (x<5 && y<7)
				{
					if ((x+y)%2==0)
						d.setPixelHSB(x,y,0.666f,1,intensity);
					else
						d.setPixelHSB(x,y,0,0,intensity);
				}
				else
				{
					if (x%2==0)
						d.setPixelHSB(x,y,0,1,intensity);
					else
						d.setPixelHSB(x,y,0,0,intensity);
				}
			}
		}
	}

}
