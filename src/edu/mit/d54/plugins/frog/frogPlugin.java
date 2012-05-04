package edu.mit.d54.plugins.frog;

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
 * This is a plugin implementing the Frog game.  User input is received over the TCP socket on port 12345.
 */
public class frogPlugin extends DisplayPlugin {
	
	private enum State { IDLE, GAME, GAME_END_1, GAME_END_2, LEVEL_START, IDLE_ANIM };
	
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
		
	private int levelNumber = 1;

	private int frogPosX = 0;
	private int frogPosY = 0;

	private int lane1Enabled = 0;
	private int lane1X = 0;
	private int lane1Car1Y = 0;
	private int lane1Car2Y = 0;
	private int lane1Car3Y = 0;
	private int lane1Car4Y = 0;

	private int lane2Enabled = 0;
	private int lane2X = 0;
	private int lane2Car1Y = 0;
	private int lane2Car2Y = 0;
	private int lane2Car3Y = 0;
	private int lane2Car4Y = 0;

	private int lane3Enabled = 0;
	private int lane3X = 0;
	private int lane3Car1Y = 0;
	private int lane3Car2Y = 0;
	private int lane3Car3Y = 0;
	private int lane3Car4Y = 0;

	private int lane4Enabled = 0;
	private int lane4X = 0;
	private int lane4Car1Y = 0;
	private int lane4Car2Y = 0;
	private int lane4Car3Y = 0;
	private int lane4Car4Y = 0;

	private int lane5Enabled = 0;
	private int lane5X = 0;
	private int lane5Car1Y = 0;
	private int lane5Car2Y = 0;
	private int lane5Car3Y = 0;
	private int lane5Car4Y = 0;

	private int lane6Enabled = 0;
	private int lane6X = 0;
	private int lane6Car1Y = 0;
	private int lane6Car2Y = 0;
	private int lane6Car3Y = 0;
	private int lane6Car4Y = 0;


	private double lane1StepTime = 0.4;
	private double lane1LastStep = 0.0;

	private double lane2StepTime = 0.6;
	private double lane2LastStep = 0.0;

	private double lane3StepTime = 0.6;
	private double lane3LastStep = 0.0;

	private double lane4StepTime = 0.6;
	private double lane4LastStep = 0.0;

	private double lane5StepTime = 0.6;
	private double lane5LastStep = 0.0;

	private double lane6StepTime = 0.6;
	private double lane6LastStep = 0.0;

	private int vert = 0;
	
	public frogPlugin(Display2D display, double framerate) throws IOException {
		super(display, framerate);
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
				gameState=State.LEVEL_START;
				animTime=0;
			}
			break;
		case LEVEL_START:
			animTime+=timestep;
			showL();			// show which level
			showLevelNumber(levelNumber);

			loadLevel(levelNumber);		// load the level data

			frogPosX = 0;			// put frog on the left
			frogPosY = 8;

			if (animTime > 3.0){		// wait 3 seconds and start level
			gameState=State.GAME;
			}
			break;
		case GAME:
			animTime+=timestep;
			drawScreen();		// draws the frog and all enabled cars in their corresponding colour
			moveCars();  		// checks whether it's time to move each lane and does so if necessary 

			// move frog
			switch (userInput)
			{
			case 'L':
				if (frogPosX > 0){
					frogPosX = frogPosX - 1;
				}
				
				break;
			case 'R':
				if (frogPosX < 8){
					frogPosX = frogPosX + 1;
				}
				
				if (frogPosX == 8){ // if the frog has reached the right hand side

					levelNumber = levelNumber + 1;		// go up a level

					if (levelNumber == 10){  // if you've finished all 9 levels
	
						clearScreen();
						gameState=State.GAME_END_2;
						animTime=0;

					}else{  // or if it's just the next stage

						clearScreen();
						gameState=State.LEVEL_START;
						animTime=0;
					}
				}

				break;
			case 'U':

				if (frogPosY > 0){
					frogPosY = frogPosY - 1;
				}
				break;
			case 'D':

				if (frogPosY < 16){
					frogPosY = frogPosY + 1;
				}
				break;
			case -1: //there was an error in the network socket or no client connected -- "pause" the game
				return;
			}

			hitCheck();  // check whether frog and any of the enabled cars occupy the same area and if so, die


			break;
		case GAME_END_1:   // lose
			animTime+=timestep;

		for (int i = 0;i < vert; i++){			// death animation
			for (int horiz = 0; horiz < 9; horiz++){
				img.setRGB(horiz, i, ((255-(i*15)) << 16) + (0 << 8) + 0); // (red)
			}
		}

			
		if (animTime - animTimeLastStep > 0.1){  // time to write a new line
			if (vert < 17){
				vert = vert + 1;
			}			
		animTimeLastStep = animTime;
		}

			if (animTime > 2.5){   // after 2.5 secs go back to the title screen

				gameState=State.IDLE;
	
			}

			
			break;
			case GAME_END_2:  // win
			animTime+=timestep;
			showWin(); 		// write WIN on the screen

			for (int i = 0;i < vert; i++){		// animated wipe away of "WIN" 
				for (int horiz = 0; horiz < 9; horiz++){
					img.setRGB(horiz, i, (0 << 16) + (0 << 8) + 0); // (BLACK)
				}
			}

		if (animTime > 5.0){
			if (animTime - animTimeLastStep > 0.1){  // time to write a new line
				if (vert < 17){
					vert = vert + 1;
				}			
			animTimeLastStep = animTime;
			}
		}


			if (animTime > 8.0){  // wait a bit longer before going back to title screen

				gameState=State.IDLE;
	
			}




