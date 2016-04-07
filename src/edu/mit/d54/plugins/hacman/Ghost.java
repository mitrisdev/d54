package edu.mit.d54.plugins.hacman;

public class Ghost extends Object {

	public int numChoicesPrev;

	public float hue;
	public boolean isWoobly;

	public Transform transform;

	private float xPrev;
	private float yPrev;

	public Ghost() {
		isWoobly = false;
		transform = new Transform();
	}

}