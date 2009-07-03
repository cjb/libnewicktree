
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

import java.util.*;
import java.awt.*;
import java.io.*;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JProgressBar;

import net.sourceforge.olduvai.accordiondrawer.*;


/**
 * An abstract class representing a multi-scale rectangular grid on which 
 * a tree consisting of horizontal and vertical line segments is drawn
 *
 * Fills in the fields of the abstract class AccordionDrawer based on
 * information from the Tree class.
 * 
 * @author  Tamara Munzner, Li Zhang
 * @version 2.2
 * @see     AccordionDrawer
 * @see     net.sourceforge.olduvai.treejuxtaposer.AccordionTreeDrawerFinal
 * @see     Tree
 * @see     TreeNode
 * @see     GridCell
 */
public abstract class AccordionTreeDrawer extends AccordionDrawer {
	
    /** Label offset for leaf nodes. */
    protected int labeloffset[] = new int[2];
    /** Amount of space in X and Y (in pixels) that labels must be separated by. */
    protected int labelbuffer[] = new int[2];
    
    /** Inverted block size: the minimum partition size is 1/pixelDiv, if a section of tree requires culling this matters. */
    public final float pixelDiv = 4.0f;

	/**
     * framebuffer pixels from last mouseover label drawn
     */
    protected FloatBuffer labelPixels; // pixels for drawing the label (if there is one)
    /** Label box for the flash (mouse-over) drawing object */
    protected LabelBox flashLabelBox = null;
    /** Drawing buffer for mouse-over drawing/undrawing. */
    protected FloatBuffer linePixel[] = new FloatBuffer[2]; // vertical/horizontal line pixels (if there is one for each)
    /** Length of nodes for horizontal and vertical edges. */
    protected int lineLength[] = new int[2]; // length of x/y edges (width is line thickness + 1 on each side)
    /** Location of tree edges (for flash drawing) that we are storing in {@link #linePixel} */
    protected int linePos[][] = new int[2][2]; // 

    /** 
     * width and height of last mouseover label drawn
     */
    protected int drawnlabelsize[] = new int[2];

    //how the labels are drawn
    // dealing with variable font size
    /** Minimum size of fonts. */
    public int minFontHeight = 10;
    /** Maximum size of fonts. */
    public int maxFontHeight = 14;
    
    /** Size of fonts for mouse-over highlighting. */
    public int popupFontHeight = 14;

    /** Distance to an object in pixels that is close enough for a successful pick.  Good for picking in sparse regions. */
    public int pickFuzz; 
    
    /** set to true when we want to draw labels only at the leaf level.
     ** Functionality removed: ambiguous definition of the problem
    //  - only way to effectively draw best labels for subtrees at the leaf
    //    level is to draw them after rendering tree, drawing labels last
    //    isn't supported by progressive rendering
    //  - best attempts either draw:
    //     1) very few labels
    //        - greedy label on thin subtree will show very high ancestor in tree
    //        - not drawing subsequent nodes that are descendants of the drawn labels shows little information
    //     2) lots of labels, but mixed hierarchy in same subtree region
    //        - mammals and rats may appear at leaf level at the same time
    //        - descendant subtrees probably shouldn't be shown with labeled ancestors
     * */
	protected boolean labelAtLeaves = false;

	/** Tree that is drawn in this drawer's canvas. */
    public Tree tree;
    /** Root of the {@link #tree}. */
    protected TreeNode rootNode;
    
    /**
     *  If true, all nodes will have fully qualified names.
	Fully qualified names are good for trees that look like file systems.
	Fully qualified names are bad for trees that look like phylogenies, or guaranteed unique tree nodes.
    */
    public static boolean fullyQualified;

    // benchmarking 
    /** Count of number of objects drawn for the current frame, benchmark. */
    public static int countDrawnFrame;
    /** Count of number of objects drawn for the current scene, benchmark. */
    public static int countDrawnScene;
    /** Cumulative time for a frame, benchmark. */
    public static int cumFrameTime;

    /** Default color for objects that aren't marked. */
    protected Color objectColor;
    
