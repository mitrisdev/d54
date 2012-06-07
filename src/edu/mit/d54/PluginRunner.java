package edu.mit.d54;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;

/**
 * This is a program which will load a DisplayPlugin specified as a command-line argument using reflection, and run
 * the plugin in a preview window.
 */
public class PluginRunner {

	public static void main(String[] args) {
		if (args.length<1)
		{
			System.out.println("Usage: java PluginRunner [--framerate=<framerate] <plugin-class-name>");
			System.exit(1);
		}
		String clsname=null;
		Display2D display=new GBDisplay();
		double framerate=15.0;
		for (String arg : args)
		{
			if (arg.startsWith("--framerate="))
			{
				framerate=Float.parseFloat(arg.substring(12));
			}
			else if (clsname==null)
			{
				clsname=arg;
			}
			else
			{
				System.out.println("Unknown or extra argument: "+arg);
				System.exit(10);
			}
		}

		DisplayPlugin plugin=null;
		try
		{
			Class<? extends DisplayPlugin> cls=Class.forName(clsname).asSubclass(DisplayPlugin.class);
			Constructor<? extends DisplayPlugin> ctor=cls.getDeclaredConstructor(Display2D.class,double.class);
			plugin=ctor.newInstance(display,framerate);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("ERROR: Class "+clsname+" not found!");
			System.exit(2);
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("ERROR: Constructor "+clsname+"(Display2D,double) not found!");
			System.exit(3);
		}
		catch (InvocationTargetException e)
		{
			System.out.println("ERROR: InvocationTargetException while calling ctor "+clsname+"(Display2D,double)");
			e.printStackTrace();
			System.exit(4);
		}
		catch (IllegalAccessException e)
		{
			System.out.println("ERROR: IllegalAccessException while calling ctor "+clsname+"(Display2D,double)");
			e.printStackTrace();
			System.exit(5);
		}
		catch (InstantiationException e)
		{
			System.out.println("ERROR: InstantiationException while calling ctor "+clsname+"(Display2D,double)");
			e.printStackTrace();
			System.exit(6);
		}
		
		JFrame frame=new JFrame("D54 Preview");
		DisplayPanel dPanel=new DisplayPanel(display);
		frame.add(dPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.pack();
		frame.setVisible(true);
		plugin.start();
	}

}
