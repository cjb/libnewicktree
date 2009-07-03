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

import java.awt.*;
import java.io.IOException;
import java.util.*;

import javax.media.opengl.GL;

import net.sourceforge.olduvai.accordiondrawer.*;
import net.sourceforge.olduvai.treejuxtaposer.TreePairs;

/**
 * A class representing a node of a (phylognenetic) tree. The tree that this
 * node belongs to is of type Tree. Nodes have fields that store a pre- and
 * post-ordering.
 * 
 * A TreeNode has a list of children, a unique key, a leftmostleaf and a
 * rightmost leaf
 * 
 * @author Tamara Munzner, Li Zhang, Yunhong Zhou
 * @version 2.2
 * @see Tree
 * @see GridCell
 */
public class TreeNode implements CellGeom, Comparable {

	/** Array of child nodes that are attached below this internal node.  Null if this is a leaf. */
	protected ArrayList children; // eventually turn this into an array (need
									// to change parser)

	/** key is unique for nodes in one tree.  Keys are pre-ordered (root = 0, depth-traversal ordering). */
	public int key;

	/** The GridCell that this node is attached to.  4 split lines make up the cell boundaries. */
	public GridCell cell;

	/** Height of font in font points used to draw the label. */
	private int fontSize;

	/** Score for a node in [0,1] that corresponds to the topological similarity between two tree drawers.
	 @see TreePairs#getBestCorrNodeScore(Tree, TreeNode, Tree, int) */
	private Double bcnScore;

	// /**
	// * The offset of the point with respect to the cell. We only have
	// * this for the row offset as we assume that the vertical edges
	// * are all aligned. When computing the Y coordinate of a node, we
	// * add nodeOffsetR to the pointOffset[1], a fixed parameter set by
	// * AccordionDrawer.
	// */
	/**
	 * The last frame that had a computed {@link #midYPosition}, for caching.
	 */
	protected int computedFrame; // store frame midYPosition was last
									// calculated (needed to place parents)

	/** Cached location (world-space) of the mid point in the vertical of a cell where the horizontal tree edge is drawn. 
	 * This is (1/2 of cell size + minY) for leaves, midway between first and last child edge for internal nodes. */
	private double midYPosition;

	/**  Returns the minimum key value of nodes in the subtree rooted by this node.
	 * @return The index of the smallest descendant node (which is the key for this node). */
	// this is the key for this node
	public int getMin() {
		return key;
	}

	/** Returns the maximum key value of nodes in the subtree rooted but this node.
	 * @return The index of the smallest descendant node (which is the key for the rightmost leaf node). */
	// this is the key of the rightmost leaf
	public int getMax() {
		return rightmostLeaf.key;
	}

	/**
	 * Returns the key for this node.
	 * @return The value of {@link #key} for this node.
	 */
	public int getKey() {
		return key;
	}

	/**
	 * Returns the label for this node, which is {@link #name}.
	 * @return The value of {@link #name} for this node.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Draws this node inside cell, the GridCell that it is attached to.
	 * Size checks are done before this step.  Labels are drawn if there is
	 * no label overlap with previously drawn nodes.  This function splits
	 * the edges into horizontal and vertical components for {@link #drawInCell(Color, double, boolean)},
	 * the edge drawing drawInCell.  This function gets input from {@link #drawInCell(ArrayList, double)},
	 * the color and drawing depth function.
	 * 
	 * @param col
	 *            The color to draw this node, determined by the calls
	 *            from {@link #drawInCell(ArrayList, double)}, which is responsible
	 *            for picking color and plane.
	 * @param plane
	 *            The plane to draw this node in, determined by {@link #drawInCell(ArrayList, double)}.
	 * 
	 * @see net.sourceforge.olduvai.accordiondrawer.GridCell
	 * @see #drawInCell(ArrayList, double)
	 * @see #drawInCell(Color, double, boolean)
	 */
	public void drawInCell(Color col, double plane) {
		for (int xy = 0; xy < 2; xy++) {
			if (getEdge(xy))
				drawInCell(col, plane, xy == 0);
		}
	}

	/**
	 * The color and drawing depth determining function that calls down
	 * to {@link #drawInCell(Color, double)}, for edge splitting (horizontal and vertical)
	 * @param col An array of colors from {@link AccordionTreeDrawer#getColorsForCellGeom(CellGeom)}
	 * @param plane The drawing plane for this cell
	 */
	public void drawInCell(ArrayList col, double plane) {

		if (cell.getDrawnFrame() >= cell.drawer.getFrameNum())
			return; // already drawn in this frame
		cell.setDrawnFrame(cell.drawer.getFrameNum());
		if (isLeaf()) // drawing counters for debugging
			((AccordionTreeDrawer) cell.drawer).leafDrawCount++;
		else
			((AccordionTreeDrawer) cell.drawer).internalDrawCount++;
		Color c = null;
		if (col != null && col.size() > 0) {
			c = (Color) col.get(0);
			plane = cell.drawer.gethiliteplane() - .004 * c.getAlpha();
		}
		drawInCell(c, plane);
	}

	/**
	 * Sets the grid cell for this node to the given value.  Cells bound nodes in all directions.
	 * @param c The cell for this node.
	 */
	protected void setCell(GridCell c) {
		cell = c;
	}

	/**
	 * Gets the grid cell that surrounds this node.
	 * @return A grid cell that contains this node.  This is for referencing additional information not stored in each node (such as the drawer object), so we don't store more pointers than necessary for common objects.
	 */
	public GridCell getCell() {
		return cell;
	}