			break;
		
		}
}

	public void moveCars()   // check whether it's time to move each lane, and if so, move the cars
	{

	if (animTime - lane1LastStep > lane1StepTime){  // time to step lane 1

				if (lane1Car1Y < 16){  // move car 1
					lane1Car1Y = lane1Car1Y + 1;
				}else{
					lane1Car1Y = 0;
				}

				if (lane1Car2Y < 16){  // move car 2
					lane1Car2Y = lane1Car2Y + 1;
				}else{
					lane1Car2Y = 0;
				}

				if (lane1Car3Y < 16){  // move car 3
					lane1Car3Y = lane1Car3Y + 1;
				}else{
					lane1Car3Y = 0;
				}
				
				if (lane1Car4Y < 16){  // move car 4
					lane1Car4Y = lane1Car4Y + 1;
				}else{
					lane1Car4Y = 0;
				}
				
			lane1LastStep = animTime;
			}

			if (animTime - lane2LastStep > lane2StepTime){  // time to step lane 2

				if (lane2Car1Y > 0){  // move car 1
					lane2Car1Y = lane2Car1Y - 1;
				}else{
					lane2Car1Y = 16;
				}

				if (lane2Car2Y > 0){  // move car 2
					lane2Car2Y = lane2Car2Y - 1;
				}else{
					lane2Car2Y = 16;
				}

				if (lane2Car3Y > 0){  // move car 3
					lane2Car3Y = lane2Car3Y - 1;
				}else{
					lane2Car3Y = 16;
				}
				
				if (lane2Car4Y > 0){  // move car 4
					lane2Car4Y = lane2Car4Y - 1;
				}else{
					lane2Car4Y = 16;
				}

			lane2LastStep = animTime;
			}

			if (animTime - lane3LastStep > lane3StepTime){  // time to step lane 3

				if (lane3Car1Y < 16){  // move car 1
					lane3Car1Y = lane3Car1Y + 1;
				}else{
					lane3Car1Y = 0;
				}

				if (lane3Car2Y < 16){  // move car 2
					lane3Car2Y = lane3Car2Y + 1;
				}else{
					lane3Car2Y = 0;
				}

				if (lane3Car3Y < 16){  // move car 3
					lane3Car3Y = lane3Car3Y + 1;
				}else{
					lane3Car3Y = 0;
				}
				
				if (lane3Car4Y < 16){  // move car 4
					lane3Car4Y = lane3Car4Y + 1;
				}else{
					lane3Car4Y = 0;
				}
				
			lane3LastStep = animTime;
			}

			if (animTime - lane4LastStep > lane4StepTime){  // time to step lane 4

				if (lane4Car1Y > 0){  // move car 1
					lane4Car1Y = lane4Car1Y - 1;
				}else{
					lane4Car1Y = 16;
				}

				if (lane4Car2Y > 0){  // move car 2
					lane4Car2Y = lane4Car2Y - 1;
				}else{
					lane4Car2Y = 16;
				}

				if (lane4Car3Y > 0){  // move car 3
					lane4Car3Y = lane4Car3Y - 1;
				}else{
					lane4Car3Y = 16;
				}
				
				if (lane4Car4Y > 0){  // move car 4
					lane4Car4Y = lane4Car4Y - 1;
				}else{
					lane4Car4Y = 16;
				}

			lane4LastStep = animTime;
			}

			if (animTime - lane5LastStep > lane5StepTime){  // time to step lane 5

				if (lane5Car1Y < 16){  // move car 1
					lane5Car1Y = lane5Car1Y + 1;
				}else{
					lane5Car1Y = 0;
				}

				if (lane5Car2Y < 16){  // move car 2
					lane5Car2Y = lane5Car2Y + 1;
				}else{
					lane5Car2Y = 0;
				}

				if (lane5Car3Y < 16){  // move car 3
					lane5Car3Y = lane5Car3Y + 1;
				}else{
					lane5Car3Y = 0;
				}
				
				if (lane5Car4Y < 16){  // move car 4
					lane5Car4Y = lane5Car4Y + 1;
				}else{
					lane5Car4Y = 0;
				}
				
			lane5LastStep = animTime;
			}

			if (animTime - lane6LastStep > lane6StepTime){  // time to step lane 6

				if (lane6Car1Y > 0){  // move car 1
					lane6Car1Y = lane6Car1Y - 1;
				}else{
					lane6Car1Y = 16;
				}

				if (lane6Car2Y > 0){  // move car 2
					lane6Car2Y = lane6Car2Y - 1;
				}else{
					lane6Car2Y = 16;
				}

				if (lane6Car3Y > 0){  // move car 3
					lane6Car3Y = lane6Car3Y - 1;
				}else{
					lane6Car3Y = 16;
				}
				
				if (lane6Car4Y > 0){  // move car 4
					lane6Car4Y = lane6Car4Y - 1;
				}else{
					lane6Car4Y = 16;
				}
			lane6LastStep = animTime;
			}
	}


	
	public void drawScreen() // draw the frog and all the cars in the right place and colour
	{
		BufferedImage img=display.getImage();

		img.setRGB(frogPosX, frogPosY, (0 << 16) + (255 << 8) + 0); // frog 

//////////////////  LANE 1  /////////////////////////
	if (lane1Enabled == 1){
		img.setRGB(lane1X, lane1Car1Y, (255 << 16) + (0 << 8) + 0); // car 1 (red)


		img.setRGB(lane1X, lane1Car2Y, (255 << 16) + (125 << 8) + 125); // car 2

		if (lane1Car2Y+1 < 17){ // if second part overflows draw at 0
			img.setRGB(lane1X, lane1Car2Y+1, (255 << 16) + (125 << 8) + 125); // car 2 (pink)
		}
		if (lane1Car2Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane1X, 0, (255 << 16) + (125 << 8) + 125); // car 2 (pink)
		}

		img.setRGB(lane1X, lane1Car3Y, (255 << 16) + (187 << 8) + 0); // car 3 (orange)


		img.setRGB(lane1X, lane1Car4Y, (0 << 16) + (0 << 8) + 255); // car 4 (blue)

		if (lane1Car4Y+1 < 17){ // if second part doesn't overflow draw
			img.setRGB(lane1X, lane1Car4Y+1, (0 << 16) + (0 << 8) + 255); // car 4 (blue)
		}
		if (lane1Car4Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane1X, 0, (0 << 16) + (0 << 8) + 255); // car 4 (blue)
		}
		if (lane1Car4Y+2 < 17){ // if third part doesn't overflow draw
			img.setRGB(lane1X, lane1Car4Y+2, (0 << 16) + (0 << 8) + 255); // car 4 (blue)
		}
		if (lane1Car4Y+2 == 17){ // if third part overflows 1 draw at 0
			img.setRGB(lane1X, 0, (0 << 16) + (0 << 8) + 255); // car 4 (blue)
		}
		if (lane1Car4Y+2 == 18){ // if third part overflows 2 draw at 1
			img.setRGB(lane1X, 1, (0 << 16) + (0 << 8) + 255); // car 4 (blue)
		}
	}
