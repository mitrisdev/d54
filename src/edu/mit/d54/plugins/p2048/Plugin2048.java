package edu.mit.d54.plugins.p2048;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import edu.mit.d54.ArcadeController;
import edu.mit.d54.ArcadeListener;
import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;
import edu.mit.d54.PixelFont;
import edu.mit.d54.TwitterClient;

/**
 * This is a plugin implementing the 2048 game.  User input is received over the TCP socket on port 12345.
 */
public class Plugin2048 extends DisplayPlugin implements ArcadeListener {
	
	private enum State {GAME, IDLE, LOSE, WIN};

	private final double timestep;
	private final int width;
	private final int height;

	private State gameState;
	private int score;
	private double animTime;
	private double animTimeLastStep;
	private int textPos;
	
	private double dt;
	private double time;
	private int segment3Length;
	private ArcadeController controller;
	
	private int[][] board = new int[4][4];
	
	private static final int BASE_X = 1;
	private static final int BASE_Y = 6;
	private static final float ROTATE_PERIOD = 2;
	private static final double TEXT_ANIM_STEP = 0.1;
	
	public Plugin2048(Display2D display, double framerate) throws IOException {
		super(display, framerate);
		timestep=1/framerate;
		dt=1./framerate;
		time=0;
		width=display.getWidth();
		height=display.getHeight();
		
		controller = ArcadeController.getInstance();

		System.out.println("Game paused until client connect");
		TwitterClient.tweet("2048 is now being played on the MIT Green Building! #mittetris");
		
		gameState=State.IDLE;
	}
	