	/**
	 * Tests to see if this node has a vertical or horizontal edge component.
	 * @param xy 0/X for horizontal, 1/Y for vertical nodes.
	 * @return True if this node has an edge in the chosen direction.  Only root nodes don't have a horizontal edge, and only leaves don't have vertical edges.
	 */
	protected boolean getEdge(int xy) {
		if (xy == 0)
			return !isRoot();
		else
			return !isLeaf();
	}

	/** Implements Comparable interface - sorts on key field. 
	 * @param o The other object to compare this node to.
	 * @return -1 if this is smaller than the object's key, +1 if greater, 0 if equal. */
	public int compareTo(Object o) {
		if (key == ((TreeNode) o).key)
			return 0;
		else if (key < ((TreeNode) o).key)
			return -1;
		else
			return 1;
	}

	/** The parent of this node.  This is null for the root node. */
	public TreeNode parent;

	/**
	 * Node name with default "". Most internal nodes have no name and all leaf
	 * nodes have a name.  This becomes the long version of the node name when fully
	 * qualified names are used.
	 */
	protected String name = ""; // the long form in fully qualified names

	/** The text that appears when the node is highlighted or has a name displayed. */
	public String label = ""; // always short form

	/** Distance from this node to the root node. The root is at height 1. */
	protected int height;

	/** Weight is the horizontal edge length for the edge immediately above the node.  Edge lengths are not determined by this number currently; all edges are stretched to make leaves right aligned, with minimal integral lengths. */
	public float weight = 0.0f;

	/**
	 * Draw label of this TreeEdge at maximum size (intended for mouseover
	 * highlighting).  Uses current flash label box for drawing (check to make sure this isn't null before calling this function).
	 * @param fontheight Size of text to draw label.
	 * @param horiz Label is only drawn if the current edge being drawn is horizontal, this must be true.
	 */
	public void drawLabelBig(int fontheight, boolean horiz) {
		AccordionTreeDrawer drawer = (AccordionTreeDrawer) getCell().drawer;
		if (!horiz || !drawer.getDrawGeoms() || label.length() < 1)
			return;
		drawLabelBox(drawer.flashLabelBox, fontheight, true);
	}

	/** Leftmost (minimum) leaf node under this internal node (or this node for leaves). */
	public TreeNode leftmostLeaf;
	/** Rightmost (maximum) leaf node under this internal node (or this node for leaves). */
	public TreeNode rightmostLeaf;

	/** The number of leaves under this internal node (or 1 for leaves). */
	public int numberLeaves;

	/** The next preorder node. */
	public TreeNode preorderNext = null;

	/** The next postorder node. */
	public TreeNode posorderNext = null;

	/**
	 * Default tree node constructor.
	 * Children list initially set to capacity 2 as in most case binary.
	 * 	Used in 2 places: create the root when creating the tree;
	 *  the parser uses this to create nodes attached to the root.
	 */
	public TreeNode() {
		children = new ArrayList(2);
		bcnScore = new Double(0.0);
	}

	/**
	 * Main drawing function for tree nodes, draws one edge.  
	 * Draws either the horizontal or vertical tree edge for this node.
	 * Calls label drawing functions after finished if this is a horizontal edge.
	 * @param col Color to draw edge.
	 * @param plane Plane in which to draw edges.
	 * @param horiz True for horizontal edges, false for vertical.
	 */
	private void drawInCell(Color col, double plane, boolean horiz) {
		int X = horiz ? 0 : 1;
		int Y = horiz ? 1 : 0;
		AccordionTreeDrawer atd = (AccordionTreeDrawer) cell.drawer;
		boolean isBase = false; // setup for postscript drawing, stays false if no color
		if (col == null) {
			col = atd.objectColor; // default drawing color for unmarked nodes
			isBase = true; // base pass since no color
		}

		if (!atd.getDrawGeoms())
			return;
		AccordionTreeDrawer.countDrawnFrame++;
		GL gl = cell.drawer.getCanvas().getGL();
		Color drawColor = setGLColor(atd, col);

		// draw the line
		int thick = atd.getLineThickness();
		if (col != atd.getObjectColor() && !atd.isDoingFlash())
			thick += 2;
		float[] posStart = new float[2], posEnd = new float[2];
		setPositions(posStart, posEnd, horiz);// , atd.drawDirection);
		if (atd.takeSnapshot) {
			if ((atd.basePass && isBase) || (atd.groupPass && !isBase))
				try {
					int cellMinPix[] = { atd.w2s(posStart[X], X),
							atd.w2s(posStart[Y], Y) };
					int cellMaxPix[] = { atd.w2s(posEnd[X], X),
							atd.w2s(posEnd[Y], Y) };
					atd.snapShotWriter.write(thick + " setlinewidth newpath "
							+ cellMinPix[X] + " " + cellMinPix[Y] + " moveto "
							+ cellMaxPix[X] + " " + cellMaxPix[Y] + " lineto "
							+ "closepath " + "gsave " + drawColor.getRed()
							/ 255f + " " + drawColor.getGreen() / 255f + " "
							+ drawColor.getBlue() / 255f + " setrgbcolor "
							+ "stroke grestore\n");
				} catch (IOException ioe) {
					System.out.println("Error: IOException while trying to write cell to file: "
									+ atd.snapShotWriter.toString());
				}

		} else {
			atd.setColorGL(drawColor);
			gl.glLineWidth(thick);
			gl.glBegin(GL.GL_LINES);
			gl.glVertex3d(posStart[0], posStart[1], plane);
			gl.glVertex3d(posEnd[0], posEnd[1], plane);
			gl.glEnd();
		}

		if (horiz) { // draw the dot at the end of the horizontal section
			if (atd.takeSnapshot) {
				if ((atd.basePass && isBase) || (atd.groupPass && !isBase))
					try {
						int cellMaxPix[] = { atd.w2s(posEnd[X], X),
								atd.w2s(posEnd[Y], Y) };
						atd.snapShotWriter.write("newpath "
								+ (cellMaxPix[X] - thick) + " "
								+ (cellMaxPix[Y] - thick) + " moveto "
								+ (cellMaxPix[X] - thick) + " "
								+ (cellMaxPix[Y] + thick) + " lineto "
								+ (cellMaxPix[X] + thick) + " "
								+ (cellMaxPix[Y] + thick) + " lineto "
								+ (cellMaxPix[X] + thick) + " "
								+ (cellMaxPix[Y] - thick) + " lineto "
								+ "closepath " + "gsave " + drawColor.getRed()
								/ 255f + " " + drawColor.getGreen() / 255f
								+ " " + drawColor.getBlue() / 255f
								+ " setrgbcolor " + "eofill grestore\n");
					} catch (IOException ioe) {
						System.out.println("Error: IOException while trying to write cell to file: "
										+ atd.snapShotWriter.toString());
					}

			} else {
				gl.glPointSize((float) (thick + 2.0f));
				gl.glBegin(GL.GL_POINTS);
				gl.glVertex3d(posEnd[0], posEnd[1], plane);
				gl.glEnd();
			}
		}
		if (horiz && atd.drawlabels && label != null && label.length() > 0
				&& !atd.basePass && !atd.groupPass) {
			LabelBox fits = fitLabelBox(label, posStart, posEnd, atd);
			if (fits != null)
				drawLabelBox(fits, fits.getFontHeight(), false);
		}
	}

