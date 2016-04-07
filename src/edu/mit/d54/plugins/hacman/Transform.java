package edu.mit.d54.plugins.hacman;

public class Transform extends Object {

	public float x;
	public float y;

	public float vx;
	public float vy;

	public float startX;
	public float startY;

	public boolean isStationary;

	public Transform() {

	}

	public void setVelocity(float _vx, float _vy) {
		vx = _vx;
		vy = _vy;
	}

	public void setStartPosition(float _x, float _y) {
		startX = _x;
		startY = _y;
	}

	public void reset() {

		isStationary = true;
		
		x = startX;
		y = startY;

		setVelocity(0,0);
	}
}