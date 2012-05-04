package edu.mit.d54.plugins.mitris;

import java.awt.Color;
import java.util.Random;

public enum Piece
{
	L,S,Z,J,I,T,O;
	
	private static final int N_COMPONENTS=4;
	private static final Random rand;
	private static final Piece[] values;
	
	private final int x[];
	private final int y[];
	private final int maxRot;
	private final Color color;
	
	static
	{
		values=Piece.values();
		rand=new Random();
	}
	
	public static Piece getRandom()
	{
		return values[rand.nextInt(values.length)];
	}
	
	public int getMaxRotation()
	{
		return maxRot;
	}
	
	public Color getColor(int level, double time)
	{
		level%=10;
		if (level==0)
			return color;
		else if (level<5)
		{
			float[] hsb=new float[3];
			Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
			hsb[1]-=0.2*level;
			return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		}
		else 
		{
			level-=2;
			float[] hsb=new float[3];
			Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
			float delta=(float)(time*(level-1)/7)%1;
			if (level%2==0)
			{
				hsb[0]+=delta;
				hsb[0]%=1;
			}
			else
			{
				hsb[0]+=1-delta;
				hsb[0]%=1;
			}
			return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		}
	}
	
	public int getX(int ind)
	{
		return x[ind];
	}
	
	public int getY(int ind)
	{
		return y[ind];
	}
	
	public int getNumComponents()
	{
		return N_COMPONENTS;
	}
	
	private Piece()
	{
		x=new int[N_COMPONENTS];
		y=new int[N_COMPONENTS];
		switch (this.name().charAt(0))
		{
		case 'L':
			maxRot=4;
			color=new Color(255, 50, 0);
			x[0]=-1;	y[0]=0;
			x[1]=0;		y[1]=0;
			x[2]=1;		y[2]=0;
			x[3]=1;		y[3]=1;
			break;
		case 'S':
			maxRot=2;
			color=Color.GREEN;
			x[0]=0;		y[0]=0;
			x[1]=1;		y[1]=0;
			x[2]=-1;	y[2]=1;
			x[3]=0;		y[3]=1;
			break;
		case 'Z':
			maxRot=2;
			color=Color.RED;
			x[0]=-1;	y[0]=0;
			x[1]=0;		y[1]=0;
			x[2]=0;		y[2]=1;
			x[3]=1;		y[3]=1;
			break;
		case 'J':
			maxRot=4;
			color=Color.BLUE;
			x[0]=-1;	y[0]=0;
			x[1]=0;		y[1]=0;
			x[2]=1;		y[2]=0;
			x[3]=-1;	y[3]=1;
			break;
		case 'I':
			maxRot=2;
			color=Color.CYAN;
			x[0]=-2;	y[0]=0;
			x[1]=-1;	y[1]=0;
			x[2]=0;		y[2]=0;
			x[3]=1;		y[3]=0;
			break;
		case 'T':
			maxRot=4;
			color=new Color(178,0,255);
			x[0]=-1;	y[0]=0;
			x[1]=0;		y[1]=0;
			x[2]=1;		y[2]=0;
			x[3]=0;		y[3]=1;
			break;
		case 'O':
			maxRot=1;
			color=new Color(200, 255, 0);
			x[0]=-1;	y[0]=0;
			x[1]=0;		y[1]=0;
			x[2]=-1;	y[2]=1;
			x[3]=0;		y[3]=1;
			break;
		default:
			throw new RuntimeException("the impossible has occurred");
		}
	}
}