    /**
     * Class constructor for new tree drawers.
     * Initializes many application-specific state values for TreeJuxtaposer.
     * Calls construction function for split lines and cells, {@link #initCells(JProgressBar)}. 
     * @param t Tree to be drawn on the drawer's canvas.
     * @param w width of canvas, in pixels.
     * @param h height of canvas, in pixels.
     * @param jpb Progress bar.
     */
    public AccordionTreeDrawer(Tree t, int w, int h, JProgressBar jpb) {
	super(w,h);
	
	dimbrite = true;
	objectColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	labeloffset[X] = 2; // label offset for leaf nodes
	labeloffset[Y] = 4;
	labelbuffer[X] = 10;
	labelbuffer[Y] = 1;
	pickFuzz = 5; // in pixels
	
	tree = t;
	rootNode = tree.getRoot();
	initCells(jpb);
	t.setLeafSplitAxis((StaticSplitAxis)splitAxis[Y]);
//	buildGrids();
//	attachEdges();
	flashGeom = tree.getRoot();
	flashGeomOld = null;
	doBox = false;
//    drawAngle = 30.0f;
//	init(canvas); // no explicit call for jogl, should be called automatically
    }

	/**
	 * Clear the objects initiated in ATD.  This might not be necessary.
	 */
		public void shutdown(){
			super.shutdown();      
		 }
 
		/**
		 * Finalize function for this class, which calls {@link #shutdown()}, and might not be necessary.
		 */
		 protected void finalize() throws Throwable {
		 
					 try {
						 shutdown();
					 }
					 finally {
//						System.out.println("finally clean ATD");
						 super.finalize();     
					 }
		 }

	// get the maxline for some node given children have set minlines
    /** Helper function for {@link #buildCells(JProgressBar)}.
     * Get the maxline for the parent of the given list of children. 
     * Children are later extended to reach the same line if they are not the closest to the root. */
    private StaticSplitLine getMaxLineX(ArrayList children)
	{
		StaticSplitLine maxLine = (StaticSplitLine)splitAxis[X].getMaxLine();
		int maxLineIndex = maxLine.getSplitIndex();
		Iterator iter = children.iterator();
		while (iter.hasNext())
		{
			StaticSplitLine currNodeMinLine = (StaticSplitLine)((TreeNode)iter.next()).getCell().getMinLine(X); 
			int currNodeMinIndex = currNodeMinLine.getSplitIndex();
			if (currNodeMinIndex < maxLineIndex)
			{
				maxLine = currNodeMinLine;
				maxLineIndex = currNodeMinIndex;
			}
		}
		return maxLine;
	}
	
	// update the children minlines to the maxline of the parent
    /** Helper function for {@link #buildCells(JProgressBar)}.
     * Extends the child minlines (leftmost split line) to reach its parent.
     * @param children Child list
     * @param newMinLine new minimum X split line for each tree node in the child list
     * */
    private void updateMinLineX(ArrayList children, StaticSplitLine newMinLine)
	{
		Iterator iter = children.iterator();
		while (iter.hasNext())
		{
			((TreeNode)iter.next()).getCell().setMinLine(newMinLine, X);
		}
	}
	
    /** Build cells for each TreeNode.  O(n) operation that partitions the worldspace and uses already created split lines.
     * @param jpb progress bar
     * */
    private void buildCells(JProgressBar jpb)
	{
	    if (jpb != null)
	    {
	        jpb.setMinimum(0);
	        jpb.setMaximum(tree.getTotalNodeCount());
	    }
	    int percentage = 0;
	    int counter = 0;
		int leafCount = 0;
//		Vector leafVector = new Vector();
		SplitLine currY = splitAxis[Y].getMinStuckLine(); // for positioning leaves in splitLine Y
		((StaticSplitAxis)splitAxis[Y]).createSplitLineArray(tree.getLeafCount());
		for(TreeNode n = tree.root.leftmostLeaf; n!=null; n=n.posorderNext)
		{
		    counter++;
			GridCell c = new GridCell(this);
			n.setCell(c);
			n.computedFrame = -1;
			if (n.isLeaf())
			{  // leaf
				SplitLine endSplit = splitAxis[X].getMaxStuckLine();
				splitAxis[X].addCell(c, splitAxis[X].getPreviousSplit(endSplit));
				
				splitAxis[Y].addCell(c, currY);
				currY = splitAxis[Y].getNextSplit(currY);
				
//				leafVector.add(n);
				if (splitAxis[Y].getSize() > leafCount)
				{
					// assign each leaf to a split line for culling function, so the max split line can reference its leaf directly					
					SplitLine currLine = splitAxis[Y].getSplitFromIndex(leafCount++);
					((StaticSplitAxis)splitAxis[Y]).addSplitLine(leafCount-1, currLine);
					currLine.setCullingObject(n);
				}
				else
				{
					leafCount++;
					// max split line doesn't exist in the tree, so the last leaf doesn't get assigned to a split line
				}
				n.rightmostLeaf = n.leftmostLeaf = n;
//				leafCount++;
			}
			else
			{ // internal node
//				n.addEdge(new TreeEdge(n, false), Y); // vertical edge
				n.rightmostLeaf = n.lastChild().rightmostLeaf;
				n.leftmostLeaf = n.firstChild().leftmostLeaf;
				StaticSplitLine maxLineX = getMaxLineX(n.children);
				updateMinLineX(n.children, maxLineX);
				splitAxis[X].addCell(c, splitAxis[X].getPreviousSplit(maxLineX));
				StaticSplitLine minLineY = (StaticSplitLine)n.leftmostLeaf.getCell().getMinLine(Y);
				StaticSplitLine maxLineY = (StaticSplitLine)n.rightmostLeaf.getCell().getMaxLine(Y); 
				splitAxis[Y].addCell(c, minLineY, maxLineY);
			}
			if (!n.isRoot())
			{
//				n.addEdge(new TreeEdge(n, true), X); // horizontal edge
			}
			else
			{
				rootNode = n;
			}
			if (jpb != null && (int)((counter/tree.getTotalNodeCount()*1.0) * 100.0) > percentage)
			{
			    percentage = (int)((counter/tree.getTotalNodeCount()*1.0) * 100.0);
			    jpb.setValue(percentage);
			    jpb.setString("Init cells: " + percentage +"%");
			}
		}
	}

