package edu.mit.d54.plugins.life;

import edu.mit.d54.Display2D;
import edu.mit.d54.DisplayPlugin;


import java.io.IOException;
import edu.mit.d54.ArcadeController;
import edu.mit.d54.ArcadeListener;


class LifeCell {

	public LifeCell(int iCol, int iRow) {
		m_iCol = iCol;
		m_iRow = iRow;
		m_bIsAlive = false;
		m_bNextState = false;
	}

	public boolean IsAlive(){
		return m_bIsAlive;
	}

	public void SetLife(boolean bLife){
		m_bIsAlive = bLife;
	}

	public void SetNextState(boolean bNextState){
		m_bNextState = bNextState;
	}

	public boolean UpdateLife(){
		boolean bHasChanged = m_bIsAlive != m_bNextState;
		m_bIsAlive = m_bNextState;
		return bHasChanged;
	}

	private int m_iRow;
	private int m_iCol;
	private boolean m_bIsAlive;
	private boolean m_bNextState;
}

/**
 * This is a plugin implementing Conway's Game of Life.  User input is received over the TCP socket on port 12345.
 */
public class LifePlugin extends DisplayPlugin implements ArcadeListener  {

	public static final int kNumColors = 200;
	public static final int kNumUnchangedFramesBeforeReseting = 20;
	public static final int kMinFrameRatePeriod = 1;
	public static final int kMaxFrameRatePeriod = 50;
	public static final int kDefaultFrameRatePeriod = 20;
	public static final int kStepFrameRatePeriod = 5;

	private int m_nFrameRateCount;
	private int m_nUnchangedStateCounter;
	private int m_nFrameRateTimer;
	private int m_nFrameRatePeriod;

	private LifeCell[][] m_Cells;
	private float m_fColor;

	private ArcadeController controller;

	public LifePlugin(Display2D display, double framerate) {
		super(display, framerate);
		m_nFrameRatePeriod = kDefaultFrameRatePeriod;
		try
		{
			controller = ArcadeController.getInstance();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		m_fColor = 0.0f;
		reset();
	}

	private void reset() {
		m_nFrameRateTimer = 0;
		m_nUnchangedStateCounter = 0;
		Display2D d=getDisplay();
		m_Cells = new LifeCell[d.getWidth()][d.getHeight()];
		for (int iCol=0; iCol<d.getWidth();++iCol) {
			for (int iRow=0; iRow<d.getHeight(); ++iRow) {
				m_Cells[iCol][iRow] = new LifeCell(iCol, iRow);
				m_Cells[iCol][iRow].SetLife(false);
				if (java.lang.Math.random() < 0.25f) {
					m_Cells[iCol][iRow].SetLife(true);
				}
			}
		}
	}

	@Override
	protected void onStart()
	{
		controller.setListener(this);
	}

	public void arcadeButton(byte b)
	{
		// On left and right, reset board. On up/down, change speed of evolution
		switch (b)
		{
		case 'L':
		case 'R':
			reset();
			break;
		case 'U':
			m_nFrameRatePeriod -= kStepFrameRatePeriod;
			m_nFrameRatePeriod = m_nFrameRatePeriod < kMinFrameRatePeriod ? kMinFrameRatePeriod : m_nFrameRatePeriod;
			break;
		case 'D':
			m_nFrameRatePeriod += kStepFrameRatePeriod;
			m_nFrameRatePeriod = m_nFrameRatePeriod > kMaxFrameRatePeriod ? kMaxFrameRatePeriod : m_nFrameRatePeriod;
			break;
		}
	}

	@Override
	protected void loop() {
		Display2D d=getDisplay();
		incrementColor();

		m_nFrameRateTimer--;
		boolean bDoUpdate = m_nFrameRateTimer <= 0;
		if (bDoUpdate) {
			m_nFrameRateTimer = m_nFrameRatePeriod;

			// Determine next state from number of live neighbors
			for (int iCol=0; iCol<m_Cells.length; ++iCol) {
				LifeCell[] rows = m_Cells[iCol];
				for (int iRow=0; iRow<rows.length; ++iRow) {
					LifeCell cell = m_Cells[iCol][iRow];
					int nLiveNeighbors = getWorldWrapNeighborCount(iCol, iRow);
					if (cell.IsAlive()){
						boolean bLivesOn = nLiveNeighbors == 2 || nLiveNeighbors == 3;
						cell.SetNextState(bLivesOn);
					} else {
						boolean bLives = nLiveNeighbors == 3;
						cell.SetNextState(bLives);
					}
				}
			}
		}

		// Update all cells
		boolean bHasChangedState = false;
		for (int iCol=0; iCol<m_Cells.length; ++iCol) {
			LifeCell[] rows = m_Cells[iCol];
			for (int iRow=0; iRow<rows.length; ++iRow) {
				LifeCell cell = m_Cells[iCol][iRow];
				bHasChangedState = (cell.UpdateLife() || bHasChangedState);
				if (cell.IsAlive()) {
					d.setPixelHSB(iCol,iRow,m_fColor,1,1);
				} else {
					d.setPixelHSB(iCol,iRow,0,0,0);
				}
			}
		}

		if (bDoUpdate && !bHasChangedState) {
			m_nUnchangedStateCounter++;
		}

		if (m_nUnchangedStateCounter >= kNumUnchangedFramesBeforeReseting) {
			reset();
		}
	}

	private int getNeighborCount(int iSelfCol, int iSelfRow) {
		int nAliveCount = 0;
		int iMinCol = java.lang.Math.max(0, iSelfCol - 1);
		int iMaxCol = java.lang.Math.min(iSelfCol + 1, m_Cells.length - 1);
		for (int iCol=iMinCol; iCol<=iMaxCol; ++iCol) {
			LifeCell[] rows = m_Cells[iCol];
			int iMinRow = java.lang.Math.max(0, iSelfRow - 1);
			int iMaxRow = java.lang.Math.min(iSelfRow + 1, rows.length - 1);
			for (int iRow=iMinRow; iRow<=iMaxRow; ++iRow) {
				if (m_Cells[iCol][iRow].IsAlive()) {
					if (iRow == iSelfRow && iCol == iSelfCol) {
						continue;
					}
					nAliveCount++;
				}
			}
		}
		return nAliveCount;
	}

	private int getWorldWrapNeighborCount(int iSelfCol, int iSelfRow) {
		int nAliveCount = 0;
		int nNumCols = m_Cells.length;
		for (int dCol=-1; dCol<=1; ++dCol) {
			int iCol = iSelfCol + dCol;
			iCol = iCol < 0 ? nNumCols - 1 : iCol;
			iCol = iCol >= nNumCols ? 0 : iCol;

			int nNumRows = m_Cells[iCol].length;
			for (int dRow=-1; dRow<=1; ++dRow) {
				int iRow = iSelfRow + dRow;
				iRow = iRow < 0 ? nNumRows - 1 : iRow;
				iRow = iRow >= nNumRows ? 0 : iRow;
				if (m_Cells[iCol][iRow].IsAlive()) {
					if (iRow == iSelfRow && iCol == iSelfCol) {
						continue;
					}
					nAliveCount++;
				}
			}
		}
		return nAliveCount;
	}

	private void incrementColor(){
		m_fColor += 1.0f/(float)kNumColors;
		if (m_fColor > 1.0f) {
			m_fColor = 0;
		}
	}
}

