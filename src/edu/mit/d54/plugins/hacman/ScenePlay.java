package edu.mit.d54.plugins.hacman;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;

import edu.mit.d54.Display2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;


public class ScenePlay extends Object {

	private static final int kSpawnWallColor = 0x000080;
	private static final int kSpawnPlayerColor = 0xFFFF00;
	private static final int kSpawnPelletColor = 0x333333;
	private static final int kSpawnPelletPowerColor = 0xFFFFFF;
	private static final int kSpawnGhostInkyColor = 0x00FFFF;
	private static final int kSpawnGhostPinkyColor = 0xFF0080;
	private static final int kSpawnGhostBlinkyColor = 0xFF0000;
	private static final int kSpawnGhostClydeColor = 0xFF8000;

	private static final float wooblinessPeriod = 7.5f;
	private float wooblinessTimer;

	private static final float pelletPulsePeriod = 4.0f;
	private float pelletPulseTimer;

	private static final float playerPulsePeriod = 0.25f;
	private float playerPulseTimer;

	private static final float ghostPulsePeriod = 1.0f;
	private float ghostPulseTimer;

	private Display2D d;

	public enum State {Playing, Won, Lost, Winning, Losing,};
	public State state;

	private static final float winPeriod = 2.0f;
	private float winTimer;

	private static final float losePeriod = 2.0f;
	private float loseTimer;

	private static final float kPlayerSpeed = 4.0f;
	private static final float kGhostSpeed = 2.0f;

	private Player player;

	private ArrayList<Transform> physicsTransfoms;
	private long timeSinceLastUpdate;

	private HashMap<Transform,ArrayList<Transform>> collisionMap;

	private ArrayList<Transform> walls;
	private ArrayList<Pellet> pellets;
	private ArrayList<Ghost> ghosts;

	private boolean[][] unoccupied;

	private boolean hasPlayerMoved;

	private int boardWidth;
	private int boardHeight;

	private int heightOffset;

