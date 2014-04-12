package edu.mit.d54.plugins.mario;

import java.util.HashMap;
import java.util.Map;

public class Constants {

	// Level Maps
	public static final String ONE_ONE_MAP = "/images/mario/Mario-1-1-map.png";
	public static final String ONE_TWO_MAP = "/images/mario/Mario-1-2-map.png";
	
	// Display 
	public static final int DISPLAY_WIDTH = 9;
	public static final int DISPLAY_HEIGHT = 17;
	public static final int GROUND_Y = DISPLAY_HEIGHT - 1 - 2; // adjust for 0-index and ground height

	// Gravity
	public static final double GRAVITY = 1.1;
	
	/**
	 * ******************************************************
	 *  Color for Dynamic Elements/Actors
	 * ******************************************************
	 */
	// Colors	
	public static final int SMALL_MARIO_COLOR = 0xFF0000;
	public static final int GOOMBA_RENDER_COLOR = 0xFFFF00;
	public static final int USED_COIN_QUESTION_BLOCK_COLOR = 0x555500; // set this!
	public static final int ACQUIRED_COIN_FLASH_COLOR= 0xFFFFFF; // set this!
	public static final int COIN_RENDER_COLOR = 0xFF4400;
	/**
	 * ******************************************************
	 *  End of Color for Dynamic Elements/Actors
	 * ******************************************************
	 */
	
	// Coins
	public static final int ACQUIRED_COIN_FLASH_START_HEIGHT = 1;
	public static final int ACQUIRED_COIN_FLASH_MAX_HEIGHT = 4;
	public static final int ACQUIRED_COIN_FLASH_END_HEIGHT = 2;
	
	// Movements
	public static final int GOOMBA_FRAMES_PER_MOVE = 4;
	public static final int MARIO_FRAMES_PER_MOVE = 5;
	
	// Mario
	public static final int POSITION_FROM_LEFT = 2;
	public static final double MARIO_B_BUTTON_ACCELERATION_RATIO = 2;
	public static final double MARIO_JUMP_VELOCITY = -2.7;
	public static final double MARIO_B_BUTTOM_JUMP_VELOCITY = -3;
	
