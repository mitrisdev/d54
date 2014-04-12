package edu.mit.d54.plugins.mario;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import edu.mit.d54.Display2D;

public class Level {

	private int[][] levelArt;
	private boolean[][] levelObstacles; 
	private boolean[][] levelWinLocations;
	private int levelHeight;
	private int levelWidth;
	
	private static final int DEFAULT_COLOR = 0x0;

	private List<NotMario> actors;
	private List<NotMario> deadActors;
	
	private List<BlockActor> blocks;
	private List<BlockActor> removedBlocks;
	
	private Mario mario;
	
	private boolean inBonusMode;
	
	public Mario getMario() {
		return mario;
	}
	
	/**
	 * Three pixels behind Mario
	 * @return
	 */
	public int getScreenLeftX() {
		return (int)getMario().getX() - Constants.POSITION_FROM_LEFT;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			double y = 1.7;
			System.out.println(y);
			System.out.println((int)y);
			
			Level level = new Level("Mario-1-1-map.png",1.2,false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Runs one iteration of the level actors, including mario. Returns true if the game should continue (ie Mario is still alive)
	 * @param display
	 * @return
	 */
	public LevelStatus iterateActors(Display2D display) {
				
		// iterate Mario
		mario.updatePosition();
		mario.renderSelf(display, getScreenLeftX());
		
		// iterate the blocks
		for(BlockActor block: blocks) {
			block.interactWithMario(mario);
			if(block.shouldRemove()) {
				removedBlocks.add(block);
			}else {
				block.renderSelf(display,getScreenLeftX());	
			}	

		}
		blocks.removeAll(removedBlocks);
		removedBlocks.clear();
		
		// iterate the actors
		for(NotMario actor: actors) {
			actor.spawn(getScreenLeftX());
			actor.updatePosition();
			actor.renderSelf(display, getScreenLeftX());
			actor.interactWithMario(mario);
			
			if(actor.isDead()) {
				deadActors.add(actor);
			}else {
				// check for collisions
				for(GravityActor otherParty: actors) {
					// skip self
					if(otherParty != actor) {
						actor.checkCollisionAndReact(otherParty);	
					}
				}
			}
		}
		actors.removeAll(deadActors);
		deadActors.clear();	
	
		// if mario dies, it's over
		if(mario.isDead())
		{
			return LevelStatus.KILLED;
		}else if(mario.hasCompletedLevel()) {
			return LevelStatus.COMPLETE;
		}else {
			return LevelStatus.ACTIVE;
		}
	}
	
	public Level(String levelDataFile, double frameTime, boolean inBonusMode) throws IOException {
		actors = new ArrayList<NotMario>();
		deadActors = new ArrayList<NotMario>();
		blocks = new ArrayList<BlockActor>();
		removedBlocks = new ArrayList<BlockActor>();
		// set up Mario
		mario = new Mario(this,frameTime,Constants.POSITION_FROM_LEFT,Constants.GROUND_Y);
			
		// assume levelArt is "pathToLevelData.gif"
		BufferedImage levelArtImage = ImageIO.read(Level.class.getResourceAsStream(levelDataFile));
		
		levelArt = new int[levelArtImage.getWidth()][levelArtImage.getHeight()];
		levelObstacles = new boolean[levelArtImage.getWidth()][levelArtImage.getHeight()];
		levelWinLocations = new boolean[levelArtImage.getWidth()][levelArtImage.getHeight()];

		Map<Integer,Integer> levelRenderMapping = Constants.levelFileToRenderMappingMap.get(levelDataFile);
		for(int i = 0;i<levelArtImage.getWidth();i++) {
			for(int j=0;j<levelArtImage.getHeight();j++) {
				int currentPixel = levelArtImage.getRGB(i,j) & 0xFFFFFF;
				levelObstacles[i][j] = Constants.palletteMemberToObstacleMap.get(currentPixel);
				levelArt[i][j] = levelRenderMapping.get(currentPixel);
				levelWinLocations[i][j] = Constants.palletteMemberToWinLocationMap.get(currentPixel);

				// handle the different level objects and actors encoded in the map file
				switch(currentPixel) {
				case Constants.GOOMBA_COLOR:
					actors.add(new Goomba(this,frameTime,i,j));
					break;	
				case Constants.COIN_QUESTION_BLOCK_COLOR:
					blocks.add(new CoinQuestionBlock(frameTime,i,j));
					break;
				case Constants.COIN_COLOR:
					blocks.add(new Coin(i,j));
					break;
				}			
				//				levelArt[i][j][0] = levelArtImage.getRGB(i,j) & 0xFF;
				//				levelArt[i][j][1] = ((levelArtImage.getRGB(i,j)& 0xFF00) >> 8) & 0xFF;
				//				levelArt[i][j][3] = ((levelArtImage.getRGB(i,j)& 0xFF0000) >> 16) & 0xFF;
				
			}
		}

		levelHeight = levelArtImage.getHeight();
		levelWidth = levelArtImage.getWidth();
		
		this.inBonusMode = inBonusMode;
	}

	public boolean isObstacle(int x, int y) {
		// return false if off screen
		if(x >= 0 && x < levelWidth && y >= 0 && y < levelHeight) {
			return levelObstacles[x][y];	
		}
		return false;
	}
	
	public boolean isWinLocation(int x, int y) {
		// return false if off screen
		if(x >= 0 && x < levelWidth && y >= 0 && y < levelHeight) {
			return levelWinLocations[x][y];	
		}
		return false;
	}

	/**
	 * Sets the colors to match the level art. Others may then overwrite settings. Render from xPos to displayWidth. If this is not possible, make the rest DEFAULT.
	 * @param g
	 */
	public void applyLevelColoring(Display2D display) {
			
		int screenLeftX = getScreenLeftX();
		int displayIndex = 0 ;
		for(int i=screenLeftX;i<screenLeftX+Constants.DISPLAY_WIDTH;i++) {
			if(isXonLevel(i)){
				for(int j=0;j<Constants.DISPLAY_HEIGHT;j++) {
					display.setPixelRGB(displayIndex, j, levelArt[i][j]);
				}
			}
			else {
				for(int j=0;j<levelHeight;j++) {
					display.setPixelRGB(displayIndex, j, DEFAULT_COLOR);
				}
			}
			displayIndex ++;
		}
		
		// update the levelArt if inBonusMode
		if(inBonusMode) {
			for(int i=0;i<this.levelWidth;i++) {
				for(int j=0;j<this.levelHeight;j++) {
					int currentColor = levelArt[i][j];
					int red = (currentColor & 0xFF0000) >> 16;
				int green = (currentColor & 0xFF00) >> 8 ;
				int blue = (currentColor & 0xFF) ;

				float[] result = Color.RGBtoHSB(red, green, blue, null);
				result[0] = result[0] + (float).015;
				if(result[0] > 1){
					result[0] = 0;
				}
				currentColor = Color.HSBtoRGB(result[0], result[1], result[2]);
				levelArt[i][j] = currentColor;
				}
			}
		}
	}

	/**
	 * Returns true if the requested x point is on the level
	 * @param x
	 * @return
	 */
	public boolean isXonLevel(int x) {
		return x >= 0 && x < levelWidth;
	}

	/**
	 * Returns true if the requested y point is on the level
	 * @param y
	 * @return
	 */
	public boolean isYonLevel(int y) {
		return y >= 0 && y < levelHeight;
	}
	
	public boolean isYBelowLevel(int y) {
		return y >= levelHeight;
	}
}
