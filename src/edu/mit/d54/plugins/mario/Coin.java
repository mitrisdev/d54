package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

public class Coin extends BlockActor {

	public Coin(int x, int y) {
		super(x, y);
		
	}

	@Override
	public void renderSelf(Display2D display, int screenLeftX) {
		// coins are a single dot. Use their default rendering until hit
		if(Util.onScreenX(screenLeftX,(int)x)) {
			display.setPixelRGB(Util.convertXToDisplayX(screenLeftX,(int)x), (int)y, Constants.COIN_RENDER_COLOR);
		}
	}

	@Override
	public void interactWithMario(Mario mario) {
		if(inContact(mario)) {
			// once Mario gets the coin, it can disappear
			mario.incrementPoints(Constants.pointsLookupTable.get(Constants.COIN_COLOR));
			shouldRemove = true;
		}
	}

	private boolean inContactX(GravityActor otherParty) {
		return getX() < otherParty.getX() && otherParty.getX() < (getX() + 1);
	}
	
	private boolean inContactY(GravityActor otherParty) {
		return getY() < otherParty.getY() && otherParty.getY() < (getY() + 1);
	}
	
	/**
	 * Check if they are within one in X and Y
	 * @param otherParty
	 * @return
	 */
	public boolean inContact(GravityActor otherParty) {
		return inContactX(otherParty) && inContactY(otherParty);
	}
}
