
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

package net.sourceforge.olduvai.treejuxtaposer.drawer;

import java.awt.Color;
import java.util.*;

import net.sourceforge.olduvai.accordiondrawer.AbstractRangeList;
import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;
import net.sourceforge.olduvai.accordiondrawer.DrawableRange;
import net.sourceforge.olduvai.accordiondrawer.SplitAxis;
import net.sourceforge.olduvai.accordiondrawer.StaticSplitLine;


/**
 * A class representing a list of RangeInTrees. A RangeInTree
 * represents ordered pair (min,max)) of TreeNodes.
 *
 * This class is a helper class for TreeJuxtaposer and keeps a
 * resizeable array of RangeInTree's.
 *
 * Note that each RangeInTrees item in a RangeList can be associated
 * with a different Tree.
 * 
 * @see net.sourceforge.olduvai.treejuxtaposer.drawer.RangeInTree
 * @see net.sourceforge.olduvai.treejuxtaposer.drawer.TreeNode
 * 
 * @author  Tamara Munzner
 * */

public class RangeList extends AbstractRangeList {

	/**
	 * group ID: key refers to priority, this is for reference to groups by well known static assigned integers to constants.
	 */
	private int groupID;
	/**
	 * Accessor flag for comparator in {@link RangeInTree}.
	 */
    public static boolean returnObject = false;
    /**
     * Accessor storage for comparator in {@link RangeInTree}.
     */
	public static RangeInTree matchRange;
    /**
     * Only highlight group items for this tree, not all the others
     */ 
    private boolean thisTreeOnly;

    /**
     * Constuctor for a range list, for storing details about marked groups of nodes.
     * @param i key for this range list.
     * @param groupID groupID for this range list.
     */
    public RangeList(int i, int groupID) {
	color = new Color((float).5, (float).5, (float).5);
	enabled = true;
	ranges = new TreeSet();
	thisTreeOnly = false;
	key = i;
	this.groupID = groupID;
    }

    /**
     * Add a range to the list of ranges.  Collates all matches if there is an overlap.
     * @param min min index of new range
     * @param max max index of new range
     * @param t drawer for new range
     */
    public void addRange(int min, int max, AccordionTreeDrawer t) { // add range with intersection checking, this means the list is ordered
	returnObject = true;
	RangeInTree addTree = new RangeInTree(min, max, t); // new range to add
	while (ranges.contains(addTree))
	{
		ranges.remove(matchRange);
		addTree = new RangeInTree(Math.min(addTree.getMin(), matchRange.getMin()), Math.max(addTree.getMax(), matchRange.getMax()), t);
		matchRange = null;
	}
	ranges.add(addTree);
	returnObject = false;
	}
    
    /**
     * Find and remove a given range.
     * @param min min index of the range to remove
     * @param max max index of the range to remove
     * @param t drawer for the range to remove.
     */
    public void removeRange(int min, int max, AccordionTreeDrawer t) {
	RangeInTree r = null;
	Iterator iter = ranges.iterator();
	while (iter.hasNext())
	{
		RangeInTree curr = (RangeInTree)iter.next();
		if (curr.getMin() == min && curr.getMax() == max && curr.atd == t)
		{
			r = curr;
			break;
		}
	}
	// delete if it it's identical to something previously passed in...
	if (r != null)
		ranges.remove(r); 
    }

    /**
     * Reset rangeList
     */
    public void clear() {
	ranges.clear();
    }

    /**
     * Flag access to {@link #thisTreeOnly}.
     * @return value of {@link #thisTreeOnly}
     */
    public boolean isThisTreeOnly() { return thisTreeOnly;}
    public void setThisTreeOnly(boolean on) { thisTreeOnly = on;}
    /**
     * Get the whole Set of ranges
     * @return ranges, the raw set
     */
    public Set getRanges() {
		return ranges;
	}
	
    /**
     * Get the first range in the set.
     * @return First range
     */
	public RangeInTree getFirst(){
		return (RangeInTree)((TreeSet)ranges).first();
	}
	
	/**
	 * Get the number of ranges in this list.
	 * @return The size of the ranges container.
	 */
	public int getNumRanges()
	{
		return ranges.size();
	}
	

	/**
	 * Debugging output function.
	 * @return each range on a separate line
	 */
	public String toString()
	{
		String returnString = (enabled?"+":"-") + (groupID)+"[";
		Iterator iter = ranges.iterator();
		RangeInTree next = null;
		if (iter.hasNext())
			next =(RangeInTree)iter.next(); 
		while (next != null && iter.hasNext())
		{
			returnString += next + ",\n\t";
			next = (RangeInTree)iter.next();
		}
		if (ranges.size() > 0)
			returnString += next;
		returnString += "]";
		return returnString;
	}
	/**
	 * Collate split line indices into an array of integers.
	 * @param horizontal true for horizontal/X, false for vertical/Y
	 * @return array of indices, two for each range (index[0,1] is a pair for range 0, index[2,3] is a pair for range 1, etc).
	 */
	public int[] getSplitIndices(boolean horizontal) {
		int[] returnArray = new int[ranges.size() * 2];
		Iterator iter = ranges.iterator();
		int i = 0;
		while (iter.hasNext())
		{
			RangeInTree currRange = (RangeInTree)iter.next(); 
			returnArray[i*2] = currRange.getMin()-1;
			returnArray[i*2+1] = currRange.getMax();
			i++;
		}
		return returnArray;
	}

