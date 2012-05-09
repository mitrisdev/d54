package edu.mit.d54.plugins.mitris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPanel;
import edu.mit.d54.DisplayPlugin;
import edu.mit.d54.GBDisplay;

/**
 * This is a plugin implementing the MITris game.  User input is received over the TCP socket on port 12345.
 */
public class MITrisPlugin extends DisplayPlugin {
	
	private enum State { IDLE, GAME, GAME_END_1, GAME_END_2, GAME_START, IDLE_ANIM, GAME_END_ALT_2 };
	
	private static final double ANIM_TIME_STEP=0.3;
	private static final double GAME_END_WAIT=2.0;
	private static final double LOGO_ANIM_STEP=0.1;
	
	private ServerSocket servSock;
	private Socket sock;
	private InputStream in;
	
	private final double timestep;
	private final int width;
	private final int height;

	private State gameState;
	private double gameDisplayTime;
	private double animTime;
	private double animTimeLastStep;
	private int logoPos;
	
	private MITrisGame mitrisGame;
	private MITrisBoard gameOverBoard;
	
	//secret end anim
	private static final double CIRC_ADD_TIME=1.3;
	private static final double CIRC_ANIM_TOTAL_TIME=15;
	private static final double CIRC_ANIM_STOPADD_TIME=10;
	private static final double CIRC_ANIM_FADE_WAIT_TIME=2.0;
	private static final double CIRC_ANIM_FADE_TIME=5.0;
	private double[] circTime;
	private double[] circX;
	private double[] circY;
	private double[] circHue;
	private int circPos;
	
	private static final BufferedImage mitrisLogo;
	
