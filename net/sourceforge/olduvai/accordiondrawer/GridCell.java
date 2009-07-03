
/*
  Copyright (c) 2002 Compaq Computer Corporation
   
  SOFTWARE RELEASE
   
  Permission is hereby granted, free of charge, to any person obtaining
  a copy of this software and associated documentation files (the
  "Software"), to deal in the Software without restriction, including
  without limitation the rights to use, copy, modify, merge, publish,
  distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject to
  the following conditions:
   
  - Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
   
  - Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
   
  - Neither the names of Compaq Research, Compaq Computer Corporation
  nor the names of its contributors may be used to endorse or promote
  products derived from this Software without specific prior written
  permission.
   
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
  IN NO EVENT SHALL COMPAQ COMPUTER CORPORATION BE LIABLE FOR ANY CLAIM,
  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package net.sourceforge.olduvai.accordiondrawer;

/**
 * A class representing a cell, which is a rectangular region between 2 pairs of split lines in an Accordion Drawer.
 * 
 * A GridCell contains information about split lines on four sides of it. 
 * 
 * @author  Tamara Munzner, Serdar Tasiran, 
 * @see     AccordionDrawer
 * @see     net.sourceforge.olduvai.accordiondrawer.GridCell
 */

public class GridCell {

	/** The accordoin drawer for this grid cell. */
    public AccordionDrawer drawer; // My AD 

	/**
	 * Split Lines above [ {@link AccordionDrawer#X} ] or to the left [ {@link AccordionDrawer#Y} ] of me. 
	 */
	protected SplitLine minLine[] = new SplitLine[2];
	/**
	 * Split Lines below [ {@link AccordionDrawer#X} ] or to the right [ {@link AccordionDrawer#Y} ] of me.
	 */
    protected SplitLine maxLine[] = new SplitLine[2];
    
    /**
     * Cache the last drawn frame for detecting determining when this cell was last drawn in.
     */
    protected int drawnFrame; 

    /**
     * Default constructor for grid cells, sets the min and max lines to null and initializes drawer.
     * @param drawer The drawer for this grid cell.
     */
    public GridCell(AccordionDrawer drawer) {
    	minLine[AccordionDrawer.X] = maxLine[AccordionDrawer.X] = minLine[AccordionDrawer.Y] = maxLine[AccordionDrawer.Y] = null;
    	this.drawer = drawer;
    }

    /**
     * Sets the value of {@link AccordionDrawer#drawBackground} to the parameter value.
     * @param on The new value of {@link AccordionDrawer#drawBackground}
     */
    public void setDrawBackground(boolean on) {
		drawer.drawBackground = on;
    }

    /**
     * Gets the world-space position of the minimum split line for this cell, in X or Y.
     * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
     * @return The world-space position of the minimum split line in the given dimension.
     */
	public double getMinSplitAbsolute(int xy)
	{
		return drawer.splitAxis[xy].getAbsoluteValue(minLine[xy], drawer.getFrameNum());
	}

	/**
	 * Gets the world-space position of the maximum split line for this cell, in X or Y.
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 * @return The world-space position of the maximum split line in the given dimension.
	 */
	public double getMaxSplitAbsolute(int xy)
	{
		return drawer.splitAxis[xy].getAbsoluteValue(maxLine[xy], drawer.getFrameNum());
	}

	/**
	 * Returns the world-space size in X or Y for this cell.
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 * @return The world-space difference in position between the minimum and maximum sides for the given dimension.
	 */
	public double getSize(int xy)
	{
		return getMaxSplitAbsolute(xy) - getMinSplitAbsolute(xy);
	}

	/**
	 * Return the minimum split line in the given dimension.
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 * @return The minimum split line in the given dimension.
	 */
	public SplitLine getMinLine(int xy) { return minLine[xy];}
	/**
	 * Return the minimum split line in the given dimension. 
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 * @return The maximum split line in the given dimension.
	 */
	public SplitLine getMaxLine(int xy) { return maxLine[xy];}
    
	/**
	 * Sets the maximum split line in the given dimension.
	 * @param maxline The new split line for {@link #maxLine} in xy
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 */
	public void setMaxLine(SplitLine maxline, int xy) {
		maxLine[xy] = maxline;
	}

	/**
	 * Sets the minimum split line in the given dimension. 
	 * @param minline The new split line for {@link #minLine} in xy
	 * @param xy Either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 */
	public void setMinLine(SplitLine minline, int xy) {
		minLine[xy] = minline;
	}
	
	/**
	 * Accessor for the last drawn frame for this cell.
	 * @return The value of {@link #drawnFrame}, the last known drawn frame for this cell.
	 */
	public int getDrawnFrame() {
		return drawnFrame;
	}

	/**
	 * Updates the drawn frame {@link #drawnFrame} to the given value.
	 * @param i The new value for {@link #drawnFrame}
	 */
	public void setDrawnFrame(int i) {
		drawnFrame = i;
	}
	
	/**
	 * Returns a string representation for objects of this class.
	 * @return String that represents the bounds for this grid cell (X: min -> max, Y: min -> max).
	 */
	public String toString()
	{
		return "X: " + (minLine[0]) + " -> " +
			(maxLine[0]) + ", Y: " +
			(minLine[1]) + " -> " +
			(maxLine[1]);
	}
};
