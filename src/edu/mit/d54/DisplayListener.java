package edu.mit.d54;

/**
 * A DisplayListener will be notified every time a frame has been rendered with an associated display.  DisplayListeners
 * generally take the finished frame data and do something with it, such as draw the image on the screen or send it
 * out over the network.
 */
public interface DisplayListener {
	/**
	 * This is called by an associated Display2D every time it has been updated with a new image.  
	 * @param display The display with a new image update
	 */
	public void displayUpdated(Display2D display);
}
