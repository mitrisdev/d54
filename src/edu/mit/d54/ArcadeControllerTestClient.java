package edu.mit.d54;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	
	private Map<Character, Boolean> keyState;
	private Map<Character, JButton> buttons;
	
	public ArcadeControllerTestClient(String host, int port)
	{
		this.host = host;
		this.port = port;
		
		keyState = new HashMap<Character, Boolean>();
		keyState.put('u', false);
		keyState.put('d', false);
		keyState.put('r', false);
		keyState.put('l', false);
		buttons = new HashMap<Character, JButton>();
		buttons.put('u', new JButton("Up"));
		buttons.put('d', new JButton("Down"));
		buttons.put('r', new JButton("Left"));
		buttons.put('l', new JButton("Right"));
		for (final char key : buttons.keySet())
		{
			JButton button = buttons.get(key);
			button.setFocusable(false);
			button.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseReleased(MouseEvent arg0) {
					release(key);
				}
				
				@Override
				public void mousePressed(MouseEvent arg0) {
					press(key);
				}
			});
		}
	
		setFocusable(true);
		
		setSize(200,200);
		setPreferredSize(getSize());
		setLayout(new GridLayout(3,3));
		add(new JLabel(""));
		add(buttons.get('u'));
		add(new JLabel(""));
		add(buttons.get('l'));
		add(new JLabel(""));
		add(buttons.get('r'));
		add(new JLabel(""));
		add(buttons.get('d'));
		add(new JLabel(""));
		
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
					case KeyEvent.VK_W:
						release('u');
						break;
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_S:
						release('d');
						break;
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_A:
						release('l');
						break;
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_D:
						release('r');
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
					switch (e.getKeyCode())
					{
					case KeyEvent.VK_UP:
					case KeyEvent.VK_W:
						press('u');
						break;
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_S:
						press('d');
						break;
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_A:
						press('l');
						break;
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_D:
						press('r');
						break;
					}
			}
		});
	}
	
	private void press(char key)
	{
		keyState.put(key, true);
		try
		{
			if (out == null)
			{
				openSocket();
			}
			out.write(Character.toUpperCase(key));
		}
		catch (IOException e)
		{
			closeSocket();
		}
		buttons.get(key).setSelected(true);
	}
	
	private void release(char key)
	{
		if (keyState.get(key))
		{
			keyState.put(key, false);
			try
			{
				if (out == null)
				{
					openSocket();
				}
				out.write(key);
			}
			catch (IOException e)
			{
				closeSocket();
			}
		}
		buttons.get(key).setSelected(false);
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