	/**
	 * Fitting function for labels given a range of label sizes, first, check
	 * smallest font, to see if anything at all can fit.
	 * 
	 * If it does, then do binary search to find largest possible box that fits.
	 * after we know which box size to use, draw it.
	 * 
	 * This algorithm means drawing order will have a radical effect on how
	 * labels look!
	 * 
	 * We should set min to one less than the real bound. new real bound is
	 * minFontSize+1 (because we already tested minFontSize), so leave min set
	 * to current value of (minFontSize+1)-1 = minFontSize
	 * 
	 * @param label String to place in label.
	 * @param posStart starting location for label, in world-space.
	 * @param posEnd ending location for label, in world-space.
	 * @param atd Drawer for this tree node.
	 * @return Biggest label box that can fit in the given remaining space. Null if no possible label fits.
	 */
	private LabelBox fitLabelBox(String label, float[] posStart,
			float[] posEnd, AccordionTreeDrawer atd) {
		LabelBox fits = null;
		int min = atd.getMinFontHeight();
		int max = atd.getMaxFontHeight();

		int mid;
		LabelBox minFit = makeLabelBox(min, -1, -1, posStart, posEnd,
				atd.labelAtLeaves);
		ArrayList intersectMin = intersectLabelBox(minFit, atd.getDrawnLabels());
		if (intersectMin.size() == 0) { // at least one font in [min, max] fits,
										// continue
			fits = minFit; // worst case is the smallest font fits
			if (max != min) { // if (max = min) then we're done, only one
								// choice
				LabelBox maxFit = makeLabelBox(max, -1, -1, posStart, posEnd,
						atd.labelAtLeaves);
				ArrayList intersectMax = intersectLabelBox(maxFit, atd
						.getDrawnLabels());
				if (intersectMax.size() == 0) { // max fits with no overlaps,
												// we're done, label is max size
					fits = maxFit;
				} else
					while (min != max && (max-min > 1)) {
						
						mid = (int) ((min + max) / 2.0);
						LabelBox currentMidLabel = makeLabelBox(mid, -1, -1,
								posStart, posEnd, atd.labelAtLeaves);
						ArrayList intersectMid = intersectLabelBox(
								currentMidLabel, intersectMax);
						if (intersectMid.size() > 0) { // something intersects,
														// mid is the new max
							max = mid;
							intersectMax = intersectMid; // smaller set to
															// check
															// intersections
						} else { // no intersections, check higher than mid
							min = mid;
							fits = currentMidLabel;
						}
					} // end max check, fits will be returned with max label
						// size
				// end while loop, fits will be the best choice between min and
				// max
			} // end of original max!=min, fits will be returned with the only
				// choice
		} // end of original min intersecting, fits will be returned empty on
			// else case

		return fits;
	}

	/**
	 * Clean this node of children.
	 */
	public void close() {
		children.clear();
	}

	/**
	 * Destroy this node.  Runs {@link #close()}.
	 */
	protected void finalize() throws Throwable {

		try {
			close();
		} finally {
			super.finalize();
			// System.out.println("finally clean treeNodes");
		}
	}

	/**
	 * Set the name for this node, the name is usually the label drawn with this node.
	 * @param s The new value of {@link #name}, the name for this node.
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 * Get the number of children under this node.
	 * @return Number of nodes stored in the children array {@link #children}.
	 */
	public int numberChildren() {
		return children.size();
	}

	/**
	 * Get a given child for this node, with range checking and casting.
	 * @param i The child index to get.
	 * @return The i(th) child for this node.
	 */
	public TreeNode getChild(int i) {
		if (i < children.size())
			return (TreeNode) children.get(i);
		else
			return null;
	}

	/**
	 * Tests to determine if this node is a leaf.  Does not work for nodes not in the tree structure.
	 * @return True if this node has no linked children, and therefore is a leaf node for the tree.
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * Tests to determine if this node is the root of its tree. Does not work for nodes not in the tree structure.
	 * @return True if this node has no linked parent, and therefore is the root of the tree.
	 */
	public boolean isRoot() {
		return (null == parent);
	}

	/**
	 * Tests nodes for equality, based on the name of the node.
	 * @param n Second node to test vs. this node.
	 * @return True if the names of both nodes are the same, false otherwise.
	 */
	public boolean equals(TreeNode n) {
		return (name.equals(n.name));
	}

