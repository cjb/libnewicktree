package net.sourceforge.olduvai.accordiondrawer;

import java.util.TreeSet;

public class StaticSplitLine extends SplitLine {

	
	/**
	 * Position of split line in array.
	 * Ssed in TJ with static tree layouts.
	 * A cached value of this may be possible in dynamic cases,
	 * if a method of detecting changes is added to the split axis class
	 * to flush the cache.
	 */
	private int splitLineIndex;

	/**
	 * Create a static split line, with the constructor of {@link SplitLine}.
	 * @param isVirtual true for virtual split lines, created for rendering but not added to the split axis hierarchy.
     * @param value initial {@link #absoluteValue} value for this split line.  Will be used to place and determine relative value.
     * @param parent {@link #parent} split line
     * @param opAncestor opposite split line, {@link #opBound}
	 */
	public StaticSplitLine(double value, SplitLine parent, SplitLine opAncestor, boolean isVirtual)
	{
		super(value, parent, opAncestor, isVirtual);
	}

	/**
	 * Get the split line index for this line. Stored as state, so this is O(1).
	 * @return value of {@link #splitLineIndex}
	 */
	public int getSplitIndex()
	{
		return splitLineIndex;
	}
	
	/**
	 * Set the split line index for this line. O(1) to update state.
	 * @param splitLineIndex new value of {@link #splitLineIndex}.
	 */
	public void setSplitIndex(int splitLineIndex)
	{
		this.splitLineIndex = splitLineIndex; 
	}

	/**
	 * Recursively descend and assign split line index values to all
	 * split lines.
	 * @param rightAdd The amount to add to a right side child, initially 0 for root. 
	 */
	public void computeSplitLineIndex(int rightAdd)
	{
		if (getLeftChild() != null)
			((StaticSplitLine)getLeftChild()).computeSplitLineIndex(rightAdd);
		splitLineIndex = getLeftChild().getSubTreeSize() + rightAdd;
		if (getRightChild() != null)
			((StaticSplitLine)getRightChild()).computeSplitLineIndex(splitLineIndex + 1);
	}

	/**
	 * Get the parent object, as a {@link StaticSplitLine}.
	 * @return result of {@link #getParent()}, casted.
	 */
	public StaticSplitLine getStaticParent()
	{
		return (StaticSplitLine)getParent();
	}
	
	/**
	 * Get the off-parent object, as a {@link StaticSplitLine}.
	 * @return result of {@link #getOpBound()}, casted.
	 */
	public StaticSplitLine getOffParentBound()
	{
		return (StaticSplitLine)getOpBound();
	}
	
	/**
	 * debug output function, print the split line index for this line.
	 * @return the split line index {@link #splitLineIndex} as a string.
	 */
	public String toString()
	{
		return "" + splitLineIndex;
	}
	
	/**
	 * Natural order comparator that uses indices to determine which object (this or the input) precedes the other, or if they are the same.
	 * Stores a split line in the static {@link SplitLine#splitLineFound} variable if {@link SplitLine#getSplitLine} is set to true.
	 * @param o other object to test.
	 * @return -1 if this is less than o, +1 if greater than, 0 if equal, according to {@link #splitLineIndex} of each.
	 */
	public int compareTo(Object o) {
				
		StaticSplitLine ob = (StaticSplitLine) o;
		if (getSplitLine && splitLineFound == null)
			splitLineFound = ob;
		
		if ( splitLineIndex < ob.splitLineIndex ) 
			return -1;
		else if ( splitLineIndex > ob.splitLineIndex ) {
			if (getSplitLine )
				splitLineFound = ob;
			return 1;
		}

		if (getSplitLine )
			splitLineFound = ob;
		return 0;
	}
	
	/**
	 * Returns a SplitLine whose absoluteValue is either equal to the input SplitLine's
	 * absoluteValue, or is the closest SplitLine less than the input SplitLine's 
	 * absoluteValue.
	 * 
	 *  NOTE: if you return a value smaller than the first line in the partition, this 
	 *  function will RETURN the first line in the partition, not the minStuckLine as you 
	 *  might expect.
	 *    
	 * @param searchTree Partitioned set of split lines from a rendering frame.
	 * @param input line that is being looked for in searchTree
	 * @param axis axis that line and search tree belong to
	 * @return the lowest split line that is bounded
	 */
	public static StaticSplitLine getOverlapStaticSplitCell(TreeSet searchTree, StaticSplitLine input, SplitAxis axis)
	{
		StaticSplitLine[] bounds = input.getBounds(axis);
		StaticSplitLine root = (StaticSplitLine)axis.getRoot();
		
		
		return (StaticSplitLine)getOverlapSplitCell(searchTree, input);
	}

	/**
	 * Get the bounds of this split line in the given axis, which are the parent and offparent.
	 * These are used to give relative position of the split line, and an overall absolute position
	 * when computed with all ancestors. 
	 * @return ordered (smaller then larger index) pair of static split lines that 
	 * bounds the movement of this split line (one of which is the parent, other is 
	 * usually off parent or a min/max stuck line)
	 */
	public StaticSplitLine[] getBounds(SplitAxis axis)
	{
		// set up the return values to be root's values
		// if the node isn't the root, at least one of these values will change
		StaticSplitLine[] returnBounds = {
				(StaticSplitLine)axis.getMinStuckLine(),
				(StaticSplitLine)axis.getMaxStuckLine()};
		int[] boundIndices = {-1, axis.getSize()};
		StaticSplitLine currLine = (StaticSplitLine)axis.getRoot();
		while (currLine.splitLineIndex != this.splitLineIndex)
		{
			int index = currLine.splitLineIndex < splitLineIndex ? 1 : 0; 
			boundIndices[index] = currLine.splitLineIndex;
			returnBounds[index] = currLine;
			currLine = currLine.splitLineIndex < splitLineIndex ?
				(StaticSplitLine)currLine.getRightChild() :
				(StaticSplitLine)currLine.getLeftChild();
		}
		return returnBounds;
	}
}