	static
	{
		try
		{
			InputStream stream=MITrisPlugin.class.getResourceAsStream("/images/mitris_logo.png");
			mitrisLogo=ImageIO.read(stream);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public MITrisPlugin(Display2D display, double framerate) throws IOException {
		super(display, framerate);
		timestep=1/framerate;
		width=display.getWidth();
		height=display.getHeight();
		servSock=new ServerSocket(12345);
		servSock.setSoTimeout(20);

		System.out.println("Game paused until client connect");
		
		gameState=State.IDLE;
	}

	@Override
	protected void loop() {
		Display2D display=getDisplay();
		byte userInput=getUserInput();
		Graphics2D g=display.getGraphics();
		
		switch (gameState)
		{
		case IDLE:
			animTime=0;
			animTimeLastStep=0;
			logoPos=0;
			gameState=State.IDLE_ANIM;
			break;
		case IDLE_ANIM:
			animTime+=timestep;
			if (animTime-animTimeLastStep>=LOGO_ANIM_STEP)
			{
				animTimeLastStep=animTime;
				logoPos++;
				if (logoPos>100)
					gameState=State.IDLE;
			}
			g.drawImage(mitrisLogo, 10-logoPos, 6, null);
			
			switch (userInput)
			{
			case 'L':
			case 'R':
			case 'U':
			case 'D':
				gameState=State.GAME_START;
			}
			break;
		case GAME_START:
			mitrisGame=new MITrisGame(width,height,timestep);
			gameState=State.GAME;
			break;
		case GAME:
			//check validity of user move
			switch (userInput)
			{
			case 'L':
				mitrisGame.moveLeft();
				break;
			case 'R':
				mitrisGame.moveRight();
				break;
			case 'U':
				mitrisGame.rotatePiece();
				break;
			case 'D':
				mitrisGame.dropPiece();
				break;
			case -1: //there was an error in the network socket or no client connected -- "pause" the game
				return;
			}
			//move piece down if it's time
			mitrisGame.clockTick();
			gameDisplayTime=mitrisGame.getTime();
			sendBoardToDisplay(mitrisGame.getDisplayBoard(),display);
			if (mitrisGame.isGameOver())
			{
				System.out.println(String.format("Game over! Lines cleared: %d  Time: %3.1f",mitrisGame.getDisplayBoard().getNumCleared(),mitrisGame.getTime()));
				gameState=State.GAME_END_1;
				gameOverBoard=mitrisGame.getDisplayBoard();
				animTime=0;
			}
			break;
		case GAME_END_1:
			animTime+=timestep;
			if (animTime>GAME_END_WAIT)
			{
				gameState=State.GAME_END_2;
				animTime=0;
				animTimeLastStep=0;
				if (gameOverBoard.getLevel()>=8)
				{
					gameState=State.GAME_END_ALT_2;
					circTime=new double[] {-100,-100,-100,-100,-100};
					circX=new double[5];
					circY=new double[5];
					circHue=new double[5];
					circPos=0;
				}
			}
			sendBoardToDisplay(gameOverBoard,display);
			break;
		case GAME_END_2:
			animTime+=timestep;
			if (animTime-animTimeLastStep>=ANIM_TIME_STEP)
			{
				animTimeLastStep=animTime;
				if (gameOverBoard.isBoardEmpty())
				{
					gameState=State.IDLE;
				}
				gameOverBoard=gameOverBoard.shiftBoardDown();
			}
			sendBoardToDisplay(gameOverBoard,display);
			break;
		case GAME_END_ALT_2:
			animTime+=timestep;
			if (animTime>=CIRC_ANIM_FADE_WAIT_TIME)
			{
				if (animTime<=CIRC_ANIM_FADE_WAIT_TIME+CIRC_ANIM_FADE_TIME)
				{
					double brightness=1-(animTime-CIRC_ANIM_FADE_WAIT_TIME)/CIRC_ANIM_FADE_TIME;
					sendBoardToDisplay(gameOverBoard,display,brightness);
				}
			}
			else
				sendBoardToDisplay(gameOverBoard,display);
			if (animTime-animTimeLastStep>=CIRC_ADD_TIME && animTime<=CIRC_ANIM_STOPADD_TIME)
			{
				animTimeLastStep=animTime;
				circX[circPos]=Math.random()*width;
				circY[circPos]=Math.random()*height;
				circTime[circPos]=animTime-0.3;
				circHue[circPos]=Math.random();
				circPos=(circPos+1)%circX.length;
			}
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			for (int k=0; k<circX.length; k++)
			{
				g.setColor(new Color(Color.HSBtoRGB((float)circHue[k], 1, 1)));
				g.setStroke(new BasicStroke(1.5f));
				double circH=(animTime-circTime[k])*5;
				double circW=circH/display.getPixelAspect();
				g.draw(new Ellipse2D.Double(circX[k]-circW/2, circY[k]-circH/2, circW, circH));
			}
			if (animTime>=CIRC_ANIM_TOTAL_TIME)
				gameState=State.IDLE;
		}
	}
	
	private byte getUserInput()
	{
		byte userInput=0;
		while (sock==null)
		{
			try
			{
				sock=servSock.accept();
				sock.setSoTimeout(5);
				in=sock.getInputStream();
				System.out.println("Client connected");
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
			userInput=(byte)in.read();
			if (userInput==-1)
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
	
	private void sockCleanup()
	{
		try
		{
			sock.close();
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			sock=null;
			in=null;
		}
	}

	private void sendBoardToDisplay(MITrisBoard board, Display2D display)
	{
		sendBoardToDisplay(board,display,1.0f);
	}
	
	private void sendBoardToDisplay(MITrisBoard board, Display2D display, double brightness)
	{
		int width=display.getWidth();
		int height=display.getHeight();
		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				Piece p=board.getPosition(x, y);
				Color c=p==null?Color.BLACK:p.getColor(board.getLevel(),gameDisplayTime);
				float[] hsb=Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), new float[3]);
				hsb[2]*=brightness;
				display.setPixelHSB(x, height-1-y, hsb[0], hsb[1], hsb[2]);
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		Display2D display=new GBDisplay();
		
		JFrame frame=new JFrame("MITris!");
		DisplayPanel dPanel=new DisplayPanel(display);
		frame.add(dPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.pack();
		frame.setVisible(true);
		
		MITrisPlugin plugin=new MITrisPlugin(display, 15);
		plugin.start();
	}
}
