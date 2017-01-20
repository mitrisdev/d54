package edu.mit.d54.plugins.pixels;

import java.awt.Graphics2D;
import java.io.IOException;

import edu.mit.d54.ArcadeController;
import edu.mit.d54.ArcadeListener;
import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;
import edu.mit.d54.PixelFont;
import edu.mit.d54.TwitterClient;

/**
 * This is a plugin to turn on individual pixels.  It provides knobs to set x, y, and color,
 * and set the pixel at (x,y) whenever the color knob is changed.
 */
public class PixelsPlugin extends DisplayPlugin {

    private final int width;
    private final int height;

    private int xKnob = 0;
    private int yKnob = 0;

    private int[][] pixels;

    public PixelsPlugin(Display2D display, double framerate) throws IOException
    {
        super(display, framerate);

        width = display.getWidth();
        height = display.getHeight();

        pixels = new int[width][height];

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                pixels[i][j] = 0;
            }
        }

        registerKnob("x", "0", String.format("X Coordinate (0 to %1$d inclusive)", width - 1));
        registerKnob("y", "0", String.format("Y Coordinate (0 to %1$d inclusive)", height - 1));
        registerKnob("color", "000000", "Color as a 6-digit hexadecimal value.  Setting sets the pixel.");
    }

    @Override
    protected void loop()
    {
        Display2D display = getDisplay();

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                display.setPixelRGB(i, j, pixels[i][j]);
            }
        }
    }

    @Override
    protected void knobChanged(String knob, String value)
    {
        if (knob.equals("x"))
        {
            int newX = Integer.parseInt(value);
            int maxX = width - 1;
            if (newX < 0 || newX > maxX)
                throw new IllegalArgumentException(String.format("X must be between 0 and %1$d", maxX));

            xKnob = newX;
        }
        else if (knob.equals("y"))
        {
            int newY = Integer.parseInt(value);
            int maxY = height - 1;
            if (newY < 0 || newY > maxY)
                throw new IllegalArgumentException(String.format("Y must be between 0 and %1$d", maxY));

            yKnob = newY;
        }
        else if (knob.equals("color"))
        {
            int color = Integer.parseInt(value, 16);
            if (color < 0 || color > 0xFFFFFF)
                throw new IllegalArgumentException("Color must be between \"0\" and \"FFFFFF\"");

            pixels[xKnob][yKnob] = color;
        }
    }
}
