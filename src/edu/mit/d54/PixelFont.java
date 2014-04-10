package edu.mit.d54;

import java.awt.Font;
import java.io.InputStream;

public class PixelFont {
	
	private static final Font font;
	private static final Font instance;
	
	static
	{
		try
		{
			InputStream stream=PixelFont.class.getResourceAsStream("/resources/font/d54-5px.ttf");
	        font = Font.createFont(Font.TRUETYPE_FONT, stream);
	        instance = getScaledInstance(1);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Font getInstance()
	{
		return instance;
	}
	
	public static Font getScaledInstance(float scale)
	{
		return font.deriveFont(8f * scale);
	}
}
