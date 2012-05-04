package edu.mit.d54.plugins.mitris;


public class MITrisGame {

	private final double tickTime;
	private double time;
	private final int width; 
	private final int height;

	private MITrisBoard lockBoard;
	private Piece activePiece;
	private int pieceX,pieceY;
	private int rotation;
	private double lastStepTime;
	
	private boolean gameOver=false;
	
	public MITrisGame(int width, int height, double tickTime)
	{
		this.width=width;
		this.height=height;
		time=0;
		this.tickTime=tickTime;
		lockBoard=new MITrisBoard(width,height);
	}
	
	private static double getStepTime(int level)
	{
		return 5.0/(level+5.0);
	}
	
	public void clockTick()
	{
		if (gameOver)
			return;
		time+=tickTime;
		if (time-lastStepTime>=getStepTime(lockBoard.getLevel()))
		{
			lastStepTime=time; 
			if (lockBoard.checkPiece(activePiece, rotation, pieceX,pieceY-1))
				pieceY-=1;
			else
				finalizePiece();
		}
		lockBoard=lockBoard.clearRows();
		if (activePiece==null)
			if (!addNewPiece())
				gameOver=true;
	}
	
	private boolean addNewPiece()
	{
		lastStepTime=time;
		activePiece=Piece.getRandom();
		pieceX=4;
		pieceY=height-1;
		rotation=0;
		return lockBoard.checkPiece(activePiece, rotation, pieceX, pieceY);
	}
	
	public MITrisBoard getDisplayBoard()
	{
		if (gameOver)
			return lockBoard;
		return lockBoard.addPiece(activePiece, rotation, pieceX, pieceY);
	}
	
	public MITrisBoard getLockedBoard()
	{
		return lockBoard;
	}
	
	public boolean moveLeft()
	{
		if (lockBoard.checkPiece(activePiece, rotation, pieceX-1, pieceY))
		{
			pieceX-=1;
			return true;
		}
		return false;
	}
	
	public boolean moveRight()
	{
		if (lockBoard.checkPiece(activePiece, rotation, pieceX+1, pieceY))
		{
			pieceX+=1;
			return true;
		}
		return false;
	}
	
	public void dropPiece()
	{
		if (activePiece==null)
			return;
		while (lockBoard.checkPiece(activePiece, rotation, pieceX, pieceY-1))
		{
			pieceY-=1;
		}
		finalizePiece();
	}
	
	private void finalizePiece()
	{
		if (activePiece==null)
			return;
		lockBoard=lockBoard.addPiece(activePiece, rotation, pieceX, pieceY);
		activePiece=null;
	}
	
	public boolean rotatePiece()
	{
		if (lockBoard.checkPiece(activePiece, rotation+1, pieceX, pieceY))
		{
			rotation+=1;
			return true;
		}
		return false;
	}

	public boolean isGameOver() {
		return gameOver;
	}
	
	public double getTime()
	{
		return time;
	}
}
