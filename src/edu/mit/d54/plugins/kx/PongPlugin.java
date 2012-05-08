package edu.mit.d54.plugins.kx;

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
 * This is a plugin implementing a Pong game. Written by KX, based on source code from MITris
 *  User input is received over the TCP socket on port 12345.
 */
public class PongPlugin extends DisplayPlugin {
	
	private enum State { IDLE, GAME, GAME_END_1, GAME_END_2, PLAY_START, IDLE_ANIM };
	
	private ServerSocket servSock;
	private Socket sock;
	private InputStream in;
	
	private final Display2D display;
	
	private final double timestep;
	private final int width;
	private final int height;

	private State gameState;
	private double gameDisplayTime;
	private double animTime;
	private double animTimeLastStep;
		
	private int p1Score = 0;
	private int p2Score = 0;
	private int winScore = 4;  // Change this to set numbers of points required to win
	private int winner = 0;

	private int p1Pos = 4;
	private int p2Pos = 4;

	private int ballPosX = 4;
	private int ballPosY = 8;

	private double ballStepTime = 0.5;
	private double ballLastStep = 0.0;

	private double originalSpeed = 0.5;		// Change this to set what speed the ball starts at
	private double accelerationSpeed = 0.03;	// Change this to set how quickly the ball gets faster

	private int ballDirection = 0;  //  0 = downleft, 1 = upleft, 2 = upright, 3 = downright

	private int vert = 0;
	
	public PongPlugin(Display2D display, double framerate) throws IOException {
		super(display, framerate);
		this.display=display;
		timestep=1/framerate;
		width=display.getWidth();
		height=display.getHeight();
		servSock=new ServerSocket(12345);
		servSock.setSoTimeout(20);

		System.out.println("Game paused until client connects");
		
		gameState=State.IDLE;
	}

	@Override
	protected void loop() {
		Display2D display=getDisplay();
		byte userInput=getUserInput();
		BufferedImage img=display.getImage();
		
		switch (gameState)
		{
		case IDLE:
			animTime=0;       		// initialize 
			animTimeLastStep=0;
			vert = 0;
			p1Score = 0;
			p2Score = 0;
			gameState=State.IDLE_ANIM;
			break;
		case IDLE_ANIM:

			showTitle();			// show title until user presses button
			
			switch (userInput)
			{
			case 'L':
			case 'R':
			case 'U':
			case 'D':
				gameState=State.PLAY_START;
				animTime=0;
			}
			break;
		case PLAY_START:
			animTime+=timestep;

			showNumber(p1Score, 2, 0, 255, 255, 255);
			showNumber(p2Score, 2, 9, 255, 255, 255);	// show player's scores

 			p1Pos = 4;			// reset the play area
			p2Pos = 4;
			ballPosX = 4;
			ballPosY = 8;
			ballStepTime = 0.5;
			ballLastStep = 0.0;		

			if (animTime > 3.0){		// wait 3 seconds and start play
			gameState=State.GAME;
			}
			break;
		case GAME:
			animTime+=timestep;
			drawScreen();		// draws the frog and all enabled cars in their corresponding colour
			moveBall();  		// checks whether it's time to move the ball and does so if necessary 

			// move paddles
			switch (userInput)
			{
			case 'L':		// player 1 Move Left
				if (p1Pos > 0){
					p1Pos = p1Pos - 1;
				}
				
				break;
			case 'R':		// player 2 Move Right
				if (p2Pos < 7){
					p2Pos = p2Pos + 1;
				}

				break;
			case 'U':		// player 1 Move Right

				if (p1Pos < 7){
					p1Pos = p1Pos + 1;
				}
				break;
			case 'D':		// player 2 Move Left

				if (p2Pos > 0){
					p2Pos = p2Pos - 1;
				}
				break;
			case -1: //there was an error in the network socket or no client connected -- "pause" the game
				return;
			}


			break;
			case GAME_END_2:  // win
			animTime+=timestep;

			if (animTime < 5){
			 showP(255, 255, 255);
			 showNumber(winner, 2, 9, 255, 255, 255);
			}else{
				if (animTime < 10){
					showWin(); 		// write WIN on the screen
				}
			}

			if (animTime > 10.0){  // wait a bit longer before going back to title screen

				gameState=State.IDLE;
	
			}

			break;
		
		}
}