	public ScenePlay(Display2D display, String levelName, int _heightOffset) throws IOException {

		d = display;
		heightOffset = _heightOffset;

		// we need to set up our arrays before we populate them
		walls = new ArrayList<Transform>();
		pellets = new ArrayList<Pellet>();
		ghosts = new ArrayList<Ghost>();

		// we set up our characters before reading the map; we're going to position them based off the map though
		//
		// create MS HAC MAN
		player = new Player();

		// read in the current level
		BufferedImage levelImg = ImageIO.read(ScenePlay.class.getResourceAsStream(levelName));

		// the ghosts will want to look at the empty tiles in order to figure out movement direction
		unoccupied = new boolean[levelImg.getWidth()][levelImg.getHeight()];

		// we'll also use the level dimensions in the case of a mini display
		boardWidth = levelImg.getWidth();
		boardHeight = levelImg.getHeight();

		// we generate walls, ghosts, pellets (regular and power), and the player from the map
		// we do this by color

		for (int iX = 0; iX < boardWidth; ++iX) {
			for (int iY = 0; iY < boardHeight; ++iY) {

				// we'll just mark off what's occupied
				boolean isOccupied = false;

				int pixelCol = levelImg.getRGB(iX,iY) & 0xFFFFFF;

				// WALLS!
				if (pixelCol == kSpawnWallColor) {
					Transform wall = new Transform();
					wall.x = iX;
					wall.y = iY;
					walls.add(wall);

					isOccupied = true;
				}

				// MS. HAC MAN
				else if (pixelCol == kSpawnPlayerColor) {
					player.transform.setStartPosition(iX,iY);
				}

				// PELLETS
				//
				// regular
				else if (pixelCol == kSpawnPelletColor) {
					Pellet pellet = new Pellet();
					pellet.transform.x = iX;
					pellet.transform.y = iY;
					pellets.add(pellet);
				}
				// powerrr!!!
				else if (pixelCol == kSpawnPelletPowerColor) {
					Pellet pellet = new Pellet();
					pellet.isPower = true;
					pellet.transform.x = iX;
					pellet.transform.y = iY;
					pellets.add(pellet);
				}

				// G-G-Gh-Ghosts!
				//
				// INKY, PINKY, BLINKY, & CLYDE
				else if (pixelCol == kSpawnGhostInkyColor ||
					pixelCol == kSpawnGhostPinkyColor ||
					pixelCol == kSpawnGhostBlinkyColor ||
					pixelCol == kSpawnGhostClydeColor) {

					Ghost ghost = new Ghost();

					// spooooooky fancy math to get the right hue for the rgb value
					float r = (float)((pixelCol >> 16) & 0xFF) / 255.0f;
					float g = (float)((pixelCol >> 8) & 0xFF) / 255.0f;
					float b = (float)((pixelCol >> 0) & 0xFF) / 255.0f;

					float max = Math.max(r,Math.max(g,b));
					float min = Math.min(r,Math.min(g,b));

					float hue = 0.0f;
					if (max == r) {hue = (g - b)/(max - min);}
					else if (max == g) {hue = 2.0f + (b - r)/(max - min);}
					else if (max == b) {hue = 4.0f + (r - g)/(max - min);}

					hue /= 6.0f;
					if (hue < 0.0f) {hue += 1.0f;}

					ghost.hue = hue;

					ghost.transform.setStartPosition(iX,iY);
					ghosts.add(ghost);

				}

				unoccupied[iX][iY] = !isOccupied;

			}
		}

		// COLLISIONS!
		//
		// our collision system requires only moving objects to map to things with which they can collide
		collisionMap = new HashMap<Transform,ArrayList<Transform>>();
		//
		// collide player with walls
		ArrayList<Transform> playerColliders = new ArrayList<Transform>();
		for (Transform transform : walls) {
			playerColliders.add(transform);
		}
		collisionMap.put(player.transform,playerColliders);
		// collide ghosts with walls
		ArrayList<Transform> ghostColliders = new ArrayList<Transform>();
		for (Transform transform : walls) {
			ghostColliders.add(transform);
		}
		for (Ghost ghost : ghosts) {
			collisionMap.put(ghost.transform,ghostColliders);
		}

		// all moving objects should be added to the physics list, so they can be moved
		physicsTransfoms = new ArrayList<Transform>();
		physicsTransfoms.add(player.transform);
		for (Ghost ghost : ghosts) {
			physicsTransfoms.add(ghost.transform);
		}

		// we need to set up our initial time here, so that the first update doesn't get sad
		timeSinceLastUpdate = System.nanoTime();

		// start the level
		restartLevel();
	}

	public void restartLevel() {

		// if we don't set the state, the game will still think it's losing/winning
		state = State.Playing;

		hasPlayerMoved = false;

		wooblinessTimer = 0;

		player.transform.reset();

		for (Ghost ghost : ghosts) {
			ghost.transform.reset();
			ghost.isWoobly = false;
		}
	}

	private void lose() {
		state = State.Losing;
		loseTimer = losePeriod;
	}

	private void win() {
		state = State.Winning;
		winTimer = winPeriod;
	}