	/**
	 * Add a child to the end of the list of children.  Note there is no remove child method, this is permanent.
	 * Additional processing for linking nodes (setting up pointers and leaf properties, for example) is done later.
	 * @param n New child node for this node.
	 */
	public void addChild(TreeNode n) {
		children.add(n);
		n.parent = this;
	}
	/**
	 * Get the parent for this node.
	 * @return Value of {@link #parent}.
	 */
	public TreeNode parent() {
		return parent;
	}

	/**
	 * Set the weight of this treenode, which encodes the length of the horizontal edge.
	 * Edge weights are not implemented currently for drawing.
	 * @param w New edge weight for this node, {@link #weight}.
	 */
	public void setWeight(double w) {
		weight = (float) w;
	}

	/**
	 * Get the weight of this treenode, which encodes the length of the horizontal edge.
	 * Edge weights are not implemented currently for drawing.
	 * @return Edge weight for this node, {@link #weight}.
	 */
	public float getWeight() {
		return weight;
	}

	/** Get the first child of this node. Doesn't work with leaf nodes.
	 * @return First child of this internal node.
	 */
	protected TreeNode firstChild() {
		return (TreeNode) children.get(0);
	}

	/** Get the last child of this node. Doesn't work with leaf nodes.
	 * @return Last child of this internal node.
	 */
	public TreeNode lastChild() {
		return (TreeNode) children.get(children.size() - 1);
	}

	/**
	 * Test function for determining if one node is an ancestor of another.
	 * @return <code>true</code> if this is an ancestor of <code>that</code>
	 * @param that
	 *            Another TreeNode object
	 */
	public boolean isAncestorOf(TreeNode that) {
		if (leftmostLeaf.getLindex() > that.leftmostLeaf.getLindex()
				|| rightmostLeaf.getLindex() < that.rightmostLeaf.getLindex())
			return false;
		return true;

	}

	/**
	 * Compute the lowest common ancestor between this node and
	 * <code>that</that>.
	 * The two nodes must belong to the same tree.
	 * @param    that A TreeNode in the Tree that this TreeNode belongs to
	 * @return   the lowest common ancestor between this node and "that"
	 */

	public TreeNode lca(TreeNode that) {
		if (that.isAncestorOf(this))
			return that;
		TreeNode current = this;
		while (current != null) {
			if (current.isAncestorOf(that))
				return current;
			current = current.parent;
		}
		return null;
	}

	/**
	 * Compute the lowest common ancestor between this leaf and "that" The two
	 * nodes must belong to the same tree and must be leaves
	 * 
	 * @param that
	 *            A TreeNode in the Tree that this TreeNode belongs to
	 * @return the lowest common ancestor between this leaf and
	 *         <code>that</code>, null if one of the nodes is not a leaf
	 */
	public TreeNode leafLca(TreeNode that) {

		if (!isLeaf())
			return null;
		if (!that.isLeaf())
			return null;
		TreeNode current = this;
		int a = that.getLindex();
		if (getLindex() > that.getLindex()) {
			current = that;
			a = getLindex();
		}
		for (; current != null; current = current.parent) {
			if (current.rightmostLeaf.getLindex() >= a)
				return current;
		}
		return null;
	}

	/**
	 * Long form printing for a single node. Used in conjunction with
	 * {@link #printSubtree()} to display a whole subtree.
	 * 
	 */
	public void print() {
		if (name != null)
			System.out.print("node name: " + name + "\t");
		else
			System.out.print("node name null,\t");
		System.out.println("key: " + key);
	}

	/**
	 * For debugging, prints the subtree contents, recursive.
	 * 
	 */
	private void printSubtree() {
		print();
		for (int i = 0; i < children.size(); i++)
			getChild(i).printSubtree();
	}

	/**
	 * Set the extreme leaves for this node.  This is done in leaf->root direction, so all linking can be done in O(n) time.
	 *
	 */
	public void setExtremeLeaves() {
		if (isLeaf()) {
			leftmostLeaf = this;
			rightmostLeaf = this;
			return;
		}
		leftmostLeaf = firstChild().leftmostLeaf;
		rightmostLeaf = lastChild().rightmostLeaf;
	}

	/** root->leaf traversal, depth first in direction of leftmost leaf. */
	public void linkNodesInPreorder() {
		if (isLeaf())
			return;
		preorderNext = firstChild();
		for (int i = 0; i < numberChildren() - 1; i++)
			getChild(i).rightmostLeaf.preorderNext = getChild(i + 1);
		// rightmostLeaf.preorderNext = null; // redundant
	}

	/** Leaf->root traversal, starting at leftmost leaf of tree. */
	public void linkNodesInPostorder() {
		if (isLeaf())
			return;
		// n.posorderNext = null; // redundant
		for (int i = 0; i < numberChildren() - 1; i++)
			getChild(i).posorderNext = getChild(i + 1).leftmostLeaf;
		lastChild().posorderNext = this;
	}

	/**
	 * Sets the number of leaves, must be run on leaves first (pre-order)
	 * 
	 * @return The number of leaves ({@link #numberLeaves}) including the
	 *         current node (leaves = 1)
	 */
	public int setNumberLeaves() {
		numberLeaves = 0;
		if (isLeaf())
			numberLeaves = 1;
		else
			for (int i = 0; i < children.size(); i++)
				numberLeaves += getChild(i).numberLeaves;
		return numberLeaves;
	}

	/**
	 * String value of this node, name + key + tree height information.
	 * @return String representation of this node.
	 */
	public String toString() {
		// String edge[] = {edges[0]!=null?edges[0].toString():"X",
		// edges[1]!=null?edges[1].toString():"Y"};
		return name + "(" + key + " @ " + height + ")";
	}

