package edu.mit.d54.plugins.mario;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import edu.mit.d54.ArcadeController;
import edu.mit.d54.ArcadeListener;
import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;
import edu.mit.d54.plugins.mitris.MITrisPlugin;

public class MarioPlugin extends DisplayPlugin implements ArcadeListener {

	private enum State { IDLE, GAME, LOAD_LEVEL, GAME_OVER, COMPLETE};

	private final double frameTime;

	private Level level;

	private int delta;

	private ArcadeController controller;

	private State gameState;

	// startScreen info
	private final BufferedImage startScreen;
	private final int startScreenResetWidth;
	private final int startScreenStepsPerFrame;
	private int startScreenScrollPosition;
	private int lives;
	
	// gameOverScreen info
	private final BufferedImage gameOverScreen;
	private final int gameOverScreenResetWidth;
	private final int gameOverScreenStepsPerFrame;
	private int gameOverScreenScrollPosition;
	private int minimumTimeAtGameOverScreen; // make sure people don't mash buttons and miss it
	// complete info
	private final BufferedImage completeScreen;
	private final int completeScreenResetWidth;
	private final int completeScreenStepsPerFrame;
	private int completeScreenScrollPosition;
	private int minimumTimeAtCompleteScreen; // make sure people don't mash buttons and miss it
	
	// level listing
	private List<String> levelMaps;
	private boolean inBonusMode; // occurs after all the levels are finished
	private int currentLevelIndex;

	public MarioPlugin(Display2D display, double framerate) throws IOException {
		super(display, framerate);

		this.delta = 1;
		frameTime =1./framerate;

		controller = ArcadeController.getInstance();

		// startScreen setup
		startScreen=ImageIO.read(MITrisPlugin.class.getResourceAsStream("/images/mario/start_screen.png"));
		startScreenResetWidth = startScreen.getWidth() - 9;
		startScreenStepsPerFrame = 2;
		startScreenScrollPosition = 0;

		// gameOverScreen setup
		gameOverScreen=ImageIO.read(MITrisPlugin.class.getResourceAsStream("/images/mario/game_over_screen.png"));
		gameOverScreenResetWidth = gameOverScreen.getWidth() - 9;
		gameOverScreenStepsPerFrame = 1;
		gameOverScreenScrollPosition = 0;
		minimumTimeAtGameOverScreen = 100; // currently not using this
		
		// completeScreen setup
		completeScreen=ImageIO.read(MITrisPlugin.class.getResourceAsStream("/images/mario/complete_screen.png"));
		completeScreenResetWidth = completeScreen.getWidth() - 9;
		completeScreenStepsPerFrame = 1;
		completeScreenScrollPosition = 0;
		minimumTimeAtCompleteScreen = 9; // starts the message scrolling
		
		// set up level maps
		levelMaps = new ArrayList<String>();
		levelMaps.add(Constants.ONE_ONE_MAP);
		levelMaps.add(Constants.ONE_TWO_MAP);	
	}

	private void init() {
		startScreenScrollPosition = 0;
		gameOverScreenScrollPosition = 0;
		completeScreenScrollPosition = 0;

		// map reset
		currentLevelIndex = 0;
		inBonusMode = false;
		lives = 3;
		try {
			level = new Level(levelMaps.get(currentLevelIndex),frameTime,inBonusMode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onStart()
	{
		controller.setListener(this);
		gameState=State.IDLE;
	}


	@Override
	protected void loop() {
		if(gameState != State.IDLE) {
			if (!controller.isConnected())
				return;
		}

		Display2D display=getDisplay();
		Graphics2D g=display.getGraphics();

		switch(gameState) {
		case IDLE:
			int startScreenDisplayPosition = -1 * ( startScreenScrollPosition / startScreenStepsPerFrame );
			if(startScreenDisplayPosition == (-1 * startScreenResetWidth)) {
				startScreenScrollPosition = 0;
			}else {
				startScreenScrollPosition++;
			}			
			g.drawImage(startScreen,startScreenDisplayPosition,0,null);
			break;
		case GAME:
			level.applyLevelColoring(display);
			switch(level.iterateActors(display)) {
			case KILLED:
				lives--;
				if(lives == 0) {
					gameState = State.GAME_OVER;
				}else {
					gameState = State.LOAD_LEVEL;
				}
				break;
			case COMPLETE:
				currentLevelIndex++;
				// finished the levels regular and bonus
				if(currentLevelIndex == levelMaps.size() && inBonusMode) {
					// done! say good job and reset
					gameState = State.COMPLETE;
				}else if(currentLevelIndex == levelMaps.size() && !inBonusMode) {
					currentLevelIndex = 0;
					inBonusMode = true;
					gameState = State.LOAD_LEVEL;
				}
				else {
					// load the next level and play!
					gameState = State.LOAD_LEVEL;
				}
				break;
			case ACTIVE:
			default:
				break;
			}			
			break;
		case LOAD_LEVEL:
			try {
				level = new Level(levelMaps.get(currentLevelIndex),frameTime,inBonusMode);
				gameState = State.GAME;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			break;
		case COMPLETE:
			int completeScreenDisplayPosition = -1 * ( completeScreenScrollPosition / completeScreenStepsPerFrame );
			if(completeScreenDisplayPosition == (-1 * completeScreenResetWidth)) {
				init();
				gameState = State.IDLE;				
			}else {
				completeScreenScrollPosition++;
			}			
			g.drawImage(completeScreen,completeScreenDisplayPosition,0,null);
			break;
		case GAME_OVER:
			int gameOverScreenDisplayPosition = -1 * ( gameOverScreenScrollPosition / gameOverScreenStepsPerFrame );
			if(gameOverScreenDisplayPosition == (-1 * gameOverScreenResetWidth)) {
				init();
				gameState = State.IDLE;				
			}else {
				gameOverScreenScrollPosition++;
			}			
			g.drawImage(gameOverScreen,gameOverScreenDisplayPosition,0,null);
			break;
		default: 
			break;
		}

	}

	@Override
	public void arcadeButton(byte b) {

		switch(gameState) {
		case IDLE:
			switch (b)
			{
			case 'L':
			case 'R':
			case 'U':
			case 'D':
				init();
				gameState=State.GAME;				
			}
			break;
		case COMPLETE:
			if(completeScreenScrollPosition > minimumTimeAtCompleteScreen) {
				switch (b)
				{
				case 'L':
				case 'R':
				case 'U':
				case 'D':
					gameState=State.IDLE;				
				}
			}
		case GAME_OVER:
			if(gameOverScreenScrollPosition > minimumTimeAtGameOverScreen) {
				switch (b)
				{
				case 'L':
				case 'R':
				case 'U':
				case 'D':
					gameState=State.IDLE;				
				}
			}
		case GAME:
			Mario mario = level.getMario();
			switch (b)
			{
			case 'L':
				mario.setMarioDirectionXBackward();
				break;
			case 'l':
				mario.setMarioDirectionXNone();
				break;
			case 'U':
				mario.startJump();
				break;
			case 'u':

				break;
			case 'D':
				mario.setBButtonPressed(true);
				break;
			case 'd': 
				mario.setBButtonPressed(false);
				break;
			case 'R':
				mario.setMarioDirectionXForward();
				break;
			case 'r':
				mario.setMarioDirectionXNone();
				break;
			}
		default:
			break;
		}
	}

}
