package net.sourceforge.olduvai.accordiondrawer;

import java.awt.Color;

import javax.media.opengl.GL;

/**
 * Draw and cache information about an onscreen rubberband box.
 * Because we need to draw exactly this box again to erase it in XOR
 * mode, we must cache the corner points instead of looking them up
 * dynamically.
 * @see GridCell
 * @author Tamara Munzner */

public class InteractionBox {
	
	/** Redeclare X locally */
	private static final int X=AccordionDrawer.X;
	/** Redeclare Y locally */
	private static final int Y=AccordionDrawer.Y;
	
	/** Width of lines used to draw an interaction box */
	private float linewidth = 3.0f;
	/** Outline color of interaction box */
	private Color col;
	/** Drawing plane for the interaction box */
	private float plane;
	
	/** Store where the interaction box drag starts in the interaction box */
	public int dragStart[] = new int[2];
	/** Store where the interaction box drag ends in the interaction box */
	public int dragEnd[] = new int[2];
	/** Store where the previous interaction box drag ended in the interaction box */
	public int oldDragEnd[] = new int[2];
	/** Store where the interaction box drag starts/ends in the interaction box */
	public int originalMovePixelPosition[] = new int[2];
	
	/** Stuck split lines when starting drag */
	public SplitLine stuckLines[] = new SplitLine[2];
	
	/** Moving split lines set when starting drag */
	public SplitLine moveLines[] = new SplitLine[2];
	
	/** Bounding box min lines, later assigned to either {@link #stuckLines} or {@link #moveLines} after drag starts. */
	private SplitLine[] minLine = new SplitLine[2];
	/** Bounding box max lines, later assigned to either {@link #stuckLines} or {@link #moveLines} after drag starts. */
	private SplitLine[] maxLine = new SplitLine[2];
	
	/** The item that is contained in the interaction box, if applicable */
	public CellGeom item;
	/** The drawer for this interaction box. */
	private AccordionDrawer d;
	
	/**
	 * Creates an interaction box with the given split lines, data object, and drawer
	 * @param minSplit Minimum splits in {@link #X} and {@link #Y} for this box
	 * @param maxSplit Maximum splits in {@link #X} and {@link #Y} for this box
	 * @param item Item that is attached to this interaction box, not required
	 * @param d Drawer for this interaction box
	 */
	public InteractionBox(SplitLine minSplit[], SplitLine maxSplit[], CellGeom item, AccordionDrawer d) {
		this.minLine[X] = minSplit[X];
		this.minLine[Y] = minSplit[Y];
		this.maxLine[X] = maxSplit[X];
		this.maxLine[Y] = maxSplit[Y];
		this.d = d;
		this.dragStart[X] = this.dragStart[Y] = -1; // init
		this.item = item;
	}
	
	/**
	 * Sets the start and end drag point variables.  This happens after an interaction box is created, on the first drag.
	 * First detect which split lines are moving and which are stuck ({@link #moveLines}, {@link #stuckLines})
	 * @param dragStart Determines which split line is moving and which is stuck, sets starting position of current drag. 
	 * @param dragEnd Updates the ending position of the current drag.
	 */
	public void setDragPoints(int[] dragStart, int[] dragEnd)
	{
		int frameNum = d.getFrameNum();
		for (int xy = X; xy <= Y; xy++)
		{
			if (this.dragStart[xy] == -1) 
			{ // first time only, initialize moving and stuck lines
				this.dragStart[xy] = dragStart[xy];
				if (isMinLineMoving(dragStart[xy], xy))
				{
					moveLines[xy] = minLine[xy];
					stuckLines[xy] = maxLine[xy];
				}
				else
				{
					moveLines[xy] = maxLine[xy];
					stuckLines[xy] = minLine[xy];
				}
				originalMovePixelPosition[xy] = d.w2s(d.splitAxis[xy].getAbsoluteValue(moveLines[xy], frameNum), xy);
			}
			// each time update the end drag point
			this.dragEnd[xy] = dragEnd[xy];
		}
	}
	
	/**
	 * Updates {@link #oldDragEnd} with the previous value of {@link #dragEnd}, and {@link #dragEnd} with the new value passed in.
	 * Essentially this is {@link #setDragPoints(int[], int[])} but ignoring the drag start, which happens if the start is set to something other than -1.
	 * @param dragEnd The new value for {@link #dragEnd}
	 */
	public void updateDrag(int[] dragEnd)
	{
		for (int xy = X; xy <= Y; xy++)
		{
			oldDragEnd[xy] = this.dragEnd[xy];
			this.dragEnd[xy] = dragEnd[xy];
		}
	}
	
	/**
	 * Determines if dragPosition (the starting drag value) is closer to min or max.
	 * If closer to min, the min line is moving, otherwise the min line will be stuck.
	 * @param dragPosition The position of the cursor to check for proximity to the min or max box line
	 * @param xy The direction of the cursor being checked
	 * @return True when dragPosition is closer to {@link #getMin(int)} than {@link #getMax(int)}
	 */
	private boolean isMinLineMoving(int dragPosition, int xy)
	{
		return (getMax(xy) + getMin(xy))/2 >= dragPosition; 
		// midpoint of box is bigger than dragEnd, so when mid is closer to min, return true
	}