	/**
	 * Set the {@link #bcnScore} for this node.
	 * @param n New value of {@link #bcnScore}.
	 */
	public void setBcnScore(float n) {
		bcnScore = new Double(n);
	}

	/**
	 * Get the BCN score for this treenode.
	 * @return Value of {@link #bcnScore} for this node.
	 */
	public Double getBcnScore() {
		return bcnScore;
	}

	// where the horizontal line is drawn
	// this CAN'T be recursive, that would be too expensive
	/**
	 * Get the position of the vertical world-space position of the horizontal tree edge for this node.
	 * @return World-space vertical position of this node's horizontal edge. 
	 */
	public double getMidY() {
		if (computedFrame >= cell.drawer.getFrameNum())
			return midYPosition;
		if (isLeaf()) {
			midYPosition = getSize(AccordionDrawer.Y) * 0.5
					+ cell.getMinSplitAbsolute(AccordionDrawer.Y);
		} else if (children.size() == 1) {
			midYPosition = ((TreeNode) children.get(0)).getMidY();
		} else {
			// halfway between the offsets of end children:
			TreeNode child0 = (TreeNode) children.get(0);
			TreeNode childN = (TreeNode) children.get(children.size() - 1);
			midYPosition = (child0.cell.getMaxSplitAbsolute(AccordionDrawer.Y) + 
					childN.cell.getMinSplitAbsolute(AccordionDrawer.Y)) / 2;
		}
		computedFrame = cell.drawer.getFrameNum();
		return midYPosition;
	}

	// where the vertical line stops
	/**
	 * Get the vertical starting point (world-space) for the vertical tree edge for this node.
	 * Returns the position of the middle of the cell for the first child of this node.
	 * @return Vertical position of the middle of the cell for the first child of this node. 
	 */
	private double getMinY() {
		if (isLeaf())
			return -1.0; // no vertical line for leaf
		return ((TreeNode) children.get(0)).getMidY();
	}

	// where the vertical line stops
	/**
	 * Get the vertical endpoint (world-space) for the vertical tree edge for this node.
	 * Returns the position of the middle of the cell for the last child of this node.
	 * @return Vertical position of the middle of the cell for the last child of this node. 
	 */
	public double getMaxY() {
		if (isLeaf())
			return -1.0; // no vertical line for leaf
		return ((TreeNode) children.get(children.size() - 1)).getMidY();
	}

	/**
	 * Gets the cell world-space size (max - min) for this node's cell in the given direction.
	 * @param xy Direction for getting size.
	 * @return Size for the cell in one dimension.
	 */
	private double getSize(int xy) {
		return cell.getMaxSplitAbsolute(xy) - cell.getMinSplitAbsolute(xy);
	}

	/**
	 * Test function to determine if this tree node is picked.
	 * @param x Horizontal cursor world-space position.
	 * @param y Vertical cursor world-space position.
	 * @param xFuzz World-space maximum horizontal distance for cursor to this node's cell. 
	 * @param yFuzz World-space maximum vertical distance for cursor to this node's cell.
	 * @return true if both x and y cursor positions are close enough to the tree edges for this node to suggest that this tree node is picked.
	 */
	public boolean isNodePicked(double x, double y, double xFuzz, double yFuzz) {
		double min[] = { cell.getMinSplitAbsolute(AccordionDrawer.X),
				cell.getMinSplitAbsolute(AccordionDrawer.Y) };
		double max[] = { cell.getMaxSplitAbsolute(AccordionDrawer.X),
				cell.getMaxSplitAbsolute(AccordionDrawer.Y) };
		double midY = getMidY(); // where to draw the horizontal line
		return (x > max[AccordionDrawer.X] - xFuzz
				&& x < max[AccordionDrawer.X] + xFuzz
				&& y > min[AccordionDrawer.Y] - yFuzz 
				&& y < max[AccordionDrawer.Y] + yFuzz)
				|| (x > min[AccordionDrawer.X] - xFuzz
					&& x < max[AccordionDrawer.X] + xFuzz
					&& y > midY - yFuzz 
					&& y < midY + yFuzz);
	}

	/**
	 * Test function for cursor proximity to a given world-space position.
	 * @param value World-space cursor position to check.
	 * @param fuzz World-space maximum distance for cursor to this node's cell.
	 * @param xy 0/1 value for X/Y
	 * @return true if cursor is close enough or inside the cell for this node.
	 */
	private boolean xyInRange(double value, double fuzz, int xy) {
		return cell.getMinSplitAbsolute(xy) - fuzz <= value
				&& cell.getMaxSplitAbsolute(xy) + fuzz >= value;
	}

