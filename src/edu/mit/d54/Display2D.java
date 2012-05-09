package edu.mit.d54;

import java.awt.Color;
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
	 * This method has been deprecated and will be removed in the future.  Please use the getPixel and setPixel methods instead.
	 * @deprecated Use the setRGB and getRGB methods in this class instead.
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
	 * Get the RGB value for the specified pixel in the currently active frame.
	 * @param x Pixel x coordinate
	 * @param y Pixel y coordinate
	 * @return The pixel RGB value as a packed int
	 */
	public int getPixelRGB(int x, int y)
	{
		return imageData.getRGB(x, y);
	}
	
	/**
	 * Set the RGB color for the pixel as individual components
	 * @param x Pixel x coordinate
	 * @param y Pixel y coordinate
	 * @param r The red component of the color (0-255)
	 * @param g The green component of the color (0-255)
	 * @param b The blue component of the color (0-255)
	 */
	public void setPixelRGB(int x, int y, int r, int g, int b)
	{
		if (r>255 || g>255 || b>255 || r<0 || g<0 || b<0)
			throw new IllegalArgumentException("Color values out of bounds");
		imageData.setRGB(x, y, r<<16+g<<8+b);
	}
	
	/**
	 * Set the RGB color for the pixel as a packed integer
	 * @param x Pixel x-coordinate
	 * @param y Pixel y-coordinate
	 * @param rgb The color of the pixel as a packed RGB int
	 */
	public void setPixelRGB(int x, int y, int rgb)
	{
		imageData.setRGB(x,y,rgb);
	}
	
	/**
	 * Sets the color of the pixel using a hue, saturation, brightness (HSB) representation
	 * @param x Pixel x-coordinate
	 * @param y Pixel y-coordinate
	 * @param h The hue of the color
	 * @param s The saturation of the color (0.0-1.0)
	 * @param b The brightness of the color (0.0-1.0)
	 */
	public void setPixelHSB(int x, int y, float h, float s, float b)
	{
		imageData.setRGB(x, y, Color.HSBtoRGB(h, s, b));
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