//////////////////  LANE 2  /////////////////////////
	if (lane2Enabled == 1){
		img.setRGB(lane2X, lane2Car1Y, (255 << 16) + (255 << 8) + 0); // car 1 (yellow)


		img.setRGB(lane2X, lane2Car2Y, (0 << 16) + (130 << 8) + 130); // car 2	dark green

		if (lane2Car2Y-1 > -1){ // if second part doesn't overflow, draw 
			img.setRGB(lane2X, lane2Car2Y-1, (0 << 16) + (130 << 8) + 130); // car 2 
		}
		if (lane2Car2Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane2X, 16, (0 << 16) + (130 << 8) + 130); // car 2 
		}

		img.setRGB(lane2X, lane2Car3Y, (190 << 16) + (0 << 8) + 255); // car 3 (purple)


		img.setRGB(lane2X, lane2Car4Y, (255 << 16) + (0 << 8) + 0); // car 4 (RED)

		if (lane2Car4Y-1 > -1){ // if second part doesn't overflow draw
			img.setRGB(lane2X, lane2Car4Y-1, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane2Car4Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane2X, 16, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane2Car4Y-2 > -1){ // if third part doesn't overflow draw
			img.setRGB(lane2X, lane2Car4Y-2, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane2Car4Y-2 == -1){ // if third part overflows 1 draw at 16
			img.setRGB(lane2X, 16, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane2Car4Y-2 == -2){ // if third part overflows 2 draw at 15
			img.setRGB(lane2X, 15, (255 << 16) + (0 << 8) + 0); // car 4 
		}
	}

//////////////////  LANE 3  /////////////////////////
	if (lane3Enabled == 1){
		img.setRGB(lane3X, lane3Car1Y, (150 << 16) + (100 << 8) + 0); // car 1 (brown)
     

		img.setRGB(lane3X, lane3Car2Y, (150 << 16) + (100 << 8) + 255); // car 2

		if (lane3Car2Y+1 < 17){ // if second part overflows draw at 0
			img.setRGB(lane3X, lane3Car2Y+1, (150 << 16) + (100 << 8) + 255); // car 2 (purple)
		}
		if (lane3Car2Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane3X, 0, (150 << 16) + (100 << 8) + 255); // car 2 (purple)
		}

		img.setRGB(lane3X, lane3Car3Y, (150 << 16) + (255 << 8) + 255); // car 3 (light blue)


		img.setRGB(lane3X, lane3Car4Y, (255 << 16) + (175 << 8) + 133); // car 4 (peach)

		if (lane3Car4Y+1 < 17){ // if second part doesn't overflow draw
			img.setRGB(lane3X, lane3Car4Y+1, (255 << 16) + (175 << 8) + 133); // car 4 (blue)
		}
		if (lane3Car4Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane3X, 0, (255 << 16) + (175 << 8) + 133); // car 4 (blue)
		}
		if (lane3Car4Y+2 < 17){ // if third part doesn't overflow draw
			img.setRGB(lane3X, lane3Car4Y+2, (255 << 16) + (175 << 8) + 133); // car 4 (blue)
		}
		if (lane3Car4Y+2 == 17){ // if third part overflows 1 draw at 0
			img.setRGB(lane3X, 0, (255 << 16) + (175 << 8) + 133); // car 4 (blue)
		}
		if (lane3Car4Y+2 == 18){ // if third part overflows 2 draw at 1
			img.setRGB(lane3X, 1, (255 << 16) + (175 << 8) + 133); // car 4 (blue)
		}
	}
//////////////////  LANE 4  /////////////////////////
	if (lane4Enabled == 1){
		img.setRGB(lane4X, lane4Car1Y, (150 << 16) + (150 << 8) + 150); // car 1 (grey)


		img.setRGB(lane4X, lane4Car2Y, (0 << 16) + (150 << 8) + 150); // car 2	aqua

		if (lane4Car2Y-1 > -1){ // if second part doesn't overflow, draw 
			img.setRGB(lane4X, lane4Car2Y-1, (0 << 16) + (150 << 8) + 150); // car 2 
		}
		if (lane4Car2Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane4X, 16, (0 << 16) + (150 << 8) + 150); // car 2 
		}

		img.setRGB(lane4X, lane4Car3Y, (213 << 16) + (0 << 8) + 45); // car 3 (maroon)


		img.setRGB(lane4X, lane4Car4Y, (0 << 16) + (111 << 8) + 255); // car 4 (sky blue)

		if (lane4Car4Y-1 > -1){ // if second part doesn't overflow draw
			img.setRGB(lane4X, lane4Car4Y-1, (0 << 16) + (111 << 8) + 255); // car 4 
		}
		if (lane4Car4Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane4X, 16, (0 << 16) + (111 << 8) + 255); // car 4 
		}
		if (lane4Car4Y-2 > -1){ // if third part doesn't overflow draw
			img.setRGB(lane4X, lane4Car4Y-2, (0 << 16) + (111 << 8) + 255); // car 4 
		}
		if (lane4Car4Y-2 == -1){ // if third part overflows 1 draw at 16
			img.setRGB(lane4X, 16, (0 << 16) + (111 << 8) + 255); // car 4 
		}
		if (lane4Car4Y-2 == -2){ // if third part overflows 2 draw at 15
			img.setRGB(lane4X, 15, (0 << 16) + (111 << 8) + 255); // car 4 
		}
	}