	// removed recursion
	// needs to be stack based for picking to left/right of large subtrees
	/**
	 * Non-recursion method used to descend the tree structure, finding the node close enough
	 * to the given cursor position.
	 * @param x Cursor horizontal position.
	 * @param y Cursor vertical position.
	 * @param xFuzz Horizontal threshold for picking.
	 * @param yFuzz Vertical threshold for picking.
	 * @return Treenode close enough to x/y, within xFuzz/yFuzz.
	 */
	public TreeNode pickDescend(double x, double y, double xFuzz, double yFuzz) {
		Stack pickingStack = new Stack();
		pickingStack.push(this);
		while (pickingStack.size() > 0) {
			TreeNode currRoot = (TreeNode) pickingStack.pop(); // next root to
																// check
			if (currRoot.isNodePicked(x, y, xFuzz, yFuzz))
				return currRoot;
			if (currRoot.isLeaf() || // unpicked leaf
					!currRoot.xyInRange(y, yFuzz, AccordionDrawer.Y) || // bad Y value
					currRoot.cell.getMinSplitAbsolute(AccordionDrawer.X) > x) // bad X value
			{
				continue;
			}
			// some child must have y in range
			int minChild = 0;
			int maxChild = currRoot.children.size() - 1;
			int midChild = (minChild + maxChild + 1) / 2;
			TreeNode currChild = (TreeNode) (currRoot.children.get(midChild));
			while (minChild != maxChild && // converged to currChild, currChild
											// is descended or picked
					!currChild.xyInRange(y, 0.0, AccordionDrawer.Y)) 
				// binary search in Y, no fuzz
			{ // find appropriate child
				if (currChild.cell.getMinSplitAbsolute(AccordionDrawer.Y) > y)
					// curr child is too big, max = mid
					maxChild = midChild;
				else
					minChild = midChild;
				if (minChild + 1 == maxChild && midChild == minChild)
					midChild = maxChild;
				else
					midChild = (minChild + maxChild) / 2;
				currChild = (TreeNode) (currRoot.children.get(midChild));
			}
			if (currChild.isNodePicked(x, y, xFuzz, yFuzz)) {
				return currChild; // found something
			}
			if (midChild > 0)
				pickingStack.push(currRoot.children.get(midChild - 1));
			if (midChild < currRoot.children.size() - 1)
				pickingStack.push(currRoot.children.get(midChild + 1));
			pickingStack.push(currChild); // the next root
		}
		return null;
	}

	// 
	/**
	 * Assuming this is a leaf, return the leaf index (the split position in Y
		direction, actually)
		@return Leaf index of this node.
	 */
	public int getLindex() {
		if (cell == null)
			System.out.println("Error when finding Lindex for " + this);
		return ((StaticSplitLine) cell.getMaxLine(AccordionDrawer.Y))
				.getSplitIndex();
	}

	// don't draw self, just call this on parents after calling drawDescend
	// ascend drawing until the root is drawn or the node drew in the frame
	// already
	/**
	 * Don't draw self, just call this on parents after calling drawDescend 
	 * ascend drawing until the root is drawn or the node drew in the frame
	 * already.
	 * @param frameNum Current frame number.
	 * @param plane Plane to draw nodes in. 
	 */
	public void drawAscend(int frameNum, float plane) {
		if (cell.getDrawnFrame() < frameNum) {
			drawInCell(cell.drawer.getColorsForCellGeom(this), plane);
			TreeNode n = parent;
			while (n != null) {
				n.drawInCell(cell.drawer.getColorsForCellGeom(n), plane);
				n.cell.setDrawnFrame(frameNum);
				n = n.parent;
			}
		}
	}

	// don't draw self, just call this
	/**
	 * Descend from this node and draw children down to leaves. The direction of
	 * descent must draw leaves between min and max, indices into the list of
	 * leaves.  This node is also drawn.
	 * @param frameNum Current frame number.
	 * @param plane Drawing plane for this node.
	 * @param min Index of leaf with minimum node key.
	 * @param max Index of leaf with maximum node key.
	 * @param tree Tree that this node belongs to.
	 * 
	 */
	public void drawDescend(int frameNum, float plane, int min, int max,
			Tree tree) {
		drawInCell(cell.drawer.getColorsForCellGeom(this), plane);
		TreeNode currNode = this;
		// binary search
		int minChild = 0;
		int maxChild = currNode.children.size() - 1;
		int midChild = (minChild + maxChild) / 2;
		TreeNode minLeaf = tree.getLeaf(min + 1);
		TreeNode maxLeaf = tree.getLeaf(max);

		while (!currNode.isLeaf()) {
			TreeNode midNode = currNode.getChild(midChild);
			if (midNode.leftmostLeaf == null || midNode.rightmostLeaf == null)
				System.out
						.println("Debug: error, left or rightmost leaf is null");
			if (midNode.leftmostLeaf.key > maxLeaf.key) // left > max => binary
														// search
			{
				maxChild = midChild;
			} else if (midNode.rightmostLeaf.key < minLeaf.key) {
				if (maxChild - minChild == 1)
					minChild = maxChild;
				else
					minChild = midChild;
			} else // mid <= max & mid >= min, draw and descend mid
			{
				midNode.drawInCell(cell.drawer.getColorsForCellGeom(midNode),
						plane);
				currNode = midNode;
				minChild = 0;
				maxChild = currNode.children.size() - 1;
			}
			midChild = (minChild + maxChild) / 2;
		}
	}

	/**
	 * Draws this TreeEdge inside the GridCell to which it is attached, and its
	 * label if appropriate.
	 * 
	 * Find font size to use for drawing the label, by checking for occlusions.
	 * Don't draw it all if it's occluded even at the smallest size.
	 * 
	 * @param atd Tree drawer for this node/tree
	 * @param col
	 *            The color to draw this node in
	 * @see net.sourceforge.olduvai.accordiondrawer.GridCell
	 */

	private Color setGLColor(AccordionTreeDrawer atd, Color col) {
		float rgbcol[] = new float[3];
		Color newrgb;
		if (atd.dimcolors || atd.dimbrite) {

			col.getRGBColorComponents(rgbcol);
			float hsbcol[] = Color.RGBtoHSB((int) (rgbcol[0] * 255),
					(int) (rgbcol[1] * 255), (int) (rgbcol[2] * 255),
					(new float[3]));
			float howdim;
			// H stays the same
			// if started out greyscale, don't crank up S
			if (hsbcol[1] > 0f && atd.dimcolors) {
				// depends on size of your subtended subtree screen area
				InteractionBox b = atd.makeBox(this); // box around subtree
				if (null == b)
					System.out.println("interaction box null");
				// howdim base: one pixel 0, fullscreen 1.0
				howdim = (float) (b.getMax(AccordionDrawer.Y) - b
						.getMin(AccordionDrawer.Y))
						/ (float) atd.getWinMax(1); // screen coords
				// small dynamic range:, 0%=.5, 20% and up =1.0
				howdim = .5f + 40 * howdim;
				howdim = (howdim > 1.0) ? 1.0f : howdim;
				hsbcol[1] = (float) howdim;
			}

			if (atd.dimbrite && col == atd.getObjectColor()) {
				// depends on depth in tree
				int treeheight = atd.tree.getHeight();
				// howdim ranges from 0 at root to 1.0 at leaf
				howdim = (height - 1.0f) / (treeheight - 1.0f);
				// compress the range
				hsbcol[2] = (float) (0.2 + howdim * .7);
			}
			newrgb = Color.getHSBColor(hsbcol[0], hsbcol[1], hsbcol[2]);
		} else {
			newrgb = col;
		}
		return newrgb;
	}