	/**
	 * Collate range sizes into an array of doubles.
	 * @param splitLine Split axis for this range.
	 * @param frameNum current frame
	 * @return array of doubles, one for each range.
	 */
	public double[] getSizesOfAllRanges(SplitAxis splitLine, int frameNum) {
		Iterator iter = ranges.iterator();
		double[] size = new double[size()];
		int i = 0;
		while (iter.hasNext())
		{
			RangeInTree range = (RangeInTree)iter.next();
			size[i++] = range.getSize(splitLine, frameNum);
		}
		return size;
	}

	/**
	 * Prune the range list and return only a range list of ranges for the given drawer.
	 * @param d drawer of interest for ranges, all other drawers will not have their ranges added to the return value
	 * @return RangeList of ranges from the given drawer.
	 */
	public AbstractRangeList onlyThisAD(AccordionDrawer d) {
		RangeList newR = new RangeList(key, groupID);
		Iterator iter = ranges.iterator();
		while (iter.hasNext())
		{
			RangeInTree r = (RangeInTree)iter.next();
			if (r.atd == d)
				newR.ranges.add(r);
		}
		return newR;
	}

	/** Return a list of ranges of split line indices.
	Input ranges (this object's range list) are ranges of tree nodes.
	Added boolean horiz to fix off-by-one problems between compressing adjacent 
	ranges and comparing adjacent ranges for additions.
	@param xy X for horizontal, Y for vertical
	@param horiz redundant?  ever different from xy in use?
	@return rangeList of split line ranges corresponding to this rangelist of tree node indices
	*/
	public RangeList nodeKeyToSplitLine(int xy, boolean horiz)
	{
		RangeList newR = new RangeList(key, groupID);
		Iterator iter = ranges.iterator();
		while (iter.hasNext())
		{
			RangeInTree rit = (RangeInTree)iter.next();
			RangeInTree splitLineRange = rit.getSplitLineRange(xy, horiz);
			newR.addRange(splitLineRange.getMin(), splitLineRange.getMax(), rit.atd);
		}
		return newR;	
	}

	/**
	 * Invert this range list to perform a shrink operation (which is actually a grow on the unmarked ranges).
	 * @param xy Axis for this list of ranges
	 * @param ad drawer for this range list
	 * @return The list of ranges between the list of ranges of this object.
	 */
	public AbstractRangeList flipRangeToShrink(int xy, AccordionDrawer ad) {
		RangeList returnRangeList = new RangeList(this.key, groupID);
		int splitLineSize = ad.getSplitAxis(xy).getSize(); 
		if (ranges.size() == 0)
		{
			returnRangeList.addRange( 0, splitLineSize, (AccordionTreeDrawer) ad );
			return returnRangeList;
		}
		Iterator flipIter = ranges.iterator();
		DrawableRange prev = (DrawableRange)flipIter.next();
		if (prev.getMin() > 0)
			returnRangeList.addRange(0, prev.getMin()-1, (AccordionTreeDrawer)ad);
		while( flipIter.hasNext())
		{
			RangeInTree curr = (RangeInTree)flipIter.next();
			returnRangeList.addRange( prev.getMax()+1, curr.getMin()-1, (AccordionTreeDrawer)ad );
			prev = curr; 
		}
		if (prev.getMax() < splitLineSize)
			returnRangeList.addRange(prev.getMax()+1, splitLineSize, (AccordionTreeDrawer)ad);
		return returnRangeList;
	}

	/**
	 * Determines if the given range of min,max is overlapping with this rangelist
	 * @param min min of range to test
	 * @param max max of range to test
	 * @param atd drawer of testing range 
	 * @return true if min,max overlaps with any range in this list of ranges
	 */
	public boolean isThisRangeInList(int min, int max, AccordionTreeDrawer atd)
	{
	    RangeInTree.doAdj = false;
	    boolean returnValue = ranges.contains(new RangeInTree(min, max, atd));
	    RangeInTree.doAdj = true;
	    return returnValue;
	}
		
	/**
	 * Accumulate the unshrinkable total for this set of ranges.  This will restrict grows to support a minimum gap between ranges, if possible.
	 * @param ad Drawer for this rangelist
	 * @param splitAxis Axis that this range list is on
	 * @param frameNum current frame number
	 */
	public double getUnshrinkableTotal(AccordionDrawer ad, SplitAxis splitAxis, int frameNum)
	{
		double size = 0.0;
		if (ranges.size() == 0) return size;
		Iterator iter = ranges.iterator();
		RangeInTree rit = (RangeInTree)iter.next();
		RangeInTree betweenRanges = new RangeInTree(0, rit.getMin()-1, rit.atd); // 0 since getsize subtracts 1 from min
		double betweenSize = betweenRanges.getSize(splitAxis, frameNum);
		if (betweenSize < ad.minContextPeriphery)
			size += betweenSize;
		int prevMax = rit.getMax();
		while (iter.hasNext())
		{
			rit = (RangeInTree)iter.next();
			if (iter.hasNext())
			{
				betweenRanges = new RangeInTree(prevMax+1, rit.getMin()-1, rit.atd); // min+1 since getsize subtracts 1 from min
				betweenSize = betweenRanges.getSize(splitAxis, frameNum);
				if (betweenSize < ad.minContextInside)
					size += betweenSize;
			}
			prevMax = rit.getMax();
		}
		betweenRanges = new RangeInTree(rit.getMax()+1, splitAxis.getSize(), rit.atd);
		betweenSize = betweenRanges.getSize(splitAxis, frameNum);
		if (betweenSize < ad.minContextPeriphery)
			size += betweenSize;
		return size;
	}
	
	/**
	 * Group ID accessor.
	 * @return value of {@link #groupID()}
	 */
	public int groupID()
	{
		return groupID;
	}


};