//////////////////  LANE 5  /////////////////////////
	if (lane5Enabled == 1){
		img.setRGB(lane5X, lane5Car1Y, (0 << 16) + (255 << 8) + 255); // car 1 (light blue)


		img.setRGB(lane5X, lane5Car2Y, (255 << 16) + (145 << 8) + 0); // car 2

		if (lane5Car2Y+1 < 17){ // if second part overflows draw at 0
			img.setRGB(lane5X, lane5Car2Y+1, (255 << 16) + (145 << 8) + 0); // car 2 (pink)
		}
		if (lane5Car2Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane5X, 0, (255 << 16) + (145 << 8) + 0); // car 2 (greenbrown)
		}

		img.setRGB(lane5X, lane5Car3Y, (200 << 16) + (250 << 8) + 150); // car 3 (pastel green)


		img.setRGB(lane5X, lane5Car4Y, (255 << 16) + (0 << 8) + 150); // car 4 (hot pink)

		if (lane5Car4Y+1 < 17){ // if second part doesn't overflow draw
			img.setRGB(lane5X, lane5Car4Y+1, (255 << 16) + (0 << 8) + 150); // car 4 
		}
		if (lane5Car4Y+1 == 17){ // if second part overflows draw at 0
			img.setRGB(lane5X, 0, (255 << 16) + (0 << 8) + 150); // car 4 
		}
		if (lane5Car4Y+2 < 17){ // if third part doesn't overflow draw
			img.setRGB(lane5X, lane5Car4Y+2, (255 << 16) + (0 << 8) + 150); // car 4 
		}
		if (lane5Car4Y+2 == 17){ // if third part overflows 1 draw at 0
			img.setRGB(lane5X, 0, (255 << 16) + (0 << 8) + 150); // car 4 
		}
		if (lane5Car4Y+2 == 18){ // if third part overflows 2 draw at 1
			img.setRGB(lane5X, 1, (255 << 16) + (0 << 8) + 150); // car 4 
		}
	}