    /**
     * Initializes split lines, and builds cells for each tree node.
     * @param jpb progress bar.
     */
    public void initCells(JProgressBar jpb) {
	int numLeaves = tree.getLeafCount();
	int treeHeight = tree.getHeight();

	initSplitLines(false, true, treeHeight, numLeaves); // no reset, use static lines
	splitAxis[X].setMinStuckValue(0.01f);

	buildCells(jpb);
	if (AccordionDrawer.debugOutput)
	    System.out.println("root cell " + rootNode.getCell());
    }

    /** Pick the object at the given X/Y pixel location.
     * @param x horizontal location of cursor, in screen space
     * @param y vertical location of cursor, in screen space
     * @return CellGeom (TreeNode) at the given position, determined by {@link TreeNode#pickDescend(double, double, double, double)}. 
     * */
    public CellGeom pickGeom(int x, int y)
    {
		double xPos = s2w(x, X);
		double yPos = s2w(y, Y);
		double xFuzz = s2w(pickFuzz, X);
		double yFuzz = s2w(pickFuzz, Y);
    	return rootNode.pickDescend(xPos, yPos, xFuzz, yFuzz);
    }

    /** 
     * Create box enclosing the subtree beneath input node/edge. The
     * resulting box is the nearest GridLine boundaries to that point,
     * which are cached so that redraws to erase the box can happen
     * after the lines change position.
     * @param cg TreeNode to act upon, passed in as CellGeom from
     *  AccordionDrawer (where TreeNodes are not known)
     * @see InteractionBox
     */
    public InteractionBox makeBox(CellGeom cg)
    {
		if (null == cg)
			return null;
		TreeNode tn;
		if (cg instanceof TreeNode)
		    tn = (TreeNode)cg;
	    else
	    	return null; // not a node or an edge?
		TreeNode minLeaf = tn.leftmostLeaf;
		TreeNode maxLeaf = tn.rightmostLeaf;
		SplitLine[] minLine = {tn.getCell().getMinLine(X), minLeaf.getCell().getMinLine(Y)};
		// maxLine[X] could use any leaf to get rightmost splitLine
		SplitLine[] maxLine = {minLeaf.getCell().getMaxLine(X), maxLeaf.getCell().getMaxLine(Y)};
	
		return new InteractionBox(minLine, maxLine, cg, tn.getCell().drawer);
    }



    /**
     * Flash drawing wrapper, must be called for flash requests; {@link #flashDraw()} is later called back within a drawing cycle.
     * On mouseover, flash the object under the mouse and draw its
     * label at maximum size.
     *
     * @see      net.sourceforge.olduvai.accordiondrawer.GridCell
     * @see      net.sourceforge.olduvai.treejuxtaposer.drawer.Tree
     * @see      net.sourceforge.olduvai.treejuxtaposer.drawer.TreeNode
     */
    public void doFlash() {
		if (noflash
//				|| (flashGeom == flashGeomOld && flashBox == flashBoxOld)
//				|| keepDrawing()
				)
		{
//			System.out.println("Early escape from flash" + 
//					noflash + " " + 
//					(flashGeom == flashGeomOld && flashBox == flashBoxOld) + " " +
//					keepDrawing());
			return;
		}
		doingFlash = true;
		requestRedraw(); // wraps the flash drawing found in flashDraw
		doingFlash = false;
	}
    
