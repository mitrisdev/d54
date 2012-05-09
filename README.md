# Display 54 #

The MIT Green Building Display plugin interface

## Background ##

On April 20, 2012, MIT hackers installed a game similar to Tetris using the windows of the MIT Green Building (Building 54) as pixels.  The game (named MITris) was controlled from a wooden box placed outside of the building and could be played by members of the MIT community and the public.  The Green Building display was originally installed in a separate hack, displaying an American flag on the building to commemorate the tenth anniversary of the September 11, 2001 attacks.

We've decided to release some of the software used to run the hack.  Our goal in doing this is to inspire the world at large to create interesting games or visualizations or just about anything.  If we see something cool enough, we'll try to find a chance to run your code.

## Technical Information ##

The display consists of 153 pixels arranged in a 9 wide by 17 tall configuration.  These are all of the windows of the same size on the south side of the building.  Each window contains a custom-designed power LED module which is individually addressed.  All of the windows are controlled via a wireless broadcast protocol, which can drive the display at 15 frames per second in 24-bit color mode.

The game controller consisted of a wooden box with 4 lighted switches placed near the Great Sail, about 70 meters from the front of the Green Building.  The output from the switches was sent over the wireless network to a computer which ran the game and generated the output for the display.

The software released here is extremely similar to what was running for the hack.  All code that actually communicates with the display has been removed.  The released code includes the display plugin API which allows one to write and preview a plugin which would be compatible with the actual display.  The MITris game plugin as well as a simple test plugin are also included.

# Installation and Usage #

## Installation ##

The included ant build script will create a jar file which can be used directly.  Alternatively, a pre-built jar file with the initial release can be downloaded.  An installation of Java 1.6 or higher is required to run the software.

## Usage ##

To run the test plugin:

	java -jar d54-0.0.1.jar edu.mit.d54.plugins.test.TestPlugin

To run the MITris plugin:

	java -jar d54-0.0.1.jar edu.mit.d54.plugins.mitris.MITrisPlugin

The game opens up a socket on port 12345 to accept user commands.  The real controller would connect to this socket and send the button presses to the game as the characters `U`, `D`, `R`, and `L`.  This plugin certainly wasn't designed to take local input from the keyboard, but if you want to play locally, you can `telnet localhost 12345` in a separate terminal and type those letters.  You may have to hit `Return` to actually send the characters, depending on your `telnet` client.

## Writing plugins ##

To create your own plugin, you need to extend the `DisplayPlugin` class in `edu.mit.d54`.  The only method that needs to be implemented is `void loop()` which will be called every frame to draw the display.  To be compatible with the `PluginRunner` main class in the jar file, you must also define a constructor of the form `YourPlugin(Display2D display, double framerate)`.

In your `loop` method, you should call `getDisplay()` to grab a reference to the `Display2D` object, and then `getGraphics()` to get a `java.awt.Graphics2D` to draw on or the `setPixel` methods to modify the image data directly.

To run a custom plugin:

	java -jar d54-0.0.1.jar -cp <your .class file directory> your.pkg.YourPlugin

## Other hints ##

By default, the `PluginRunner` creates a `GBDisplay`, which is a `Display2D` with the dimensions of the Green Building.  You can create your own display with a different number of pixels as well as a different pixel aspect ratio by changing the arguments to the Display2D constructor.  This might require modifying `PluginRunner` (there is also a bare-bones launcher written in `edu.mit.d54.plugins.test.TestRunner`).

If you wanted to display the output of your plugin on something other than the preview window, you can implement a new `DisplayListener` to do that.  The `DisplayListener` is notified every time the display is updated and reads the updated image from the `getBufferedImage()` method in `Display2D`.

## Erratum ##

This signature can be used to prove authorship on the original commit date of this repository:

	7bc9bd54f9ce7725d4fe6a8dfedac6b9f62e7a379e34f32b698ed86fe3b56d513b3f33c1bcf1c390b5623ee13d69fd7881680ce1a3bec7c15a45cfefed502602