	// Level Loading Pallette
	public static final int GROUND_AND_BRICK_COLOR = 0xFF0000;
	public static final int COIN_COLOR = 0xFFFF00;
	public static final int COIN_QUESTION_BLOCK_COLOR = 0xAAAA00;
	public static final int GOOMBA_COLOR = 0xFF6600;
	public static final int TURTLE_COLOR = 0x66BB00;
	public static final int PIPE_COLOR = 0x006600;
	public static final int PIRANHA_PLANT_COLOR = 0x009900;
	public static final int CLOUD_COLOR = 0xFFFFFF;
	public static final int BACKGROUND_GREENERY_COLOR = 0x00FF00;
	public static final int OTHER_SOLID_SURFACE_COLOR = 0xAA00AA;
	public static final int FLAG_POLE_COLOR = 0x000000;
	public static final int RED_PLATFORM_COLOR = 0xAA0000;
	public static final int STAR_COLOR = 0x777777;
	public static final int EXTRA_LIFE_COLOR = 0xBBBBBB;
	public static final int SKY_COLOR = 0x0000FF;
	public static final int MUSHROOM_COLOR = 0xFF00FF;
	
	
	/**
	 * ******************************************************
	 *  Level Display Pallettes
	 *  Change Values HERE to affect displayed colors for static things
	 *  Above there is an area to change dynamic elements' colors
	 * ******************************************************
	 */
	// Level 1-1 Display Pallette
	//	public static final int ONE_ONE_GROUND_AND_BRICK_COLOR = 0xFF0000;
	//	public static final int ONE_ONE_COIN_COLOR = 0;
//	public static final int ONE_ONE_COIN_QUESTION_BLOCK_COLOR = 0xAAAA00;
//	public static final int ONE_ONE_GOOMBA_COLOR = 0xFF6600;
//	public static final int ONE_ONE_TURTLE_COLOR = 0x66BB00;
//	public static final int ONE_ONE_PIPE_COLOR = 0x006600;
//	public static final int ONE_ONE_PIRANHA_PLANT_COLOR = 0;
//	public static final int ONE_ONE_CLOUD_COLOR = 0xFFFFFF;
//	public static final int ONE_ONE_BACKGROUND_GREENERY_COLOR = 0x00FF00;
//	public static final int ONE_ONE_OTHER_SOLID_SURFACE_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
//	public static final int ONE_ONE_FLAG_POLE_COLOR = 0x000000;
//	public static final int ONE_ONE_RED_PLATFORM_COLOR = 0;
//	public static final int ONE_ONE_STAR_COLOR = 0x777777;
//	public static final int ONE_ONE_EXTRA_LIFE_COLOR = 0xBBBBBB;
//	public static final int ONE_ONE_SKY_COLOR = 0x5C94FC;
//	public static final int ONE_ONE_MUSHROOM_COLOR = 0xFF00FF;
	// Level 1-1 Display Pallette Simplified
	public static final int ONE_ONE_GROUND_AND_BRICK_COLOR = 0xd54b00;
	public static final int ONE_ONE_COIN_COLOR = 0xFFFF00;
	public static final int ONE_ONE_COIN_QUESTION_BLOCK_COLOR = 0xAAAA00;
	public static final int ONE_ONE_GOOMBA_COLOR = 0xFFFF00;
	public static final int ONE_ONE_TURTLE_COLOR = 0x66BB00;
	public static final int ONE_ONE_PIPE_COLOR = 0x00FF00;
	public static final int ONE_ONE_PIRANHA_PLANT_COLOR = 0;
	public static final int ONE_ONE_CLOUD_COLOR = 0xFFFFFF;
	public static final int ONE_ONE_BACKGROUND_GREENERY_COLOR = 0x123808;
	public static final int ONE_ONE_OTHER_SOLID_SURFACE_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	public static final int ONE_ONE_FLAG_POLE_COLOR = 0xd5ed3b;
	public static final int ONE_ONE_RED_PLATFORM_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	public static final int ONE_ONE_STAR_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	public static final int ONE_ONE_SKY_COLOR = 0x0000FF;
	public static final int ONE_ONE_EXTRA_LIFE_COLOR = ONE_ONE_SKY_COLOR;
	public static final int ONE_ONE_MUSHROOM_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	// Level 1-2 Display Pallette Simplified
	public static final int ONE_TWO_GROUND_AND_BRICK_COLOR = 0x12AA08;
	public static final int ONE_TWO_COIN_COLOR = 0xFFFF00;
	public static final int ONE_TWO_COIN_QUESTION_BLOCK_COLOR = 0xAAAA00;
	public static final int ONE_TWO_GOOMBA_COLOR = 0xFFFF00;
	public static final int ONE_TWO_TURTLE_COLOR = 0x66BB00;
	public static final int ONE_TWO_PIPE_COLOR = 0x00FF00;
	public static final int ONE_TWO_PIRANHA_PLANT_COLOR = 0;
	public static final int ONE_TWO_CLOUD_COLOR = 0xFFFFFF;
	public static final int ONE_TWO_OTHER_SOLID_SURFACE_COLOR = ONE_TWO_GROUND_AND_BRICK_COLOR;
	public static final int ONE_TWO_FLAG_POLE_COLOR = 0xd5ed3b;
	public static final int ONE_TWO_RED_PLATFORM_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	public static final int ONE_TWO_STAR_COLOR = ONE_TWO_GROUND_AND_BRICK_COLOR;
	public static final int ONE_TWO_SKY_COLOR = 0x0;
	public static final int ONE_TWO_EXTRA_LIFE_COLOR = ONE_TWO_SKY_COLOR;
	public static final int ONE_TWO_MUSHROOM_COLOR = ONE_ONE_GROUND_AND_BRICK_COLOR;
	public static final int ONE_TWO_BACKGROUND_GREENERY_COLOR = ONE_TWO_SKY_COLOR;
	/**
	 * ******************************************************
	 *  End of Level Display Pallettes
	 * ******************************************************
	 */
	