    /** Not to be called directly, only called by {@link #doFlash()} via flag doingFlash that redirects
     * in a normal drawing call. 
     * Flash drawing function, called from within the display function.
     *  order matters: 1) undraw old box 2) redraw old pixels
		3) save new overdrawn pixels 4) draw flash box
     * */
    public void flashDraw()
    {
    	GL gl = canvas.getGL();
		boolean doubleBufferedOriginal = getDoubleBuffer();
//		System.out.println("doublebufferorigninal: " + doubleBufferedOriginal);
		setDoubleBuffer(false); // deactivate double buffering for
									   // drawing flash, redrawing old flash
		InteractionBox stretchBox = null;
		if (doBox)
			if (flashBox != null)
				stretchBox = flashBox;
			else if (flashGeom != null)
				stretchBox = makeBox(flashGeom);

		// order matters: 1) undraw old box 2) dump old pixels
		//		  3) save new pixels 4) draw box
		if (doBox && flashBoxOld != null)
			flashBoxOld.undraw();

		int flashBoxBuffer = getLineThickness()/2 + 2; // added to the top/bottom/sides of each line
		int bufferedWidth = 2 * flashBoxBuffer + getLineThickness();
		boolean drawlabelsreal = drawlabels;
		drawlabels = false;
		
		if (null != flashGeomOld)
		{
			// draw in old flash cell
			TreeNode tno = (TreeNode)flashGeomOld;
			boolean[] teo = {tno.getEdge(X), tno.getEdge(Y)};
			for (int xy = X; xy <= Y; xy++)
			{
				if (!teo[xy]) continue;
				if (xy == X && tno.label != null && tno.label.length() > 0)
				{
					if (flashLabelBox != null) 
					{
					// draw back around the old label
					gl.glRasterPos3d(s2w(flashLabelBox.bottomLeftPos(X), X),
						s2w(flashLabelBox.bottomLeftPos(Y), Y), getLabelplane());
					gl.glDrawPixels(flashLabelBox.size(X), flashLabelBox.size(Y),
						GL.GL_RGB, GL.GL_FLOAT, labelPixels);
					
					
					}
				}
				// draw back old highlighted lines
				int length[] = {xy == X ? lineLength[xy] : bufferedWidth,
						xy == X ? bufferedWidth : lineLength[xy]};	
				gl.glRasterPos3d(s2w(linePos[xy][X], X),
					s2w(linePos[xy][Y], Y), getLabelplane());
				gl.glDrawPixels(length[X], // length if horiz, else width
					length[Y], // width if horiz, else length
					GL.GL_RGB, GL.GL_FLOAT, linePixel[xy]);
			} // end for loop
		} // end if (null != flashGeomOld)

		if (null != flashGeom)
		{
			// read old cells, draw in new flash cell

			// read pixels
			TreeNode tn = (TreeNode)flashGeom;
			boolean[] te = {tn.getEdge(X), tn.getEdge(Y)};
			gl.glReadBuffer(GL.GL_FRONT);
			for (int xy = X; xy <= Y; xy++)
			{
				if (!te[xy]) continue;
				float[] posStart = new float[2];
				float[] posEnd = new float[2];
				tn.setPositions(posStart, posEnd, xy == X);
				int[] posStartPix = {w2s(posStart[X],X), w2s(posStart[Y],Y)};
				int[] posEndPix = {w2s(posEnd[X],X), w2s(posEnd[Y],Y)};
				if (xy == X && tn.label != null && tn.label.length() > 0) // get the horizontal edge to save the pixels under the new big label
				{
					flashLabelBox = tn.makeLabelBox(popupFontHeight, flashX, flashY, posStart, posEnd, false);
					drawnlabelsize[X] = flashLabelBox.size(X);
					drawnlabelsize[Y] = flashLabelBox.size(Y);
					labelPixels = FloatBuffer.allocate(drawnlabelsize[0] * 3 * drawnlabelsize[1]);
					// 	read pixels under new label from frontbuffer
					gl.glReadPixels(flashLabelBox.bottomLeftPos(X),
						getWinsize(Y) - flashLabelBox.bottomLeftPos(Y),
						drawnlabelsize[X], drawnlabelsize[Y],
						GL.GL_RGB, GL.GL_FLOAT, labelPixels);
				}
				else if (xy == X)
				{
					flashLabelBox = null;
					labelPixels = null;
				}
				// assert: start < end
				if (xy == X)
				{
//					posStart[Y] = posEnd[Y] = (float)tn.getMidY(); // debug, remove later
					posStartPix[Y] = posEndPix[Y] = w2s(tn.getMidY(),Y);
				}
				else // te is vert, has children since it's not null
				{
					posStartPix[Y] = w2s(tn.getChild(0).getMidY(),Y);
					posEndPix[Y] = w2s(tn.getChild(tn.numberChildren()-1).getMidY(),Y);
				}
				lineLength[xy] = posEndPix[xy] - posStartPix[xy] + 2 * flashBoxBuffer;
				linePos[xy][X] = posStartPix[X] - flashBoxBuffer;
				if (linePos[xy][X] < 0) linePos[xy][X] = 0;
				
				linePos[xy][Y] = posEndPix[Y] + flashBoxBuffer; // note: Y coords reversed!
				if (linePos[xy][Y] > getWinsize(Y)) linePos[xy][Y] = getWinsize(Y);
				
				int length[] = {xy == X ? lineLength[xy] : bufferedWidth, 
						xy == X ? bufferedWidth : lineLength[xy]};
				linePixel[xy] = FloatBuffer.allocate(3*length[X]*length[Y]);
				// read under the new drawing, direction xy
				gl.glReadPixels(linePos[xy][X], getWinsize(Y) - linePos[xy][Y],
						length[X], length[Y],
						GL.GL_RGB, GL.GL_FLOAT, linePixel[xy]);
			}
			
			// don't draw flash for now, debug
			for (int xy = X; xy <= Y; xy++)
			{
				if (!te[xy]) continue;
				
				if (xy == X)// && flashLabelBox != null)
					// draw in label
					if (flashLabelBox != null)
						tn.drawLabelBig(popupFontHeight, xy == X);
//					System.out.println("drawing tree node: " + tn);
			}
			tn.drawInCell(flashCol, interactionplane);
			
		} // finished saving pixels and drawing in flash cells
			
		if (doBox && stretchBox != null)
		{
//			System.out.println("fbw: " + flashBoxWidth);
			// INFOVIS 2006 box highlighting, demo mode
			if (getDrawGrid())
			{
				if (flashBoxWidth == 3)
					stretchBox.draw(Color.CYAN, flashBoxWidth +2, interactionplane);
				else
					stretchBox.draw(rubberbandColor, flashBoxWidth +2, interactionplane);
			}
			else
				stretchBox.draw(rubberbandColor, flashBoxWidth, interactionplane);
		}

		drawlabels = drawlabelsreal;
		if (doBox)
			flashBoxOld = stretchBox;
		flashGeomOld = flashGeom;
		flashXOld = flashX;
		flashYOld = flashY;
		setDoubleBuffer(doubleBufferedOriginal); // no redraw
    }

