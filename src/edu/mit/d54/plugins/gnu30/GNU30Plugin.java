package edu.mit.d54.plugins.gnu30;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;

/**
 * This plugin was developed by a member of GNU strikeforce delta.
 * under the BSD-3 license that the rest of the d54 project is under.
 *
 *  Contributors:
 *
 *    paultag@mit.edu
 *    nico@incocesar.com
 */
public class GNU30Plugin extends DisplayPlugin {

	int currentFrame;

	private long lastUpdateTime;
	private double secondDelay = 0.5;
	private int currentGif = 0;
	private int frameCount;
	private ArrayList<BufferedImage> frames;
	private ArrayList<ArrayList<BufferedImage>> images;

	private int heightMax = 17;
	private int widthMax = 9;


	public GNU30Plugin(Display2D display, double framerate) throws IOException {
		super(display, framerate);
		this.lastUpdateTime = 0;
		this.images = new ArrayList<ArrayList<BufferedImage>>();
		this.loadIndex();
	}

	protected void loadIndex() throws IOException {
		InputStream stream = GNU30Plugin.class.getResourceAsStream("/resources/gnu30/images.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ( (line = br.readLine()) != null ) {
			String path = "/images/gnu30/" + line.trim();
			ArrayList<BufferedImage> bi = this.loadGifObject(GNU30Plugin.class.getResourceAsStream(path));
			this.images.add(bi);
		}
		this.loadGif(0);
	}
	
	public void loadGif(int index) {
		this.currentGif = index;
		this.setGifSet(this.images.get(this.currentGif));
	}
	
	public ArrayList<BufferedImage> loadGifObject(InputStream is) throws IOException {
		ImageInputStream image = ImageIO.createImageInputStream(is);
		ImageReader ir = (ImageReader) ImageIO.getImageReadersBySuffix("gif").next();
		ir.setInput(image, false);
		int frames = ir.getNumImages(true);
		ArrayList<BufferedImage> br = new ArrayList<BufferedImage>();
		for (int i = 0; i < frames; ++i) {
			BufferedImage frame = ir.read(i);
			br.add(frame);
		}
		return br;
	}

	public void setGifObject(InputStream is) throws IOException {
		this.setGifSet(this.loadGifObject(is));
	}
	
	public void setGifSet(ArrayList<BufferedImage> localFrames) {
		synchronized(this) {
			if (localFrames.size() == 0) {
				System.out.println("The image sucks a whole lot");
			} else {
				this.frames = localFrames;
				this.frameCount = this.frames.size();
				this.currentFrame = 0;
			}
		}
	}

	@Override
	protected void loop() {
		synchronized(this) {
			Display2D display = getDisplay();
			BufferedImage frame = this.frames.get(this.currentFrame);

			int width = frame.getWidth() > this.widthMax ? this.widthMax : frame.getWidth();
			int height = frame.getHeight() > this.heightMax ? this.heightMax : frame.getHeight();
			
			for (int ix = 0; ix < width; ++ix) {
				for (int iy = 0; iy < height; ++iy) {					
					int pixel = frame.getRGB(ix, iy);

					int red = (pixel >> 16) & 0xFF;
					int green = (pixel >> 8) & 0xFF;
					int blue = pixel & 0xFF;

					display.setPixelRGB(ix, iy, red, green, blue);
				}
			}
			
			if (this.lastUpdateTime + (this.secondDelay * 1000) < System.currentTimeMillis()) {
                if (this.currentFrame == (this.frameCount - 1)) {
                    if (this.currentGif == (this.images.size() - 1)) {
                        /* Loop! */
                        this.loadGif(0);
                    } else {
                        this.loadGif(this.currentGif + 1);
                    }
                    
                }
				this.currentFrame = (this.currentFrame + 1) % this.frameCount;
				this.lastUpdateTime = System.currentTimeMillis();
			}
		}
	}
}
