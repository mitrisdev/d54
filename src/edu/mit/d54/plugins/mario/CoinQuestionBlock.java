package edu.mit.d54.plugins.mario;

import edu.mit.d54.Display2D;

public class CoinQuestionBlock extends BlockActor{

	// once the block is hit, it changes color and becomes inert
	private boolean isHit;

	// for the flash that occurs with the coin bouncing out when a coin box is hit
	private boolean animateFlash;
    private int flashPositionIndex;
	
	public CoinQuestionBlock(double frameTime,int x, int y) {
		super(frameTime,x, y);
		isHit = false;
		flashPositionIndex = 0;
	}
	
	@Override
	public void renderSelf(Display2D display, int screenLeftX) {
		// coin question blocks are a single dot. Use their default rendering until hit
		if(isHit && Util.onScreenX(screenLeftX,(int)x)) {
			display.setPixelRGB(Util.convertXToDisplayX(screenLeftX,(int)x), (int)y, Constants.USED_COIN_QUESTION_BLOCK_COLOR);
		}		
		
		if(animateFlash) {
			if(Util.onScreenX(screenLeftX, (int)x)) {
				display.setPixelRGB(Util.convertXToDisplayX(screenLeftX,(int)x), ((int)y)-Constants.ACQUIRED_COIN_FLASH_MAX_HEIGHT + Math.abs(flashPositionIndex - (Constants.ACQUIRED_COIN_FLASH_MAX_HEIGHT - Constants.ACQUIRED_COIN_FLASH_START_HEIGHT)), Constants.ACQUIRED_COIN_FLASH_COLOR);
			}
			flashPositionIndex++;
			if(flashPositionIndex > (Constants.ACQUIRED_COIN_FLASH_MAX_HEIGHT - Constants.ACQUIRED_COIN_FLASH_START_HEIGHT) && Constants.ACQUIRED_COIN_FLASH_END_HEIGHT == Constants.ACQUIRED_COIN_FLASH_MAX_HEIGHT - Math.abs(flashPositionIndex - (Constants.ACQUIRED_COIN_FLASH_MAX_HEIGHT - Constants.ACQUIRED_COIN_FLASH_START_HEIGHT))) {
				animateFlash = false;
			}
		}
	}

	@Override
	public void interactWithMario(Mario mario) {
		if(!isHit && inContactBelow(mario)) {
			isHit = true;
			animateFlash = true;
			mario.incrementPoints(Constants.pointsLookupTable.get(Constants.COIN_QUESTION_BLOCK_COLOR));
		}				
	}

}