    /** Function that runs prior to a new scene.  Makes pixel ranges (partitioned list), and initializes postscript output. */
    protected void drawPreNewFrame() {
		GL gl = canvas.getGL();
		
	if (!groupPass && !textPass) // ok for no snapshots or for the basePass
	{
		countDrawnFrame = 0;
		countDrawnScene = 0;
		cumFrameTime = 0;
		splitAxis[Y].makePixelRanges(pixelDiv, frameNum); // divide into block size / 4
		if (debugOutput)
			System.out.println(" (dpnf) list of leaves created: " + splitAxis[Y].getPartitionedList().size());
		pixelSize = getPixelSize(Y);
//	DebugFrame.setStateMachine(DebugFrame.GL_DEPTH _TEST, true);
		// one of these statements causes crash
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
	}

	if (basePass) // first pass of text output
	{
//		System.out.println("staring new output");
		int xSize = winsize[X];
		int ySize = winsize[Y];

		try
		{
			snapShotWriter.write("%!PS-Adobe-2.0 EPSF-2.0\n" +
			"%%BoundingBox: 0 0 " + xSize + " " + ySize + "\n" +
			"%%Magnification: 1.0000\n" +
			"%%EndComments\n" +
			
			"gsave\n" +
			
			"newpath " +
			"0 " + ySize + " moveto " +
			"0 0 lineto " +
			xSize + " 0 lineto " +
			xSize + " " + ySize + " lineto " +
			"closepath " +
			"clip newpath\n" +
			"1 -1 scale\n" +
			"0 -" + ySize + " translate\n");
		}
		catch (IOException ioe)
		{
			System.out.println("Error: unable to write to file: " + snapShotWriter.toString());
		}
	}
    }

    /** Function that runs before a frame that is being drawn for a continued scene. 
     * Currently used to set debugging values. */
    protected void drawPreContFrame() {
	countDrawnFrame = 0;
    }

