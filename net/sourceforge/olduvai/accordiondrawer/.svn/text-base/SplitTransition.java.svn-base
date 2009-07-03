
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
 * A class that helps compute coordinates for smooth animated transitions. 
 * 
 * This is a simple container class that stores a beginning relative position, an
 * ending relative position and uses a straightforward (linear) equation to transition
 * between them in a specified number of steps. These objects are created and
 * enqueued in the SplitAxis class.  They are read out of the queue from the 
 * drawFrame method of AccordionDrawer.  
 * 
 * @author Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
 * 
 * @see net.sourceforge.olduvai.accordiondrawer.AccordionDrawer
 * @see net.sourceforge.olduvai.accordiondrawer.SplitAxis
 */

public class SplitTransition {

	/** Pointer to split line that is moving. */
	private SplitLine index; // object that's moving
	/** Current step of transition, between 0 and {@link #maxStep} - 1. */
	private int curStep;
	/** Maximum step number, the total number of linear steps this line is taking, indexed by {@link #curStep}. */
	private int maxStep;
	
	/** Starting relative position of this split line, between {@link SplitLine#getOpBound()} and {@link SplitLine#getParent()}.*/
	private double startRelative;
	/** Ending relative position of this split line, between {@link SplitLine#getOpBound()} and {@link SplitLine#getParent()}.*/
	private double endRelative;

	/** Create a split transition.
	 * @param index split line to move.  Value of {@link #startRelative} is stored here.
	 * @param endRelative Final relative position of the line.  Sets {@link #endRelative}.
	 * @param numSteps Number of steps in the linear transition.
	 */	
	public SplitTransition(SplitLine index, double endRelative, int numSteps)
	{
		this.index = index;
		this.endRelative = endRelative;
		startRelative = index.relativeValue;
		curStep = 0;
		maxStep = numSteps;
		
//		System.out.println("ST:" + this);
		if (!(endRelative < 1 && endRelative > 0))
		{
			System.out.println("end position is OUT OF RANGE: " + endRelative);
		}
	}
    
	/** 
	 * Do a single step of movement in the relative space of the splitline {@link #index}.
	 * ({@link #curStep}/{@link #maxStep}) * ({@link #endRelative} - {@link #startRelative}) + {@link #startRelative}
	 * */
	public void move()
	{
		index.relativeValue = (double)curStep/(double)maxStep * (endRelative - startRelative) + startRelative;

		if (index.relativeValue < 0 || index.relativeValue > 1)
			System.out.println("SplitTransition: bad split line set in move: " + index.relativeValue);	
	}
	
	/** Update the {@link #curStep} by one, used before {@link #move()}. */
	public void incr()
	{
		curStep++;
	}
    
	/** Test to see if the transitions are done for {@link #index}. 
	 * @return true if this line has no more transitions, and is now at its final position {@link #endRelative}.
	 * */
	public boolean done()
	{
		return curStep > maxStep; 
	}
    
	/** Finalization of transition, verification of correct relative location of a jump cut is made, or round-off errors.
	 */
	public void end()
	{
//		System.out.println("Ending transition: " + this);
		index.relativeValue = endRelative;
	}
	
	/** Debugging output function.
	 * @return String that represents this transition, at it's current position. */
	public String toString()
	{
		return "(" +
			index +  
			"@"+ endRelative+", " + curStep + "/" + maxStep+")";
	}
};
