package net.sourceforge.olduvai.accordiondrawer;


/**
 * 
 * A drawable range represents a range of objects on a given axis.  (Could be X or Y.)  
 * 
 * The min and max are indices into the SplitLine array that represent the beginning
 * and end of the range.  
 * 
 * AbstractRangeList stores multiple DrawableRanges.  
 * 
 * @see net.sourceforge.olduvai.accordiondrawer.AbstractRangeList
 * 
 */
public abstract class DrawableRange implements Comparable {

	/** The minimum split line index of all objects in the range. */
	protected int min;
	/** The maximum split line index of all objects in the range. */
	protected int max;
	/** The drawer for this range. */
	protected AccordionDrawer drawer;
	/** The group that this range is within, if applicable. */
	protected AbstractRangeList group;
	
	/**
	 * Drawer accessor.
	 * @return The drawer for this range, {@link #drawer}.
	 */
	public AccordionDrawer getAD()
	{
		return drawer;
	}
	
	/**
	 * Minimum split line accessor.
	 * @return The minimum split line bound of the range (integer)
	 */
	public int getMin()
	{
		return min;
	}
	/**
	 * Minimum split line mutator.
	 * @param min The new minimum split line bound of the range {@link #min}.
	 */
	public void setMin(int min)
	{
		this.min = min;
	}

	/**
	 * Maximum split line accessor.
	 * @return The maximum split line bound of the range (integer)
	 */
	public int getMax()
	{
		return max;
	}

	/**
	 * Maximum split line mutator.
	 * @param max The new maximum split line bound of the range {@link #max}.
	 */
	public void setMax(int max)
	{
		this.max = max;
	}

	/**
	 * Range length accessor.
	 * @return The length of the range, inclusive of the min and max bounds.
	 */
	public int rangeLength()
	{
		return max - min + 1;
	}

	/**
	 * Equals comparator for application-specific ranges.
	 * @param o The other range for comparison.
	 * @return True when o = this, false otherwise.
	 */
	public abstract boolean equals(Object o);

	/**
	 * Group accessor for the range.
	 * @return Returns the group this range is associated with.
	 */
	public AbstractRangeList getGroup() {
		return group;
	}

	/**
	 * Mutator for {@link #group}, the group for this range.
	 * @param group The group to set.
	 */
	public void setGroup(AbstractRangeList group) {
		this.group = group;
	}

}