//////////////////  LANE 6  /////////////////////////
	if (lane6Enabled == 1){
		img.setRGB(lane6X, lane6Car1Y, (255 << 16) + (255 << 8) + 0); // car 1 (yellow)


		img.setRGB(lane6X, lane6Car2Y, (0 << 16) + (130 << 8) + 130); // car 2	dark green

		if (lane6Car2Y-1 > -1){ // if second part doesn't overflow, draw 
			img.setRGB(lane6X, lane6Car2Y-1, (0 << 16) + (130 << 8) + 130); // car 2 
		}
		if (lane6Car2Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane6X, 16, (0 << 16) + (130 << 8) + 130); // car 2 
		}

		img.setRGB(lane6X, lane6Car3Y, (190 << 16) + (0 << 8) + 255); // car 3 (purple)


		img.setRGB(lane6X, lane6Car4Y, (255 << 16) + (0 << 8) + 0); // car 4 (RED)

		if (lane6Car4Y-1 > -1){ // if second part doesn't overflow draw
			img.setRGB(lane6X, lane6Car4Y-1, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane6Car4Y-1 == -1){ // if second part overflows draw at 16
			img.setRGB(lane6X, 16, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane6Car4Y-2 > -1){ // if third part doesn't overflow draw
			img.setRGB(lane6X, lane6Car4Y-2, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane6Car4Y-2 == -1){ // if third part overflows 1 draw at 16
			img.setRGB(lane6X, 16, (255 << 16) + (0 << 8) + 0); // car 4 
		}
		if (lane6Car4Y-2 == -2){ // if third part overflows 2 draw at 15
			img.setRGB(lane6X, 15, (255 << 16) + (0 << 8) + 0); // car 4 
		}
	}

}



	public void hitCheck()  // check whether frog has collided with any cars, and if so, die
	{

	 int hit = 0;
	if (lane1Enabled == 1){	
		if (frogPosX == lane1X){			// Lane 1
			if (frogPosY == lane1Car1Y || frogPosY == lane1Car2Y || frogPosY == lane1Car2Y+1 || frogPosY == lane1Car3Y || frogPosY == lane1Car4Y || frogPosY == lane1Car4Y+1 || frogPosY == lane1Car4Y+2){
				hit = 1;
			}

			if (lane1Car2Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}

			if (lane1Car4Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}
			if (lane1Car4Y+2 == 17 && frogPosY == 0){  // when overlap pixel 3
				hit = 1;
			}
			if (lane1Car4Y+2 == 18 && frogPosY == 1){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}
	if (lane2Enabled == 1){
		if (frogPosX == lane2X){			// Lane 2
			if (frogPosY == lane2Car1Y || frogPosY == lane2Car2Y || frogPosY == lane2Car2Y-1 || frogPosY == lane2Car3Y || frogPosY == lane2Car4Y || frogPosY == lane2Car4Y-1 || frogPosY == lane2Car4Y-2){
				hit = 1;
			}

			if (lane2Car2Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}

			if (lane2Car4Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}
			if (lane2Car4Y-2 == -1 && frogPosY == 16){  // when overlap pixel 3
				hit = 1;
			}
			if (lane2Car4Y-2 == -2 && frogPosY == 15){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}
	if (lane3Enabled == 1){	
		if (frogPosX == lane3X){			// Lane 3
			if (frogPosY == lane3Car1Y || frogPosY == lane3Car2Y || frogPosY == lane3Car2Y+1 || frogPosY == lane3Car3Y || frogPosY == lane3Car4Y || frogPosY == lane3Car4Y+1 || frogPosY == lane3Car4Y+2){
				hit = 1;
			}

			if (lane3Car2Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}

			if (lane3Car4Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}
			if (lane3Car4Y+2 == 17 && frogPosY == 0){  // when overlap pixel 3
				hit = 1;
			}
			if (lane3Car4Y+2 == 18 && frogPosY == 1){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}
	if (lane4Enabled == 1){
		if (frogPosX == lane4X){			// Lane 4
			if (frogPosY == lane4Car1Y || frogPosY == lane4Car2Y || frogPosY == lane4Car2Y-1 || frogPosY == lane4Car3Y || frogPosY == lane4Car4Y || frogPosY == lane4Car4Y-1 || frogPosY == lane4Car4Y-2){
				hit = 1;
			}

			if (lane4Car2Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}

			if (lane4Car4Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}
			if (lane4Car4Y-2 == -1 && frogPosY == 16){  // when overlap pixel 3
				hit = 1;
			}
			if (lane4Car4Y-2 == -2 && frogPosY == 15){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}
	if (lane5Enabled == 1){	
		if (frogPosX == lane5X){			// Lane 5
			if (frogPosY == lane5Car1Y || frogPosY == lane5Car2Y || frogPosY == lane5Car2Y+1 || frogPosY == lane5Car3Y || frogPosY == lane5Car4Y || frogPosY == lane5Car4Y+1 || frogPosY == lane5Car4Y+2){
				hit = 1;
			}

			if (lane5Car2Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}

			if (lane5Car4Y+1 == 17 && frogPosY == 0){  // when overlap pixel 2
				hit = 1;
			}
			if (lane5Car4Y+2 == 17 && frogPosY == 0){  // when overlap pixel 3
				hit = 1;
			}
			if (lane5Car4Y+2 == 18 && frogPosY == 1){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}
	if (lane6Enabled == 1){
		if (frogPosX == lane6X){			// Lane 6
			if (frogPosY == lane6Car1Y || frogPosY == lane6Car2Y || frogPosY == lane6Car2Y-1 || frogPosY == lane6Car3Y || frogPosY == lane6Car4Y || frogPosY == lane6Car4Y-1 || frogPosY == lane6Car4Y-2){
				hit = 1;
			}

			if (lane6Car2Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}

			if (lane6Car4Y-1 == -1 && frogPosY == 16){  // when overlap pixel 2
				hit = 1;
			}
			if (lane6Car4Y-2 == -1 && frogPosY == 16){  // when overlap pixel 3
				hit = 1;
			}
			if (lane6Car4Y-2 == -2 && frogPosY == 15){  // when overlap pixel 3
				hit = 1;
			}
	
		}
	}



	if (hit == 1){			//  die
		gameState=State.GAME_END_1;
		System.out.println("Game Over - You got to level : "+levelNumber);
		animTime=0; // reset timer
		levelNumber = 1;
		hit = 0;
	}


	}

	public void loadLevel(int levelNumber)  // all of the information to build the levels - easy to make more if needed
	{

		switch (levelNumber)
			{
			case 1:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 0;
			lane4Enabled = 0;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;

			lane1StepTime = 0.4;
			lane2StepTime = 0.6;

			lane1X = 3;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 5;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;


				break;
			case 2:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 0;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;

			lane1StepTime = 0.6;
			lane2StepTime = 0.5;
			lane3StepTime = 0.4;

			lane1X = 2;
			lane1Car1Y = 3;
			lane1Car2Y = 5;
			lane1Car3Y = 0;
			lane1Car4Y = 10;

			lane2X = 4;
			lane2Car1Y = 9;
			lane2Car2Y = 2;
			lane2Car3Y = 12;
			lane2Car4Y = 7;

			lane3X = 6;
			lane3Car1Y = 5;
			lane3Car2Y = 11;
			lane3Car3Y = 8;
			lane3Car4Y = 0;

				break;
			case 3:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;

			lane1StepTime = 0.5;
			lane2StepTime = 0.4;
			lane3StepTime = 0.4;
			lane4StepTime = 0.5;

			lane1X = 1;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 3;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 5;
			lane3Car1Y = 2;
			lane3Car2Y = 5;
			lane3Car3Y = 10;
			lane3Car4Y = 13;

			lane4X = 7;
			lane4Car1Y = 2;
			lane4Car2Y = 6;
			lane4Car3Y = 9;
			lane4Car4Y = 13;

				break;
			case 4:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;

			lane1StepTime = 0.4;
			lane2StepTime = 0.6;
			lane3StepTime = 0.5;
			lane4StepTime = 0.4;

			lane1X = 4;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 2;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 7;
			lane3Car1Y = 0;
			lane3Car2Y = 2;
			lane3Car3Y = 5;
			lane3Car4Y = 7;

			lane4X = 5;
			lane4Car1Y = 0;
			lane4Car2Y = 4;
			lane4Car3Y = 8;
			lane4Car4Y = 12;

				break;
			case 5:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;

			lane1StepTime = 0.5;
			lane2StepTime = 0.5;
			lane3StepTime = 0.4;
			lane4StepTime = 0.4;

			lane1X = 2;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 5;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 3;
			lane3Car1Y = 2;
			lane3Car2Y = 4;
			lane3Car3Y = 9;
			lane3Car4Y = 13;

			lane4X = 6;
			lane4Car1Y = 1;
			lane4Car2Y = 5;
			lane4Car3Y = 8;
			lane4Car4Y = 12;

				break;
			case 6:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 0;
			lane5Enabled = 0;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;

			lane1StepTime = 0.4;
			lane2StepTime = 0.6;
			lane3StepTime = 0.5;

			lane1X = 5;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 4;
			lane2Car1Y = 10;
			lane2Car2Y = 2;
			lane2Car3Y = 12;
			lane2Car4Y = 7;

			lane3X = 3;
			lane3Car1Y = 2;
			lane3Car2Y = 5;
			lane3Car3Y = 9;
			lane3Car4Y = 12;

				break;
			case 7:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 1;
			lane6Enabled = 0;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;
			lane5LastStep=0.0;

			lane1StepTime = 0.4;
			lane2StepTime = 0.6;
			lane3StepTime = 0.5;
			lane4StepTime = 0.5;
			lane5StepTime = 0.4;

			lane1X = 7;
			lane1Car1Y = 0;
			lane1Car2Y = 3;
			lane1Car3Y = 8;
			lane1Car4Y = 12;

			lane2X = 3;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 4;
			lane3Car1Y = 1;
			lane3Car2Y = 4;
			lane3Car3Y = 9;
			lane3Car4Y = 11;

			lane4X = 5;
			lane4Car1Y = 2;
			lane4Car2Y = 6;
			lane4Car3Y = 13;
			lane4Car4Y = 9;

			lane5X = 1;
			lane5Car1Y = 0;
			lane5Car2Y = 3;
			lane5Car3Y = 6;
			lane5Car4Y = 10;

				break;
			case 8:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 1;
			lane6Enabled = 1;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;
			lane5LastStep=0.0;
			lane6LastStep=0.0;

			lane1StepTime = 0.6;
			lane2StepTime = 0.6;
			lane3StepTime = 0.5;
			lane4StepTime = 0.5;
			lane5StepTime = 0.4;
			lane6StepTime = 0.4;


			lane1X = 4;
			lane1Car1Y = 0;
			lane1Car2Y = 3;
			lane1Car3Y = 8;
			lane1Car4Y = 12;

			lane2X = 5;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 1;
			lane3Car1Y = 0;
			lane3Car2Y = 3;
			lane3Car3Y = 9;
			lane3Car4Y = 12;

			lane4X = 2;
			lane4Car1Y = 0;
			lane4Car2Y = 4;
			lane4Car3Y = 9;
			lane4Car4Y = 13;

			lane5X = 7;
			lane5Car1Y = 1;
			lane5Car2Y = 3;
			lane5Car3Y = 7;
			lane5Car4Y = 11;

			lane6X = 8;
			lane6Car1Y = 1;
			lane6Car2Y = 5;
			lane6Car3Y = 8;
			lane6Car4Y = 12;

				break;
			case 9:

			lane1Enabled = 1;
			lane2Enabled = 1;
			lane3Enabled = 1;
			lane4Enabled = 1;
			lane5Enabled = 1;
			lane6Enabled = 1;

			lane1LastStep=0.0;
			lane2LastStep=0.0;
			lane3LastStep=0.0;
			lane4LastStep=0.0;
			lane5LastStep=0.0;
			lane6LastStep=0.0;

			lane1StepTime = 0.4;
			lane2StepTime = 0.6;
			lane3StepTime = 0.5;
			lane4StepTime = 0.5;
			lane5StepTime = 0.4;
			lane6StepTime = 0.3;


			lane1X = 1;
			lane1Car1Y = 0;
			lane1Car2Y = 4;
			lane1Car3Y = 8;
			lane1Car4Y = 11;

			lane2X = 2;
			lane2Car1Y = 10;
			lane2Car2Y = 1;
			lane2Car3Y = 13;
			lane2Car4Y = 7;

			lane3X = 3;
			lane3Car1Y = 0;
			lane3Car2Y = 2;
			lane3Car3Y = 6;
			lane3Car4Y = 11;

			lane4X = 5;
			lane4Car1Y = 2;
			lane4Car2Y = 7;
			lane4Car3Y = 10;
			lane4Car4Y = 12;

			lane5X = 6;
			lane5Car1Y = 0;
			lane5Car2Y = 3;
			lane5Car3Y = 8;
			lane5Car4Y = 13;

			lane6X = 7;
			lane6Car1Y = 0;
			lane6Car2Y = 3;
			lane6Car3Y = 8;
			lane6Car4Y = 12;

				break;

			}


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

		int R, G, B;
		BufferedImage img=display.getImage();
		R = 0;
		G = 255;
		B = 0;
		img.setRGB(0, 0, (R << 16) + (G << 8) + B);		// Brightest Green
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

		R = 125;
		G = 255;
		B = 128;

		img.setRGB(1, 5, (R << 16) + (G << 8) + B);		// FADED Green
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

		R = 176;
		G = 255;
		B = 0;

		img.setRGB(0, 12, (R << 16) + (G << 8) + B);		// Other Green
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

	private void showL()
	{

		int R, G, B;
		BufferedImage img=display.getImage();
		R = 0;
		G = 255;
		B = 0;
		img.setRGB(2, 1, (R << 16) + (G << 8) + B);		// Brightest Green
		img.setRGB(2, 2, (R << 16) + (G << 8) + B);
		img.setRGB(2, 3, (R << 16) + (G << 8) + B);		// L
		img.setRGB(2, 4, (R << 16) + (G << 8) + B); 
		img.setRGB(2, 5, (R << 16) + (G << 8) + B);
		img.setRGB(2, 6, (R << 16) + (G << 8) + B);
		
		img.setRGB(3, 1, (R << 16) + (G << 8) + B);		
		img.setRGB(3, 2, (R << 16) + (G << 8) + B);
		img.setRGB(3, 3, (R << 16) + (G << 8) + B);		
		img.setRGB(3, 4, (R << 16) + (G << 8) + B); 
		img.setRGB(3, 5, (R << 16) + (G << 8) + B);
		img.setRGB(3, 6, (R << 16) + (G << 8) + B);
		
		img.setRGB(4, 5, (R << 16) + (G << 8) + B);
		img.setRGB(5, 5, (R << 16) + (G << 8) + B);
		img.setRGB(6, 5, (R << 16) + (G << 8) + B);
		img.setRGB(7, 5, (R << 16) + (G << 8) + B);

		img.setRGB(4, 6, (R << 16) + (G << 8) + B);
		img.setRGB(5, 6, (R << 16) + (G << 8) + B);
		img.setRGB(6, 6, (R << 16) + (G << 8) + B);
		img.setRGB(7, 6, (R << 16) + (G << 8) + B);

	}

	private void showLevelNumber(int levelNumber)
	{

		int R, G, B;
		BufferedImage img=display.getImage();

		switch (levelNumber)
			{
			case 1:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(4, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(4, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 11, (R << 16) + (G << 8) + B);		// 1
				img.setRGB(4, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(4, 13, (R << 16) + (G << 8) + B);
				img.setRGB(4, 14, (R << 16) + (G << 8) + B);
		
				img.setRGB(5, 9, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 10, (R << 16) + (G << 8) + B);
				img.setRGB(5, 11, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(5, 13, (R << 16) + (G << 8) + B);
				img.setRGB(5, 14, (R << 16) + (G << 8) + B);
		
				img.setRGB(3, 15, (R << 16) + (G << 8) + B);
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);
				img.setRGB(6, 15, (R << 16) + (G << 8) + B);

				img.setRGB(3, 16, (R << 16) + (G << 8) + B);
				img.setRGB(4, 16, (R << 16) + (G << 8) + B);
				img.setRGB(5, 16, (R << 16) + (G << 8) + B);
				img.setRGB(6, 16, (R << 16) + (G << 8) + B);

				img.setRGB(3, 10, (R << 16) + (G << 8) + B);


				break;
			case 2:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(5, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(6, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(7, 11, (R << 16) + (G << 8) + B);		// 2
				img.setRGB(6, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(5, 13, (R << 16) + (G << 8) + B);
				img.setRGB(4, 14, (R << 16) + (G << 8) + B);
		
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 10, (R << 16) + (G << 8) + B);
				img.setRGB(6, 11, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(4, 13, (R << 16) + (G << 8) + B);
				img.setRGB(3, 14, (R << 16) + (G << 8) + B);
		
				img.setRGB(3, 15, (R << 16) + (G << 8) + B);
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);
				img.setRGB(6, 15, (R << 16) + (G << 8) + B);
				img.setRGB(7, 15, (R << 16) + (G << 8) + B);

				img.setRGB(3, 16, (R << 16) + (G << 8) + B);
				img.setRGB(4, 16, (R << 16) + (G << 8) + B);
				img.setRGB(5, 16, (R << 16) + (G << 8) + B);
				img.setRGB(6, 16, (R << 16) + (G << 8) + B);
				img.setRGB(7, 16, (R << 16) + (G << 8) + B);

				img.setRGB(3, 10, (R << 16) + (G << 8) + B);
				img.setRGB(4, 10, (R << 16) + (G << 8) + B);




				break;
			case 3:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(3, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 9, (R << 16) + (G << 8) + B);		// 3
				img.setRGB(6, 9, (R << 16) + (G << 8) + B); 
				img.setRGB(3, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 10, (R << 16) + (G << 8) + B); 

				img.setRGB(6, 11, (R << 16) + (G << 8) + B); 

				img.setRGB(3, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(3, 13, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 13, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 13, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 13, (R << 16) + (G << 8) + B); 

				img.setRGB(6, 14, (R << 16) + (G << 8) + B);

				img.setRGB(3, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 15, (R << 16) + (G << 8) + B); 
				img.setRGB(3, 16, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 16, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 16, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 16, (R << 16) + (G << 8) + B);
		

				break;
			case 4:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(3, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(3, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 11, (R << 16) + (G << 8) + B);		// 4
				img.setRGB(3, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(3, 13, (R << 16) + (G << 8) + B);	

				img.setRGB(2, 9, (R << 16) + (G << 8) + B);	
				img.setRGB(2, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(2, 11, (R << 16) + (G << 8) + B);		
				img.setRGB(2, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(2, 13, (R << 16) + (G << 8) + B);
				img.setRGB(2, 14, (R << 16) + (G << 8) + B); 
				img.setRGB(2, 15, (R << 16) + (G << 8) + B);	 

				img.setRGB(3, 14, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 14, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 14, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 14, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 14, (R << 16) + (G << 8) + B);

				img.setRGB(3, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 15, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 15, (R << 16) + (G << 8) + B);

				img.setRGB(5, 13, (R << 16) + (G << 8) + B);
				img.setRGB(6, 13, (R << 16) + (G << 8) + B);

				img.setRGB(5, 16, (R << 16) + (G << 8) + B);
				img.setRGB(6, 16, (R << 16) + (G << 8) + B);


				break;
			case 5:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(3, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 9, (R << 16) + (G << 8) + B);		// 5
				img.setRGB(6, 9, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 9, (R << 16) + (G << 8) + B);	

				img.setRGB(3, 10, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 10, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 10, (R << 16) + (G << 8) + B);	

				img.setRGB(3, 11, (R << 16) + (G << 8) + B);

				img.setRGB(3, 12, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 12, (R << 16) + (G << 8) + B); 

				img.setRGB(3, 13, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 13, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 13, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 13, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 13, (R << 16) + (G << 8) + B);

				img.setRGB(7, 14, (R << 16) + (G << 8) + B);

				img.setRGB(3, 15, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 15, (R << 16) + (G << 8) + B); 
				img.setRGB(7, 15, (R << 16) + (G << 8) + B);

				img.setRGB(3, 16, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 16, (R << 16) + (G << 8) + B);		
				img.setRGB(5, 16, (R << 16) + (G << 8) + B);		
				img.setRGB(6, 16, (R << 16) + (G << 8) + B);

				break;
			case 6:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(4, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(3, 10, (R << 16) + (G << 8) + B);	
				img.setRGB(3, 9, (R << 16) + (G << 8) + B);
				img.setRGB(2, 10, (R << 16) + (G << 8) + B);	
				img.setRGB(2, 11, (R << 16) + (G << 8) + B);		// 6
				img.setRGB(2, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(2, 13, (R << 16) + (G << 8) + B);	
				img.setRGB(2, 14, (R << 16) + (G << 8) + B);
				img.setRGB(2, 15, (R << 16) + (G << 8) + B);

				img.setRGB(3, 15, (R << 16) + (G << 8) + B);
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);

				img.setRGB(6, 14, (R << 16) + (G << 8) + B);

				img.setRGB(6, 13, (R << 16) + (G << 8) + B);

				img.setRGB(3, 12, (R << 16) + (G << 8) + B);
				img.setRGB(4, 12, (R << 16) + (G << 8) + B);
				img.setRGB(5, 12, (R << 16) + (G << 8) + B);


				break;
			case 7:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(2, 9, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(3, 9, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);
				img.setRGB(5, 9, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 9, (R << 16) + (G << 8) + B);		// 7

				img.setRGB(2, 10, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 10, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 10, (R << 16) + (G << 8) + B);
				img.setRGB(5, 10, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 10, (R << 16) + (G << 8) + B);		

				img.setRGB(5, 11, (R << 16) + (G << 8) + B);
				img.setRGB(6, 11, (R << 16) + (G << 8) + B);

				img.setRGB(5, 12, (R << 16) + (G << 8) + B);
				img.setRGB(6, 12, (R << 16) + (G << 8) + B);

				img.setRGB(4, 13, (R << 16) + (G << 8) + B);
				img.setRGB(5, 13, (R << 16) + (G << 8) + B);

				img.setRGB(3, 14, (R << 16) + (G << 8) + B);
				img.setRGB(4, 14, (R << 16) + (G << 8) + B);

				img.setRGB(2, 15, (R << 16) + (G << 8) + B);
				img.setRGB(3, 15, (R << 16) + (G << 8) + B);


				break;
			case 8:

				R = 0;
				G = 255;
				B = 0;

				//img.setRGB(2, 8, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(3, 8, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 8, (R << 16) + (G << 8) + B);
				img.setRGB(5, 8, (R << 16) + (G << 8) + B);	
				//img.setRGB(6, 8, (R << 16) + (G << 8) + B);		// 8

				img.setRGB(2, 9, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 9, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);
				img.setRGB(5, 9, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 9, (R << 16) + (G << 8) + B);		

				img.setRGB(2, 10, (R << 16) + (G << 8) + B);			
				img.setRGB(6, 10, (R << 16) + (G << 8) + B);

				img.setRGB(2, 11, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 11, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 11, (R << 16) + (G << 8) + B);
				img.setRGB(5, 11, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 11, (R << 16) + (G << 8) + B);	

				img.setRGB(2, 12, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 12, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 12, (R << 16) + (G << 8) + B);
				img.setRGB(5, 12, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 12, (R << 16) + (G << 8) + B);

				img.setRGB(2, 13, (R << 16) + (G << 8) + B);			
				img.setRGB(6, 13, (R << 16) + (G << 8) + B);

				img.setRGB(2, 14, (R << 16) + (G << 8) + B);		
				img.setRGB(3, 14, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 14, (R << 16) + (G << 8) + B);
				img.setRGB(5, 14, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 14, (R << 16) + (G << 8) + B);	
		
				img.setRGB(3, 15, (R << 16) + (G << 8) + B);	
				img.setRGB(4, 15, (R << 16) + (G << 8) + B);
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);	

				break;
			case 9:

				R = 0;
				G = 255;
				B = 0;

				img.setRGB(4, 15, (R << 16) + (G << 8) + B);		// Brightest Green
				img.setRGB(5, 14, (R << 16) + (G << 8) + B);	
				img.setRGB(5, 15, (R << 16) + (G << 8) + B);
				img.setRGB(6, 14, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 13, (R << 16) + (G << 8) + B);		// 9
				img.setRGB(6, 12, (R << 16) + (G << 8) + B); 
				img.setRGB(6, 11, (R << 16) + (G << 8) + B);	
				img.setRGB(6, 10, (R << 16) + (G << 8) + B);

				img.setRGB(5, 9, (R << 16) + (G << 8) + B);
				img.setRGB(4, 9, (R << 16) + (G << 8) + B);
				img.setRGB(3, 9, (R << 16) + (G << 8) + B);

				img.setRGB(2, 10, (R << 16) + (G << 8) + B);

				img.setRGB(2, 11, (R << 16) + (G << 8) + B);

				img.setRGB(5, 12, (R << 16) + (G << 8) + B);
				img.setRGB(4, 12, (R << 16) + (G << 8) + B);
				img.setRGB(3, 12, (R << 16) + (G << 8) + B);

				break;

			}




	}



	private void showTitle()
	{
		// FROGGER TITLE SCREEN
	int R, G, B;
	BufferedImage img=display.getImage();
		R = 0;
		G = 255;
		B = 0;
		img.setRGB(0, 0, (R << 16) + (G << 8) + B);		// Brightest Green
		img.setRGB(1, 0, (R << 16) + (G << 8) + B);
		img.setRGB(2, 0, (R << 16) + (G << 8) + B);		// F
		img.setRGB(3, 0, (R << 16) + (G << 8) + B); 
		
		img.setRGB(0, 1, (R << 16) + (G << 8) + B);
		img.setRGB(1, 2, (R << 16) + (G << 8) + B);

		img.setRGB(0, 2, (R << 16) + (G << 8) + B);

		img.setRGB(0, 3, (R << 16) + (G << 8) + B);
		img.setRGB(0, 4, (R << 16) + (G << 8) + B);

		R = 164;
		G = 255;
		B = 0;

		img.setRGB(1, 5, (R << 16) + (G << 8) + B);
		img.setRGB(2, 5, (R << 16) + (G << 8) + B);		// R
		img.setRGB(3, 5, (R << 16) + (G << 8) + B);

		img.setRGB(1, 6, (R << 16) + (G << 8) + B);
		img.setRGB(3, 6, (R << 16) + (G << 8) + B);

		img.setRGB(1, 7, (R << 16) + (G << 8) + B);
		img.setRGB(2, 7, (R << 16) + (G << 8) + B);

		img.setRGB(1, 8, (R << 16) + (G << 8) + B);
		img.setRGB(3, 8, (R << 16) + (G << 8) + B);


		R = 0;
		G = 255;
		B = 140;

		img.setRGB(4, 9, (R << 16) + (G << 8) + B);		// O
		img.setRGB(5, 9, (R << 16) + (G << 8) + B);

		img.setRGB(3, 10, (R << 16) + (G << 8) + B);
		img.setRGB(6, 10, (R << 16) + (G << 8) + B);

		img.setRGB(3, 11, (R << 16) + (G << 8) + B);
		img.setRGB(6, 11, (R << 16) + (G << 8) + B);

		img.setRGB(4, 12, (R << 16) + (G << 8) + B);
		img.setRGB(5, 12, (R << 16) + (G << 8) + B);


		R = 128;
		G = 240;
		B = 126;
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


	public static void main(String[] args) throws Exception
	{
		Display2D display=new GBDisplay();
		
		JFrame frame=new JFrame("Frog!");
		DisplayPanel dPanel=new DisplayPanel(display);
		frame.add(dPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.pack();
		frame.setVisible(true);
		
		frogPlugin plugin=new frogPlugin(display, 15);
		plugin.start();
	}
}