	// obstacles from input map
	public static Map<Integer,Boolean> palletteMemberToObstacleMap;
	static {
		palletteMemberToObstacleMap = new HashMap<Integer,Boolean>();
		palletteMemberToObstacleMap.put(GROUND_AND_BRICK_COLOR, true);
		palletteMemberToObstacleMap.put(COIN_COLOR, false);
		palletteMemberToObstacleMap.put(COIN_QUESTION_BLOCK_COLOR, true);
		palletteMemberToObstacleMap.put(GOOMBA_COLOR, false);
		palletteMemberToObstacleMap.put(TURTLE_COLOR, false);
		palletteMemberToObstacleMap.put(PIPE_COLOR, true);
		palletteMemberToObstacleMap.put(PIRANHA_PLANT_COLOR, false);
		palletteMemberToObstacleMap.put(CLOUD_COLOR, false);
		palletteMemberToObstacleMap.put(BACKGROUND_GREENERY_COLOR, false);
		palletteMemberToObstacleMap.put(OTHER_SOLID_SURFACE_COLOR, true);
		palletteMemberToObstacleMap.put(FLAG_POLE_COLOR, false);
		palletteMemberToObstacleMap.put(RED_PLATFORM_COLOR, true);
		palletteMemberToObstacleMap.put(STAR_COLOR, true);
		palletteMemberToObstacleMap.put(EXTRA_LIFE_COLOR, false);
		palletteMemberToObstacleMap.put(SKY_COLOR, false);
		palletteMemberToObstacleMap.put(MUSHROOM_COLOR, true);
	}
	
	// win locations from input map
	public static Map<Integer,Boolean> palletteMemberToWinLocationMap;
	static {
		palletteMemberToWinLocationMap = new HashMap<Integer,Boolean>();
		palletteMemberToWinLocationMap.put(GROUND_AND_BRICK_COLOR, false);
		palletteMemberToWinLocationMap.put(COIN_COLOR, false);
		palletteMemberToWinLocationMap.put(COIN_QUESTION_BLOCK_COLOR, false);
		palletteMemberToWinLocationMap.put(GOOMBA_COLOR, false);
		palletteMemberToWinLocationMap.put(TURTLE_COLOR, false);
		palletteMemberToWinLocationMap.put(PIPE_COLOR, false);
		palletteMemberToWinLocationMap.put(PIRANHA_PLANT_COLOR, false);
		palletteMemberToWinLocationMap.put(CLOUD_COLOR, false);
		palletteMemberToWinLocationMap.put(BACKGROUND_GREENERY_COLOR, false);
		palletteMemberToWinLocationMap.put(OTHER_SOLID_SURFACE_COLOR, false);
		palletteMemberToWinLocationMap.put(FLAG_POLE_COLOR, true);
		palletteMemberToWinLocationMap.put(RED_PLATFORM_COLOR,false);
		palletteMemberToWinLocationMap.put(STAR_COLOR, false);
		palletteMemberToWinLocationMap.put(EXTRA_LIFE_COLOR, false);
		palletteMemberToWinLocationMap.put(SKY_COLOR, false);
		palletteMemberToWinLocationMap.put(MUSHROOM_COLOR, false);
	}

	// coloring for level 1-1
	public static Map<Integer,Integer> levelOneOneRenderMapping;
	static {
		levelOneOneRenderMapping = new HashMap<Integer,Integer>();
		levelOneOneRenderMapping.put(GROUND_AND_BRICK_COLOR, ONE_ONE_GROUND_AND_BRICK_COLOR);
		levelOneOneRenderMapping.put(COIN_COLOR, ONE_ONE_SKY_COLOR);
		levelOneOneRenderMapping.put(COIN_QUESTION_BLOCK_COLOR, ONE_ONE_COIN_QUESTION_BLOCK_COLOR);
		levelOneOneRenderMapping.put(GOOMBA_COLOR, ONE_ONE_SKY_COLOR);
		levelOneOneRenderMapping.put(TURTLE_COLOR, ONE_ONE_SKY_COLOR);
		levelOneOneRenderMapping.put(PIPE_COLOR, ONE_ONE_PIPE_COLOR);
		levelOneOneRenderMapping.put(PIRANHA_PLANT_COLOR, ONE_ONE_SKY_COLOR);
		levelOneOneRenderMapping.put(CLOUD_COLOR, ONE_ONE_CLOUD_COLOR);
		levelOneOneRenderMapping.put(BACKGROUND_GREENERY_COLOR, ONE_ONE_BACKGROUND_GREENERY_COLOR);
		levelOneOneRenderMapping.put(OTHER_SOLID_SURFACE_COLOR, ONE_ONE_OTHER_SOLID_SURFACE_COLOR);
		levelOneOneRenderMapping.put(FLAG_POLE_COLOR, ONE_ONE_FLAG_POLE_COLOR);
		levelOneOneRenderMapping.put(RED_PLATFORM_COLOR, ONE_ONE_RED_PLATFORM_COLOR);
		levelOneOneRenderMapping.put(STAR_COLOR, ONE_ONE_STAR_COLOR);
		levelOneOneRenderMapping.put(EXTRA_LIFE_COLOR, ONE_ONE_EXTRA_LIFE_COLOR);
		levelOneOneRenderMapping.put(SKY_COLOR, ONE_ONE_SKY_COLOR);
		levelOneOneRenderMapping.put(MUSHROOM_COLOR, ONE_ONE_MUSHROOM_COLOR);
	}

