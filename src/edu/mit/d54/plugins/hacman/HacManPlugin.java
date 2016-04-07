package edu.mit.d54.plugins.hacman;

import java.io.IOException;
import edu.mit.d54.ArcadeController;
import edu.mit.d54.ArcadeListener;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

import java.util.ArrayList;

/**
 * 
 */
public class HacManPlugin extends DisplayPlugin implements ArcadeListener {

	private static final boolean isMini = true;

	private enum State {
		Attract, Play,
	}
	private State state;

	private static final int numLivesStart = 3;
	private int numLivesCurrent;

	private int levelIndexCurrent;

	private ArrayList<String> levelFilePaths;

	private ArcadeController controller;

	private ScenePlay scenePlay;

	private BufferedImage logo;

	private static final float logoScrollPeriod = 10.0f;
	private float logoScrollTimer;

	private long timeSinceLastUpdate;

	private int logoScrollCount;

	public HacManPlugin(Display2D display, double framerate) throws IOException {

		super(display, framerate);

		state = State.Attract;

		// we need to do this so we can set the listener later
		controller = ArcadeController.getInstance();

		// we store the full file path for each file
		levelFilePaths = new ArrayList<String>();
		// in the case of a small grid (9x11), we'll need smaller maps and a smaller logo
		String prefix = isMini ? "mini_" : "";
		levelFilePaths.add("/images/hacman/" + prefix + "hacman_lvl1.png");
		levelFilePaths.add("/images/hacman/" + prefix + "hacman_lvl2.png");
		levelFilePaths.add("/images/hacman/" + prefix + "hacman_lvl3.png");

		try {
			logo = ImageIO.read(HacManPlugin.class.getResourceAsStream("/images/hacman/" + prefix + "hacman_logo.png"));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// startNewGame();
	}

	private void startAttractMode() {

		logoScrollCount = 0;

		logoScrollTimer = logoScrollPeriod;

		timeSinceLastUpdate = System.nanoTime();

		state = State.Attract;
	}

	private void startNewGame() {
		numLivesCurrent = numLivesStart;
		levelIndexCurrent = 0;
		startLevel();
		state = State.Play;
	}

	private void startLevel() {
		String fileName = levelFilePaths.get(levelIndexCurrent);

		try{
			int heightOffset = isMini ? 4 : 0;
		  	scenePlay = new ScenePlay(getDisplay(),fileName,heightOffset);
		}catch(IOException e){
		  	e.printStackTrace();
		}
	}

	private void continueCurrentGame() {

		numLivesCurrent--;

		//System.out.println(numLivesCurrent);

		if (numLivesCurrent < 0) {
			
			startAttractMode();

			return;
		}

		scenePlay.restartLevel();
	}

	private void advanceToNextLevel() {
		levelIndexCurrent ++;
		if (levelIndexCurrent >= levelFilePaths.size()) {levelIndexCurrent = 0;}
		startLevel();
	}

	@Override
	protected void onStart()
	{
		controller.setListener(this);
	}

	@Override
	public void arcadeButton(byte b) {

		switch (state) {
			case Attract:
				if (b == 'L' || b == 'R' || b == 'U' || b == 'D') {
					startNewGame();
				}
				break;
			case Play:
				switch (b) {
				case 'L':
					scenePlay.MovePlayer(-1,0);
					break;
				case 'R':
					scenePlay.MovePlayer(1,0);
					break;
				case 'U':
					scenePlay.MovePlayer(0,-1);
					break;
				case 'D':
					scenePlay.MovePlayer(0,1);
					break;
				default:
					break;
				}
		}

	}

	@Override
	protected void loop() {

		switch (state) {

			case Attract:

				long currentTime = System.nanoTime();
				float dt = (float)(currentTime - timeSinceLastUpdate) / 1000000000.0f;
				timeSinceLastUpdate = currentTime;

				Display2D disp = getDisplay();
				Graphics2D g = disp.getGraphics();

				logoScrollTimer -= dt;
				if (logoScrollTimer < 0) {
					logoScrollTimer = logoScrollPeriod;
					logoScrollCount++;
				}
				float percentage = logoScrollPeriod == 0 ? 1.0f : 1.0f - logoScrollTimer/logoScrollPeriod;

				// auto-start the game after 3 cycles
				if (logoScrollCount >= 3) {startNewGame(); break;}
				
				int logoPos = (int)(percentage * (logo.getWidth() + disp.getWidth() + 1));
				g.drawImage(logo, disp.getWidth() + 1 -logoPos, (isMini ? 4 : 0), null);

				break;

			case Play:

				// TODO: go to the main menu on a loss
				if (scenePlay.state == ScenePlay.State.Lost) {
					continueCurrentGame();
				}

				// on a win, the map gets incremented
				if (scenePlay.state == ScenePlay.State.Won) {
					advanceToNextLevel();
				}

				// update the game
				scenePlay.update();

				// display lives as hud
				Display2D d = getDisplay();
				for (int life = 0; life < numLivesCurrent; ++life) {

					int col = life;
					// right now, there's no way to get more lives, but... why not, right?
					if (life > d.getWidth()) {continue;}
					int row = d.getHeight() - 1 - (isMini ? 2 : 0);

					d.setPixelHSB(col,row,0.15f,1,1);
				}

				break;
		}
		
		
	}

}
