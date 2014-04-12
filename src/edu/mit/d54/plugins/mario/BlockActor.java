package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

public abstract class BlockActor {

	// location
	protected int x;
	protected int y;
	protected double marioStepSize;
	protected boolean shouldRemove;
	
	public BlockActor(double frameTime, int x, int y) {
		this.x = x;
		this.y = y;
		this.marioStepSize = frameTime * Constants.MARIO_FRAMES_PER_MOVE;
		shouldRemove = false;
	}
	
	public BlockActor(int x, int y) {
		this.x = x;
		this.y = y;
		shouldRemove = false;
	}
	
	/**
	 * Render the actor if appropriate
	 * @param display
	 * @param xPos
	 */
	public abstract void renderSelf(Display2D display,int screenLeftX);
	
	public abstract void interactWithMario(Mario mario);

	/**
	 * is the otherParty in contact but below this
	 * @param otherParty
	 * @return
	 */
	public boolean inContactBelow(GravityActor otherParty) {
		// remember a greater Y is lower in the screen
		return inContact(otherParty) && getY() < otherParty.getY();
	}
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	private boolean inContactX(GravityActor otherParty) {
		return getX() <= otherParty.getX() && otherParty.getX() <= (getX() + 1);
	}
	
	private boolean inContactY(GravityActor otherParty) {
		return Math.abs(getY() - otherParty.getY()) < (1+marioStepSize);
	}
	
	/**
	 * Check if they are within one in X and Y
	 * @param otherParty
	 * @return
	 */
	public boolean inContact(GravityActor otherParty) {
		return inContactX(otherParty) && inContactY(otherParty);
	}
	
	public boolean shouldRemove() {
		return shouldRemove;
	}
}