	/**
	 * Set the color, plane height and line width, then draw the rubber band (interaction box outer ring) with those properties.
	 * @param col Drawing color, XOR to the actual color (all {@link #drawRubberband()} painting is XOR draw to be reversable on subsequent draws)
	 * @param linewidth Line width, in pixels
	 * @param plane Height of the drawing plane for the interaction box
	 */
	public void draw(Color col, float linewidth, float plane) {
		this.col = col;
		this.linewidth = linewidth;
		this.plane = plane;
		drawRubberband();
	}
	
	/**
	 * Similar to {@link #draw(Color, float, float)}, but only draw the rubber band with {@link #drawRubberband()} and the same set parameters, to undraw the previous interaction box.
	 *
	 */
	public void undraw(){
		drawRubberband();
	}
	
	/**
	 * Get the screen space position of the minimum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The current screen position of {@link #minLine} in X or Y.
	 */
	public double getMinAbs(int xy)
	{
		return d.splitAxis[xy].getAbsoluteValue(minLine[xy], d.getFrameNum());
	}
	/**
	 * Get the screen space position of the maximum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The current screen position of {@link #maxLine} in X or Y.
	 */
	public double getMaxAbs(int xy)
	{
		return d.splitAxis[xy].getAbsoluteValue(maxLine[xy], d.getFrameNum());
	}

	/**
	 * Get the actual minimum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The minimum split line in X or Y.
	 */
	public SplitLine getMinLine(int xy)
	{
		return minLine[xy];
	}
	/**
	 * Get the actual maximum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The maximum split line in X or Y.
	 */
	public SplitLine getMaxLine(int xy)
	{
		return maxLine[xy];
	}
	/**
	 * Return the index of the minimum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The index of the minimum split line, determined by {@link SplitAxis#getSplitIndex(SplitLine)}.
	 */
	public int getMinIndex(int xy) { 
		return d.splitAxis[xy].getSplitIndex(minLine[xy]);
	}
	/**
	 * Return the index of the maximum split line for a given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The index of the minimum split line, determined by {@link SplitAxis#getSplitIndex(SplitLine)}.
	 */
	public int getMaxIndex(int xy) { 
		return d.splitAxis[xy].getSplitIndex(maxLine[xy]);
	}
	

	/**
	 * Returns the pixel-position of the minimum split line in the given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The screen position of the minimum split line, in pixels.
	 */
	public int getMin(int xy)
	{
		return d.w2s(getMinAbs(xy), xy);
	}
	/**
	 * Returns the pixel-position of the maximum split line in the given dimension.
	 * @param xy Either {@link #X} or {@link #Y}.
	 * @return The screen position of the maximum split line, in pixels.
	 */	
	public int getMax(int xy)
	{
		return d.w2s(getMaxAbs(xy), xy);
	}
	
	/**
	 * Rendering function for interaction boxes.
	 * Draw everything according to the set parameters, drawing and undrawing should return to initial dataset view.
	 * Draw a line loop with a center dot.
	 *
	 */
	public void drawRubberband() {
		GL gl = d.getCanvas().getGL();
		this.col = col != null ? col : Color.getHSBColor(120f/360f,.0f,0.5f);
		int x = 0, y = 1;
		
		// set rendering in frontbuffer if you are
		// still in doublebuffer mode
		// don't set it to backbuffer
		// this is only for the startframe
		
		// enable states		
		gl.glEnable(GL.GL_COLOR_LOGIC_OP);
		gl.glLogicOp(GL.GL_XOR);
		
		double min[] = new double[2];
		double max[] = new double[2];
		
		double center[] = new double[2];
		for (int xy = X; xy <= Y; xy++)
		{
			min[xy] = getMinAbs(xy);
			max[xy] = getMaxAbs(xy);
			center[xy] = (int)((max[xy]+min[xy])/2.0);
		}
		
		float thecol[] = new float[3];
		col.getRGBColorComponents(thecol);
		gl.glColor3f(thecol[0], thecol[1], thecol[2]);
		
		final double halfPixel[] = { d.s2w(.5, X), d.s2w(.5, Y) };
		
		gl.glLineWidth(linewidth);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3d(min[x] + halfPixel[X], max[y] - halfPixel[Y], plane);
			gl.glVertex3d(max[x] - halfPixel[X], max[y] - halfPixel[Y], plane);
			gl.glVertex3d(max[x] - halfPixel[X], min[y] + halfPixel[Y], plane);
			gl.glVertex3d(min[x] + halfPixel[X], min[y] + halfPixel[Y], plane);
			gl.glEnd();
			gl.glPointSize(linewidth+1f);
			gl.glBegin(GL.GL_POINTS);
			gl.glVertex3d(center[x], center[y], plane);
			gl.glEnd();
		// disable states
		gl.glDisable(GL.GL_COLOR_LOGIC_OP);
	}
	
	/**
	 * Return the string representation of this interaction box.
	 * @return (minX maxX minY maxY)
	 */
	public String toString()
	{
		return "minX:" + minLine[X] + " maxX:" + maxLine[X] + " minY" + minLine[Y] + " maxY:" + maxLine[Y];
	}
		
};
