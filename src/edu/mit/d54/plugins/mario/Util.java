package edu.mit.d54.plugins.mario;

public class Util {

	/**
	 * Checks if the x point is on the visible screen
	 * @param screenLeftX
	 * @param x
	 * @return
	 */
	public static boolean onScreenX(int screenLeftX, int x) {
		return x >= screenLeftX && x < screenLeftX+Constants.DISPLAY_WIDTH;
	}
	
	/**
	 * Assumes that x is onScreenX. Translates the x value from the level to be placed on the display correctly
	 * @param screenLeftX
	 * @param x
	 * @return
	 */
	public static int convertXToDisplayX(int screenLeftX, int x) {
		return x - screenLeftX;
	}
}
