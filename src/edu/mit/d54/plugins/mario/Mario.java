package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

public class Mario extends GravityActor {

	private enum MarioState {SMALL,BIG,FIRE_FLOWER};
		
	// Mario's height varies depending on his state
	@Override
	public int getHeight() {
		switch(marioState) {
		case SMALL:
			return 1;
		case BIG:
			return 2;
		case FIRE_FLOWER:
			return 2;
		default:
			return 1;
		}
	}

	private MarioState marioState;
	private boolean hasCompletedLevel;
	private int points; 

	private boolean bButtonPressed;

	public void setBButtonPressed(boolean isPressed) {
		bButtonPressed = isPressed;
	}

	/**
	 * If mario doesn't have ground under him, this does nothing
	 */
	public void startJump() {
		if(level.isObstacle((int)x,(int)y+1)) {
			if(bButtonPressed) {
				setVelocityY(Constants.MARIO_B_BUTTOM_JUMP_VELOCITY);
			}else {
				setVelocityY(Constants.MARIO_JUMP_VELOCITY);
			}
		}
	}

	public void setMarioDirectionXForward() {
		directionX = 1;
	}

	public void setMarioDirectionXBackward() {
		directionX = -1;
	}

	public void setMarioDirectionXNone() {
		directionX = 0;
	}

	public boolean hasCompletedLevel() {
		return hasCompletedLevel;
	}
	
	public Mario(Level level, double frameTime, int startingX, int startingY) {
		super(level, frameTime, startingX, startingY,1 /*height*/,false /*isEnemy*/);
		this.isSpawned = true;	
		bButtonPressed = false;
		hasCompletedLevel = false;
		this.stepSize = frameTime*(Constants.MARIO_FRAMES_PER_MOVE);
		marioState = MarioState.SMALL;
		points = 0;
	}

	public void incrementPoints(int newPoints) {
		points += newPoints;
	}
	
	@Override
	public void updatePosition() {
		super.updatePosition();

		// set the nextMoveTime
		double bButtonAdjustment = bButtonPressed ? Constants.MARIO_B_BUTTON_ACCELERATION_RATIO : 1.0;

		// check to see if we're going to hit something in the level. if so, don't move forward
		if(!level.isObstacle((int)x+directionX, (int)y)) {
			x += directionX*stepSize*bButtonAdjustment;
		}	
		
		if(level.isWinLocation((int)x+directionX, (int)y)){
			hasCompletedLevel = true; 
		}
	}
	
	//	private int currentColor = Constants.SMALL_MARIO_COLOR;
	
	@Override
	public void renderSelf(Display2D display, int screenLeftX) {
		// Mario are a single dot when small
		if(Util.onScreenX(screenLeftX,(int)x)) { 
			for(int heightY = 0; heightY < getHeight();heightY++) {
				if(level.isYonLevel((int)y-heightY)) {
		
			
					display.setPixelRGB(Util.convertXToDisplayX(screenLeftX,(int)x), (int)y-heightY, Constants.SMALL_MARIO_COLOR);

				}				
			}
		}
	}

}