    /** Function that runs following a scene's completion.
     * Currently resets GL.GL_DEPTH_TEST, and finalizes snapshot output of a tree to postscipt. */
    protected void drawPostScene() {
		GL gl = canvas.getGL();
	gl.glDisable(GL.GL_DEPTH_TEST);
	if (textPass) // end of the last pass
	{
		try
		{
			snapShotWriter.write("grestore\n" +
			"showpage\n");
			takeSnapshot = false;
			textPass = false;
			snapShotWriter.close();
		}
		catch (IOException ioe)
		{
			System.out.println("Error: can not write to file: " + snapShotWriter);
		}

	}
    }

    /** Function that runs following a frame.  Currently used for debugging. */
    protected void drawPostFrame() {
    	long now = ignoreProgressive ? 0 : System.currentTimeMillis();			
    	countDrawnScene += countDrawnFrame;
    	long frameTime = now-dynamicStart;
    	cumFrameTime += frameTime;
    	if (dumpstats) stats.print(" df "+countDrawnFrame+" frameTIME "+frameTime);
    }

    /** Get the tree that this drawer represents.
     * @return value of {@link #tree}. */
    public Tree getTree() { return tree;}
    /** Canvas accessor. 
     * @return the canvas for this drawer */
    public GLAutoDrawable getCanvasDrawable() { return (GLAutoDrawable) canvas; }
    /** Node access by node key.
     * @param key key for node of interest.
     * @return Node at the given key. */
    public TreeNode getNodeByKey(int key) { return tree.getNodeByKey(key);}
    /** Node access by node name.
     * @param name name/label for node of interest
     * @return Node with the given name. */
    public TreeNode getNodeByName(String name) { return tree.getNodeByName(name);}

    /** Get the size for the label buffer in the given axis by 1.
     * @param xy X or Y (0/horizontal,1/vertical) axis. */
    public int getLabelBuffer(int xy) {return labelbuffer[xy];}
    /** Set the size for the label buffer in both axes. 
     * @param buffer new buffer amount, in pixels.
     */
    public void setLabelBuffer(int buffer) {
    	if (buffer < 1) buffer = 1;
    	labelbuffer[X] = buffer;
    	labelbuffer[Y] = buffer;
    	requestRedraw();
    }
    /** Set the size for the label buffer in the given axis. 
     * @param buffer new buffer amount, in pixels.
     * @param xy X or Y (0/horizontal,1/vertical) axis. */
    public void setLabelBuffer(int buffer, int xy) {
	if (buffer < 1) buffer = 1;
	labelbuffer[xy] = buffer;
	requestRedraw();
    }
    /** Increment the size for the label buffer in the given axis by 1. 
     * @param xy X or Y (0/horizontal,1/vertical) axis. */
    public void increaseLabelBuffer(int xy) {
	setLabelBuffer(labelbuffer[xy]+1, xy);
    }
    /** Decrement the size for the label buffer in the given axis by 1. 
     * @param xy X or Y (0/horizontal,1/vertical) axis. */
    public void decreaseLabelBuffer(int xy) {
	setLabelBuffer(labelbuffer[xy]-1, xy);
    }

    /** Get the current maximum font height.
     * @return current value of {@link #maxFontHeight}. */
    public int getMaxFontHeight() {return maxFontHeight;}
    /** Set the maximum font height to the given value.
     * @param fontheight new value for {@link #maxFontHeight}. */
    public void setMaxFontHeight(int fontheight) {
	if (fontheight < 1) fontheight = 1;
	maxFontHeight = fontheight;
	popupFontHeight = maxFontHeight;
	requestRedraw();
    }
    /** Increment maximum font height by 1. */
    public void increaseMaxFontHeight() {
	setMaxFontHeight(maxFontHeight+1);
    }
    /** Decrement maximum font height by 1. */
    public void decreaseMaxFontHeight() {
	setMaxFontHeight(maxFontHeight-1);
    }

    /** Get the current minimum font height.
     * @return current value of {@link #minFontHeight}. */
    public int getMinFontHeight() {return minFontHeight;}
    /** Set the minimum font height to the given value.
     * @param fontheight new value for {@link #minFontHeight}. */
    public void setMinFontHeight(int fontheight) {
	if (fontheight < 1) fontheight = 1;
	minFontHeight = fontheight;
	requestRedraw();
    }
    /** Increment minimum font height by 1. */
    public void increaseMinFontHeight() {
	setMinFontHeight(minFontHeight+1);
    }
    /** Decrement minimum font height by 1. */
    public void decreaseMinFontHeight() {
	setMinFontHeight(minFontHeight-1);
    }