	public void moveBall()   // check whether it's time to move ball, and if so, do it
	{			 // also detect collisions and bounce

	if (animTime - ballLastStep > ballStepTime){  // time to step ball


		switch (ballDirection) // move ball coordinates 
			{

			case 0:		// downleft

				ballPosX = ballPosX - 1;
				ballPosY = ballPosY + 1;

			break;
			case 1:		// upleft

				ballPosX = ballPosX - 1;
				ballPosY = ballPosY - 1;

			break;
			case 2:		// upright

				ballPosX = ballPosX + 1;
				ballPosY = ballPosY - 1;

			break;
			case 3:		//downright

				ballPosX = ballPosX + 1;
				ballPosY = ballPosY + 1;

			break;		
			}
	
		if (ballPosX == 8 && ballDirection == 2){  // change direction if hitting a wall 
			ballDirection = 1;
		}

		if (ballPosX == 8 && ballDirection == 3){
			ballDirection = 0;
		}

		if (ballPosX == 0 && ballDirection == 0){
			ballDirection = 3;
		}

		if (ballPosX == 0 && ballDirection == 1){
			ballDirection = 2;
		}


		
		if (ballPosY == 15 && ballDirection == 0){   // detect collision with bottom paddle

			if (p2Pos == ballPosX - 1){
				ballDirection = 1;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

			if (p2Pos + 1 == ballPosX - 1){
				ballDirection = 2;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

		}

		if (ballPosY == 15 && ballDirection == 3){

			if (p2Pos == ballPosX + 1){
				ballDirection = 1;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

			if (p2Pos + 1 == ballPosX + 1){
				ballDirection = 2;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

		}

		if (ballPosY == 1 && ballDirection == 1){   // detect collision with top paddle

			if (p1Pos == ballPosX - 1){
				ballDirection = 0;
				ballStepTime = ballStepTime - accelerationSpeed;
				
			}

			if (p1Pos + 1 == ballPosX - 1){
				ballDirection = 3;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

		}

		if (ballPosY == 1 && ballDirection == 2){

			if (p1Pos == ballPosX + 1){
				ballDirection = 0;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

			if (p1Pos + 1 == ballPosX + 1){
				ballDirection = 3;
				ballStepTime = ballStepTime - accelerationSpeed;
			}

		}

		if (ballPosY == 16 && ballPosX == p2Pos){    //  last moment catches
			ballPosY = ballPosY - 1;
			if (ballDirection == 0){ballDirection = 1;}
			if (ballDirection == 3){ballDirection = 2;}
		}


		if (ballPosY == 16 && ballPosX == p2Pos+1){
			ballPosY = ballPosY - 1;
			if (ballDirection == 0){ballDirection = 1;}
			if (ballDirection == 3){ballDirection = 2;}
		}

		if (ballPosY == 0 && ballPosX == p1Pos){
			ballPosY = ballPosY + 1;
			if (ballDirection == 1){ballDirection = 0;}
			if (ballDirection == 2){ballDirection = 3;}
		}

		if (ballPosY == 0 && ballPosX == p1Pos+1){
			ballPosY = ballPosY + 1;
			if (ballDirection == 1){ballDirection = 0;}
			if (ballDirection == 2){ballDirection = 3;}
		}



		if (ballPosY == 16){		// Detect missing the ball
		ballStepTime = originalSpeed;
		p1Score = p1Score + 1;

			if (p1Score == winScore){
				gameState=State.GAME_END_2;
				winner = 1;
			}else{
			gameState=State.PLAY_START;	
			}
		animTime=0;
		}

		if (ballPosY == 0){
		ballStepTime = originalSpeed;
		p2Score = p2Score + 1;

			if (p2Score == winScore){
				gameState=State.GAME_END_2;
				winner = 2;
			}else{		
			gameState=State.PLAY_START;
			}
		animTime=0;
		if (ballPosX<0){ballPosX=0;}	// catch-alls for crash prevention
		if (ballPosX>8){ballPosX=8;}
		if (ballPosY<0){ballPosY=0;}
		if (ballPosY>16){ballPosY=16;}
		}

	ballLastStep = animTime;
	}
}

	
	public void drawScreen() // draw the paddles and ball in the right place 
	{
		BufferedImage img=display.getImage();

		img.setRGB(p1Pos, 0, (255 << 16) + (255 << 8) + 255); // p1 paddle
		img.setRGB(p1Pos+1, 0, (255 << 16) + (255 << 8) + 255);

		img.setRGB(p2Pos, 16, (255 << 16) + (255 << 8) + 255); // p2 paddle
		img.setRGB(p2Pos+1, 16, (255 << 16) + (255 << 8) + 255);

		img.setRGB(ballPosX, ballPosY, (255 << 16) + (255 << 8) + 255); // ball
	}




	public void clearScreen()
	{
		BufferedImage img=display.getImage();
	      for (int y = 0;y < 17; y++){
		for (int x = 0;x < 9; x++){

		img.setRGB(x, y, (0 << 16) + (0 << 8) + 0); // black pixel

		}
	      }
	}


private void showWin()
	{

		BufferedImage img=display.getImage();

		int R = 0;
 		int G = 142;
		int B = 255;

		img.setRGB(0, 0, (R << 16) + (G << 8) + B);		
		img.setRGB(1, 0, (R << 16) + (G << 8) + B);
		img.setRGB(3, 0, (R << 16) + (G << 8) + B);		// W
		img.setRGB(4, 0, (R << 16) + (G << 8) + B); 
		img.setRGB(6, 0, (R << 16) + (G << 8) + B);
		img.setRGB(7, 0, (R << 16) + (G << 8) + B);
		
		img.setRGB(0, 1, (R << 16) + (G << 8) + B);		
		img.setRGB(1, 1, (R << 16) + (G << 8) + B);
		img.setRGB(3, 1, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 1, (R << 16) + (G << 8) + B); 
		img.setRGB(6, 1, (R << 16) + (G << 8) + B);
		img.setRGB(7, 1, (R << 16) + (G << 8) + B);

		img.setRGB(0, 2, (R << 16) + (G << 8) + B);		
		img.setRGB(1, 2, (R << 16) + (G << 8) + B);
		img.setRGB(3, 2, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 2, (R << 16) + (G << 8) + B); 
		img.setRGB(6, 2, (R << 16) + (G << 8) + B);
		img.setRGB(7, 2, (R << 16) + (G << 8) + B);
		
		img.setRGB(0, 3, (R << 16) + (G << 8) + B);
		img.setRGB(1, 3, (R << 16) + (G << 8) + B);
		img.setRGB(2, 3, (R << 16) + (G << 8) + B);
		img.setRGB(3, 3, (R << 16) + (G << 8) + B);
		img.setRGB(4, 3, (R << 16) + (G << 8) + B);
		img.setRGB(5, 3, (R << 16) + (G << 8) + B);
		img.setRGB(6, 3, (R << 16) + (G << 8) + B);
		img.setRGB(7, 3, (R << 16) + (G << 8) + B);

		img.setRGB(1, 4, (R << 16) + (G << 8) + B);
		img.setRGB(2, 4, (R << 16) + (G << 8) + B);
		img.setRGB(3, 4, (R << 16) + (G << 8) + B);
		img.setRGB(4, 4, (R << 16) + (G << 8) + B);
		img.setRGB(5, 4, (R << 16) + (G << 8) + B);
		img.setRGB(6, 4, (R << 16) + (G << 8) + B);


		R = 142;
		G = 68;
		B = 255;

		img.setRGB(1, 5, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 5, (R << 16) + (G << 8) + B);
		img.setRGB(3, 5, (R << 16) + (G << 8) + B);		// I
		img.setRGB(4, 5, (R << 16) + (G << 8) + B); 
		img.setRGB(5, 5, (R << 16) + (G << 8) + B);
		img.setRGB(6, 5, (R << 16) + (G << 8) + B);
		
		img.setRGB(1, 6, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 6, (R << 16) + (G << 8) + B);
		img.setRGB(3, 6, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 6, (R << 16) + (G << 8) + B); 
		img.setRGB(5, 6, (R << 16) + (G << 8) + B);
		img.setRGB(6, 6, (R << 16) + (G << 8) + B);

		img.setRGB(3, 7, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 7, (R << 16) + (G << 8) + B);

		img.setRGB(3, 8, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 8, (R << 16) + (G << 8) + B); 

		img.setRGB(3, 9, (R << 16) + (G << 8) + B);
		img.setRGB(4, 9, (R << 16) + (G << 8) + B);
		
		img.setRGB(1, 10, (R << 16) + (G << 8) + B);
		img.setRGB(2, 10, (R << 16) + (G << 8) + B);
		img.setRGB(3, 10, (R << 16) + (G << 8) + B);
		img.setRGB(4, 10, (R << 16) + (G << 8) + B);
		img.setRGB(5, 10, (R << 16) + (G << 8) + B);
		img.setRGB(6, 10, (R << 16) + (G << 8) + B);

		img.setRGB(1, 11, (R << 16) + (G << 8) + B);
		img.setRGB(2, 11, (R << 16) + (G << 8) + B);
		img.setRGB(3, 11, (R << 16) + (G << 8) + B);
		img.setRGB(4, 11, (R << 16) + (G << 8) + B);
		img.setRGB(5, 11, (R << 16) + (G << 8) + B);
		img.setRGB(6, 11, (R << 16) + (G << 8) + B);

		R = 100;
		G = 0;
		B = 255;
		img.setRGB(0, 12, (R << 16) + (G << 8) + B);		
		img.setRGB(0, 13, (R << 16) + (G << 8) + B);
		img.setRGB(0, 14, (R << 16) + (G << 8) + B);		// N
		img.setRGB(0, 15, (R << 16) + (G << 8) + B); 
		img.setRGB(0, 16, (R << 16) + (G << 8) + B);
		
		img.setRGB(1, 12, (R << 16) + (G << 8) + B);		
		img.setRGB(1, 13, (R << 16) + (G << 8) + B);
		img.setRGB(1, 14, (R << 16) + (G << 8) + B);		
		img.setRGB(1, 15, (R << 16) + (G << 8) + B); 
		img.setRGB(1, 16, (R << 16) + (G << 8) + B);

		img.setRGB(6, 12, (R << 16) + (G << 8) + B);		
		img.setRGB(6, 13, (R << 16) + (G << 8) + B);
		img.setRGB(6, 14, (R << 16) + (G << 8) + B);		
		img.setRGB(6, 15, (R << 16) + (G << 8) + B); 
		img.setRGB(6, 16, (R << 16) + (G << 8) + B);
		
		img.setRGB(7, 12, (R << 16) + (G << 8) + B);		
		img.setRGB(7, 13, (R << 16) + (G << 8) + B);
		img.setRGB(7, 14, (R << 16) + (G << 8) + B);		
		img.setRGB(7, 15, (R << 16) + (G << 8) + B); 
		img.setRGB(7, 16, (R << 16) + (G << 8) + B);

		img.setRGB(2, 12, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 13, (R << 16) + (G << 8) + B);

		img.setRGB(3, 13, (R << 16) + (G << 8) + B);		
		img.setRGB(3, 14, (R << 16) + (G << 8) + B); 

		img.setRGB(4, 14, (R << 16) + (G << 8) + B);
		img.setRGB(4, 15, (R << 16) + (G << 8) + B);
		
		img.setRGB(5, 15, (R << 16) + (G << 8) + B);
		img.setRGB(5, 16, (R << 16) + (G << 8) + B);


	}



	private void showNumber(int number, int x, int y, int R, int G, int B)
	{

		BufferedImage img=display.getImage();

		switch (number)
			{

			case 0:

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B); 

				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+4, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B); 

				img.setRGB(x+5, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+5, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+5, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+5, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+0, (R << 16) + (G << 8) + B); 

				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+5, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+5, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+5, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B); 

				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B);

				break;
			case 1:

				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+2, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+5, (R << 16) + (G << 8) + B);
		
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+5, (R << 16) + (G << 8) + B);
		
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+7, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);


				break;
			case 2:

				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+4, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+5, (R << 16) + (G << 8) + B);
		
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+0, y+5, (R << 16) + (G << 8) + B);
		
				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+7, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);

				break;
			case 3:

				img.setRGB(x+0, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B); 

				img.setRGB(x+3, y+2, (R << 16) + (G << 8) + B); 

				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B); 

				img.setRGB(x+3, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+7, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+7, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+7, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);
		

				break;
			case 4:

				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);	

				img.setRGB(x+0, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+0, y+5, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);	 

				img.setRGB(x+1, y+5, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+5, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+5, (R << 16) + (G << 8) + B);		
				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B); 
				img.setRGB(x+5, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B); 
				img.setRGB(x+5, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+7, (R << 16) + (G << 8) + B);


				break;
			case 5:

				img.setRGB(x+0, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B); 
				img.setRGB(x+4, y+0, (R << 16) + (G << 8) + B);	

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B); 
				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);	

				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B); 

				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B); 
				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B); 
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+7, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+7, (R << 16) + (G << 8) + B);		
				img.setRGB(x+2, y+7, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);

				break;
			case 6:

				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);
				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);		
				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B);	
				img.setRGB(x+0, y+5, (R << 16) + (G << 8) + B);
				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);

				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B);


				break;
			case 7:

				img.setRGB(x+0, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+0, (R << 16) + (G << 8) + B);		

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);		

				img.setRGB(x+3, y+2, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+2, (R << 16) + (G << 8) + B);

				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+3, (R << 16) + (G << 8) + B);

				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+1, y+5, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);


				break;
			case 8:

				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B);	

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+1, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+1, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);		

				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);			
				img.setRGB(x+4, y+2, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+3, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+3, (R << 16) + (G << 8) + B);	

				img.setRGB(x+0, y+4, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+4, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+4, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+4, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+5, (R << 16) + (G << 8) + B);			
				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+1, y+6, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+6, (R << 16) + (G << 8) + B);	
		
				img.setRGB(x+1, y+7, (R << 16) + (G << 8) + B);	
				img.setRGB(x+2, y+7, (R << 16) + (G << 8) + B);
				img.setRGB(x+3, y+7, (R << 16) + (G << 8) + B);	

				break;
			case 9:

				img.setRGB(x+2, y+6, (R << 16) + (G << 8) + B);		
				img.setRGB(x+3, y+5, (R << 16) + (G << 8) + B);	
				img.setRGB(x+3, y+6, (R << 16) + (G << 8) + B);
				img.setRGB(x+4, y+5, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+4, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+3, (R << 16) + (G << 8) + B); 
				img.setRGB(x+4, y+2, (R << 16) + (G << 8) + B);	
				img.setRGB(x+4, y+1, (R << 16) + (G << 8) + B);

				img.setRGB(x+3, y+0, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+0, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+0, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+1, (R << 16) + (G << 8) + B);

				img.setRGB(x+0, y+2, (R << 16) + (G << 8) + B);

				img.setRGB(x+3, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+2, y+3, (R << 16) + (G << 8) + B);
				img.setRGB(x+1, y+3, (R << 16) + (G << 8) + B);

				break;

			}




	}


	private void showP(int R, int G, int B){

	BufferedImage img=display.getImage();

		img.setRGB(3, 0, (R << 16) + (G << 8) + B);		// White
		img.setRGB(4, 0, (R << 16) + (G << 8) + B);
		img.setRGB(5, 0, (R << 16) + (G << 8) + B);		// P

		img.setRGB(3, 1, (R << 16) + (G << 8) + B);		
		img.setRGB(4, 1, (R << 16) + (G << 8) + B);
		img.setRGB(5, 1, (R << 16) + (G << 8) + B);		
		img.setRGB(6, 1, (R << 16) + (G << 8) + B); 

		img.setRGB(2, 0, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 1, (R << 16) + (G << 8) + B);
		img.setRGB(2, 2, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 3, (R << 16) + (G << 8) + B); 
		img.setRGB(2, 4, (R << 16) + (G << 8) + B);		
		img.setRGB(2, 5, (R << 16) + (G << 8) + B); 
		img.setRGB(2, 6, (R << 16) + (G << 8) + B); 
		img.setRGB(2, 7, (R << 16) + (G << 8) + B); 

		img.setRGB(3, 2, (R << 16) + (G << 8) + B);
		img.setRGB(3, 3, (R << 16) + (G << 8) + B);
		img.setRGB(3, 4, (R << 16) + (G << 8) + B);
		img.setRGB(3, 5, (R << 16) + (G << 8) + B);
		img.setRGB(3, 6, (R << 16) + (G << 8) + B);
		img.setRGB(3, 7, (R << 16) + (G << 8) + B);		

		img.setRGB(4, 4, (R << 16) + (G << 8) + B);
		img.setRGB(4, 5, (R << 16) + (G << 8) + B);


		img.setRGB(5, 4, (R << 16) + (G << 8) + B);
		img.setRGB(6, 4, (R << 16) + (G << 8) + B);
		img.setRGB(5, 5, (R << 16) + (G << 8) + B);

		img.setRGB(6, 2, (R << 16) + (G << 8) + B);
		img.setRGB(6, 3, (R << 16) + (G << 8) + B);



	}


	private void showTitle()
	{
		// PONG TITLE SCREEN
	int R, G, B;
	BufferedImage img=display.getImage();
		R = 255;
		G = 255;
		B = 255;
		img.setRGB(0, 0, (R << 16) + (G << 8) + B);		// White
		img.setRGB(1, 0, (R << 16) + (G << 8) + B);
		img.setRGB(2, 0, (R << 16) + (G << 8) + B);		// P
		img.setRGB(3, 0, (R << 16) + (G << 8) + B); 
		
		img.setRGB(0, 1, (R << 16) + (G << 8) + B);
		img.setRGB(1, 2, (R << 16) + (G << 8) + B);

		img.setRGB(0, 2, (R << 16) + (G << 8) + B);
		img.setRGB(2, 2, (R << 16) + (G << 8) + B);
		img.setRGB(3, 2, (R << 16) + (G << 8) + B);

		img.setRGB(3, 1, (R << 16) + (G << 8) + B);

		img.setRGB(0, 3, (R << 16) + (G << 8) + B);
		img.setRGB(0, 4, (R << 16) + (G << 8) + B);

		R = 255;
		G = 255;
		B = 255;

		img.setRGB(2, 5, (R << 16) + (G << 8) + B);		// O
		img.setRGB(3, 5, (R << 16) + (G << 8) + B);

		img.setRGB(1, 6, (R << 16) + (G << 8) + B);
		img.setRGB(4, 6, (R << 16) + (G << 8) + B);

		img.setRGB(1, 7, (R << 16) + (G << 8) + B);
		img.setRGB(4, 7, (R << 16) + (G << 8) + B);

		img.setRGB(2, 8, (R << 16) + (G << 8) + B);
		img.setRGB(3, 8, (R << 16) + (G << 8) + B);

		R = 255;
		G = 255;
		B = 255;

		img.setRGB(2, 9, (R << 16) + (G << 8) + B);
		img.setRGB(2, 10, (R << 16) + (G << 8) + B);		// N
		img.setRGB(2, 11, (R << 16) + (G << 8) + B);
		img.setRGB(2, 12, (R << 16) + (G << 8) + B);

		img.setRGB(5, 9, (R << 16) + (G << 8) + B);
		img.setRGB(5, 10, (R << 16) + (G << 8) + B);		
		img.setRGB(5, 11, (R << 16) + (G << 8) + B);
		img.setRGB(5, 12, (R << 16) + (G << 8) + B);

		img.setRGB(3, 10, (R << 16) + (G << 8) + B);
		img.setRGB(4, 11, (R << 16) + (G << 8) + B);



		R = 255;
		G = 255;
		B = 255;
		img.setRGB(6, 13, (R << 16) + (G << 8) + B);	// G
		img.setRGB(7, 13, (R << 16) + (G << 8) + B);

		img.setRGB(5, 14, (R << 16) + (G << 8) + B);

		img.setRGB(5, 15, (R << 16) + (G << 8) + B);
		img.setRGB(8, 15, (R << 16) + (G << 8) + B);

		img.setRGB(6, 16, (R << 16) + (G << 8) + B);
		img.setRGB(7, 16, (R << 16) + (G << 8) + B);
		img.setRGB(8, 16, (R << 16) + (G << 8) + B);


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
}
