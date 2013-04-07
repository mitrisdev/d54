package edu.mit.d54;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This is a test controller for the ArcadeController input scheme.  The panel will accept keyboard input
 * using the directional arrow keys and send the appropriate command to the controller over a network socket.
 */
public class ArcadeControllerTestClient extends JPanel {
	private static final long serialVersionUID = 2524626593471003618L;
	
	private String host;
	private int port;
	
	private Socket sock;
	private InputStream in;
	private OutputStream out;
	
	public ArcadeControllerTestClient(String host, int port)
	{
		this.host = host;
		this.port = port;
	
		setFocusable(true);
		
		setSize(200,200);
		setPreferredSize(getSize());
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				try
				{
					if (out == null)
					{
						openSocket();
					}
					switch (e.getKeyCode())
					{
					case KeyEvent.VK_UP:
						out.write('u');
						break;
					case KeyEvent.VK_DOWN:
						out.write('d');
						break;
					case KeyEvent.VK_LEFT:
						out.write('l');
						break;
					case KeyEvent.VK_RIGHT:
						out.write('r');
						break;
					}
				}
				catch (IOException e2)
				{
					closeSocket();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				try
				{
					if (out == null)
					{
						openSocket();
					}
					switch (e.getKeyCode())
					{
					case KeyEvent.VK_UP:
						out.write('U');
						break;
					case KeyEvent.VK_DOWN:
						out.write('D');
						break;
					case KeyEvent.VK_LEFT:
						out.write('L');
						break;
					case KeyEvent.VK_RIGHT:
						out.write('R');
						break;
					}
				}
				catch (IOException e2)
				{
					closeSocket();
				}
			}
		});
	}
	
	public ArcadeControllerTestClient()
	{
		this("localhost",12345);
	}
	
	private void openSocket() throws IOException
	{
		sock = new Socket(host, port);
		in = sock.getInputStream();
		out = sock.getOutputStream();
	}
	
	private void closeSocket()
	{
		try
		{
			if (sock != null)
			{
				sock.close();
				out.close();
				in.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sock = null;
			out = null;
			in = null;
		}
	}

	/**
	 * Start a ClientTestController connected to localhost on the default port
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.add(new ArcadeControllerTestClient());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.pack();
		frame.setVisible(true);
	}
}