    /** Get the {@link #objectColor}.
     * @return The value of {@link #objectColor}. */
    public Color getObjectColor() { return objectColor; }
    /** Set the {@link #objectColor}.
     * @param objectColor new value of {@link #objectColor}. */
    public void setObjectColor(Color objectColor) { this.objectColor = objectColor;}


    /** Debugging value, for information of drawing efficiency. */
    public int leafDrawCount;
    /** Debugging value, for information of drawing efficiency. */
    public int internalDrawCount;
	
	/**
	 * Draws a geometric object (treenode here) within a containing cell
	 * A node is drawn when it's larger than some ratio of block value,
	 * as determined from the drawRange function, or when a node is marked
	 * individually, such as mouse over highlighting or single node marking
	 * @param cg The treenode to draw
	 * @param r The drawable range that the tree node is in, for directing the descent to a proper leaf in range.
	 */
	public void drawGeom(CellGeom cg, DrawableRange r)
	{
		TreeNode subTreeRoot = (TreeNode)cg;
		if (r == null)
		{
			// no range passed when drawing a node, so it doesn't matter where descent goes
			// this happens when drawing a skeleton, not into a specific range of nodes subtending a vertical pixel
			r = new RangeInTree(subTreeRoot.leftmostLeaf.getLindex()-1, subTreeRoot.rightmostLeaf.getLindex(), this);
			if (r.getMin() > r.getMax())
				System.out.println("Error: min > max in Range: " + r);
		}
		subTreeRoot.drawDescend(frameNum, objplane, r.getMin(), r.getMax(), tree);
		//subTreeRoot.leftmostLeaf.key, subTreeRoot.rightmostLeaf.key);
//		subTreeRoot.drawInCell(getColorsForRange(subTreeRoot.key, subTreeRoot.key), objplane);
		if (!subTreeRoot.isRoot())
			subTreeRoot.parent.drawAscend(frameNum, objplane);

	}
	
	/** Size of block/pixel used for rendering.  Set by drawprenewframe. */
    private double pixelSize;
	
    /** Index for uncolored node in {@link #getLeftMostNodes(TreeNode, TreeNode)}. */
    private final int uncoloredNode = 0;
    /** Index for colored node in {@link #getLeftMostNodes(TreeNode, TreeNode)}. */
    private final int coloredNode = 1;
    /** Get the "left most nodes".  For a range of nodes, we want to draw the path that overlaps the range and also has the closest screen-space
     * position to the root node.  This prevents gaps, as explained in the PRISAD paper.  
     * @param minChild minimum tree node in the block range, a leaf.
     * @param maxChild maximum tree node in the block range, a leaf.
     * @return 1 or 2 Tree nodes that are the tallest/leftmost/closest to the root in screen space, one for colored (if any nodes are colored in the leaf
     * range) and one that is not.
     * */
    private TreeNode[] getLeftMostNodes(TreeNode minChild, TreeNode maxChild)
	{
		TreeNode[] leftMostNode = {null, null}; // return values
		Color leftMostColor = null;
		
		if (minChild == maxChild) // single tree node, no choice but to draw it
		{
			TreeNode[] singleNode = {minChild, minChild};
			return singleNode;
		}
		
		// colored different from uncolored node since uncolored portions aren't drawn when this node isn't as far left as
		//  some other node for this pixel, but need to draw some colored single pixel node if it isn't the farthest
		//  node to the left
		StaticSplitLine[] leftMostPosition = {(StaticSplitLine)minChild.getCell().getMinLine(X),
				(StaticSplitLine)minChild.getCell().getMaxLine(X)};
		int currMin = minChild.key;
		TreeNode currRootNode = minChild.parent;
		while (currMin <= maxChild.key)
		{
			// ascend tree to pixelSize
			TreeNode prevRootNode = null; // last subpixel node
			double currCellSize = currRootNode.getCell().getSize(Y);
			while (currCellSize < pixelSize)
			{
				prevRootNode = currRootNode;
				currRootNode = currRootNode.parent;
				currCellSize = currRootNode.getCell().getSize(Y);
			}
			if (prevRootNode == null) // ok now, since we start currRootNode at one up from leaf
			{
				prevRootNode = currRootNode;
			}
			ArrayList colors = getColorsForCellGeom(prevRootNode);
			StaticSplitLine minLinePositionCheck = (StaticSplitLine)prevRootNode.getCell().getMinLine(X); 
			if (minLinePositionCheck.getSplitIndex() < leftMostPosition[uncoloredNode].getSplitIndex())
			{
				leftMostPosition[uncoloredNode] = minLinePositionCheck;
				leftMostNode[uncoloredNode] = prevRootNode;
			}
			if (colors.size() > 0 && 
					(minLinePositionCheck.getSplitIndex() < leftMostPosition[coloredNode].getSplitIndex() || 
							leftMostColor.getAlpha() < ((Color)colors.get(0)).getAlpha()))
			{
				leftMostPosition[coloredNode] = minLinePositionCheck;
				leftMostNode[coloredNode] = prevRootNode;
				leftMostColor = (Color)colors.get(0);
			}
			
			// set up next round
			int nextSubtreeKey = currRootNode.rightmostLeaf.key+1; // some key in the next tree over
			TreeNode tempNode = getNodeByKey(nextSubtreeKey); // the next subtree root
			if (tempNode != null)
			{
				currMin = tempNode.leftmostLeaf.key;
				currRootNode = tempNode.leftmostLeaf;
			}
			else // beyond range of nodes
				currMin = nextSubtreeKey;
		}
		if (leftMostNode[coloredNode] != null && 
				leftMostPosition[coloredNode] == leftMostPosition[uncoloredNode])
			leftMostNode[uncoloredNode] = null; // colored node is better to draw 
		return leftMostNode;
	}
	