	public void update() {

		// it's just nice to have a delta... and we need it later
		long currentTime = System.nanoTime();
		float dt = (float)(currentTime - timeSinceLastUpdate) / 1000000000.0f;
		timeSinceLastUpdate = currentTime;

		if (state == State.Winning) {
			winTimer -= dt;
			if (winTimer <= 0) {state = State.Won; winTimer = 0;}
		}

		if (state == State.Losing) {
			loseTimer -= dt;
			if (loseTimer <= 0) {state = State.Lost; loseTimer = 0;}
		}

		// Gameplay logic may only happen if we're playing
		if (state == State.Playing) {

			// PHYSICS!
			UpdatePhysicsTransforms(dt);

			// Advance to the next stage if all pellets have been eaten! (cycle back to the first if it's the last one)
			if (pellets.size() <= 0) {win();}

			// Lose a life if the player gets haunted or whatever by a ghost
			for (Ghost ghost : ghosts) {
				if ((int)player.transform.x == (int)ghost.transform.x && (int)player.transform.y == (int)ghost.transform.y) {
					
					if (ghost.isWoobly) {
						ghost.transform.reset();
						ghost.isWoobly = false;
					} else {
						lose();
					}
				}
			}
			// Then reset ghost and player positions; pellets should remain as they were

			// Make Ghosts wander with some poorly-written and bad AI
			if (hasPlayerMoved) {

				for (Ghost ghost : ghosts) {

					// what the ghosts do, btw, is actually kinda cool; they're modeled after the pac man ghosts,
					// which, whenever possible, don't move backwards, creating the illusion of chase

					int col = (int)ghost.transform.x;
					int row = (int)ghost.transform.y;

					int colMax = boardWidth;
					int rowMax = boardHeight;

					int colW = col - 1 >= 0 ? col - 1 : colMax - 1;
					int colE = col + 1 < colMax ? col + 1 : 0;
					int rowN = row - 1 >= 0 ? row - 1 : rowMax - 1;
					int rowS = row + 1 < rowMax ? row + 1 : 0;

					boolean canMoveE = unoccupied[colE][row];
					boolean canMoveN = unoccupied[col][rowN];
					boolean canMoveW = unoccupied[colW][row];
					boolean canMoveS = unoccupied[col][rowS];

					int numChoices = 0;
					if (canMoveE) {numChoices ++;}
					if (canMoveN) {numChoices ++;}
					if (canMoveW) {numChoices ++;}
					if (canMoveS) {numChoices ++;}

					if (ghost.transform.isStationary || (numChoices > 2 && numChoices != ghost.numChoicesPrev)) {

						boolean didMove = false;
						int direction = (int)(Math.random() * 3.5f);
						for (int i = 0; i < 4; ++i) {

							float dx = 0.0f;
							float dy = 0.0f;

							if (direction == 0 && canMoveE && (ghost.transform.vx >= 0 || numChoices == 1)) {
								dx = 1.0f;
								didMove = true;
							} else if (direction == 1 && canMoveN && (ghost.transform.vy <= 0 || numChoices == 1)) {
								dy = -1.0f;
								didMove = true;
							} else if (direction == 2 && canMoveW && (ghost.transform.vx <= 0 || numChoices == 1)) {
								dx = -1.0f;
								didMove = true;
							} else if (direction == 3 && canMoveS &&  (ghost.transform.vy >= 0 || numChoices == 1)) {
								dy = 1.0f;
								didMove = true;
							}
							
							if (didMove) {

								// System.out.println("p: " + dx + ", " + dy + " :: numChoices: " + numChoices + " :: v: " + ghost.transform.vx + ", " + ghost.transform.vy);

								ghost.transform.setVelocity(dx * kGhostSpeed,dy * kGhostSpeed);
								break;
							}

							direction++;
							if (direction > 3) {direction = 0;}
						}

						ghost.numChoicesPrev = numChoices;

					}

				}
			}

			// UnWooblify Ghosts
			if (wooblinessTimer > 0) {
				wooblinessTimer -= dt;
				if (wooblinessTimer < 0) {
					for (Ghost ghost : ghosts) {
						ghost.isWoobly = false;
					}
				}
			}

			// Eat Pellets
			boolean didEatPowerPellet = false;
			ArrayList<Pellet> pelletsToRemove = new ArrayList<Pellet>();
			for (Pellet pellet : pellets) {
				if ((int)pellet.transform.x == (int)player.transform.x && (int)pellet.transform.y == (int)player.transform.y) {
					if (pellet.isPower) {didEatPowerPellet = true;}
					// System.out.println("om nom");
					pelletsToRemove.add(pellet);
				}
			}
			for (Pellet pellet : pelletsToRemove) {
				pellets.remove(pellet);
			}
			// the ghosts get all woobly if you eat a power pellet
			if (didEatPowerPellet) {
				// System.out.println("POWER!");
				for (Ghost ghost : ghosts) {
					wooblinessTimer = wooblinessPeriod;
					ghost.isWoobly = true;
				}
			}
		}


		// DRAW!

		// draw map
		for (Transform wall : walls) {
			float percentageUnwoobly = wooblinessPeriod == 0 ? 0 : 1.0f - wooblinessTimer/wooblinessPeriod;
			float hue = 0.65f * (percentageUnwoobly);
			float percentageWon = winPeriod == 0 ? 0.0f : winTimer/winPeriod;
			float saturation = (float)(0.5f * (1.0f + Math.cos(Math.PI * 8.0f * percentageWon)));
			d.setPixelHSB((int)wall.x,(int)wall.y + heightOffset,hue,saturation,1);
		}

		// make the pellets pulse because, grooviness
		pelletPulseTimer -= dt;
		if (pelletPulseTimer < 0) {
			pelletPulseTimer = pelletPulsePeriod;
		}

		// draw pellets
		for (Pellet pellet : pellets) {
			float brightness = pellet.isPower ? 1.0f : 0.35f;
			float pulsePercentage = pelletPulsePeriod == 0 ? 0.0f : pelletPulseTimer/pelletPulsePeriod;
			brightness *= 0.5f * (1.0f + Math.cos(2.0f * Math.PI * pulsePercentage)) * 0.25f + 0.75f;
			d.setPixelHSB((int)pellet.transform.x,(int)pellet.transform.y + heightOffset,0,0,brightness);
		}

		// pulse ghosts toooo
		ghostPulseTimer -= dt;
		if (ghostPulseTimer < 0) {
			ghostPulseTimer = ghostPulsePeriod;
		}

		// draw ghosts
		for (Ghost ghost : ghosts) {
			float pulsePercentage = ghostPulsePeriod == 0 ? 0.0f : 1.0f - ghostPulseTimer/ghostPulsePeriod;
			float brightness = (float)(0.5f * (1.0f + Math.cos(2.0f * Math.PI * pulsePercentage)) * 0.25f + 0.75f);
			float hue = ghost.isWoobly ? 0.65f : ghost.hue;
			d.setPixelHSB((int)ghost.transform.x,(int)ghost.transform.y + heightOffset,hue,1.0f,brightness);
		}

		// only pulse the player when moving!
		if (!player.transform.isStationary) {
			playerPulseTimer -= dt;
			if (playerPulseTimer < 0) {
				playerPulseTimer = playerPulsePeriod;
			}
		} else {
			playerPulseTimer = 0;
		}

		// draw player
		float playerPulsePercentage = playerPulsePeriod == 0 ? 0.0f : 1.0f - playerPulseTimer/playerPulsePeriod;
		float playerBrightness = (float)(0.5f * (1.0f + Math.cos(2.0f * Math.PI * playerPulsePercentage)) * 0.5f + 0.5f);
		if (state == State.Losing) {
			float losePercentage = losePeriod == 0.0f ? 1.0f : loseTimer/losePeriod;
			playerBrightness *= losePercentage;
		}
		d.setPixelHSB((int)player.transform.x,(int)player.transform.y + heightOffset,0.15f,1,playerBrightness);

	}

