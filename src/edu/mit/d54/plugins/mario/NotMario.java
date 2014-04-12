package edu.mit.d54.plugins.mario;


public abstract class NotMario extends GravityActor {

	public NotMario(Level level, double frameTime, double startingX, double startingY, int height, boolean isEnemy) {
		super(level, frameTime, startingX, startingY, height, isEnemy);
	}
	
	public abstract void interactWithMario(Mario mario);
		
	/**
	 * Check if there is a collision with the otherParty, if so, only modify own behavior accordingly
	 */
	public abstract void checkCollisionAndReact(GravityActor otherParty);
	
	/**
	 * Let the not-mario decide when to start moving
	 * @param x
	 */
	public abstract void spawn(int x);
	
}
