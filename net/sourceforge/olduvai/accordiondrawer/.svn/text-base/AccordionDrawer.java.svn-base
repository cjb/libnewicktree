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
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

import com.sun.opengl.util.Screenshot;


/**
 * An abstract class representing a two dimensional rectangular grid on which
 * any geometric shape can be drawn.  The geometric shapes use the grid as reference
 * points, and user-controlled deformations on the grid deform the shapes accordingly.
 * Some developed applications that render on the deformable grid include:
 *              Trees: {@link net.sourceforge.olduvai.treejuxtaposer.TreeJuxtaposer}
 *              Sequences: {@link net.sourceforge.olduvai.sequencejuxtaposer.SequenceJuxtaposer}
 *              Network monitoring: {@link net.sourceforge.olduvai.lrac.LiveRAC}
 *
 * @author  Tamara Munzner, Serdar Tasiran, James Slack
 * @version 2.1
 */

public abstract class AccordionDrawer implements MouseListener, MouseMotionListener, KeyListener, GLEventListener {

	/** Set flag to true to display debug output, false for interactive performance or releases. */
	public static final boolean debugOutput = false;

	/** The drawing surface object, a 1-to-1 relationship between {@link #canvas} and this AccordionDrawer */
	protected GLCanvas canvas;

	/** Default font family (concatenate string with other font properties) to use for all labels. */
	public static final String DEFAULTFONTFAMILY = "Arial";
	/** Default title font size, in points. */
	public static final float DEFAULTTITLEFONTSIZE = 14;
	/** Default title font type, using bolded fonts in the default title size. */
	public static final Font DEFAULTTITLEFONT = Font.decode(DEFAULTFONTFAMILY + "-BOLD").deriveFont(DEFAULTTITLEFONTSIZE);
	/** Default label font size, in points. */
	public static final float DEFAULTLABELFONTSIZE = 12;
	/** Default title font type, using normal fonts in the default label size. */
	public static final Font DEFAULTLABELFONT = Font.decode(DEFAULTFONTFAMILY).deriveFont(DEFAULTLABELFONTSIZE);
	/** Font wrapper (to jFTGL library) object for drawing labels. */
	public FontWrapper bff;
	/** Flag to check for an already initialized font object (the first AccordionDrawer will initialize fonts for subsequent drawers) */
	protected boolean fontInitialized = false;
	/**
	 * Flags to set initial distribution of split lines ({@link #X}, {@link #Y}).  True = split lines are distributed
	 * uniformly between all objects (such as tree and sequence grid cells).  False = split
	 * lines are distributed according to application-specific parameters (such as BACJ
	 * positioning of sequence segments of different lengths and starting positions).
	 */
	protected boolean[] uniformSplits = {true, true};
	/**
	 * Retrieve the font family wrapper object to render text.
	 * @return the font family wrapper object
	 */
	public FontWrapper getBFF() { 
		return bff;
	}
	/**
	 * Get the maximum descent for the family of fonts for the given height.  The descent
	 * is the distance a character needs below the line (for characters like 'g', 'y' or 'j').
	 * @param text String used to compute the maximum descent 
	 * @param f the font (type, size, properties in the Font object)
	 * @return Largest descent for any character in the font family in pixels.
	 */
	public int getDescent(String text, Font f) {
		return bff.getDescent(text, f);
	}
	/**
	 * Get the width of a string in pixels.
	 * @param name String to get width for.
	 * @param f the font (type, size, properties in the Font object)
	 * @return Width of string in pixels.
	 */
	public int stringWidth(String name, Font f) {
		final int width = bff.stringWidth(name, f);
		return width;
	}
	/** Initialize variable font family wrapper. */
	public void initializeFont() {
//		System.out.println("Initialize font");
		bff = new FontWrapper(this);
		fontInitialized = true;
	}

	/** The split axis objects in the {@link #X} and {@link #Y} directions. */
	protected SplitAxis[] splitAxis = new SplitAxis[2];

	/** The drawing queue.  Objects to be drawn are placed in here by {@link #seedQueue()} and used by {@link #drawFrame()}.  */
	protected Vector ToDrawQ;
	/**
	 * Set initially to false, true when data is done loading and {@link #canvas} is ready to draw.  
	 * Prevents early interactions (or other draw commands) causing drawing events on slow loading datasets.
	 */
	public static boolean loaded = false; // don't draw until the data is loaded

	/** True when drawing a frame for the first time, set when in {@link #startNewFrame()} */
	protected boolean startFrame = false;
	/** True when the current drawing continues a previously unfinished frame, set in {@link #continueFrame()} */
	protected boolean continueFrame = false;

	/** Size of the window (in pixels) to draw {@link #canvas} in, for {@link #X} and {@link #Y} directions  */ 
	protected int winsize[] = new int[2];

	/** Start time for any new rendering pass, refreshed in {@link #startNewFrame()} and {@link #continueFrame()} */
	protected long dynamicStart;
	/** Start time for first drawing frame, set in {@link #startNewFrame()}*/
	protected long continueStart;
	/** Dynamic time frame that specifies minimum time spent (in ms) for a rendering pass, 
	 * set to 30 milliseconds for most applications. */
	protected long dynamicTime;
	/** Refreshed during a rendering pass in {@link #drawFrame()}, used to check time spent rendering. */
	protected long now;
	/** Frame number, updated after each scene redraw and any reshaping command. */
	protected int frameNum;

