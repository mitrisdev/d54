package edu.mit.d54;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.io.OutputStream;

public class ArcadeController implements Runnable {
	
	private static ArcadeController instance;
	
	private ServerSocket servSock;
	private Socket sock;
	private InputStream in;
	private OutputStream out;
	
	private ArcadeListener listener;
	
	private boolean stop;
	
	protected ArcadeController() throws IOException
	{
		servSock=new ServerSocket(12345);
		//servSock.setSoTimeout(20);
	}
	
	public static ArcadeController getInstance() throws IOException
	{
		if (instance == null)
		{
			instance = new ArcadeController();
			new Thread(instance).start();
		}
		return instance;
	}
	
	public void setListener(ArcadeListener l)
	{
		listener = l;
	}
	
	public void run()
	{
		stop = false;
		while (!stop)
		{
			byte b = getUserInput();
			if (b > 0)
			{
				if (listener != null)
					listener.arcadeButton(b);
			}
		}
	}
	
	private byte getUserInput()
	{
		byte userInput=0;
		while (sock==null)
		{
			try
			{
				synchronized (this)
				{
					sock=servSock.accept();
					//sock.setSoTimeout(5);
					in=sock.getInputStream();
					out=sock.getOutputStream();
					System.out.println("Client connected");
				}
			}
			catch (SocketTimeoutException e)
			{
				return -1;
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				sockCleanup();
				return -1;
			}
		}
		try
		{
			synchronized (this)
			{
				userInput=(byte)in.read();
			}
			if (userInput == -1)
			{
				sockCleanup();
				System.out.println("Client disconnect");
				return -1;
			}
			System.out.println("User input "+userInput);
			return userInput;
		}
		catch (SocketTimeoutException e)
		{
			return 0;
		}
		catch (IOException e)
		{
			System.out.println("Client error");
			e.printStackTrace();
			sockCleanup();
			return -1;
		}
	}
	
	private synchronized void sockCleanup()
	{
		try
		{
			sock.close();
			in.close();
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sock=null;
			in=null;
			out=null;
		}
	}

	public synchronized void setLED(byte led) throws IOException
	{
		if (out != null)
			out.write(led);
	}
	
	public void setLEDs(byte leds) throws IOException
	{
		setLED((byte)((leds%8 != 0)?'U':'u'));
		setLED((byte)((leds%4 != 0)?'D':'d'));
		setLED((byte)((leds%2 != 0)?'L':'l'));
		setLED((byte)((leds%1 != 0)?'R':'r'));
	}
	
	public boolean isConnected()
	{
		return sock != null;
	}
}