	/**
	 * Sets positions for a tree edge, starting and ending points for the lines
	 * that make a single edge. Horizontal edges depend on the
	 * {@link #getMidY()} function to compute links to a viable location between
	 * the min and max child.
	 * 
	 * @param start
	 *            Set by this function, a starting X/Y position in
	 *            world-coordinates
	 * @param end
	 *            Set by this function, an ending X/Y position in
	 *            world-coordinates
	 * @param horiz
	 *            Passed in parameter, horizontal true for edges that do not
	 *            change in Y from start to end, false=vertical, X doesn't
	 *            change
	 */
	protected void setPositions(float[] start, float[] end, boolean horiz)// ,
																			// int
																			// direction)
	{
		int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
		GridCell c = getCell();
		if (horiz) {
			start[X] = (float) c.getMinSplitAbsolute(X); // X is over the
															// range
			end[X] = (float) c.getMaxSplitAbsolute(X);
			start[Y] = end[Y] = (float) getMidY();
		} else {
			start[X] = end[X] = (float) c.getMaxSplitAbsolute(X);
			start[Y] = (float) getMinY();
			end[Y] = (float) getMaxY();
		}
	}

	/**
	 * Create a LabelBox for the given fontheight and positioning information.
	 * 
	 * @param fontheight Size of the font in pixels. 
	 * @param x
	 *            horizontal base location in screen/pixel coordinates
	 * @param y
	 *            vertical base location in screen/pixel coordinates
	 * @param start
	 * 			  starting position for the tree node (horizontal edge), in world-space coordinates
	 * @param end
	 * 			  ending position for the tree ndoe (horizontal edge), in world-space coordinates
	 * @param labelAtLeaves
	 * 			  if true, we label at the leaves after the edge of the max stuck line, unlike the internal drawing that starts on the horizontal edge 
	 * @see net.sourceforge.olduvai.accordiondrawer.LabelBox
	 */

	protected LabelBox makeLabelBox(int fontheight, int x, int y,
			float[] start, float[] end, boolean labelAtLeaves) {
		AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;
		int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
		int bottomLeft[] = new int[2];
		int topRight[] = new int[2];
		int bottomLeftBackground[] = new int[2];
		int startPix[] = { d.w2s(start[X], X), d.w2s(start[Y], Y) };
		int endPix[] = { d.w2s(end[X], X), d.w2s(end[Y], Y) };
		String name = label;
		int namewidth = d.stringWidth(name,
				AccordionDrawer.DEFAULTLABELFONT.deriveFont(1f * fontheight)); // expensive
		int labelwidth = namewidth + 2;
		int[] winSize = { d.getWinsize(X), d.getWinsize(Y) };

		int moveover = (isLeaf() || labelAtLeaves) ? labelwidth
				+ d.labeloffset[X] : -d.labeloffset[X];
		// if(d.drawDirection != AccordionDrawer.RIGHT)

		if (labelAtLeaves) {
			startPix[X] = d.w2s(d.getSplitAxis(X).getMaxStuckValue(), X);
			endPix[X] = startPix[X] + labelwidth;
			moveover = d.labeloffset[X];
		}
		// d.labelposright is usually true, never set to false in original
		if (d.getLabelPosRight()) {
			topRight[X] = endPix[X] + moveover;
		} else
			topRight[X] = startPix[X] - d.labeloffset[X];

		// either put label right and above junction
		// or put it left, vertically centered, and right-justified
		if (!d.getLabelPosRight()) {
			topRight[Y] = startPix[Y] - d.labeloffset[Y] - fontheight;
		} else
			topRight[Y] = (isLeaf() || labelAtLeaves) ? endPix[Y]
					- (int) (fontheight / 2.0) : endPix[Y] - d.labeloffset[Y]
					- fontheight;

		// we pad background label box by one pixel on each side so
		// that text is never right up against the edge. height
		// already has enough padding
		int labelheight = fontheight + 2;
		bottomLeftBackground[X] = topRight[X] - labelwidth;
		bottomLeftBackground[Y] = topRight[Y] + labelheight;

		if (x >= 0 && y >= 0) {
			bottomLeftBackground[X] = x - (int) (labelwidth / 2.0);
			topRight[X] = bottomLeftBackground[X] + labelwidth;
			bottomLeftBackground[Y] = y - 5;
			topRight[Y] = bottomLeftBackground[Y] - labelheight;
		}

		// if we would start offscreen, nudge over until it's all
		// onscreen. when the origin of the text string is offscreen,
		// opengl won't draw anything (sigh). we could truncate the
		// label text, but it seems more useful to just move the whole
		// thing over. while we're at it, make sure labels are always
		// totally onscreen.

		if ((bottomLeftBackground[X]) < 0) {
			bottomLeftBackground[X] = 0;
			topRight[X] = bottomLeftBackground[X] + labelwidth;
		} else if (topRight[X] > winSize[X]) {
			// do nothing - in leaf case, we want to truncate not nudge
			// as per hillis feedback.
		}

		if (bottomLeftBackground[Y] > winSize[Y]) {
			bottomLeftBackground[Y] = winSize[Y];
			topRight[Y] = bottomLeftBackground[Y] - labelheight;
		} else if (topRight[Y] < 0) {
			topRight[Y] = 0;
			bottomLeftBackground[Y] = labelheight;
		}

		// labelbuffer is used for occlusion computations to control
		// label density, but we don't want to draw the bg box that
		// big because it would look silly.
		bottomLeft[Y] = bottomLeftBackground[Y] + d.labelbuffer[Y];
		bottomLeft[X] = bottomLeftBackground[X] - d.labelbuffer[X];
		return new LabelBox(bottomLeft, topRight, bottomLeftBackground, d
				.getFrameNum(), name, this, fontheight);
	}

