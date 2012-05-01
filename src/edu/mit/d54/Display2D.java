package edu.mit.d54;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A Display2D represents a two-dimensional pixel display with a specified resolution and pixel aspect ratio.  It will
 * usually be updated by a DisplayPlugin, and then notify associated DisplayListeners at the end of each frame.  A Display2D
 * is backed by a BufferedImage, which is written on by a DisplayPlugin.  At the end of each frame, the BufferedImage is
 * saved and sent to the DisplayListeners and a new BufferedImage is created to hold the next frame.
 */
public class Display2D {
	private final int height;
	private final int width;
	private final double pixelAspect;
	private final double borderX;
	private final double borderY;
	private BufferedImage imageData;
	private BufferedImage bufferedData;
	private List<DisplayListener> displayListeners;
	
	/**
	 * Constructs a new Display2D with the specified size and aspect ratio
	 * @param height The height of the display in pixels
	 * @param width The width of the display in pixels
	 * @param pixelAspect The aspect ratio of an individual pixel (width divided by height)
	 * @param borderX The ratio between the horizontal blank portion of a pixel and the pixel horizontal pitch (width of the window frame)
	 * @param borderY The ratio between the vertical blank portion of a pixel and the pixel vertical pitch (height of the window frame)
	 */
	public Display2D(int height, int width, double pixelAspect, double borderX, double borderY)
	{
		this.height=height;
		this.width=width;
		this.pixelAspect=pixelAspect;
		this.borderX=borderX;
		this.borderY=borderY;
		this.displayListeners=new ArrayList<DisplayListener>();
		endFrame();
		endFrame();
	}
	
	/**
	 * This method indicates that the frame is finished being rendered.  The {@link DisplayListener}s are notified about the new frame.
	 */
	public void endFrame()
	{
		bufferedData=imageData;
		imageData=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (DisplayListener l : displayListeners)
		{
			l.displayUpdated(this);
		}
	}
	
	/**
	 * @return The saved image of the last completed frame on the display.
	 */
	public BufferedImage getBufferedImage()
	{
		return bufferedData;
	}
	
	/**
	 * @return A {@link Graphics2D} object associated with the frame being rendered.
	 */
	public Graphics2D getGraphics()
	{
		return imageData.createGraphics();
	}
	
	/**
	 * @return The {@link BufferedImage} backing the frame being rendered.
	 */
	public BufferedImage getImage()
	{
		return imageData;
	}
	
	/**
	 * @return The width of the display in pixels.
	 */
	public int getWidth()
	{
		return width;
	}
	
	/**
	 * @return The height of the display in pixels.
	 */
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * Returns the aspect ratio (width:height) of an individual pixel.  This is useful for rendering things which might
	 * need to be distorted to take rectangular pixels into account.
	 * @return The aspect ratio of an individual pixel.
	 */
	public double getPixelAspect()
	{
		return pixelAspect;
	}
	
	/**
	 * This returns the width of the black border of a pixel relative to the pixel pitch in the horizontal direction.  This is
	 * useful for rendering a preview of the display.
	 * @return The ratio between the horizontal blank portion of a pixel and the pixel horizontal pitch (width of the window frame)
	 */
	public double getBorderRatioX()
	{
		return borderX;
	}
	
	/**
	 * This returns the height of the black border of a pixel relative to the pixel pitch in the vertical direction.  This is
	 * useful for rendering a preview of the display.
	 * @return The ratio between the vertical blank portion of a pixel and the pixel vertical pitch (height of the window frame)
	 */
	public double getBorderRatioY()
	{
		return borderY;
	}
	
	/**
	 * Adds a new DisplayListener to the display.  This listener is notified every time a new frame is rendered on the display.
	 * @param listener DisplayListener to notify about updates
	 */
	public void addDisplayListener(DisplayListener listener)
	{
		displayListeners.add(listener);
	}
	
	/**
	 * Removes a DisplayListener from the display.
	 * @param listener DisplayListener to remove
	 */
	public void removeDisplayListener(DisplayListener listener)
	{
		displayListeners.remove(listener);
	}
	
}