	@Override
	protected void loop() {
		Display2D display=getDisplay();
		Graphics2D gr=display.getGraphics();
		time+=dt;
		switch (gameState)
		{
		case IDLE:
			break;
		default:
			if (!controller.isConnected())
				return;
			if (gameState == State.WIN || gameState == State.LOSE)
			{
				animTime+=timestep;
				if (animTime-animTimeLastStep>=TEXT_ANIM_STEP)
				{
					animTimeLastStep=animTime;
					textPos++;
					if (textPos>50)
						gameState=State.IDLE;
				}
				gr.setFont(PixelFont.getInstance());
				if (gameState == State.WIN)
					gr.drawString("YOU WIN",10-textPos,16);
				else
					gr.drawString("YOU LOSE",10-textPos,16);
			}
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					int r = 0; int g = 0; int b = 0;
					boolean rotate_v = false;
					boolean rotate_h = false;
					
					switch (board[i][j]) {
						//2
						case 1:
							r = 255; g = 0; b = 0;
							break;
						case 2:
							r = 255; g = 255; b = 0;
							break;
						//8
						case 3:
							r = 0; g = 255; b = 0;
							break;
						//16
						case 4: 
							r = 0; g = 255; b = 255;
							break;
						//32
						case 5:
							r = 0; g = 0; b = 255;
							break;
						//64
						case 6:
							r = 255; g = 0; b = 255;
							break;
						//128
						case 7:
							r = 255; g = 255; b = 255;
							break;
						//256
						case 8:
							r = 255; g = 0; b = 0;
							rotate_v = true;
							break;
						//512
						case 9:
							r = 0; g = 255; b = 0;
							rotate_v = true;
							break;
						//1024
						case 10:
							r = 0; g = 0; b = 255;
							rotate_v = true;
							break;
						//2048
						case 11:
							r = 255; g = 0; b = 0;
							rotate_h = true;
							break;
					}
					float[][] hsb = new float[4][3];
					for (int k = 0; k < 4; k++)
					{
						Color.RGBtoHSB(r, g, b, hsb[k]);
						if (rotate_v)
							hsb[k][2] = (float)(0.5*(Math.cos(2*Math.PI*(time/ROTATE_PERIOD + k/4f)) + 1));
						if (rotate_h)
							hsb[k][0] = (float)(time/ROTATE_PERIOD + (k/4f)) % 1;
					}
					display.setPixelHSB(2*j + BASE_X, 2*i + BASE_Y, hsb[0][0], hsb[0][1], hsb[0][2]);
					display.setPixelHSB(2*j + BASE_X, 2*i+1 + BASE_Y, hsb[1][0], hsb[1][1], hsb[1][2]);
					display.setPixelHSB(2*j+1 + BASE_X, 2*i+1 + BASE_Y, hsb[2][0], hsb[2][1], hsb[2][2]);
					display.setPixelHSB(2*j+1 + BASE_X, 2*i + BASE_Y, hsb[3][0], hsb[3][1], hsb[3][2]);
				}
			}
		}
	}
		
	@Override
	protected void onStart()
	{
		controller.setListener(this);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	public void arcadeButton(byte b)
	{
		switch (gameState)
		{
		case IDLE:
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					board[i][j] = 0;
				}
			}
			switch (b)
			{
			case 'L':
			case 'R':
			case 'U':
			case 'D':
				score = 0;
				gameState=State.GAME;
				board[(int) (Math.random() * 4)][(int) (Math.random() * 4)] = 1;
			}
			break;
		case GAME:
			switch (b)
			{
			case 'L':
				move('L');
				break;
			case 'R':
				move('R');
				break;
			case 'U':
				move('U');
				break;
			case 'D':
				move('D');
				break;
			}
			break;
		default:
			break;
		}
	}

	protected void move(char c) {
		boolean[][] looked = new boolean[4][4];
		boolean add = false;
		for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++) looked[i][j] = false;
		if (c == 'L') {
			//Have to make sure a pair exists
			boolean cont = false;
			for (int i = 0; i < 4; i++) for (int j = 1; j < 4; j++) if (board[i][j] != 0 && (board[i][j-1] == board[i][j] || board[i][j-1] == 0)) {add = true; cont = true;}
			if (cont) {
				for (int i = 0; i < 4; i++) {
					int left = -1;
					for (int j = 0; j < 4; j++) {
						if (board[i][j] != 0) {
							if (left == -1) {
								board[i][0] = board[i][j];
								if (j!=0) board[i][j] = 0;
								left = 0;
							}
							else if (board[i][left] == board[i][j] && looked[i][left] == false) {
								board[i][left]++;
								score += 1<<board[i][left];
								looked[i][left] = true;
								board[i][j] = 0;
							}
							else {
								board[i][++left] = board[i][j];
								if (j!=left) board[i][j] = 0;
							}
						}
					}
				}
			}
		}
		if (c == 'R') {
			//Have to make sure a pair exists
			boolean cont = false;
			for (int i = 0; i < 4; i++) for (int j = 2; j > -1; j--) if (board[i][j] != 0 && (board[i][j+1] == board[i][j] || board[i][j+1] == 0)) {add = true; cont = true;}
			if (cont) {
				for (int i = 0; i < 4; i++) {
					int left = 4;
					for (int j = 3; j > -1; j--) {
						if (board[i][j] != 0) {
							if (left == 4) {
								board[i][3] = board[i][j];
								if (j!=3) board[i][j] = 0;
								left = 3;
							}
							else if (board[i][left] == board[i][j] && looked[i][left] == false) {
								board[i][left]++;
								score += 1<<board[i][left];
								looked[i][left] = true;
								board[i][j] = 0;
							}
							else {
								board[i][--left] = board[i][j];
								if (j!=left) board[i][j] = 0;
							}
						}
					}
				}
			}
		}
		if (c == 'U') {
			//Have to make sure a pair exists
			boolean cont = false;
			for (int i = 0; i < 4; i++) for (int j = 1; j < 4; j++) if (board[j][i] != 0 && (board[j-1][i] == board[j][i] || board[j-1][i] == 0)) {add = true; cont = true;}
			if (cont) {
				for (int i = 0; i < 4; i++) {
					int left = -1;
					for (int j = 0; j < 4; j++) {
						if (board[j][i] != 0) {
							if (left == -1) {
								board[0][i] = board[j][i];
								if (j!=0) board[j][i] = 0;
								left = 0;
							}
							else if (board[left][i] == board[j][i] && looked[left][i] == false) {
								board[left][i]++;
								score += 1<<board[left][i];
								looked[left][i] = true;
								board[j][i] = 0;
							}
							else {
								board[++left][i] = board[j][i];
								if (j!=left) board[j][i] = 0;
							}
						}
					}
				}
			}
		}
		if (c == 'D') {
			//Have to make sure a pair exists
			boolean cont = false;
			for (int i = 0; i < 4; i++) for (int j = 2; j > -1; j--) if (board[j][i] != 0 && (board[j+1][i] == board[j][i] || board[j+1][i] == 0)) {add = true; cont = true;}
			if (cont) {
				for (int i = 0; i < 4; i++) {
					int left = 4;
					for (int j = 3; j > -1; j--) {
						if (board[j][i] != 0) {
							if (left == 4) {
								board[3][i] = board[j][i];
								if (j!=3) board[j][i] = 0;
								left = 3;
							}
							else if (board[left][i] == board[j][i] && looked[left][i] == false) {
								board[left][i]++;
								score += 1<<board[left][i];
								looked[left][i] = true;
								board[j][i] = 0;
							}
							else {
								board[--left][i] = board[j][i];
								if (j!=left) board[j][i] = 0;
							}
						}
					}
				}
			}
		}
		if (add) {
			int nx, ny;
			//Yeah, this sucks, but it seems to be OK
			do {
				nx = (int) (Math.random() * 4);
				ny = (int) (Math.random() * 4);
			} while (board[nx][ny] != 0);
			if (Math.random() > 0.8)
				board[nx][ny] = 2;
			else
				board[nx][ny] = 1;
		}
		boolean lose = true;
		boolean win = false;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (board[i][j] == 0) lose = false;
				if (board[i][j] == 11) win = true;
				if (i > 0 && board[i-1][j] == board[i][j]) lose = false;
				if (i < 3 && board[i+1][j] == board[i][j]) lose = false;
				if (j > 0 && board[i][j-1] == board[i][j]) lose = false;
				if (j < 3 && board[i][j+1] == board[i][j]) lose = false;
			}
		}
		animTime=0;
		animTimeLastStep=0;
		textPos=0;
		if (win) {
			gameState=State.WIN;
			System.out.println("win");
			TwitterClient.tweet(String.format("Wow! Somebody just won 2048 on the MIT Green Building! Their score was %d! #mittetris", score));

		}
		if(lose) {
			gameState=State.LOSE;
			System.out.println("lose");
			if (score >= 1000)
				TwitterClient.tweet(String.format("Somebody just played 2048 on the MIT Green Building! Their score was %d! #mittetris", score));
		}
	}
}
