package edu.mit.d54;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is the superclass of all display plugins that can run on D54.  A display plugin will create a render thread
 * and will render its output on a Display2D at a certain framerate.  DisplayPlugins can have "knobs" which are
 * parameters intended to be adjusted dynamically while the plugin is running.
 */
public abstract class DisplayPlugin implements Runnable {
	private final Display2D display;
	private final double framerate;
	private boolean stop=false;
	private boolean running=false;
	private final long startTimeMillis;
	private long curTimeMillis;
	private final Map<String,String> knobValues;
	private final Map<String,String> knobDesc;
	
	/**
	 * Create a new DisplayPlugin associated with a specific Display2D
	 * @param display The display that the plugin will render on
	 * @param framerate The number of frames per second that the plugin will render
	 */
	protected DisplayPlugin(Display2D display, double framerate)
	{
		if (framerate>15.5)
			throw new IllegalArgumentException("Framerate out of bounds");
		this.display=display;
		this.framerate=framerate;
		startTimeMillis=System.currentTimeMillis();
		knobValues=new HashMap<String,String>();
		knobDesc=new HashMap<String,String>();
	}
	
	/**
	 * Tell the plugin to stop rendering at the end of the next frame.
	 */
	public void stop()
	{
		stop=true;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void run()
	{
		onStart();
		curTimeMillis=System.currentTimeMillis();
		running=true;
		while (!stop)
		{
			loop();
			try {
				long delay=(long)(1000./framerate)-(System.currentTimeMillis()-curTimeMillis);
				if (delay>0)
					Thread.sleep(delay);
			} catch (InterruptedException e) {}
			curTimeMillis=System.currentTimeMillis();
			display.endFrame();
		}
		try {
			long delay=(long)(1000./framerate)-(System.currentTimeMillis()-curTimeMillis);
			if (delay>0)
				Thread.sleep(delay);
		} catch (InterruptedException e) {}
		display.endFrame();
		onStop();
		running=false;
	}
	
	/**
	 * Start drawing the plugin on the display
	 */
	public void start()
	{
		if (running)
			throw new RuntimeException("Plugin already running!");
		running=true;
		stop=false;
		new Thread(this).start();
	}
	
	/**
	 * Returns the time of the currently active frame in seconds since the start of the plugin
	 * @return the time of the current frame
	 */
	public double getTime()
	{
		return (double)(curTimeMillis-startTimeMillis)/1000.;
	}
	
	/**
	 * @return Running framerate of the plugin
	 */
	public double getFramerate()
	{
		return framerate;
	}
	
	/**
	 * This is the body of the render loop for the plugin.  This method is called every frame and should draw
	 * the current frame of the plugin on the display.
	 */
	protected abstract void loop();
	
	/**
	 * Placeholder for code that should be run at the time of the plugin start
	 */
	protected void onStart()
	{
	}
	
	/**
	 * Placeholder for code that should be run at the time of plugin stop
	 */
	protected void onStop()
	{	
	}
	
	/**
	 * Get the Display2D associated with this DisplayPlugin
	 * @return the plugin's Display2D
	 */
	public Display2D getDisplay()
	{
		return display;
	}
	
	/**
	 * Create a new adjustable knob for this plugin.
	 * @param knob The name of the knob
	 * @param initValue The inital value of the knob
	 * @param description A user-friendly description of the knob
	 */
	protected void registerKnob(String knob, String initValue, String description)
	{
		knobDesc.put(knob, description);
		knobValues.put(knob, initValue);
		setKnob(knob,initValue);
	}
	
	/**
	 * Get the current value of a knob
	 * @param knob The name of the knob
	 * @return the current value of the knob
	 */
	public String getKnobValue(String knob)
	{
		if (!knobValues.containsKey(knob))
			throw new IllegalArgumentException("Knob "+knob+" not available");
		return knobValues.get(knob);
	}
	
	/**
	 * Get the description of a knob
	 * @param knob The name of the knob
	 * @return the knob's description
	 */
	public String getKnobDescription(String knob)
	{
		if (!knobValues.containsKey(knob))
			throw new IllegalArgumentException("Knob "+knob+" not available");
		return knobDesc.get(knob);
	}
	
	/**
	 * Adjust the value of a knob
	 * @param knob The name of the knob
	 * @param value The new setting of the knob
	 */
	public void setKnob(String knob, String value)
	{
		if (!knobValues.containsKey(knob))
			throw new IllegalArgumentException("Knob "+knob+" not available");
		knobValues.put(knob, value);
		knobChanged(knob,value);
	}
	
	/**
	 * Placeholder to notify a plugin when a knob is changed.  A plugin can choose to either override this method
	 * or call getKnobValue every frame to determine the current value of a knob.
	 * @param knob The name of the knob which was modified
	 * @param value The new setting of the knob
	 */
	protected void knobChanged(String knob, String value)
	{
	}
	
	/**
	 * Get all of the knobs for this plugin.
	 * @return a set containing the names of all of the knobs
	 */
	public Set<String> getKnobs()
	{
		return knobDesc.keySet();
	}
	
}
