package net.sourceforge.olduvai.accordiondrawer;


/**
 * Axis-aligned bounding box class for label drawing. Stores
 * information in screen coordintes, checks for overlap with other
 * bounding boxes.
 * 
 * @author  Tamara Munzner, James Slack
 */

public class LabelBox implements Comparable{

	/** Bottom left coordinates, in pixels */
    protected int bl[] = new int[2];
    /** Top right coordinates, in pixels */
    protected int tr[] = new int[2];
    /** Bottom left corner of the label box, shifted by the label buffer distance (the "real" bottom left corner) */
    protected int blbg[] = new int[2];
    /** Center of the label box, between {@link #bl} and {@link #tr} */
    protected int center[] = new int[2];
    /** The size of the label box, from {@link #bl} to {@link #tr} */
    protected int extent[] = new int[2];
    /** Last frame in which this box is drawn */
    protected int frameNum;
    /** The string to draw as the label */
    private String name;
    /** Attached object for this label, if applicable. */
    private CellGeom attachedObject;
    /** Font height in points */
    protected int fontHeight;

    /**
     * Label box constructor.
     * @param bottomLeft Bottom left corner of actual label box, not including buffers.
     * @param topRight Top right corner of actual label box, not including buffers.
     * @param bottomLeftBG Bottom left corner of buffer sounding label box.
     * @param computedFrame Current frame for this label box.
     * @param name String to be drawn in as the label.
     * @param attachedObject The object attached to this label, if any.
     * @param fontHeight Height of font used to draw string in points.
     */
    public LabelBox(int bottomLeft[], int topRight[], int bottomLeftBG[], int computedFrame, String name, CellGeom attachedObject,
    		int fontHeight)
    {
    	for (int xy = 0; xy < 2; xy++)
    	{
    		this.bl[xy] = bottomLeft[xy];
    		this.tr[xy] = topRight[xy];
    		this.blbg[xy] = bottomLeftBG[xy];
    		extent[xy] = size(xy);
    		center[xy] = this.bl[xy]+extent[xy]/2;
    	}
    	this.name = name;
    	this.frameNum = computedFrame;
    	this.attachedObject = attachedObject;
    	this.fontHeight = fontHeight;
    }
	
    /**
     * Get the object that is attached to this label box.
     * @return The cell geom stored in {@link #attachedObject}
     */
	public CellGeom getAttachedObject()
	{
		return attachedObject;
	}

	/**
     * Compare label boxes with another for vertical overlap (0 = overlap).
     * @param o Label box to test for overlaps.
     * @return 0 if overlap, 1 if o is above this, -1 if o is below this
     */
    public int compareTo(Object o) {
    	LabelBox other = (LabelBox)o;
    	if ((center[1] + extent[1]/2 >= other.center[1] - other.extent[1]/2)  ||
    	    (center[1] - extent[1]/2 <= other.center[1] + other.extent[1]/2)) // y overlap only??
    		return 0;
    	else if (center[1] < other.center[1]) // sort on Y center
    		return -1;
    	return 1;
    }
	
    /**
     * Return a coordinate (in screen space) for the bottom or left edge of this box.
     * @param xy Either {@link AccordionDrawer#X} for left edge or {@link AccordionDrawer#Y} for bottom edge.
     * @return The coordinate for the given edge in screen space.
     */
	public int bottomLeftPos(int xy)
	{
		return blbg[xy];
	}
	
    /**
     * Return a coordinate (in screen space) for the top or right edge of this box.
     * @param xy Either {@link AccordionDrawer#X} for right edge or {@link AccordionDrawer#Y} for top edge.
     * @return The coordinate for the given edge in screen space.
     */    
	public int topRightPos(int xy)
	{
		return tr[xy];
	}

	/**
	 * Accessor for the name (label) contained in this label box.
	 * @return Current value of {@link #name}.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the size of the label box, in the given dimension.
	 * @param xy The dimension, either {@link AccordionDrawer#X} or {@link AccordionDrawer#Y}.
	 * @return The absolute value of the size of this label box, in X or Y.
	 */
	public int size(int xy)
	{
		if (tr[xy] > blbg[xy])
			return tr[xy] - blbg[xy];
		return blbg[xy] - tr[xy];
	}

	/**
	 * Accessor for font height value for this label box.
	 * @return Current value of {@link #fontHeight}.
	 */
	public int getFontHeight()
	{
		return fontHeight;
	}
	
	/**
	 * Test for box overlap with another Label box.
	 * @param other A second Label box to test for overlaps.
	 * @return True if boxes overlap, with respect to {@link #blbg} and {@link #tr} values of each
	 */
	public boolean overlaps(LabelBox other, int[] bufferSize)
	{
		int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
		return (!  // NEITHER
			(blbg[X] > other.tr[X]+bufferSize[X] || // left is right of other right is not an overlap 
			other.blbg[X] > tr[X]+bufferSize[X]) // other case similar
			&& // AND for both horizontal and vertical overlap (necessary for 2D overlap)
			!  // NEITHER
			(blbg[Y]+bufferSize[Y] < other.tr[Y] || // bottom is above other top is not an overlap
			other.blbg[Y]+bufferSize[Y] < tr[Y])); // other case similar
	}

};

