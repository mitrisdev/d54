package edu.mit.d54;

/**
 * This class is the superclass of all display plugins that can run on D54.  A display plugin will create a render thread
 * and will render its output on a Display2D at a certain framerate.
 */
public abstract class DisplayPlugin implements Runnable {
	private final Display2D display;
	private final double framerate;
	private boolean stop=false;
	private final long startTimeMillis;
	private long curTimeMillis;
	
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
	}
	
	/**
	 * Tell the plugin to stop rendering at the end of the next frame.
	 */
	public void terminate()
	{
		stop=true;
	}
	
	public boolean isTerminated()
	{
		return stop;
	}
	
	public void run()
	{
		onStart();
		curTimeMillis=System.currentTimeMillis();
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
		onStop();
	}
	
	/**
	 * Start drawing the plugin on the display
	 */
	public void start()
	{
		if (stop)
			throw new RuntimeException("DisplayPlugin already terminated");
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
	
	public Display2D getDisplay()
	{
		return display;
	}
}