	/**
	 * Draw a given range.  The range is smaller than a block size, and is a skeleton component, which means it
	 * is a single leaf->root path, for some leaf in the given range.  Two paths may be drawn if a colored path is involved for marked
	 * ranges.
	 * TODO: reference PRISAD for range drawing a leaf range.
	 * @param r Range object to be drawn.
	 */
	public void drawRange(DrawableRange r)
	{
//		System.out.println("drawing range: " + r);
		int min = r.getMin(), max = r.getMax();
		
		if (min == max)
		{
			System.err.println("no range");
			return; // no split range
		}
		TreeNode[] leftMostNode = {null, null}; // 0 is unmarked, 1 is marked nodes
		TreeNode minChild = tree.getLeaf(min+1);
		TreeNode maxChild = tree.getLeaf(max);
//		System.out.println("tree range: " + minChild + " " + maxChild);
		if (min + 1 == max) // single node in a pixel, no sub pixel drawing
		{
			leftMostNode[uncoloredNode] = minChild;
		}
		else
		// sets up array of 2 (colored and uncolored) node choices to draw
		// if colored is null, nothing colored in the range
		// if uncolored is null, colored is better to draw
			leftMostNode = getLeftMostNodes(minChild, maxChild);
		
		for (int i = 0; i < 2; i++) // for colored and uncolored nodes
			if (leftMostNode[i] != null)
				drawGeom(leftMostNode[i], r);
	}
	
	/** Stub function. */
    public void drawRange(SplitLine sl)
	{
		
	}
    
    /** Add an object ({@link RangeInTree}, or {@link TreeNode}) to the drawing queue AccordionDrawer#toDrawQ
     * @param r Object to add.  RangeInTree objects are converted into split line ranges before insertion, TreeNodes are added directly. */
    public void addToDrawQueue(Object r)
	{
		if (r != null)
		{
			if (r instanceof RangeInTree)
				ToDrawQ.add(((RangeInTree)r).getSplitLineRange(Y, false));
			else if (r instanceof TreeNode)
				ToDrawQ.add(r);
		}	
	}
	
    /** Get the list of colors for the given split line index range
     * @param min start of range to get colors for.
     * @param max end of range to get colors for.
     * @return List of java color object that are drawn in the given range.  Only the first really matters as that color is the highest priority. */
    public abstract ArrayList getColorsForRange(int min, int max);
    /** Get a range list by the given key.  Stored by the application, accessible by implemeted subclasses. 
     * @param key index into the list of ranges, aka the group number (like marked groups or difference groups) */
    public abstract RangeList getRangeByKey(int key);
	
	/**
	 * Get the leaf indexed by the input value.
	 * @param leafNum leaf index.
	 * @return Leaf at leaf position as given, note this is not the node key value.
	 */
	public TreeNode getLeaf(int leafNum) {
		return tree.getLeaf(leafNum);
	}
    
	/**
	 * Get the list of leaves, as determined by the visible (drawing) set from the partition list of split axis Y.
	 * @return Returns the listOfLeaves.
	 */
	public TreeSet getListOfLeaves() {
		return splitAxis[Y].getPartitionedList();
	}
    
	/** Stub function. For non-uniform split lines (not this type of layout)*/
    public void resetSplitValues()
    {
        
    }

    /** Stub function. */
    protected void preDraw(GLAutoDrawable canvas) { 

	}
	
};