	/** Color of the background, used by {@link #clear()}. */
	static protected Color backgroundColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);
	/** Foreground color of label text. */
	protected Color labelColor;
	/** Background color of label text. */
	protected Color labelBackColor;
	/** Foreground color of highlighted label text. */
	protected Color labelHiColor;
	/** Background color of highlighted label text. */
	protected Color labelBackHiColor;
	/** XOR color mask for rubberband interaction box. */
	protected Color rubberbandColor;

	// make sure that highlighted stuff is always visible, even it
	// might otherwise overlap with nonhighlighted stuff: always draw
	// it in front.

	/** Lowest plane for drawing, everything below clipped */
	protected float backplane;
	/** Above {@link #objplane}, below {@link #interactionplane}, for showing regions of guaranteed visibility.
	 * For applications with several highlighting colors (such as groups in TJ), groups are on different planes, based on
	 * their current priority (highest priority on top of all others). Flash text (mouse-over) is above marked groups,
	 * which are in turn above automated guaranteed visibility marks (marked differences between trees in TJ, for example). */
	protected float hiliteplane;
	/** Above {@link #labelbgplane}, below {@link #interactionplane}, for drawing actual label text */
	protected float labelplane;
	/** Above {@link #hiliteplane}, below {@link #labelplane}, for drawing label backgrounds, 
	 * where the label text is drawn 4 times (offset vertically and horizontally by a small number of pixels) in the label background color */
	protected float labelbgplane;
	/** Same as {@link #backplane}, the plane on which the deformable grid is drawn (default not drawn) */
	protected float gridplane;
	/** Above {@link #gridplane}, below {@link #labelbgplane}, the plane on which we draw geometric objects, but not the text or highlighting layers */
	protected float objplane;
	/** Highest plane for drawing, above {@link #labelplane}, for performing interactions and mouse-over label drawing */
	protected float interactionplane;
	/** Flag for background drawing */
	public boolean drawBackground;

	// TODO: find out if the focus cell is useful anymore
	// previously used to indicate location of cursor movements (arrow keys, not mouse pointer)
	// default cell is currently set to null and never changed
	public GridCell focusCell;
	public GridCell defaultFocusCell;

	/** The set of labels that we have drawn, checked by drawing functions to prevent overlaps.
	 * This arrayList stores {@link LabelBox} objects. */
	protected ArrayList drawnLabels = new ArrayList();

	/** The percentage of screen area guaranteed for smooshed things between pairs of growing regions. */
	public double minContextInside = .01; 
	/** The percentage of screen area guaranteed for smooshed things between the boundaries and nearest growing region. */
	public double minContextPeriphery = .01; 

	/** Constant for X (horizontal grid direction) */
	public final static int X=0;
	/** Constant for Y (vertical grid direction) */
	public final static int Y=1;

	/** Table of {@link SplitTransition}, which store information about {@link SplitLine} movements that are in progress. */
	public Hashtable toMove;

	/** True: double buffering is active, draw on back buffer and swap to front.  False: only draw on front.
	 * This is the initial state of {@link #doublebuffer}.*/
	public final boolean doDoubleBuffer = true; 

	/** State of progressive rendering.  True: NO progressive rendering, each frame is drawn before next transition in movement queue is processed.
	 * False: DO progressive rendering by default, frames are drawn until the frame time limit (default 30ms) expires, when a new movement will cause a complete redraw. */
	public boolean ignoreProgressive = false;

	/** State of the double buffer state of GL, which is turned off during some mouse-over flash actions.
	 * The over-all state of double buffering for normal drawing is kept in {@link #doDoubleBuffer}.*/
	protected boolean doublebuffer;

	/** State function to determine if more drawing is required for the current frame.  True when number
	 * of objects in {@link #ToDrawQ} > 0.*/
	public boolean keepDrawing() {
		return !ToDrawQ.isEmpty();
	}
	/** State function to determine if there are more navigations after the current frame.  True when number
	 * of objects in {@link #toMove} > 0.*/
	public boolean keepMoving() {
		return !toMove.isEmpty();
	}

	/** Flag turned on when user wants a snapshot of the current scene.  Turned off (false) after written.
	 * The file name is the window title (e.g. tree file names, replacing spaces with underscores), an incrementing
	 * number, and the {@link #snapShotExtension}, {@value #snapShotExtension}.*/
	public boolean takeSnapshot;
	/** Extension used for postscipt output figures. */
	public final static String snapShotExtension = ".eps";
	/** Global snapshot name that is derived from one AD object.  Names end with {@link #snapShotExtension}. */
	public static String snapshotName = "";
	/** Snapshot writer object, one for each AD, created when snapshot function ({@link #doSnapshot(ArrayList)}) is run. */
	public BufferedWriter snapShotWriter = null;

	/** Assembles a snapshot name to write a postscript figure that looks like the current scene.
	 * The result is written to the current state of {@link #snapshotName}.
	 * @param base Starting base name before the file name is appended to {@link #snapshotName}.
	 * @param directory Location to save the file.
	 * */
	public void newSnapshotName(String base, File directory)
	{
		if (directory == null)
			directory = new File(System.getProperty("user.home"));

		base = base.replace(' ', '_').replace(':', '.');
		snapshotName = directory.getAbsolutePath() + "/" + base + snapShotExtension;
		boolean success = false;
		int incrementNumber = 0;
		BufferedReader br = null;
		while (!success)
		{
			try
			{
				br = new BufferedReader( new FileReader( snapshotName ) );
			}
			catch (FileNotFoundException fnfe)
			{
				success = true;
			}
			if (!success)
			{
				try
				{
					br.close();
				}
				catch (IOException ioe)
				{
					System.out.println("Error: IOException trying to close a known open file: " + snapshotName);
				}
				snapshotName = directory.getAbsolutePath() + "/" + base + (incrementNumber++) + snapShotExtension;
			}
		}
		try
		{
			FileWriter fw = new FileWriter(snapshotName);
			snapShotWriter = new BufferedWriter( fw );
			System.out.println("Saved screen image as: " + snapshotName);
		}
		catch (IOException ioe)
		{
			System.out.println("Error: IOException trying to open a file for writing: " + snapshotName);
		}

	}


	/** Width of flash box border, in pixels. Defaults to 1 for mouse-over bounding boxes, but set to 3 or higher for during a rectilinear reshape. */
	protected float flashBoxWidth;
	/** Flag to request drawing the interaction box ({@link #flashBox}) in the drawer.  Typically defaults to false with no interaction box, but set after user selects a deformation region. */
	protected boolean doBox;
	/** The current interaction box being stetched, or selected through a mouse-over. */
	public InteractionBox flashBox;
	/** The previous interaction box that is being replaced by {@link #flashBox}.  Used to replace information kept in a pixel buffer since subsequent {@link #doFlash()} calls can not reference previously drawn flash state. */
	protected InteractionBox flashBoxOld;
	/** A temporary {@link InteractionBox} used to replace the flash box before a {@link #doFlash()} call. TODO: might be possible to make this a local variable or combine with {@link #flashBox}?*/
	public InteractionBox baseBox;
	/** A geometry that represents the currently drawn flash object (single object that is selected by a mouse-over; could be a composite/aggregate object, but it is drawn as a single entity). */
	protected CellGeom flashGeom;
	/** The geometry for the previously selected flash object (see {@link #flashGeom}, {@link #flashBox}, and {@link #flashBoxOld}). */
	protected CellGeom flashGeomOld;
	/** The color used to represent a mouse cursor, a geometry is highlighted with this special color (defaults to orange). */
	protected Color flashCol;
	/** Mouse cursor X position during a flash draw.  Used to place the pop-up label. */
	protected int flashX;
	/** Mouse cursor Y position during a flash draw.  Used to place the pop-up label. */
	protected int flashY;
	/** Old mouse cursor X position from previous flash draw.  Used to remove the previous pop-up label. */
	protected int flashXOld;
	/** Old mouse cursor Y position from previous flash draw.  Used to remove the previous pop-up label. */
	protected int flashYOld;

	/** Flag to draw a background grid, with one line from min {@link SplitAxis} to max SplitAxis.  Defaults to off, useful to show generic navigation algorithm when geometry is turned off (see {@link #drawGeoms})*/
	public boolean drawGrid = false;
	/** Flag to draw the geometries, which is default true (do draw geometries for data objects). Turn this off and {@link #drawGrid} on to see the generic navigation algorithm on a small dataset. */
	public boolean drawGeoms = true;

	/** Flag to turn flash drawing on (default, true), or off (false). */
	public boolean noflash;
	/** Flag to turn automated difference marking on (default, true), or off (false). */
	public boolean showdiffs;
	/** Thickness of lines used in geometric object drawing, currently only used in TJ, where marked nodes are (width+2). */
	private int linethickness;
	/** Flag to turn label drawing on (default, true), or off (false). */
	public boolean drawlabels;
	/** (currently only TJ) Flag to set position of labels (non-mouseover).  True: labels drawn adjacent to node junctions (default), False: labels drawn left of geometry. */
	public boolean labelposright;
	/** Flag set to draw the background (filled bounding box) for non-mouseover labels.*/
	public boolean labeldrawback;
	/** (currently only TJ) Flag to draw marked colors dimmer (less saturated).  Defaults to false where marks are a constant color, rather than following the saturation rules of unmarked (see {@link #dimbrite}). */
	public boolean dimcolors;
	/** (currently only TJ) Flag to draw unmarked nodes dimmer (less saturated for visual depth cues).  Defaults to true, where nodes change from black (close to root) to light grey (close to leaves). */
	public boolean dimbrite;
	/** (currently only TJ) Linked navigation flag, defaults to true, where certain navigations (subtree stretching) are linked between drawers.  This does not affect marked group stretching as this is done for free if linked highlighting is on. */
	public boolean linkednav;

	/** Flag to indicate that we are currently dumping stats (i.e. time to render) to {@link #stats}.*/
	public boolean dumpstats;

	/** State flag to indicate that a mouse drag is in progress. */
	protected boolean mouseDragging = false;

	/** State flag to indicate that we are currently requesting a flash draw, not a full redraw (used by {@link #requestRedraw()} to avoid extra {@link #drawFrame()}).*/
	protected boolean doingFlash;

	/**
	 * Set to true to force a full draw ignoring toDraw() and toMove() queues (queues should be cleared by {@link #forceRedraw()}). 
	 * This is set by {@link #forceRedraw()} (As opposed to requestRedraw())
	 * 
	 * @see #requestRedraw()
	 * @see #forceRedraw()
	 */
	protected boolean forceRedraw;

	/** The number of animations steps for navigation transitions.  Set to 1 for jump cuts.  Must be > 0. */
	protected int numAnimSteps; // in an animated transition
	/** The number of animation steps to use for mouse animations.  Set > 1 to smoothen mouse navigation at cost of rendering extra frames.  Must be > 0. */
	protected int mouseMoveAnimSteps; // mouse steps
	/** The number of animation steps to use for mouse animations, for normal, full-frame rendering (non-progressive rendering).  Set > 1 to smoothen mouse navigation at cost of rendering extra frames.  Must be > 0. */
	protected int mouseMoveAnimStepsNPR; // mouse steps, non-progressive rendering
	/** Percentage to add for an inflate (stretch), or subtract for deflate (squish); used to determine how much to grow each region, this value is split between resizing regions depending on their initial relative sizes. */
	protected double inflateIncr;

	/** AKA block-size, this state is the number of pixels (could be fractional, in screen coordinates) to minimally descend to for a partition (see {@link SplitAxis} descent methods). 
	 * By default set to 1 for TJ, 5 for SJ (see initialBlockSize static final in each application). */
	protected double minCellDims[] = new double[2];

	/** Single integer key for applications that may have more than one drawer object (TJ), for array lookups and identification. */
	protected int key;

	/** Statistics (rendering time for different setups) buffered writer, currently not enabled as most original (pre 2.0) techniques for drawing are deprecated.  TODO: reimplement for current interesting test cases?*/
	protected PrintWriter stats;

	/** Debugging function: print the list of calling functions (not including this one)
 	stackStart <= stackEnd, both which equal the depth of the stack to print
	 * 
	 * @param stackStart 0 = function that called this directly, +1 for each function in the call stack
	 * @param stackEnd Last call stack function to display, 0 will print only the function that called this (needs to be >= stackStart) 
	 */
	public void whoCalledMe(int stackStart, int stackEnd)
	{
		try
		{
			throw new Exception();
		}
		catch (Exception e)
		{
			StackTraceElement[] stackList = e.getStackTrace();
			for (int i = stackStart+1; i <= stackEnd+1; i++)
				System.out.println("Stack " + i + ": " + stackList[i]);
		}
	}

	/** Debugging value used to compute efficiency of drawing algorithm, incremented once per geometric object, reset to 0 at start of each frame. */
	protected int numCellsDrawnThisFrame;

	/**
	 * Main constructor for Accordion Drawer objects.  Requires canvas dimensions from the application.
	 * This function creates the canvas object, initializes state for generic accordion drawer operations,
	 * initializes the font object for all drawers (if this is the first AD for applications with many
	 * drawers), and starts event listeners.
	 * @param w the width of the new canvas object, in pixels
	 * @param h the height of the new canvas object, in pixels
	 */
	public AccordionDrawer(int w, int h) {
		canvas = new GLCanvas();
		canvas.setSize(w, h);
		if (AccordionDrawer.debugOutput)
			System.out.println("new accordion drawer: width: " + w +" height: " + h);
		winsize[0] = w;
		winsize[1] = h;


		// State
		linethickness = 1;
		flashBoxWidth = 1;
		numAnimSteps = 10;
		inflateIncr = 0.15;
		gridplane = -1.0f;
		objplane = -.5f;
		hiliteplane = -.3f;
		labelbgplane = -.25f; 
		labelplane = -.2f; 
		interactionplane = objplane -.1f;
		frameNum = 0;
		minCellDims[X] = 1;
		minCellDims[Y] = 1;
		labelColor = new Color(0.0f, 1.0f, 0.0f, 0.0f);
		labelBackColor = new Color(0.2f, 0.2f, 0.2f, 0.0f);
		labelHiColor = new Color(0.2f, 0.2f, 0.2f, 0.0f);
		labelBackHiColor = new Color(0.0f, 1.0f, 0.0f, 0.0f);
		dynamicTime = 30;
		drawGrid = false;
		noflash = false;
		showdiffs = true;
		drawlabels = true;
		labelposright = true;
		labeldrawback = true; // draw the background box of a label
		dimcolors = false;
		dimbrite = true;
		linkednav = true;
		doingFlash = false;
		mouseDragging = false;

		canvas.addGLEventListener(this);

		if (!fontInitialized) {
			initializeFont();
		}

		ToDrawQ = new Vector();

		// Listeners
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		toMove = new Hashtable();
	}

	/**
	 * Sets the size of a block, in pixels.  A block is the limiting size in partitioning a split axis
	 * prior to rendering.  Each partitioned region will be smaller than the block size, if not a
	 * single region between adjacent {@link SplitLine} objects.
	 * See {@link SplitAxis#makePixelRanges(int)}.
	 * @param newSize The new block size, in pixels, can be fractional.
	 * @param y The axis dimension for the new pixel size.  Typically pass {@link #X} or {@link #Y}) which have static values.
	 */
	public void newPixelDiv(double newSize, int y)
	{
		minCellDims[y] = newSize;
	}

	/**
	 * Returns the size of a block in world ([0,1], fractional) space size.  Uses {@link #minCellDims} in the given dimension.
	 * @param y Dimension of interest, typically {@link #X} or {@link #Y}.
	 * @return size of a block in the given dimension.  For example, in a canvas height of 200 pixels, block size of 1, dimension Y, the block size is 1/200 or 0.005 
	 */
	public double getPixelSize(int y)
	{
		double returnValue = s2w(minCellDims[y], y);
		if (returnValue <= 0)
			System.out.println("Error: pixel size is non-positive: " + returnValue);
		return returnValue;
	}

	/**
	 * Attempts to stop all animated transitions in this accordion drawer by calling {@link SplitTransition#end()}]
	 * on each transition object in the movement queue, {@link #toMove}.  Transitions are also removed from the queue after the call.
	 */
	public void endAllTransitions()
	{
		if (debugOutput)
			System.out.println(" (eot) ending transitions: " + toMove.size());
		Enumeration enumer = toMove.elements();
		while (enumer.hasMoreElements())
		{
			SplitTransition st = (SplitTransition)enumer.nextElement();
			st.end();
			toMove.remove(st);
		}
		toMove.clear();
		if (debugOutput)
			System.out.println(" (EOT) ended transitions: " + toMove.size());
	}
	

	/**
	 * Initialize the split lines for a two dimensional accordion drawing object.  See {@link SplitAxis#SplitAxis(AccordionDrawer, boolean, double, double, int, boolean, boolean)}, which handles similar inputs for each horizonal and vertical split axis.
	 * @param reset True: assign world-space, uniformly spaced, positions to all new split lines.  False: no initialization for split line positions, useful for applications with non-uniform grid layouts (datasets such as gene positions within sequences that have set relative positions)
	 * @param staticLines True: use methods that only allow static datasets, assumes no new split lines.  False: use dynamic methods, new split lines are placed with relative positions between existing lines. 
	 * @param Xsize The number of split lines to initially (or permanently in static layouts) allocate in the horizontal direction.
	 * @param Ysize The number of split lines to initially (or permanently in static layouts) allocate in the vertical direction.
	 */
	public void initSplitLines(boolean reset, boolean staticLines, int Xsize, int Ysize) {
		splitAxis[X] = new SplitAxis(this, null, true, SplitAxis.defaultMinStuckValue, SplitAxis.defaultMaxStuckValue,Xsize, reset, staticLines); // horizontal splitline
		splitAxis[Y] = new SplitAxis(this, null, false, SplitAxis.defaultMinStuckValue, SplitAxis.defaultMaxStuckValue, Ysize, reset, staticLines); // vertical splitline
	}


	/**
	 * Clear all the data structures initiated in AD, called when tree(s) get deleted from AD.  This function was initially created when AD objects were
	 * going to be reused (i.e. a new tree loaded into the drawer), but now we create new drawers with new trees.
	 */   
	public void shutdown() {				
	}

	/**
	 * Drawer delete function, which calls the {@link #shutdown()} function to clean up state.  The shutdown
	 * function is not necessary when only one drawer is used, or when we finalize a drawer (the drawer state is
	 * destroyed after the finalize, so why is shutdown needed?)
	 */
	protected void finalize() throws Throwable {		 
		try {
			shutdown();
		}
		finally {
		}
	}

	/**
	 * Sets the unique integer identifier for this drawer.  Useful when using many drawers in the same application.
	 * @param i The unique identifier for this drawer.
	 */
	public void setKey(int i) { key = i;}
	/**
	 * Returns the unique integer identifier for this drawer.
	 * @return The unique identifier for this drawer.
	 */
	public int getKey() { return key;}

	/**
	 * Retrieves the GL context for this accordion drawer.
	 * This should be called each time a drawing method is required, do not store the GL object as state.
	 * @return GL context handle for the {@link #canvas}.
	 */
	public GL getGL() {
		GL gl = canvas.getGL();
		return gl;
	}

	/**
	 * JOGL-called initialization function that sets up fonts and the main GUI window.
	 * DO NOT CALL THIS FUNCTION DIRECTLY.  This function will be called by JOGL after creating the
	 * canvas (see {@link #AccordionDrawer(int, int)}).
	 * @param canvas The canvas object. 
	 */
	public void init(GLAutoDrawable canvas)
	{
		GL gl = canvas.getGL();

		setClearColor(backgroundColor);

		gl.glLogicOp(GL.GL_XOR);
		// TODO: get a better init x and y position than (10,10)
		reshape(canvas, 10, 10, winsize[0],winsize[1]); // no redraw yet (keep manualReshape false)

//		System.out.println("auto swap buffer mode to be turned off, on for now");
		canvas.setAutoSwapBufferMode(!doDoubleBuffer);

		customGLInit(gl);
	}

	/**
	 * This is called from the init method of AccordionDrawer.  The purpose 
	 * of this method is to set any custom GL state required by the application.  
	 * This method is called AFTER all other GL setup has been performed.  
	 * 
	 * @see #init(GLAutoDrawable)
	 * 
	 */
	protected abstract void customGLInit(GL gl);

	/**
	 * Function that contains the logic to either continue an existing frame
	 * (we have started a frame already, or there's more to draw and a user hasn't performed a stretch operation)
	 *  or start a new frame from a blank canvas.  This function is called only from {@link #display(GLAutoDrawable)}.
	 *
	 */
	private void displayFrame()
	{
		if ((startFrame || keepDrawing()) && !keepMoving()) {  	
			continueFrame();
			drawEnd(ignoreProgressive || !keepDrawing());
		} else {
			startNewFrame();
			drawEnd(ignoreProgressive || getDoubleBuffer());
		}
		if (!keepDrawing())
			doFlash(); // shows the mouse over node as selected after the scene is done
	}

	/**
	 * This is the very first thing called from the display() function, for performing application-specific drawing
	 * routines (not currently necessary in exisiting applications).
	 * @param canvas The canvas object for this drawer.   
	 */
	protected abstract void preDraw(GLAutoDrawable canvas);

	/**
	 * Display function that is called ONLY by JOGL (via canvas.display()), NEVER call this directly.
	 * Calls application-specific pre-draw function, then either calls flash drawing function or normal drawing.
	 * Normal drawing is either starting a new frame, or continuing an existing one.
	 * @param canvas The canvas object for this drawer.
	 */
	public void display(GLAutoDrawable canvas) {
		long now;
		if (debugOutput) { 
			now = System.currentTimeMillis();
		}
		
		preDraw(canvas);
		if (winsize[X] < 0 || winsize[Y] < 0 || !loaded || splitAxis[X] == null || splitAxis[Y] == null)
		{
			if (debugOutput)
				System.out.println("No data to draw or invalid window size. splitAxis[X].size()=" + splitAxis[X].getSize() + " splitAxis[Y].size() = " + splitAxis[Y].getSize());
			return;
		}
		if (debugOutput)
			System.out.println("Drawing frame number: " + getFrameNum());

		if (doingFlash)
		{
			if (debugOutput)
				System.out.println("doing flash in display");
			drawStart(false);
			flashDraw();
			drawEnd(false);
			
			if (debugOutput)
			{
				final long finish = System.currentTimeMillis();
				System.out.println(" (--) DONE FLASH IN: " + (finish - now) + " ms");
			}
			return;
		}
		drawStart(false); // either starts new frame or continues old frame
		// drawEnd is called in displayFrame() to match the calls to starting or continuing
		displayFrame();
		if (keepMoving() || keepDrawing())
			canvas.repaint(); // enqueue a repaint operation for continuing
		else if (debugOutput)
		{
			final long finish = System.currentTimeMillis();
			System.out.println(" (--) DONE SCENE IN: " + (finish - now) + " ms");
		}
	}


	/**
	 * Overloaded JOGL function for reshaping the canvas object, given position and new dimensions.
	 * Do not call this function directly, this is called by JOGL after the containing window has been resized.
	 * @param canvas The canvas object for this drawer.
	 * @param x Canvas horizontal position in pixels from the left (0 = left side of screen).
	 * @param y Canvas vertical position in pixels from the top (0 = top side of screen).
	 * @param w Canvas width in pixels. 
	 * @param h Canvas height in pixels.
	 */
	public void reshape(GLAutoDrawable canvas, int x, int y, int w, int h) {
		GL gl = canvas.getGL();
		if (w < 0 || h < 0)
		{
			System.err.println("either width or height is < 0: " + w + " " + h);
			return;
		}
		if (w != winsize[X] || h != winsize[Y])
		{
		}
		winsize[0] = w;
		winsize[1] = h;
		int gridMax = 25; // old value was log(2) of max grid size, 
		// or height of tallest split line hierarchy

		gl.glViewport(0, 0, winsize[0], winsize[1]);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho( 0.0f, 1.0f, 0.0f, 1.0f, .01f, gridMax+4.0f );
		gl.glScalef(1.0f, -1.0f, 1.0f);
		gl.glTranslatef(0.0f, -1.0f, 0.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}


	/** Redraw immediately, do not continue filling in details.
	 * All drawing requests are made through this function, nothing else should call canvas.display() to protect the
	 * proper use of display (to prevent accidental ad.display() calls).
	 */
	public void requestRedraw()
	{
		canvas.display();
	}  

	/**
	 * Currently has no different function from a direct call to {@link #requestRedraw()}.  Originally used to stop
	 * progressive rendering and immediately redraw the scene, instead of requesting an eventual redraw.  Now, all redraws
	 * wait for the completion of a frame and enqueue redraw events if a user has requested one through an interaction.
	 *
	 */
	public void forceRedraw()
	{ 
		forceRedraw = true;
		requestRedraw();
		forceRedraw = false;
	}

	/**
	 * Set the background color (the neutral "clear" color) for this drawer.
	 * Does not update the scene directly, requires a redraw to take effect.
	 * @param col The color for the background, usually {@link #backgroundColor}.
	 */
	public void setClearColor(Color col)
	{
		GL gl = canvas.getGL();
		gl.glClearColor( col.getRed() / 255.0f, 
				col.getGreen() / 255.0f, 
				col.getBlue() / 255.0f, 
				1.0f);
	}

	/** The number of times clear is called by any drawer.  Used for debugging to check for extra clears that may cause flickering. */
	protected static int clearCount = 0;
	/** The number of enqueued objects to draw per inner drawing loop before checking the timer.  Applications that require intense 
	 * drawing or processing per object should adjust this (approx this many objects should be drawable in well under 30 ms, or the value
	 * set by {@link #dynamicTime}) */
	public static int dequeueChunkPerTimeCheck = 2000;
	/**
	 * Clears the canvas.  Called by {@link #startNewFrame()}.
	 */
	public void clear()
	{
		GL gl = canvas.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * Sets the state of double buffering in this drawer, and sets the drawing
	 * buffer depending on the current drawing state (different for flash drawing
	 * than starting or continuing a frame). 
	 * @param value The new value for double buffering, changes {@link #doublebuffer}.  True means double buffering is on.
	 */
	public void setDoubleBuffer(boolean value) {
		GL gl = canvas.getGL();
		doublebuffer = value;
		if (doublebuffer)
		{
			if (startFrame || ignoreProgressive)
			{
				gl.glDrawBuffer(GL.GL_BACK);
			}
			else if (continueFrame)
			{
				gl.glDrawBuffer(GL.GL_BACK);
			}
		}
		else
		{ // no double buffer, for drawing during flash/rubberband
			gl.glDrawBuffer(GL.GL_FRONT);
		}

	}

	/**
	 * Gets the current state of double buffering.
	 * @return The state of double buffering for this drawer ({@link #doublebuffer}).
	 */
	public boolean getDoubleBuffer() { return doublebuffer;}

	// Accordion drawing subclasses must implement these drawing functions:
	/** Draws a range of geoms by a DrawableRange object (see {@link DrawableRange}):
	 * - TJ draws a range of leaf nodes, and ascends to the root
	 * - SJ draws a range of marked nodes, or a column range of site nodes
	 * @param r A range of objects that initiates drawing a partitioned region.
	 */
	public abstract void drawRange(DrawableRange r);
	/** Draws a range of geoms by a splitline object (objects between split line boundaries of the input split line):
	 * - TJ determines which leaf node to draw, and ascends to the root
	 * - SJ determines which column of nucleotides to draw, and renders the color column.
	 * @param rangeLine The {@link SplitLine} object to retrieve boundaries.
	 */
	public abstract void drawRange(SplitLine rangeLine);
	/**
	 * Draws a single geom.
	 * use the range if geom might require assistance
	 * - TJ draws a tree node, descends to some leaf, ascends to the root (skeleton drawing)
	 *  - needs range to determine originating drawing range to descend toward
	 * - SJ does not draw single geoms in typical use, only aggregated columns are supported
	 * @param cg The cell to draw.  Applications require their own cell-to-object translation functions.
	 * @param r Range to draw into, if drawing a cell requires a particular direction, such as rendering
	 * internal nodes in TJ that must draw within a leaf partition.
	 * */ 
	public abstract void drawGeom(CellGeom cg, DrawableRange r);

	/**
	 * Draw the split lines for an axis that are in the partitioned list.  Fake split lines are not drawn.
	 * This function is limited to the partition set, only the top level split lines are shown (not useful for datasets with
	 * tens of thousands of split lines, but useful for small datasets to show rubbersheet function with
	 * data overlaid.)
	 * @param axis The axis to draw.  X is horizontal, Y is vertical
	 * @param minBound Starting position of the split in the other direction
	 * @param maxBound Ending position of the split in the other direction
	 */
	public void drawSplits(SplitAxis axis, double minBound, double maxBound)
	{
		GL gl = canvas.getGL();
		if (axis.partitionedList == null)
			axis.makePixelRanges(frameNum);
		if (axis.partitionedList == null)
		{
			System.out.println("unable to make a partitioned list for axis: " + axis);
			return;
		}
		double posStart[] = new double[2], 
		posEnd[] = new double[2],
		plane = objplane;
		int thisAxisIndex = axis.horizontal ? 0 : 1;
		int otherAxisIndex = axis.horizontal ? 1 : 0;
		posStart[otherAxisIndex] = minBound;
		posEnd[otherAxisIndex] = maxBound;
		gl.glColor3f(0.8f, 0.8f, 0.8f);
		gl.glLineWidth(3);
		gl.glBegin(GL.GL_LINES);
		Iterator iter = axis.partitionedList.iterator();
		while (iter.hasNext())
		{
			SplitLine curr = (SplitLine)iter.next();
			if (curr.getParent() != null)
			{
				posStart[thisAxisIndex] = posEnd[thisAxisIndex] =
					axis.getAbsoluteValue(curr.getParent(), frameNum);
				gl.glVertex3d(posStart[0], posStart[1], plane);
				gl.glVertex3d(posEnd[0], posEnd[1], plane);
			}

			if (curr.getOpBound() != null)
			{
				posStart[thisAxisIndex] = posEnd[thisAxisIndex] =
					axis.getAbsoluteValue(curr.getOpBound(), frameNum);
				gl.glVertex3d(posStart[0], posStart[1], plane);
				gl.glVertex3d(posEnd[0], posEnd[1], plane);
			}
		}
		gl.glEnd();
	}

	/**
	 * Main drawing loop: draws the objects in the ToDrawQ. Called by the display function,
	 * and is sensitive to progressive rendering, the value of {@link #dequeueChunkPerTimeCheck},
	 * and the state of taking snapshots.    
	 * 
	 * @see #display(GLAutoDrawable)
	 *
	 */
	public void drawFrame() {

//		System.out.println("Begin drawFrame: draw? " + drawGeoms + ", " + frameNum + " drawQ size: " + ToDrawQ.size() + ", toMove.size(): " + toMove.size()) ;

		now = System.currentTimeMillis();
		if (drawGrid)
		{
			for (int xy = X; xy <= Y; xy++)
				drawSplits(splitAxis[xy], 0, 1);
		}
		while (drawGeoms &&
				(takeSnapshot || 
						ignoreProgressive || 
						now - dynamicStart < dynamicTime) && ToDrawQ.size() > 0)
		{
			for (int i = 0; ToDrawQ.size() > 0 && i < dequeueChunkPerTimeCheck; i++)
			{
				Object obj = ToDrawQ.get(0);
				if (obj instanceof DrawableRange)
				{
					drawRange((DrawableRange)obj);
				}
				else if (obj instanceof CellGeom)
				{
					drawGeom((CellGeom)obj, null);

				} 
				else if (obj instanceof SplitLine ) 
				{ 
					SplitLine line = (SplitLine)ToDrawQ.get(0);
					drawRange(line);
				}
				else
					System.out.println("what am I in the todrawQ: " + ToDrawQ.get(0).getClass().getName());
				if (ToDrawQ.size() < 1)
					System.out.println("synchronization error with drawing queue");
				else
					ToDrawQ.remove(0);
			} // end the for loop (done chunks per frame)
			now = ignoreProgressive ? 0 : System.currentTimeMillis();
			if (!ignoreProgressive && keepMoving()) // break the loop after first draw in animations
				break;
		}		
		drawPostFrame();

		if (keepMoving())
		{
			ToDrawQ.clear();
			incrementFrameNumber();
		}
		else if (keepDrawing())
		{
			// don't make a recursive call here; in display() (after exiting this function) 
			// this exact state is checked and a repaint() call is made, which re-enters the 
			// drawFrame() loop after checking for interaction
			// => (repaint() iff keepMoving() || keepDrawing())
		} 
		else
		{
			drawPostScene();
			startFrame = false;
		}
	}

	/**
	 * Starts a new scene, performs a transition, seeds the drawing queue,
	 * and starts drawing the next frame.
	 */
	protected void startNewFrame() {
		if (debugOutput)
			System.out.println("start new frame");

		incrementFrameNumber();
		drawnLabels.clear();
		flashGeomOld = null; 
		flashBoxOld = null; 

		// we need these flags for later on to
		// draw pixels later on in the correct buffer
		startFrame = true;
		continueFrame = false;

		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// all rendering state changes should be set
		// locally - means: never put a rendering state
		// change anywhere in the code if you don't want
		// to draw something at this point once you finished 
		// rendering, change the state back to the original state
		// see InteractionBox.java for instance
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		dynamicStart = System.currentTimeMillis();
		continueStart = dynamicStart;
		setDoubleBuffer(doDoubleBuffer); // this does a clear when not taking a picture
		if (!takeSnapshot)
		{
			clear();
		}
		doFrameMove(); // moved from drawFrame
		drawPreNewFrame(); // initializes partition lists
		seedQueue();

		drawFrame();
	}

	/**
	 * Continues a scene by drawing an additional frame related to what is already visible.
	 * Does not perform transitions and is only used while progressive rendering (when {@link #ignoreProgressive} is set to false).
	 */
	protected void continueFrame() {
		continueFrame = true;
		if (startFrame) // first continue frame?
		{
			startFrame = false;
			setDoubleBuffer(doDoubleBuffer);
		}
		dynamicStart = System.currentTimeMillis();
		if (dumpstats) stats.print(" inbetween "+(dynamicStart-now));

		drawPreContFrame();
		drawFrame();
	}

	/**
	 * Transition function to perform a single animation step.
	 * This is run before {@link #drawFrame} to move 
	 * lines in each {@link #splitAxis} during animated transitions. 
	 *
	 * All split lines that are moved (i.e., GridCells resized) go
	 * through a gradual animated transition. Of course other
	 * lines may be moved by some amount to accomodate the directed 
	 * transitions. 
	 * @see     SplitAxis
	 * @see		SplitLine
	 * @see		SplitTransition
	 */
	protected void doFrameMove() {
		Enumeration enumer = toMove.elements();
		while (enumer.hasMoreElements()) {
			SplitTransition st = (SplitTransition)enumer.nextElement();
			st.incr();
			if (st.done()) {
				toMove.remove(st);
				if (toMove.size() < 1)
				{
					break;
				} 
			} else {
				st.move();
			}
		}
	}

	/** Accessor for current frame number. 
	 * @return {@link #frameNum}, starts at 0, increases by 1 for each new frame. */
	public int getFrameNum() { return frameNum;}
	/** Accessor for background color. 
	 * @return {@link #backgroundColor}*/
	public Color getBackgroundColor() { return backgroundColor; }
	/** Accessor for label font color. 
	 * @return {@link #labelColor}*/
	public Color getLabelColor() { return labelColor; }
	/** Accessor for label background color.
	 * @return {@link #labelBackColor}*/
	public Color getLabelBackColor() { return labelBackColor; }
	/** Accessor for highlighted label font color. 
	 * @return {@link #labelHiColor}*/
	public Color getLabelHiColor() { return labelHiColor; }
	/** Accessor for highlighted label background color.
	 * @return {@link #labelBackHiColor}*/
	public Color getLabelBackHiColor() { return labelBackHiColor; }
	/** Accessor for rubber band color.
	 * @return {@link #rubberbandColor}*/ 
	public Color getRubberbandColor() { return rubberbandColor; }

	/** Modifier for background color. 
	 * @param backgroundColor New background color (sets {@link #backgroundColor}) */
	public static void setBackgroundColor(Color backgroundColor) { 
		AccordionDrawer.backgroundColor = backgroundColor;}
	/** Modifier for label font color. 
	 * @param labelColor New label font color (sets {@link #labelColor}) */
	public void setLabelColor(Color labelColor) { this.labelColor = labelColor;}
	/** Modifier for label background color. 
	 * @param labelBackColor New label background color (sets {@link #labelBackColor}) */
	public void setLabelBackColor(Color labelBackColor) { this.labelBackColor = labelBackColor;}
	/** Modifier for highlighted label font color. 
	 * @param labelHiColor New highlighted label font color (sets {@link #labelHiColor}) */
	public void setLabelHiColor(Color labelHiColor) { this.labelHiColor = labelHiColor;}
	/** Modifier for highlighted label background color. 
	 * @param labelBackHiColor New highlighted label background color (sets {@link #labelBackHiColor}) */
	public void setLabelBackHiColor(Color labelBackHiColor) { this.labelBackHiColor = labelBackHiColor;}
	/** Modifier for rubber band color. 
	 * @param rubberbandColor New rubber band color (sets {@link #rubberbandColor}) */
	public void setRubberbandColor(Color rubberbandColor) { this.rubberbandColor = rubberbandColor;}
	/** Modifier for number of animated transition steps. 
	 * @param numAnimSteps New number of steps for each animated transition (sets {@link #numAnimSteps}) */
	public void setNumAnimSteps(int numAnimSteps) { this.numAnimSteps = numAnimSteps; }
	/** Accessor for number of animated transition steps. 
	 * @return {@link #numAnimSteps} */
	public int getNumAnimSteps() { return numAnimSteps; }

	/** Modifier for number of animated transitions for mouse movements. 
	 * @param mouseMoveAnimSteps
	 * @param nonProgressiveRendering
	 */
	public void setMouseMoveAnimSteps(int mouseMoveAnimSteps, int nonProgressiveRendering)
	{
		this.mouseMoveAnimSteps = mouseMoveAnimSteps;
		this.mouseMoveAnimStepsNPR = nonProgressiveRendering;
	}

	/**
	 * Accessory for number of animated transitions for mouse movements.
	 * Depends on the current state of progressive rendering (PR).  If we're doing PR, we typically
	 * want fewer transitions for large mouse movements (jump cuts), and for non-PR, more transitions
	 * make the animations smoother, as there is typically less to render per scene.  Many mouse
	 * actions per second will cancel previous transactions, so only "jerky" mouse movements make a
	 * difference with more transaction steps.  
	 * @return The number of animation steps per mouse movement (stretching actions).
	 */
	public int getMouseMoveAnimSteps()
	{
		if (ignoreProgressive)
			return mouseMoveAnimSteps;
		else
			return mouseMoveAnimStepsNPR;
	}


	/**
	 * Modifier for the inflation increment for each automated stretching action.
	 * Does not affect the user-controlled mouse stretching, only the "make bigger" actions
	 * that usually happen after a user types "b" (or "s" for smaller).
	 * This value should be between 0 and 1, where 0 means stretches do nothing, and 1 means
	 * a stretch will fill in the maximum size available.
	 * A stretch followed by a squish of the same area should be idempotent, but may be slightly
	 * skewed by restrictions on minimum squishing size parameters ({@link #minContextInside} and {@link #minContextPeriphery}).
	 * @param value {@link #inflateIncr}: [0,1] value that controls the amount of growing for stretches.
	 */
	public void setInflateIncr(double value) { inflateIncr = value;}
	/**
	 * Accessor for inflate increment.
	 * See {@link #setInflateIncr(double)}
	 * @return {@link #inflateIncr}
	 */
	public double getInflateIncr() { return inflateIncr; }

	/**
	 * Sets the flash state and calls the flash drawing function, {@link #doFlash()}.
	 * The state for each flash action includes: {@link #flashGeom}, {@link #flashCol},
	 * {@link #flashX}, {@link #flashY}, and {@link #doBox}.
	 * @param cg New value of {@link #flashGeom}, the object that corresponds to the highlight, if any. May be null if an actual object isn't needed by the application.
	 * @param col The color ({@link #flashCol}) of the flash action, usually the global flash color ({@link #rubberbandColor}), or any highlight color. Null values should use an application-specific neutral color or the color of {@link #flashGeom}.  
	 * @param x The horizontal mouse/cursor position, in screen (pixel) coordinates. Sets {@link #flashX}.
	 * @param y The vertical mouse/cursor position, in screen (pixel) coordinates. Sets {@link #flashY}.
	 * @param doBox Set to true if the flash drawing should also draw the rubber band (TJ only, where tree nodes are drawn inside an empty rectangular area)
	 */
	public void setFlash(CellGeom cg, Color col, int x, int y, boolean doBox) {
		flashGeom = cg;
		flashCol = col;
		flashX = x;
		flashY = y;
		this.doBox = doBox;
		doFlash(); // does stretch box draw
	}

	/**
	 * Sets the height of the highlight plane.
	 * @param w The new value of {@link #hiliteplane}.
	 */
	protected void setHighlightPlane(float w)
	{
		hiliteplane = w;		
	}
	/**
	 * Sets the highlight plane back to its default value (-0.3f = magic number?)
	 */
	protected void resetHighlightPlane()
	{
		hiliteplane = -0.3f;		
	}

	/**
	 * Sets the height of the object plane.
	 * @param w The new value of {@link #objplane}
	 */
	protected void setObjectPlane(float w)
	{
		objplane = w;		
	}

	/**
	 * Sets the object plane back to its default value (-0.5f = magic number?)
	 *
	 */
	protected void resetObjectPlane()
	{
		objplane = -.5f; 
	}

	/**
	 * Sets the focus cell to the given grid cell
	 * @param fc The new value of {@link #focusCell}
	 */
	public void setFocusCell(GridCell fc) {focusCell = fc;}
	/**
	 * Returns true if we're drawing a dimmed view of objects, used in TJ to show the depth of nodes.  False draws all nodes the
	 * same non-dimmed, standard object color.
	 * @return The value of {@link #dimbrite}
	 */
	public boolean getDimBrite() {return dimbrite;}
	/**
	 * Sets the value of dimmed/bright viewing state (TJ only).  Redraws.
	 * @param on The new value of {@link #dimbrite}.  True: draw unmarked nodes with less saturation as they are farther from the root.  False: all nodes drawn the same color.
	 */
	public void setDimBrite(boolean on) {dimbrite = on; requestRedraw();}
	/**
	 * Toggle the value of {@link #dimbrite}
	 */
	public void toggleDimBrite() {setDimBrite(!dimbrite);}
	/**
	 * Returns true if we're drawing a dimmed view of marked objects, used in TJ to show the depth of nodes.  False draws all nodes the
	 * same non-dimmed, standard marked object colors.
	 * @return The value of {@link #dimcolors}
	 */    
	public boolean getDimColors() {return dimcolors;}
	/**
	 * Sets the value of dimmed/bright viewing state for marked nodes (TJ only).  Redraws.
	 * @param on The new value of {@link #dimcolors}.  True: draw marked nodes with less saturation as they are farther from the root.  False: all nodes drawn the same color.
	 */
	public void setDimColors(boolean on) {dimcolors = on; requestRedraw();}
	/** Toggle the value of {@link #dimcolors} */
	public void toggleDimColors() {setDimColors(!dimcolors);}
	/**
	 * Returns state of label drawing ({@link #drawlabels})
	 * @return Value of {@link #drawlabels}: true = draw labels, false = don't draw labels
	 */
	public boolean getDrawLabels() {return drawlabels;}
	/**
	 * Sets the state of label drawing ({@link #drawlabels}), then redraws.
	 * @param on Value of {@link #drawlabels}: true = draw labels, false = don't draw labels
	 */
	public void setDrawLabels(boolean on) {drawlabels = on; requestRedraw();}
	/** Toggles value of {@link #drawlabels} */
	public void toggleDrawLabels() {setDrawLabels(!drawlabels);}

	/**
	 * Returns state of stats output ({@link #dumpstats})
	 * @return Value of {@link #dumpstats}: true = output stats, false = don't output stats
	 */
	public boolean getDumpStats() {return dumpstats;}
	/**
	 * Sets the state of stats output ({@link #dumpstats})
	 * @param on Value of {@link #dumpstats}: true = output stats, false = don't output stats
	 */
	public void setDumpStats(boolean on) {
		dumpstats = on; 
		if (on) {
			try {
				String fname = "stats"+key+"."+System.currentTimeMillis();
				stats = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
			} catch (IOException  e) {
				System.out.println("can't open benchmark file: stats");
			}
		} else {
			stats.flush();
			stats.close();
		}
	}
	/** Toggles value of {@link #dumpstats} */
	public void toggleDumpStats() {setDumpStats(!dumpstats);}

	/**
	 * Returns the state of drawing the label background ({@link #labeldrawback})
	 * @return Value of {@link #labeldrawback}: true = draw the rectangle behind the label, false = no background for labels
	 */
	public boolean getLabelDrawBack() {return labeldrawback;}
	/**
	 * Sets the state of drawing the label background ({@link #labeldrawback}).  Redraws.
	 * @param on Value of {@link #labeldrawback}: true = draw the rectangle behind the label, false = no background for labels
	 */
	public void setLabelDrawBack(boolean on) {labeldrawback = on; requestRedraw();}
	/** Toggles value of {@link #labeldrawback} */
	public void toggleLabelDrawBack() {setLabelDrawBack(!labeldrawback);}

	/**
	 * Returns the state of where TJ draws labels relative to mouse cursor ({@link #labelposright})
	 * @return Value of {@link #labelposright}: true = draw labels close to node junctions, false = draw labels left of geometry's bounding box (TJ only)
	 */
	public boolean getLabelPosRight() {return labelposright;}
	/**
	 * Sets the state of where TJ draws labels relative to mouse cursor ({@link #labelposright})
	 * @param on Value of {@link #labelposright}: true = draw labels close to node junctions, false = draw labels left of geometry's bounding box (TJ only)
	 */
	public void setLabelPosRight(boolean on) {labelposright = on; requestRedraw();}
	/** Toggles value of {@link #labelposright} */
	public void toggleLabelPosRight() {setLabelPosRight(!labelposright);}

	/**
	 * Returns the line thickness used in drawing ({@link #linethickness}, TJ only)
	 * @return The value of {@link #linethickness}
	 */
	public int getLineThickness() {return linethickness;}

	/**
	 * Sets the line thickness used in drawing ({@link #linethickness}, TJ only)
	 * @param thickness The new value of {@link #linethickness}
	 */
	public void setLineThickness(int thickness) {
		linethickness = thickness;
		if (linethickness < 1) linethickness = 1;
	}
	/** Increases the line thickness used in drawing by 1 ({@link #linethickness}, TJ only) */
	public void increaseLineThickness() {setLineThickness(linethickness+1);}
	/** Decreases the line thickness used in drawing by 1 ({@link #linethickness}, TJ only) */
	public void decreaseLineThickness() {setLineThickness(linethickness-1);}

	/**
	 * Returns the state of linked navigation ({@link #linkednav}, TJ only)
	 * @return Value of {@link #linkednav}: true = do linked navigation for supported navigation techniques, false = do not link navigation
	 */
	public boolean getLinkedNav() {return linkednav;}
	/**
	 * Sets the state of linked navigation ({@link #linkednav}, TJ only).
	 * @param on The new value of {@link #linkednav}: true = do linked navigation for supported navigation techniques, false = do not link navigation
	 */
	public void setLinkedNav(boolean on) {linkednav = on;}
	/** Toggles value of {@link #linkednav} */
	public void toggleLinkedNav() {linkednav = !linkednav;}

	/**
	 * Returns the state of mouse-over flash drawing ({@link #noflash})
	 * @return Value of {@link #noflash}: true = do not perform mouse-over flash drawing, false = perform mouse-over flash drawing (default)
	 */
	public boolean getNoFlash() {return noflash;}
	/**
	 * Sets the state of mouse-over flash drawing ({@link #noflash}). Redraws.
	 * @param on New value of {@link #noflash}: true = do not perform mouse-over flash drawing, false = perform mouse-over flash drawing (default)
	 */
	public void setNoFlash(boolean on) {noflash = on; requestRedraw();}
	/** Toggles value of {@link #noflash} */
	public void toggleNoFlash() {setNoFlash(!noflash);}

	/**
	 * Returns the state of drawing geometric objects to represent a dataset ({@link #drawGeoms})
	 * @return Value of {@link #drawGeoms}: true = draw geometric objects to represent data (default), false = do not draw the dataset objects (used when just drawing the stretch and squish grid)
	 */
	public boolean getDrawGeoms() {return drawGeoms;}
	/**
	 * Sets the state of drawing geometric objects to represent a dataset ({@link #drawGeoms}).  Redraws.
	 * @param on New value of {@link #drawGeoms}: true = draw geometric objects to represent data (default), false = do not draw the dataset objects (used when just drawing the stretch and squish grid)
	 */
	public void setDrawGeoms(boolean on) {drawGeoms = on; requestRedraw();}
	/** Toggles value of {@link #drawGeoms} */
	public void toggleDrawGeoms() {setDrawGeoms(!drawGeoms);}

	/**
	 * Returns the state of drawing the background grid (splitlines, {@link #drawGrid})
	 * @return Value of {@link #drawGrid}: true = draw the horizontal and vertical grid, false =
	 */
	public boolean getDrawGrid() {return drawGrid;}
	/**
	 * Sets the state of drawing the background grid (splitlines, {@link #drawGrid}).  Redraws.
	 * @param on New value of {@link #drawGrid}: true = draw the horizontal and vertical grid, false =
	 */
	public void setDrawGrid(boolean on) {drawGrid = on; requestRedraw();}
	/** Toggles value of {@link #drawGrid} */
	public void toggleDrawGrid() {setDrawGrid(!drawGrid);}

	/**
	 * Resets the canvas to a state with a uniform grid, ends transitions, then redraws.
	 * Applications that do not have a uniform grid for a default drawing (such as genome browsers?)
	 * need to over-ride this function.
	 *
	 */
	public void reset() {
		resetGridUniform();
		focusCell = defaultFocusCell;
		endAllTransitions();
		incrementFrameNumber();
		requestRedraw();
	}	
	
	public void animatedReset(int numAnimSteps) { 
		endAllTransitions();
		if (toMove == null)
			toMove = new Hashtable();
		splitAxis[X].animatedReset(toMove, numAnimSteps);
		splitAxis[Y].animatedReset(toMove, numAnimSteps);
		incrementFrameNumber();
		requestRedraw();
		reset();
	}

	/**
	 * Clears the drawing queue, {@link #ToDrawQ}.
	 *
	 */
	public void clearQueue() {
		ToDrawQ.clear();
	}

	/**
	 * Resets the grid to a uniform state.
	 * Each axis ({@link #X}, {@link #Y}) is reset.
	 *
	 */
	public void resetGridUniform() {
		for (int xy = 0; xy < 2; xy++) {
			splitAxis[xy].resetSplitValues();
		}
		endAllTransitions(); // stop all animations
	}

	/**
	 * Converts pixel (screen) coordinates into relative (world) fractional coordinates [0,1].
	 * @param s Number of pixels to convert
	 * @param xy Direction of conversion ({@link #X} or {@link #Y})
	 * @return Fraction of the screen covered by s pixels in the given direction.
	 */
	public double s2w(int s, int xy) { return ((double)s)/winsize[xy];}
	/**
	 * Converts pixel (screen) coordinates into relative (world) fractional coordinates [0,1].
	 * @param s Number of pixels to convert, may be fractional
	 * @param xy Direction of conversion ({@link #X} or {@link #Y})
	 * @return Fraction of the screen covered by s pixels in the given direction.
	 */
	public double s2w(double s, int xy) { return ((double)s)/winsize[xy];}
	/**
	 * Converts pixel (screen) coordinates into relative (world) fractional coordinates [0,1].  Returns results for both axes.
	 * @param s Number of pixels to convert, for both {@link #X} and {@link #Y}.
	 * @return Fraction of the screen covered by s pixels in both directions.
	 */
	public double [] s2w(int [] s) { 
		double [] result = new double [2];
		for (int axis = X ; axis <= Y ; axis++) { 
			result[axis] = s2w(s[axis], axis);
		}
		return result;
	}

	/**
	 * Converts relative (world) fractional coordinates from [0, 1] to pixel (screen) coordinates.
	 * @param w Fraction of the screen to convert into pixel size
	 * @param xy Direction of conversion ({@link #X} or {@link #Y})
	 * @return Number of pixels covered by the given fraction w.  Rounded down to the nearest integer.
	 */ 
	public int w2s(double w, int xy) { return (int) Math.floor(w*winsize[xy]);}

	/**
	 * Return the size of the window, in {@link #X} or {@link #Y} direction
	 * @param xy The axes of interest (either {@link #X} or {@link #Y})
	 * @return {@link #winsize}[xy] the size of the window in the given direction
	 */
	public int getWinMax(int xy) { return winsize[xy]; }

	/**
	 * Get the geometric object found at pixel coordinate (x,y)
	 * @param x Horizontal cursor location
	 * @param y Vertical cursor location
	 * @return The geometric object at (x,y)
	 */
	public abstract CellGeom pickGeom(int x, int y);

	/**
	 * Turn on the highlight flag for the given cell
	 * @param c A gridcell to highlight
	 * @param on True: turn on the highlight for the cell, False: turn off the highlight
	 */
	public void setCellHighlight(GridCell c, boolean on) {
		c.setDrawBackground(on);
	}

	/**
	 * Set the color to be used for the next GL drawing primitive.
	 * Only sets RGB, does not change the alpha channel.
	 * @param col The chosen java.awt.Color object
	 */
	public void setColorGL(Color col) {
		GL gl = canvas.getGL();
		float thecol[] = new float[3];
		col.getRGBColorComponents(thecol);
		gl.glColor3f(thecol[0], thecol[1], thecol[2]);
	}
	/**
	 * Set the color to be used for the next GL drawing primitive.
	 * Sets RGB and the alpha channel
	 * @param col The chosen java.awt.Color object
	 */
	public void setColorGLAlpha(Color col) {
		GL gl = canvas.getGL();
		float thecol[] = new float[4];
		col.getRGBComponents(thecol);
//		System.out.println("Alpha is: " + col.getAlpha());
		gl.glColor4f(thecol[0], thecol[1], thecol[2], thecol[3]);
	}
	/**
	 * Drawing wrapper function called before drawing starts.
	 * Pseudo-deprecated.
	 * @param swap True: swap the drawing buffers before starting, which we never do (always false)
	 */
	public void drawStart(boolean swap) {
		if (swap)
			canvas.swapBuffers();
	}

	/**
	 * Drawing wrapper function called after drawing ends.
	 * glFlush() done regardless here, some systems seem to do this
	 * implicitly, but macs tend not to (jslack 07/10/18).
	 * @param swap True: swap the drawing buffers after flushing.  False: do nothing.
	 */
	public void drawEnd(boolean swap) {
		GL gl = canvas.getGL();
		gl.glFlush();
		if (swap)
		{
			canvas.swapBuffers();
		}
	}

	/**
	 * Initialize the split line hierarchies, including the axes and set the initial sizes.
	 * 
	 *   Loggers may be null if no logging is desired
	 * 
	 * @param jpb A progressBar widget for displaying progress
	 * @param xLogger Logging for X axis
	 * @param yLogger Logging for Y axis  
	 */
	public abstract void initCells(JProgressBar jpb);

	/**
	 * (TJ only) Get the list of possible colors for the given cell geometry
	 * @param c The object to draw, once we have the color
	 * @return An array of java.awt.Color objects that have priorities in drawing
	 */
	public abstract ArrayList getColorsForCellGeom(CellGeom c);
	/**
	 * Perform application-specific flash drawing.
	 */
	public abstract void doFlash();
	/** Perform application-specific pre-drawing before a continuing frame */
	abstract protected void drawPreContFrame();
	/** Perform application-specific pre-drawing before a scene starting frame */
	abstract protected void drawPreNewFrame();
	/** Perform application-specific drawing after each frame */
	abstract protected void drawPostFrame();
	/** Perform application-specific drawing after each a complete scene */
	abstract protected void drawPostScene();
	/** Seed the drawing queue, application specific */
	abstract protected void seedQueue();

	/** Reset split lines to application-specific values.
	 * May be non-uniform.  For uniform resets, suitable in most applications, use {@link #resetGridUniform()}. */
	public abstract void resetSplitValues(); // for non-uniform split lines
	/** Previous location of the cursor, in {@link #X} and {@link #Y} directions.
	 * {@link #mousePrev} = {@link #mouseNow} after each mouse movement. */
	protected int mousePrev[] = new int[2];
	/** Current location of the cursor in {@link #X} and {@link #Y} directions. */
	protected int mouseNow[] = new int[2];
	/** Flag to indicate a base pass for saving the current scene as a postscript figure. 
	 * The base pass is the lower level, unmarked nodes, drawn first, and will appear below groups ({@link #groupPass} next), and text ({@link #textPass} last). */
	public boolean basePass = false;
	/** Flag to indicate a group pass for saving the current scene as a postscript figure. 
	 * The group pass is the mid level, marked nodes, drawn second, and will appear above the base ({@link #basePass} first), and below text ({@link #textPass} last). */
	public boolean groupPass = false;
	/** Flag to indicate a text pass for saving the current scene as a postscript figure. 
	 * The text pass is the top level, scene labels, drawn last, and will appear above the base ({@link #basePass} first), and groups ({@link #groupPass} previous). */
	public boolean textPass = false;

	/**
	 * Accesses the minimum cell dimensions in screen coordinates for the current block size.
	 * See {@link #minCellDims}, this value could be fractional, but is usually 1 for single pixel blocks,
	 * or 5 for larger initial block sizes.
	 * @param xy Direction of interest, {@link #X} or {@link #Y}.
	 * @return {@link #minCellDims} for the given xy direction.
	 */
	public double getMinCellDims(int xy) {
		return minCellDims[xy];
	}

	/**
	 * Sets the minimum cell dimensions in screen coordinates to the given block size.
	 * See {@link #minCellDims}, this value could be fractional, but is usually 1 for single pixel blocks,
	 * or 5 for larger initial block sizes.
	 * @param xy Direction of interest, {@link #X} or {@link #Y}.
	 * @param is New {@link #minCellDims} for the given xy direction.
	 */
	public void setMinCellDims(int xy, double is) {
		minCellDims[xy] = is;
	}

	/**
	 * Accessor for the object plane, the depth in drawing for geometric objects.
	 * @return The value of {@link #objplane}.
	 */	
	public double getObjplane() {
		return objplane;
	}

	/**
	 * Accessor for the label plane, the depth in drawing for object labels.
	 * @return The value of {@link #labelplane}.
	 */
	public double getLabelPlane() {
		return labelplane;
	}

	/**
	 * State accessor for determining if flash drawing is active.
	 * Flash state is set before calling a redraw (no state may be passed to
	 * jogl specific drawing commands) and detected in the first stages of our
	 * drawing function, {@link #display(GLAutoDrawable)}.
	 * @return State of {@link #doingFlash}.
	 */
	public boolean isDoingFlash() {
		return doingFlash;
	}

	/**
	 * Accessor for the size of the drawing canvas, in pixels, in the given direction.
	 * @param xy Horizontal {@link #X} or vertical {@link #Y} direction.
	 * @return The size of the drawing canvas, as stored in {@link #winsize}.
	 */
	public int getWinsize(int xy) {
		return winsize[xy];
	}

	/**
	 * Toggle for the state of drawing split lines.
	 * TODO: This state needs to be cleared with additional drawers as a new
	 * drawer will initially have this set to false, while a true setting in 
	 * an existing drawer is not affected.
	 */
	public void toggleDrawSplits()
	{
		drawGrid = !drawGrid;
	}
	/**
	 * Set the state of split drawing, the drawing of all split lines with no culling.
	 * @param on The state to set the grid drawing value {@link #drawGrid}.
	 */
	public void setDrawSplits(boolean on)
	{
		drawGrid = on;
	}

	/**
	 * Return the list of drawn labels, stored in an array list.
	 * @return {@link #drawnLabels}, the list of labels that are currently being displayed in the frame. 
	 */
	public ArrayList getDrawnLabels() {
		return drawnLabels;
	}

	// global magic numbers defined for determining mouse click location, relative to
	// stuck boundaries.  Clicking close to a stuck location activates a handle to
	// move the stuck position with the mouse.  Only for testing, an undocumented
	// feature.
	/** A mouse click was far from a stuck line.  Does not activate a stuck split line (boundary) for manual moving. */
	protected static final int FAR_FROM_STUCK = -1;
	/** A mouse click was close to the min stuck of X.  Allows min X stuck line to be moved manually in the canvas. */
	protected static final int MIN_STUCK_X = 0;
	/** A mouse click was close to the min stuck of Y.  Allows min Y stuck line to be moved manually in the canvas. */
	protected static final int MIN_STUCK_Y = 1;
	/** A mouse click was close to the max stuck of X.  Allows max X stuck line to be moved manually in the canvas. */
	protected static final int MAX_STUCK_X = 2;
	/** A mouse click was close to the max stuck of Y.  Allows max Y stuck line to be moved manually in the canvas. */
	protected static final int MAX_STUCK_Y = 3;
	/**
	 * Creates an interaction box with a given stuck line, if one has been chosen.
	 * @param stuckType See {@link #MIN_STUCK_X}, {@link #MIN_STUCK_Y}, {@link #MAX_STUCK_X}, {@link #MAX_STUCK_Y}.
	 * @return An interaction box with a movable stuck line.  The next drag action will allow this line to move only.
	 */
	protected InteractionBox createBoxFromCells(int stuckType)
	{
		if (stuckType == FAR_FROM_STUCK) return null;
		SplitLine minSplit[] = {splitAxis[X].getMinStuckLine(), splitAxis[Y].getMinStuckLine()};
		SplitLine maxSplit[] = {splitAxis[X].getMaxStuckLine(), splitAxis[Y].getMaxStuckLine()};
		if (stuckType == MIN_STUCK_X)
			maxSplit[X] = splitAxis[X].getMinStuckLine();
		else if (stuckType == MIN_STUCK_Y)
			maxSplit[Y] = splitAxis[Y].getMinStuckLine();
		else if (stuckType == MAX_STUCK_X)
			minSplit[X] = splitAxis[X].getMaxStuckLine();
		else if (stuckType == MAX_STUCK_Y)
			minSplit[Y] = splitAxis[Y].getMaxStuckLine();
		return new InteractionBox(minSplit, maxSplit, null, this);
	}

	/**
	 * Creates an interaction box from the drag motion input.
	 * @param dragStart X/Y coordinate of the starting position
	 * @param dragEnd X/Y coordinate of the ending position
	 * @return An interaction box from start to end corners around appropriate cell boundaries.
	 */
	protected InteractionBox createBoxFromCells(int[] dragStart, int[] dragEnd) {
		// Convert from the start and end of a drag motion to absolute
		// min/max coordinates so that computation can be carried out
		// independent of the direction that the user chose to drag.
		int min[] = new int[2];
		int max[] = new int[2];
		for (int xy = 0; xy < 2; xy++) {
			if (dragStart[xy] < dragEnd[xy]) {
				min[xy] = dragStart[xy];
				max[xy] = dragEnd[xy];
			} else {
				min[xy] = dragEnd[xy];
				max[xy] = dragStart[xy];
			}
		}
		SplitLine[] minSplit = {splitAxis[X].getMinLineForPixelValue(min[X], frameNum), 
				splitAxis[Y].getMinLineForPixelValue(min[Y], frameNum)};
		SplitLine[] maxSplit = {splitAxis[X].getMaxLineForPixelValue(max[X], frameNum),
				splitAxis[Y].getMaxLineForPixelValue(max[Y], frameNum)};
		return new InteractionBox(minSplit, maxSplit, null, this);
	}
	/**
	 * Make an InteractionBox from the GridCells nearest to the box of the mousedrag.
	 * Expects dragStart[X]/Y, dragEnd[X]/Y to be set. 
	 * 
	Positions are in pixels; where mouse started and is now while dragging.
	@param stretchBox Defined by 4 split lines (2 movable, 2 not) and is currently being resized.

	@return 2 integers: pixel differences for movable lines in X and Y
	*/
	protected int[] createRectFromPick(InteractionBox stretchBox) {
		// if they pick close enough to a corner, 
		// use stretchBox for the other corner

		int[] returnArray = new int[2]; // x min, y min, x max, y max (-1 for unchanged values)
		for (int xy = X; xy <= Y; xy++) {
			int pixelMinContextInside = w2s(minContextInside, xy);
			int stuckPixelPosition = w2s(splitAxis[xy].getAbsoluteValue(stretchBox.stuckLines[xy], frameNum), xy);
			int dragLength = stretchBox.dragStart[xy] - stretchBox.dragEnd[xy];
			returnArray[xy] = stretchBox.originalMovePixelPosition[xy] - dragLength;
			if (splitAxis[xy].getSplitIndex(stretchBox.moveLines[xy])  > splitAxis[xy].getSplitIndex(stretchBox.stuckLines[xy]) &&
					returnArray[xy] < stuckPixelPosition + pixelMinContextInside)
			{ // moveLine is bigger than stuck, max line is moving	
				returnArray[xy] = stuckPixelPosition + pixelMinContextInside;
			} else if (splitAxis[xy].getSplitIndex(stretchBox.moveLines[xy]) < splitAxis[xy].getSplitIndex(stretchBox.stuckLines[xy]) &&
					returnArray[xy] > stuckPixelPosition - pixelMinContextInside)
			{ // moveline is smaller than stuck, min line is moving
				returnArray[xy] = stuckPixelPosition - pixelMinContextInside;
			}
		}
		return returnArray;
	}

	/**
	 * Allows for the movement of the stuck lines, for user-directed control of the accordion drawing boundaries.
	 * X/Y determined by setting the split line for the input interaction box to the same value (if basebox.maxline(X) == basebox.minline(X), then we move X)
	 * MIN/MAX determined by setting the split line to either minstuck or maxstuck. 
	 * 	@param stretchBox Box that defines the stuck line to be moved, the X or the Y, MIN or MAX stuck position.
 	*/
	protected void moveStuckPosition(InteractionBox stretchBox) {
		while (keepMoving())
			endAllTransitions(); // end transitions

		// CRFP: return 2 integers, pixel differences for stretchBox movable lines in X and Y
		int[] rectBox = createRectFromPick(stretchBox);
		double newPos;
		int xy = X;
		if (stretchBox.getMinLine(X) != stretchBox.getMaxLine(X))
			xy = Y;
		double stuckValue[] = {splitAxis[xy].getMinStuckValue(), splitAxis[xy].getMaxStuckValue()};

		if (stretchBox.getMinLine(xy) == splitAxis[xy].getMinStuckLine()) { // minstuck moving
			newPos = s2w(rectBox[xy], xy);
			if (newPos > stuckValue[1] - minContextInside)
				splitAxis[xy].setMinStuckValue(stuckValue[1] - minContextInside);
			else if (newPos < SplitAxis.defaultMinStuckValue)
				splitAxis[xy].setMinStuckValue(SplitAxis.defaultMinStuckValue); 
			else
				splitAxis[xy].setMinStuckValue(newPos);
		} else { // maxStuck moving
			newPos = s2w(rectBox[xy], xy);
			if (newPos < stuckValue[0] + minContextInside)
				splitAxis[xy].setMaxStuckValue(stuckValue[0] + minContextInside);
			else if (newPos > SplitAxis.defaultMaxStuckValue)
				splitAxis[xy].setMaxStuckValue(SplitAxis.defaultMaxStuckValue);
			else
				splitAxis[xy].setMaxStuckValue(newPos);
		}
		incrementFrameNumber(); // so compute place this frame will update, this can be moved to after transitions are created
		requestRedraw(); // redraw the frame, this will enact the movements in the move queue
	}


	/**
	 * Accessor for the drawing queue.
	 * @return Vector that represents the drawing queue, {@link #ToDrawQ}
	 */
	public Vector getToDrawQ() {
		return ToDrawQ;
	}

	/**
	 * Accessor for split axes, X {@link #X} or Y {@link #Y}.
	 * @return SplitAxis for given direction from {@link #splitAxis}.
	 */
	public SplitAxis getSplitAxis(int xy) {
		return splitAxis[xy];
	}

	/**
	 * Adds 1 to the frame number.
	 */
	public void incrementFrameNumber()
	{
		frameNum++;
	}

	/**
	 * Gets the flash box, the box that currently surrounds the highlighted node/region.
	 * @return An interaction box for {@link #flashBox}.
	 */
	public InteractionBox getFlashBox()
	{
		return flashBox;
	}

	/**
	 * Gets the current flash color.
	 * @return Flash (mouse-over) color from {@link #flashCol}.
	 */
	public Color getFlashCol() {
		return flashCol;
	}

	/**
	 * Draw text on jogl canvas, or write text layer to postscript file.
	 * @param x Horizontal position
	 * @param y Vertical postition
	 * @param name String to display at the given position
	 * @param f Font object used to display string
	 * @param col Color to draw the font objects
	 * @param zplane Height in the drawing plane to render the font
	 * @param outlineColor Color for the outside edge or background for each font object.
	 */
	public void drawText(double x, double y, String name, Font f, Color col, double zplane,
			Color outlineColor)
	{
		GL gl = getGL();
		if (takeSnapshot)
		{
			// recursive calls to place background colored text in postscript output
			if (outlineColor != null)
			{
				final int shadowDepth = 1;
				double delta[] = { s2w(shadowDepth, X), s2w(shadowDepth, Y)};
				for (int xc = -1; xc < 2; xc+=2)
					for (int yc = -1; yc < 2; yc+=2)
						drawText(x + xc * delta[X], y + yc * delta[Y], name, f, outlineColor, zplane - 0.01, null);			
			}
			try
			{
				snapShotWriter.write("/Arial findfont " +
						f.getSize() + " scalefont setfont " +
						w2s(x, X) + " " + w2s(y, Y) + " moveto " +
						"gsave 1 -1 scale (" + name + ") " +
						col.getRed() / 255f	+ " " + 
						col.getGreen() / 255f + " " + 
						col.getBlue() / 255f + " setrgbcolor show grestore\n");
			} catch (IOException ioe) {
				System.out.println("Error: IOException while trying to write cell names to file: "
						+ snapShotWriter.toString());
			}
		}
		else
		{
			// outline hack
			// TODO: make colored fonts work so we can render a background halo
			// outlined fonts are not working, bold or large fonts in diff colors don't work
//			if (false && outlineColor != null)
//			{
//				final int shadowDepth = 3;
//				setColorGL(outlineColor);
//				double delta[] = { s2w(shadowDepth, X), s2w(shadowDepth, Y)};
//				for (int xc = -1; xc < 2; xc+=2)
//					for (int yc = -1; yc < 2; yc+=2)
//					{
//						Point2D p = new Point2D.Double(x+xc*delta[X], y+yc*delta[Y]);
//						bff.drawString(gl, p, zplane, name, f, FontWrapper.FTGL_PIXMAP, outlineColor);
//						drawText(x + xc * delta[X], y + yc * delta[Y], name, f, outlineColor, zplane - 0.01, null);
//					}
//			}
			Point2D p = new Point2D.Double(x, y);
			bff.drawString(gl, p, zplane, name, f, col);
		}
	}

	/**
	 * JOGL canvas function for updating the display.
	 * This function is only called by the event handler, which initializes all parameters.
	 * AD does a complete redraw, after clearing the canvas.
	 * @param drawable The drawable object, which is always the canvas object {@link #canvas}, passed in by the event handler.
	 * @param modeChanged Unused in implementation, we do a complete redraw
	 * @param deviceChanged Unused in implementation, we do a complete redraw
	 */
	public void displayChanged(GLAutoDrawable drawable,
			boolean modeChanged,
			boolean deviceChanged)
	{
//		System.out.println("display Changed");
		clear();
		requestRedraw();
	}

	/**
	 * "Real" flash drawing code, down-called from the high-level abstract drawing sequence.
	 *
	 */
	abstract public void flashDraw();

	/**
	 * Accessor for the jogl canvas object.
	 * @return Returns the canvas object {@link #canvas}.
	 */
	public GLCanvas getCanvas()
	{
		return canvas;
	}

	/**
	 * Saves a screen shot of current GL frame to the specified file. 
	 * 
	 *  The extension of the file determines what type of file is created.  Supported formats include:
	 *  tiff (tif)
	 *  jpeg (jpg)
	 *  png (png)
	 *  gif (gif)
	 *  
	 * @param file Java File object specifying file to write the output to.  
	 * 
	 */
	public void saveScreenShot( File file) { 
		final GLAutoDrawable drawable = getCanvas();
		final int result = drawable.getContext().makeCurrent();

		if (result == GLContext.CONTEXT_NOT_CURRENT) { 
			System.err.println("Could not set context as current");
			return;
		}

		try {
			Screenshot.writeToFile(file, drawable.getWidth(), drawable.getHeight());
		} catch (GLException e) {
			System.err.println( "AccordionDrawer saveScreenShot error: GLException capturing screen data" );
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println( "AccordionDrawer saveScreenShot error: IOException writing file." );
			e.printStackTrace();
		}
		drawable.getContext().release();
	}

	/**
	 * Wrapper for starting a snapshot (postscript) output.
	 * Opens a file dialog for saving.
	 * 3 stages:  1) Draw the background objects (base).  2) Draw groups (group).  3) Draw text (text).
	 * @param drawers The list of drawers to export to eps (postscript).  Each specific drawer type will require 
	 */
	static public void doSnapshot(ArrayList drawers)
	{
		Iterator iter = drawers.iterator();
		File saveDir = null;
		JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
		if (drawers.size() == 1)
			fc.setDialogTitle("Select directory to save snapshot");
		else
			fc.setDialogTitle("Select directory to save snapshots");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int fileChooseResult = fc.showOpenDialog(null);
		if (fileChooseResult == JFileChooser.APPROVE_OPTION)
		{
			saveDir = fc.getSelectedFile();
		}
		else
			return;
		while (iter.hasNext())
		{
			AccordionDrawer atd = (AccordionDrawer)iter.next();
			atd.takeSnapshot = true; // unset in the drawEnd call
			// these are set one-by-one for 3 drawing cycles
			atd.basePass = true;  // draw the base image, nothing marked, no text
			atd.groupPass = false; // draw the groups, no text
			atd.textPass = false; // draw the text
			atd.newSnapshotName(atd.toString(), saveDir);
			atd.requestRedraw();
		}
		if (debugOutput)
			System.out.println("done the base pass");

		iter = drawers.iterator();
		while (iter.hasNext())
		{
			AccordionDrawer atd = (AccordionDrawer)iter.next();
			atd.basePass = false;
			atd.groupPass = true;
			atd.requestRedraw();
		}
		if (debugOutput)
			System.out.println("done the group pass");

		iter = drawers.iterator();
		while (iter.hasNext())
		{
			AccordionDrawer atd = (AccordionDrawer)iter.next();
			atd.groupPass = false;
			atd.textPass = true;
			atd.requestRedraw(); // TODO: does nothing, fix this
		}
		if (debugOutput)
			System.out.println("done the text pass");
	}
	/**
	 * Accessor for the highlight plane.
	 * @return The current value of {@link #hiliteplane}.
	 */
	public float gethiliteplane() {
		return hiliteplane;
	}
}

