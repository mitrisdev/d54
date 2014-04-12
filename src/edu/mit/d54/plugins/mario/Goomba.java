package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

public class Goomba extends NotMario {

	@Override
	public void spawn(int screenLeftX) {
		if(Util.onScreenX(screenLeftX, (int)x)) {
			isSpawned = true;
		}
	}

	@Override
	public void updatePosition() {
		if(isSpawned) {
			super.updatePosition();		
			// check to see if we're going to hit something in the level. if so, change direction
			if(level.isObstacle((int)(x+directionX*stepSize), (int)y)) {
				directionX = -directionX;
			}
			x += directionX*stepSize;			
		}
	}

	public Goomba(Level level, double frameTime, double startingX, double startingY) {
		super(level,frameTime, startingX+1-frameTime * Constants.GOOMBA_FRAMES_PER_MOVE, startingY, 1 /*height*/, true /*enemy*/);
		this.directionX = -1; // start moving left
		this.stepSize = frameTime * Constants.GOOMBA_FRAMES_PER_MOVE;
	}

	@Override
	public void renderSelf(Display2D display, int screenLeftX) {
		// Goomba's are a single dot
		if(Util.onScreenX(screenLeftX,(int)x)) {
			for(int heightY = 0; heightY < getHeight();heightY++) {
				if(level.isYonLevel((int)y-heightY)) {
					display.setPixelRGB(Util.convertXToDisplayX(screenLeftX,(int)x), (int)y-heightY, Constants.GOOMBA_RENDER_COLOR);
				}				
			}
		}
	}

	@Override
	public void checkCollisionAndReact(GravityActor otherParty) {
		if(isSpawned) {
			if(otherParty.isEnemy && this.inContact(otherParty)) {
				directionX = -directionX;
			}
		}
	}

	@Override
	public void interactWithMario(Mario mario) {
		if(isSpawned) {
			// if Mario is above a Goomba immediately and within 1, it dies. 
			if(this.inContactAboveComingDown(mario)) {
				this.setKilled();
				mario.incrementPoints(Constants.pointsLookupTable.get(Constants.GOOMBA_COLOR));
			}
			// if Mario is within 1 in any other direction, he dies
			else if(this.inContact(mario)) {
				mario.setKilled();
			}
		}
	}

}
