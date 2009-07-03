
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

import java.awt.Color;


/**
 * This interface represents an object that goes inside of a single cell defined
 * by four split Lines (left, right, top, bottom) which are available in the
 * GridCell for this object.
 * 
 * Objects implementing this interface must implement the method to draw itself,
 * as well as the method to pick within itself.
 * 
 */
public interface CellGeom {
	/**
	 * Accessor for the key value of an object.
	 * @return The value of this object's key.
	 */
    public int getKey();
    /**
     * Accessor for the name of this object.
     * @return The string name of this object.
     */
    public String getName();
    /**
     * Draws the object contained in its cell object, in the given color and depth.
     * @param c The color to draw the object.
     * @param plane The plane in which to draw the object.
     */
    public void drawInCell(Color c, double plane);

    /**
     * Accessor for the cell that this object is contained in.
     * This cell may be a single cell wide/tall, or several cells in width/height.
     * @return A grid cell that surrounds this object.
     */
    public GridCell getCell();
    /**
     * Returns the minimum split line in the given direction for this object.
     * @param xy {@link AccordionDrawer#X} for horizontal, {@link AccordionDrawer#Y} for vertical direction.
     * @return The minimum split line in the given direction.
     */
    public abstract SplitLine getMinLine(int xy);
    /**
     * Returns the maximum split line in the given direction for this object.
     * @param xy {@link AccordionDrawer#X} for horizontal, {@link AccordionDrawer#Y} for vertical direction.
     * @return The maximum split line in the given direction.
     */
    public abstract SplitLine getMaxLine(int xy);

}
