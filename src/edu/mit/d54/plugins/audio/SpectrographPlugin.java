package edu.mit.d54.plugins.audio;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

/**
 * This plugin displays a scrolling spectrograph using the audio input from AudioProcessor.  The colors are
 * customizable through the knob interface.  It was first used for the Independence Day display on July 4, 2012.
 */
public class SpectrographPlugin extends DisplayPlugin {
	private AudioProcessor audio;

	public SpectrographPlugin(Display2D display, double framerate) {
		super(display, framerate);
		registerKnob("amplitudeMaxRatio","20","Ratio between the maximum amplitude and the minimum visible amplitude");
		registerKnob("colorShiftRate","1","Color shift rate in pixels per frame");
		registerKnob("colorPaletteWidth","20","Width of the color palette in pixels");
		registerKnob("colorPaletteAngle","-45","Angle of the color palette in degrees");
		registerKnob("colorPalette","#FF0000,#FFFF00,#00FF00,#00FFFF,#0000FF,#FF00FF,#FF0000","Comma separated list of colors to represent the palette");
		registerKnob("colorPaletteInterpolate","false","Enable interpolation for the color palette");
		registerKnob("colorPreset","0","Set the color settings to one of the presets");
		audio=new AudioProcessor(display.getWidth());
	}

	@Override
	protected void onStart()
	{
		audio.openChannel();
		levels=new float[getDisplay().getWidth()][getDisplay().getHeight()];
		delta=new float[getDisplay().getWidth()][getDisplay().getHeight()];
	}
	
	@Override
	protected void onStop()
	{
		audio.closeChannel();
	}
	
	double amplitudeMaxRatio;
	double colorShiftRate;
	double colorPaletteWidth;
	double colorPaletteAngle;
	List<Color> colorPalette;
	boolean colorPaletteInterpolate;
	
	float[][] levels;
	float[][] delta;
	@Override
	protected void loop() {
		audio.frameUpdate();
		
		int w=getDisplay().getWidth();
		int h=getDisplay().getHeight();
			
		//shift
		for (int i=0; i<w; i++)
		{
			for (int j=0; j<h-1; j++)
			{
				levels[i][j]=levels[i][j+1];
				delta[i][j]=delta[i][j+1];
			}
		}
		for (int i=0; i<w; i++)
		{
			levels[i][h-1]=audio.getFFTMagBins()[i];
			delta[i][h-1]=levels[i][h-1]-levels[i][h-2];
		}

		//render
		for (int i=0; i<w; i++)
		{
			for (int j=0; j<h; j++)
			{
				float lvl=(float)(Math.log10(amplitudeMaxRatio*levels[i][j]/audio.getFFTMaxValue())/Math.log10(amplitudeMaxRatio));
				int x=i;
				int y=h-j-1;
				getDisplay().setPixelRGB(x,y,getColor(x,y,lvl));
			}
		}
	}
	
