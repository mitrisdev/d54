package edu.mit.d54.plugins.test;

import javax.swing.JFrame;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPanel;
import edu.mit.d54.GBDisplay;
import edu.mit.d54.PluginRunner;

/**
 * This is an example "bare-bones" custom plugin runner which will run {@link TestPlugin}.  The recommended interface is 
 * to use {@link PluginRunner} instead.
 */
public class TestRunner {
	public static void main(String[] args)
	{
		Display2D display=new GBDisplay();
		JFrame frame=new JFrame("test");
		DisplayPanel dPanel=new DisplayPanel(display);
		frame.add(dPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.pack();
		frame.setVisible(true);
		TestPlugin plugin=new TestPlugin(display,15);
		plugin.start();
	}
}