	/**
	 * Occlusion check of LabelBox against array of all drawn labels.
	 * {@link AccordionDrawer#getDrawnLabels()}. Returns a list of all
	 * overlapping labels, when the label box intersects with an existing label
	 * box. 2007/02 added check for labeling leaves The list is no longer
	 * sorted. We now pass in an array of placed labels to examine, which may be
	 * all visible labels initally, and a subset as the possible overlaps are
	 * reduced in our font downsizing in
	 * {@link #fitLabelBox(String, float[], float[], AccordionTreeDrawer)} Since
	 * the number of labels tends to be small (100 concurrent labels would be
	 * very difficult to read) we can search the list of existing labels with a
	 * linear scan.)
	 * 
	 * @param lb
	 *            LabelBox to check for overlaps
	 * @param checkLabels
	 *            Array of labels that we may check against for overlaps.
	 * @return List of any labels that overlap the labelbox.
	 * 
	 * @see net.sourceforge.olduvai.accordiondrawer.LabelBox
	 */
	private ArrayList intersectLabelBox(LabelBox lb, ArrayList checkLabels) {
		AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;

		ArrayList returnList = new ArrayList();
		Iterator iter = checkLabels.iterator();
		while (iter.hasNext()) {
			LabelBox currLabel = (LabelBox) iter.next();
			if (lb.overlaps(currLabel, d.labelbuffer))
				returnList.add(currLabel);
		}
		return returnList;
	}

	/**
	 * Draw a LabelBox.  This includes the bounding box and label, identified by the label box input.
	 * 
	 * @param lb
	 *            LabelBox to use, which includes the string for the label and bounding box information.
	 * @param fontheight
	 *            Size of font to use, in points/pixels.
	 * @param drawBig
	 *            whether to draw maximum size ignoring occlusions
	 */
	private void drawLabelBox(LabelBox lb, int fontheight, boolean drawBig) {
		int X = AccordionDrawer.X, Y = AccordionDrawer.Y;
		AccordionTreeDrawer d = (AccordionTreeDrawer) getCell().drawer;
		GL gl = cell.drawer.getCanvas().getGL();
		ArrayList drawnLabels = d.getDrawnLabels();
		if (!drawBig) {
			drawnLabels.add(lb);
			Collections.sort(drawnLabels);
		}

		int[] pos = { lb.bottomLeftPos(0), lb.bottomLeftPos(1) };
		int[] topRightPos = { lb.topRightPos(0), lb.topRightPos(1) };

		// highlighted node drawing == drawBig

		double labelplane = 1.0;
		if (d.labeldrawback || drawBig) {
			float thecol[] = new float[3];
			if (drawBig)
				d.getLabelBackHiColor().getRGBColorComponents(thecol);
			else
				d.getLabelBackColor().getRGBColorComponents(thecol);
			gl.glColor3f(thecol[0], thecol[1], thecol[2]);
			// if (d.drawDirection != AccordionDrawer.RIGHT) {
			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex3d(d.s2w(pos[0], 0), d.s2w(pos[1], 1), labelplane);
			gl.glVertex3d(d.s2w(topRightPos[0], 0), d.s2w(pos[1], 1),
					labelplane);
			gl.glVertex3d(d.s2w(topRightPos[0], 0), d.s2w(topRightPos[1], 1),
					labelplane);
			gl.glVertex3d(d.s2w(pos[0], 0), d.s2w(topRightPos[1], 1),
					labelplane);
			gl.glEnd();

		}

		String name = lb.getName();
		int descent = d.getDescent(name,
				AccordionDrawer.DEFAULTLABELFONT.deriveFont(1f * fontheight));

		double b[] = { d.s2w(pos[X] + 1, X), d.s2w(pos[Y] - descent - 1, Y) };
		Font f = AccordionDrawer.DEFAULTLABELFONT.deriveFont(1f * fontheight);
		if (drawBig) // no shadow on highlight
		{
			d.setColorGL(d.getLabelHiColor());
			d.drawText(b[X], b[Y], name, f, d.getLabelColor(),
							labelplane, null);
		} else if (!d.labeldrawback) // shadow, not big, or background box
		{
			d.drawText(b[X], b[Y], name, f, d.getLabelColor(), labelplane, d.getLabelBackColor());
		} else // not big, background box so no shadow needed
		{
			d.drawText(b[X], b[Y], name, f, d.getLabelColor(),
							labelplane, null);
		}
	}

	/** Stub function. 
	 * @param xy Direction.
	 * @return Splitline for minimum cell bound in direction */
	public SplitLine getMinLine(int xy) {
		// non-functional, used for SJ, LRAC
		return null;
	}

	/** Stub function. 
	 * @param xy Direction.
	 * @return Splitline for maximum cell bound in direction */
	public SplitLine getMaxLine(int xy) {
		// non-functional, used for SJ, LRAC
		return null;
	}
	

}