	@Override
	protected void knobChanged(String knob, String value)
	{
		if (knob.equals("amplitudeMaxRatio"))
			amplitudeMaxRatio=Double.parseDouble(value);
		else if (knob.equals("colorShiftRate"))
			colorShiftRate=Double.parseDouble(value);
		else if (knob.equals("colorPaletteWidth"))
			colorPaletteWidth=Double.parseDouble(value);
		else if (knob.equals("colorPaletteAngle"))
			colorPaletteAngle=Double.parseDouble(value)*Math.PI/180;
		else if (knob.equals("colorPalette"))
		{
			String[] tok=value.split(",");
			colorPalette=new ArrayList<Color>();
			for (String t : tok)
			{
				colorPalette.add(Color.decode(t.replace("#", "0x")));
			}
		}
		else if (knob.equals("colorPaletteInterpolate"))
			colorPaletteInterpolate=Boolean.parseBoolean(getKnobValue("colorPaletteInterpolate"));
		else if (knob.equals("colorPreset"))
		{
			switch (Integer.parseInt(value))
			{
			case 1:	//R/O/Y/W colors (indy)
				setKnob("colorShiftRate","0");
				setKnob("colorPaletteWidth","17");
				setKnob("colorPaletteAngle","90");
				setKnob("colorPalette","#FF0000,#FF4000,#FFFF00,#FFFFFF");
				setKnob("colorPaletteInterpolate","true");
				break;
			case 2: //B/magenta/light blue slow scroll
				setKnob("colorShiftRate","0.1");
				setKnob("colorPaletteWidth","25");
				setKnob("colorPaletteAngle","-100");
				setKnob("colorPalette","#6666FF,#0000FF,#FF00FF,#6666FF");
				setKnob("colorPaletteInterpolate","true");
				break;
			case 3: //full color fast scroll down
				setKnob("colorShiftRate","1");
				setKnob("colorPaletteWidth","55");
				setKnob("colorPaletteAngle","-90");
				setKnob("colorPalette","#FF0000,#FFFF00,#00FF00,#00FFFF,#0000FF,#FF00FF,#FF0000");
				setKnob("colorPaletteInterpolate","true");
				break;
			case 4: //R/W/B fixed
				setKnob("colorShiftRate","0");
				setKnob("colorPaletteWidth","9");
				setKnob("colorPaletteAngle","0");
				setKnob("colorPalette","#FF0000,#FFFFFF,#0000FF");
				setKnob("colorPaletteInterpolate","false");
				break;
			case 5: //red/blue slow scroll
				setKnob("colorShiftRate","0.5");
				setKnob("colorPaletteWidth","90");
				setKnob("colorPaletteAngle","-90");
				setKnob("colorPalette","#FF0000,#0000FF,#FF0000");
				setKnob("colorPaletteInterpolate","true");
				break;
			case 6: //R/W/B diagonal stripes scrolling
				setKnob("colorShiftRate","0.2");
				setKnob("colorPaletteWidth","20");
				setKnob("colorPaletteAngle","45");
				setKnob("colorPalette","#FFFFFF,#FF0000,#FF0000,#FF0000,#FF0000,#FFFFFF,#0000FF,#0000FF,#0000FF,#0000FF");
				setKnob("colorPaletteInterpolate","false");
			}
		}
	}
	
	private int getColor(int x, int y, float scale)
	{
		scale=Math.min(1f, Math.max(0f, scale));
		float[] ret=new float[3];
		
		double colorOffset=getTime()*colorShiftRate*getFramerate();
		double xColorPos=x+0.5+colorOffset*Math.cos(colorPaletteAngle);
		double yColorPos=y+0.5+colorOffset*Math.sin(colorPaletteAngle);
		double paletteAngle=colorPaletteAngle-Math.atan2(yColorPos, xColorPos);
		double colorDist=Math.sqrt(xColorPos*xColorPos+yColorPos*yColorPos);
		double coPalettePos=colorDist*Math.cos(paletteAngle);
		
		double coPalettePosMod=coPalettePos%colorPaletteWidth;
		if (coPalettePos<0)
			coPalettePosMod=colorPaletteWidth+coPalettePosMod;
		
		if (!colorPaletteInterpolate)
		{
			double palettePos=coPalettePosMod*(colorPalette.size())/colorPaletteWidth;
			ret=colorPalette.get((int)Math.floor(palettePos)).getColorComponents(new float[3]);
		}
		else
		{
			double palettePos=coPalettePosMod*(colorPalette.size()-1)/colorPaletteWidth;
			float diff=(float)(palettePos%1);
			int colorA=(int)Math.floor(palettePos);
			int colorB=(int)Math.ceil(palettePos);
			if (colorA<0 || colorB>=colorPalette.size())
				throw new IllegalArgumentException("colorA="+colorA+" colorB="+colorB);
			float[] a=colorPalette.get(colorA).getColorComponents(new float[3]);
			float[] b=colorPalette.get(colorB).getColorComponents(new float[3]);
			a[0]=a[0]*(1-diff)+b[0]*diff;
			a[1]=a[1]*(1-diff)+b[1]*diff;
			a[2]=a[2]*(1-diff)+b[2]*diff;
			ret=a;
		}
		return new Color(ret[0]*scale,ret[1]*scale,ret[2]*scale).getRGB();
	}
}
