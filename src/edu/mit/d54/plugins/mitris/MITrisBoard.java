package edu.mit.d54.plugins.mitris;

public class MITrisBoard
{
	private final int width;
	private final int height;
	private final Piece[][] data;
	private int numCleared;
	
	public MITrisBoard(int width, int height)
	{
		this.width=width;
		this.height=height;
		numCleared=0;
		data=new Piece[width][height];
	}
	
	public MITrisBoard(MITrisBoard base)
	{
		this(base.width, base.height);
		for (int i=0; i<base.data.length; i++)
		{
			for (int j=0; j<base.data[i].length; j++)
			{
				this.data[i][j]=base.data[i][j];
			}
		}
		this.numCleared=base.numCleared;
	}
	
	public boolean checkPiece(Piece piece, int rot, int x, int y)
	{
		if (piece==null)
			return false;
		return addPiece(piece,rot,x,y)!=null;
	}
	
	public Piece getPosition(int x, int y)
	{
		return data[x][y];
	}
	
	public MITrisBoard addPiece(Piece piece, int rot, int x, int y)
	{
		if (piece==null)
			return this;
		MITrisBoard ret=new MITrisBoard(this);
		rot%=piece.getMaxRotation();
		for (int i=0; i<piece.getNumComponents(); i++)
		{
			int px=x,py=y;
			switch (rot)
			{
			case 0:
				px+=piece.getX(i);
				py+=piece.getY(i);
				break;
			case 1:
				px+=piece.getY(i);
				py-=piece.getX(i);
				break;
			case 2:
				px-=piece.getX(i);
				py-=piece.getY(i);
				break;
			case 3:
				px-=piece.getY(i);
				py+=piece.getX(i);
				break;
			default:
				throw new RuntimeException("invalid rotation");
			}
			if (py<0 || px<0 || px>=width) //out of bounds
				return null;
			else if (py<height && ret.data[px][py]!=null) //overlap
				return null;
			else
				if (py<height)
					ret.data[px][py]=piece;
		}
		return ret;
	}

	public MITrisBoard clearRows() {
		MITrisBoard ret=new MITrisBoard(this);
		int outY=0;
		for (int y=0; y<height; y++)
		{
			boolean rowClear=true;
			for (int x=0; x<data.length; x++)
				if (getPosition(x, y)==null)
					rowClear=false;
			if (!rowClear)
			{
				for (int x=0; x<data.length; x++)
					ret.data[x][outY]=data[x][y];
				outY++;
			}
			else
			{
				ret.numCleared++;
			}
		}
		for (int y=outY; y<height; y++)
		{
			for (int x=0; x<data.length; x++)
			{
				ret.data[x][y]=null;
			}
		}
		if (ret.numCleared!=numCleared)
			System.out.println("Cleared "+ret.numCleared+" lines");
		return ret;
	}
	
	public MITrisBoard shiftBoardDown()
	{
		MITrisBoard ret=new MITrisBoard(this);
		for (int y=0; y<height-1; y++)
		{
			for (int x=0; x<width; x++)
				ret.data[x][y]=data[x][y+1];
		}
		for (int x=0; x<width; x++)
		{
			ret.data[x][height-1]=null;
		}
		return ret;
	}
	
	public boolean isBoardEmpty()
	{
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				if (data[x][y]!=null)
					return false;
		return true;
	}
	
	public int getLevel()
	{
		return numCleared/4;
	}
	
	public int getNumCleared()
	{
		return numCleared;
	}
}