	public void MovePlayer(float dx, float dy) {

		hasPlayerMoved = true;

		float vx = dx * kPlayerSpeed;
		float vy = dy * kPlayerSpeed;
		player.transform.setVelocity(vx, vy);
	}

	private void UpdatePhysicsTransforms(float dt) {

		for (Transform transform : physicsTransfoms) {

			// newton's probably good enough; this stuff doesn't move fast
			float dx = transform.vx * dt;
			float dy = transform.vy * dt;

			// see where it will be
			float xNew = transform.x + dx;
			float yNew = transform.y + dy;

			// wrap around the board
			float width = boardWidth;
			float height = boardHeight;
			if (xNew >= width) {xNew -= width;}
			if (xNew < 0) {xNew += width;}
			if (yNew >= height) {yNew -= height;}
			if (yNew < 0) {yNew += height;}

			// we only want to move if we won't collide with something solid
			boolean canMove = true;
			ArrayList<Transform> colliders = collisionMap.get(transform);
			if (colliders != null) {
				for (Transform collider : colliders) {
					if ((int)xNew == (int)collider.x && (int)yNew == (int)collider.y) {

						canMove = false;
						break;

					}
				}
			}

			transform.isStationary = !canMove || (dx == 0 && dy == 0);

			if (!canMove) {
				transform.x = (int)transform.x;
				transform.y = (int)transform.y;
				continue;
			}

			// do move
			transform.x = xNew;
			transform.y = yNew;
			
		}
	}

 }