	// coloring for level 1-2
	public static Map<Integer,Integer> levelOneTwoRenderMapping;
	static {
		levelOneTwoRenderMapping = new HashMap<Integer,Integer>();
		levelOneTwoRenderMapping.put(GROUND_AND_BRICK_COLOR, ONE_TWO_GROUND_AND_BRICK_COLOR);
		levelOneTwoRenderMapping.put(COIN_COLOR, ONE_TWO_SKY_COLOR);
		levelOneTwoRenderMapping.put(COIN_QUESTION_BLOCK_COLOR, ONE_TWO_COIN_QUESTION_BLOCK_COLOR);
		levelOneTwoRenderMapping.put(GOOMBA_COLOR, ONE_TWO_SKY_COLOR);
		levelOneTwoRenderMapping.put(TURTLE_COLOR, ONE_TWO_SKY_COLOR);
		levelOneTwoRenderMapping.put(PIPE_COLOR, ONE_TWO_PIPE_COLOR);
		levelOneTwoRenderMapping.put(PIRANHA_PLANT_COLOR, ONE_TWO_SKY_COLOR);
		levelOneTwoRenderMapping.put(CLOUD_COLOR, ONE_TWO_CLOUD_COLOR);
		levelOneTwoRenderMapping.put(BACKGROUND_GREENERY_COLOR, ONE_TWO_BACKGROUND_GREENERY_COLOR);
		levelOneTwoRenderMapping.put(OTHER_SOLID_SURFACE_COLOR, ONE_TWO_OTHER_SOLID_SURFACE_COLOR);
		levelOneTwoRenderMapping.put(FLAG_POLE_COLOR, ONE_TWO_FLAG_POLE_COLOR);
		levelOneTwoRenderMapping.put(RED_PLATFORM_COLOR, ONE_TWO_RED_PLATFORM_COLOR);
		levelOneTwoRenderMapping.put(STAR_COLOR, ONE_TWO_STAR_COLOR);
		levelOneTwoRenderMapping.put(EXTRA_LIFE_COLOR, ONE_TWO_EXTRA_LIFE_COLOR);
		levelOneTwoRenderMapping.put(SKY_COLOR, ONE_TWO_SKY_COLOR);
		levelOneTwoRenderMapping.put(MUSHROOM_COLOR, ONE_TWO_MUSHROOM_COLOR);
	}
	
	public static Map<String,Map<Integer,Integer>> levelFileToRenderMappingMap;
	static {
		levelFileToRenderMappingMap = new HashMap<String,Map<Integer,Integer>>();
		levelFileToRenderMappingMap.put(ONE_ONE_MAP,levelOneOneRenderMapping);
		levelFileToRenderMappingMap.put(ONE_TWO_MAP,levelOneTwoRenderMapping);
	}
	
	// points table
	public static Map<Integer,Integer> pointsLookupTable;
    static {
		pointsLookupTable = new HashMap<Integer,Integer>();
		pointsLookupTable.put(COIN_COLOR, 200);
		pointsLookupTable.put(COIN_QUESTION_BLOCK_COLOR, 200);
		pointsLookupTable.put(GOOMBA_COLOR, 100);
	}
}
