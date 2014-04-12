package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

/**
 * 
 * A GravityActor is an actor in the level who is affected by gravity and is always trying to fall down, if possible, at a gentle rate.
 *
 */
public abstract class GravityActor {

	// current location
	protected double x;
	protected double y;
	private int height;
	protected boolean isSpawned;
	protected double frameTime;
	
	protected double velocityY;
	protected int directionX;
	protected double stepSize;
	
	protected boolean isEnemy;
	protected boolean isKilled;
	
	private boolean inContactX(GravityActor otherParty) {
		return Math.abs(getX() - otherParty.getX()) < (1-stepSize);
	}
	
	private boolean inContactY(GravityActor otherParty) {
		return Math.abs(getY() - otherParty.getY()) < (1-stepSize);
	}
	
	/**
	 * Check if they are within one in X and Y
	 * @param otherParty
	 * @return
	 */
	public boolean inContact(GravityActor otherParty) {
		return inContactX(otherParty) && inContactY(otherParty);
	}
	
	/**
	 * is the otherParty in contact but above this and coming down
	 * @param otherParty
	 * @return
	 */
	public boolean inContactAboveComingDown(GravityActor otherParty) {
		// remember a greater Y is lower in the screen
		return inContact(otherParty) && getY() > otherParty.getY() && otherParty.velocityY > 0;
	}
	
	/**
	 * is the otherParty in contact but below this
	 * @param otherParty
	 * @return
	 */
	public boolean inContactBelow(GravityActor otherParty) {
		// remember a greater Y is lower in the screen
		return inContact(otherParty) && getY() < otherParty.getY();
	}
	
	// keep a reference to the level to interact with
	protected Level level;
	
	public boolean isEnemy() {
		return isEnemy;
	}
	
	public int getHeight() {
		return height;
	}

	public void setVelocityY(double velocityY) {
		this.velocityY = velocityY;
	}

	/**
	 * Render the actor if appropriate
	 * @param display
	 * @param xPos
	 */
	public abstract void renderSelf(Display2D display,int screenLeftX);

//	public GravityActor(Level level, double frameTime, double startingX, double startingY, boolean isEnemy) {
//		this.level = level;
//		this.frameTime = frameTime;
//		this.x = startingX;
//		this.y = startingY;
//		this.isSpawned = false;
//		this.velocityY = 0.0;
//	}

	public GravityActor(Level level, double frameTime, double startingX, double startingY, int height, boolean isEnemy) {
		this.level = level;
		this.frameTime = frameTime;
		this.x = startingX;
		this.y = startingY;
		this.height = height;
		this.isSpawned = false;
		this.isKilled = false;
		this.velocityY = 0.0;
	}

	/**
	 * Update position based on gravity
	 * Returns true if the position was changed.
	 */
	public void updatePosition() {
		if(isSpawned){	
			// see if falling is possible
			int direction = (int)(velocityY/(Math.abs(velocityY)));
			// drop velocityY until hitting the ground
			// direction
			double step = direction*stepSize*velocityY;
			
			// handle increments of step > 1
			for(int i=0;i<Math.floor(step);i++) {
				if(!level.isObstacle((int)x, (int)(y+direction*1))) {
					y+=direction*1;
				}else {
					break;
				}
			}
			
			// handle increment for step part < 1
			if(!level.isObstacle((int)x, (int)(y+direction*(step - Math.floor(step))))) {
				y+=direction*(step - Math.floor(step));
			}else {
				velocityY = 0;
			}
			
			// make y sane
			if(level.isObstacle((int)x, (int)(y+1))) {
				y = (int)y;
			}
			
			velocityY += Constants.GRAVITY*stepSize;
		}
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public void setKilled() {
		isKilled = true;
	}
	
	/**
	 * Returns true if the actor is dead from falling off the screen 
	 * @return
	 */
	public boolean isDead() {
		return isSpawned && (isKilled || level.isYBelowLevel((int)y) || !level.isXonLevel((int)x));
	}
}
