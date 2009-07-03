
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

import net.sourceforge.olduvai.accordiondrawer.DrawableRange;
import net.sourceforge.olduvai.accordiondrawer.StaticSplitLine;

/**
 * A class representing a range (ordered pair (min,max)) of TreeNode
 * indices in a Tree.
 * Extension of RangeInTree, with a drawer for identification.
 * 
 * @author  Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
 *
 */

public class RangeInTree extends DrawableRange {

	/** Drawer for this range. */
	protected AccordionTreeDrawer atd;
    
	/**
	 * Basic constructor.
	 * @param minimum minimum index of the range
	 * @param maximum maximum index of the range
	 * @param t drawer for this range.
	 */
    public RangeInTree(int minimum, int maximum, AccordionTreeDrawer t) {
	min = minimum; 
	max = maximum;
	atd = t;
	group = null; // each group should set color for their own ranges
    }
    /**
     * Get the drawer for this range
     * @return Drawer {@link #atd}
     */
    public AccordionTreeDrawer getTree() { return atd; }
    /**
     * Debug string for this range.
     * @return drawer key, min, and max for this range 
     */
	public String toString()
	{
		return "(" +atd.getKey() +":" + min + "->" + max + ")";
	}
		

	/**
	 * Get the world-space size for this range.  Used to determine squishability of ranges in grow function.
	 * @param splitAxis Axis used to get size of the range
	 * @param frameNum current frame
	 * @return world-space size for this range of split lines
	 */
	public double getSize(net.sourceforge.olduvai.accordiondrawer.SplitAxis splitAxis, int frameNum)
	{
		int minCellSplit = min-1;
		int maxCellSplit = max;
		return splitAxis.getAbsoluteValue(maxCellSplit, frameNum) -
			splitAxis.getAbsoluteValue(minCellSplit, frameNum); 
	}

	/**
	 * if true, adjacent ranges are equal, and will be combined.  false is for proper matching for cell overlaps.
	 */
	public static boolean doAdj = true; // for adjacent matches
	/**
	 * Special comparator: overlaps are equal, even adjacent non-overlaps if {@link #doAdj} is true.
	 * Will save equal values if {@link RangeList#returnObject} is true.
	 */
	public int compareTo(Object o) {
	    int add = doAdj ? 1 : 0;
		RangeInTree other = (RangeInTree)o;
		int atdKey = atd.getKey();
		int otherKey = other.getTree().getKey();
		if (atdKey < otherKey)
			return -1; // this tree is less
		if (atdKey > otherKey)
			return 1; // this tree is greater
			
		// same tree
		if (max + add < other.getMin())
			return -1; // this range is less, +1 since adjacent ranges should be combined and are "equal"
		if (min > other.getMax() + add)
			return 1; // this range is greater, +1 since adjacent ranges should be combined and are "equal"

		if (RangeList.returnObject)
			RangeList.matchRange = other; // store found range for returning
		return 0; // some overlap, adjacent or identical ranges
	}

	/**
	 * Equality function that uses the overlap comparator.
	 * @param o other rangeInTree to test for overlap.
	 * @return true if ranges overlap, otherwise false.
	 */
	public boolean equals(Object o)
	{
		return compareTo(o) == 0;
	}

	// return the range of splitlines that represent min,max nodes in the Y direction (across the leaves)
	// ranges that call this should be ranges of any tree node that we want to turn into a split line range
	/**
	 * Convert the RangeInTree of leaf indices into a RangeInTree of split line indices.
	 * @param xy 0 for X, 1 for Y
	 * @param horiz redundant?
	 * @return RangeInTree of split lines in the given direction
	 */
	public RangeInTree getSplitLineRange(int xy, boolean horiz)
	{
		TreeNode minNode;
		if (horiz)
			minNode = atd.getNodeByKey(min);
		else
			minNode = atd.getNodeByKey(min).leftmostLeaf;
		TreeNode maxNode = atd.getNodeByKey(max).rightmostLeaf;
		int newMinLine = ((StaticSplitLine)minNode.getCell().getMinLine(xy)).getSplitIndex()+(horiz?0:1); // turn line into cell index
		int newMaxLine = ((StaticSplitLine)maxNode.getCell().getMaxLine(xy)).getSplitIndex();
		RangeInTree splitLineRange = new RangeInTree(newMinLine, newMaxLine, atd);
		return splitLineRange;
	}
	
};
