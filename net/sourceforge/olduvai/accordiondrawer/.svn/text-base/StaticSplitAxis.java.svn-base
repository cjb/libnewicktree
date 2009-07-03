package net.sourceforge.olduvai.accordiondrawer;

/**
 * Static hook class for static split lines, as those used by TreeJuxtaposer are not dynamic for optimal performance.
 * @author James Slack <jslack@cs.ubc.ca>
 *
 */
public class StaticSplitAxis extends SplitAxis {

	/**
	 * Array of all split lines.
	 */
	private SplitLine[] staticSplitLineArray;

	/**
	 * Create the {@link #staticSplitLineArray}.
	 * @param size Number of split lines needed for {@link #staticSplitLineArray}
	 */
	public void createSplitLineArray(int size)
	{
		staticSplitLineArray = new SplitLine[size];
	}

	/**
	 * Puts a split line at the given position in {@link #staticSplitLineArray}.
	 * @param pos Position, between 0 and the size of the split line.
	 * @param splitLine Split line to insert.  StaticSplitLine objects reference their position in the array.
	 */
	public void addSplitLine(int pos, SplitLine splitLine)
	{
		staticSplitLineArray[pos] = splitLine;
	}
	
	/**
	 * Return the object at the given position, as a proper cell geom (TreeNode in TJ)
	 * @param pos The position of the requested object.
	 * @return The {@link SplitLine#cullingObject} at pos, casted to a {@link CellGeom}.
	 */
	public CellGeom getStaticSplitLineObject(int pos) {
		return (CellGeom)staticSplitLineArray[pos].cullingObject;
	}

	/**
	 * Wrapper constructor for Static Split Axes. Only calls constructor for {@link SplitAxis} with the given parameters.
	 * @param ad Drawer that uses this split axis: {@link #ad}
	 * @param horizontal True if horizontal axis, false for vertical: {@link #horizontal}
	 * @param minStuckValue Position of minimum stuck line: {@link #minStuckLine}
	 * @param maxStuckValue Position of maximum stuck line: {@link #maxStuckLine}
	 * @param initLines Number of split lines to insert into the axis
	 * @param reset True: balance the split line tree after inserting initLines, false: do not balance, use default value for each split line: {@link #defaultSplitValue}
	 * @param staticLines True if using static split lines (TJ), false otherwise: {@link #staticLines}
	 */
	public StaticSplitAxis(AccordionDrawer ad, SplitAxisLogger logger, boolean horizontal, 
			double minStuckValue, double maxStuckValue, int initLines, boolean reset,
			boolean staticLines)
	{
		super(ad, logger, horizontal, minStuckValue, maxStuckValue, initLines, reset, staticLines);
	}
	
}
