package edu.mit.d54.plugins.audio;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

/**
 * This plugin displays a multi-band VU meter using the audio input from AudioProcessor.  The colors are
 * customizable through the knob interface.  It was first used for the Independence Day display on July 4, 2012.
 */
public class VUMeterPlugin extends DisplayPlugin {
	private AudioProcessor audio;

	public VUMeterPlugin(Display2D display, double framerate) {
		super(display, framerate);
		registerKnob("amplitudeMaxRatio","30","Ratio between the maximum amplitude and the minimum visible amplitude");
		registerKnob("colorShiftRate","0","Color shift rate in pixels per frame");
		registerKnob("colorPaletteWidth","17","Width of the color palette in pixels");
		registerKnob("colorPaletteAngle","90","Angle of the color palette in degrees");
		registerKnob("colorPalette","#FF0000,#FFFF00,#FFFF00,#00FF00,#00FF00,#00FF00,#00FF00,#00FF00","Comma separated list of colors to represent the palette");
		registerKnob("colorPaletteInterpolate","false","Enable interpolation for the color palette");
		registerKnob("colorPreset","2","Set the color settings to one of the presets");
		audio=new AudioProcessor(display.getWidth());
	}

	@Override
	protected void onStart()
	{
		audio.openChannel();
	}
	
	@Override
	protected void onStop()
	{
		audio.closeChannel();
	}
	
	private double amplitudeMaxRatio;
	private double colorShiftRate;
	private double colorPaletteWidth;
	private double colorPaletteAngle;
	private List<Color> colorPalette;
	private boolean colorPaletteInterpolate;
	
	private float[] fftBinsOld=new float[getDisplay().getWidth()];
	@Override
	protected void loop() {
			audio.frameUpdate();
			//render
			Graphics2D g=getDisplay().getGraphics();
			int w=getDisplay().getWidth();
			int h=getDisplay().getHeight();
			for (int i=0; i<w; i++)
			{
				double level=Math.log10(amplitudeMaxRatio*audio.getFFTMagBins()[i]/audio.getFFTMaxValue())/Math.log10(amplitudeMaxRatio);
				double levelOld=Math.log10(amplitudeMaxRatio*fftBinsOld[i]/audio.getFFTMaxValue())/Math.log10(amplitudeMaxRatio);
			//	double level=fftBins[i]/max*1.5f;
				for (int j=0; j<h; j++)
				{
					if (j>=(int)Math.round(h-level*h))
						getDisplay().setPixelRGB(i, j, getColor(i,j,1));
				}
		/*		if (level>levelOld)
				{
					g.setColor(Color.red);
					g.drawLine(i,(int)(h-levelOld*h),i,(int)(h-level*h));
					g.setColor(Color.green);
					g.drawLine(i, h, i, (int)(h-levelOld*h));
				}
				else
				{
					g.setColor(Color.green);
			//		g.setColor(Color.getHSBColor((float) Math.min(0.4f, 0.4f-(level-levelOld)), 1, 1));
					g.drawLine(i, h, i, (int)(h-level*h));
				}*/
			}
			fftBinsOld=audio.getFFTMagBins();
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
			case 1:	// R/W/B horiz 
				setKnob("colorShiftRate","0");
				setKnob("colorPaletteWidth","9");
				setKnob("colorPaletteAngle","0");
				setKnob("colorPalette","#FF0000,#FFFFFF,#0000FF");
				setKnob("colorPaletteInterpolate","false");
				break;
			case 2: // R/Y/G vert (traditional VU meter)
				setKnob("colorShiftRate","0");
				setKnob("colorPaletteWidth","17");
				setKnob("colorPaletteAngle","90");
				setKnob("colorPalette","#FF0000,#FFFF00,#FFFF00,#00FF00,#00FF00,#00FF00,#00FF00");
				setKnob("colorPaletteInterpolate","false");
				break;
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
