package edu.mit.d54;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * This is a JPanel which will render a {@link Display2D} on update.  It is used to generate a preview of a {@link DisplayPlugin}.
 */
public class DisplayPanel extends JPanel implements DisplayListener {
	private static final long serialVersionUID = 3798905037016922251L;
	private final Display2D display;
	private final int pixelX,pixelY,borderX,borderY;

	/**
	 * Construct a new DisplayPanel with a default pixel width (22 pixels per display pixel)
	 * @param base The {@link Display2D} which will be previewed
	 */
	public DisplayPanel(Display2D base)
	{
		this(base,22);
	}
	
	/**
	 * Construct a new DisplayPanel with a specified pixel width
	 * @param base The {@link Display2D} which will be previewed
	 * @param pixelX The width (in pixels) of a display pixel
	 */
	public DisplayPanel(Display2D base, int pixelX)
	{
		super();
		this.pixelX=pixelX;
		this.pixelY=(int)Math.round(pixelX/base.getPixelAspect());
		this.borderX=(int)Math.round(pixelX*base.getBorderRatioX()/2);
		this.borderY=(int)Math.round(pixelY*base.getBorderRatioY()/2);
		this.setPreferredSize(new Dimension(base.getWidth()*pixelX,
				base.getHeight()*pixelY));
		display=base;
		display.addDisplayListener(this);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.fillRect(0,0,this.getWidth(),this.getHeight());
		BufferedImage img=display.getBufferedImage();
		for (int x=0; x<img.getWidth(); x++)
		{
			for (int y=0; y<img.getHeight(); y++)
			{
				g.setColor(new Color(img.getRGB(x, y)));
				g.fillRect(borderX+x*(pixelX),
						borderY+y*(pixelY),
						pixelX-2*borderX, pixelY-2*borderY);
			}
		}
	}

	@Override
	public void displayUpdated(Display2D display) {
		this.repaint();
	}
}
