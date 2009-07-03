/*
 * @(#)TreeMap.java	1.65 04/02/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package net.sourceforge.olduvai.accordiondrawer;

import java.io.IOException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * A SplitAxis object represents either the X or Y axis of accordionDrawer (set
 * using the horizontal boolean). This is a heavily modified implementation of
 * red-black trees based off of Sun Java's TreeMap class API. 
 * 
 * This object contains the set of ALL SplitLine objects for this axis as well
 * as the partitionedList of SplitLine objects for this axis.
 * 
 * @see net.sourceforge.olduvai.accordiondrawer.AccordionDrawer
 * @author Peter McLachlan (spark343@cs.ubc.ca)
 * 
 */
public class SplitAxis
//extends AbstractMap
implements Cloneable, java.io.Serializable
{
	
	/**
	 * Indicate whether logging behavior is desired.  If this is enabled a check will also 
	 * be performed to confirm the existence of a SplitAxisLogger class before writing is 
	 * attempted. (In other words, it can be set to true and still perform no logging unless
	 * the SplitAxisLogger constructor is used, however, there is a performance penalty in 
	 * running this way.)    
	 */
	static final boolean LOGGING = true;
	
	/**
	 * Handles logging actions.  See {@link #LOGGING}
	 */
	private SplitAxisLogger logger = null; 
	
	/**
	 * The root split line for the represented treemap object.
	 */
	private transient SplitLine root = null;
	
	/**
	 * Hook flag for TJ: true when we use static split lines.  False for dynamic grids.
	 */
	private boolean staticLines = false; // true for TJ, with static grid
	
	/**
	 * The number of entries in the tree.
	 */
	private transient int size = 0;
	
	/**
	 * The number of structural modifications to the tree.  Monotonic increase on any add, delete, or clear of split lines from axis.
	 */
	private transient int modCount = 0;
	/**
	 * Increments counters {@link #modCount} and {@link #size}.
	 */
	private void incrementSize()   { modCount++; size++; }
	/**
	 * Decrements counters {@link #modCount} and {@link #size}. 
	 */
	private void decrementSize()   { modCount++; size--; }
		
	
	// Query Operations

	/**
	 * Removes all mappings from this TreeMap.
	 */
	public void clear() {
		modCount++;
		size = 0;
		root = null;
	}
		
	/**
	 * Value for red nodes in CRT implementation of red-black trees.
	 */
	public static final boolean RED   = false;
	/**
	 * Value for black nodes in CRT implementation of red-black trees.
	 */
	public static final boolean BLACK = true;
	
//	/**
//	 * Balancing operations.
//	 *
//	 * Implementations of rebalancings during insertion and deletion are
//	 * slightly different than the CLR version.  Rather than using dummy
//	 * nilnodes, we use a set of accessors that deal properly with null.  They
//	 * are used to avoid messiness surrounding nullness checks in the main
//	 * algorithms.
//	 */
	
	/*** Static accessors and modifiers should be in the split line class as object functions? ***/
	
	/**
	 * Returns the color of a given node (split line)
	 * @param p Split line to get color of.
	 * @return Value of {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#color}, or {@link #BLACK} if split line is null.
	 */
	private static boolean colorOf(SplitLine p) {
		return (p == null ? BLACK : p.color);
	}
	
	/**
	 * Returns the parent of a given node (split line)
	 * @param p Split line to get parent of.
	 * @return Value of {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#getParent()} for p, or null if p is null.
	 */
	private static SplitLine parentOf(SplitLine p) {
		return (p == null ? null: p.getParent());
	}
	
	/**
	 * Sets the value of {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#color} for a given split line.
	 * @param p Split line to set.
	 * @param c New color value for p.
	 */
	private static void setColor(SplitLine p, boolean c) {
		if (p != null)
			p.color = c;
	}
	
	/**
	 * Gets the left child of a given split line.
	 * @param p Node to get child from.
	 * @return Value of {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#getLeftChild()}, or null if p is null.
	 */
	private static  SplitLine leftOf(SplitLine p) {
		return (p == null) ? null: p.getLeftChild();
	}
	
	/**
	 * Gets the right child of a given split line.
	 * @param p Node to get child from.
	 * @return Value of {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#getRightChild()}, or null if p is null.
	 */
	private static  SplitLine rightOf(SplitLine p) {
		return (p == null) ? null: p.getRightChild();
	}
	
	/**
	 * Update the tree structure following an insert operation.
	 * Based on CLR implementation.
	 * @param x Split line that has just been inserted.  x changes (ascends) during this function as tree is fixed.
	 */
	private void fixAfterInsertion(SplitLine x) {
		x.color = RED;
		
		while (x != null && x != root && x.getParent().color == RED) {
			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {  // If my parent is the left child of my grandparent
				SplitLine y = rightOf(parentOf(parentOf(x))); // y is the right child of my grandparent
//				System.out.println("Y:" + y);
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == rightOf(parentOf(x))) {
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
						rotateRight(parentOf(parentOf(x)));
				}
			} else {
				SplitLine y = leftOf(parentOf(parentOf(x)));
//				System.out.println("Y:" + y);
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x),  BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
						rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
		setBounds(root);
		root.color = BLACK;
	}
	
	/* 
	 * From CLR, perform the rotation operations on this tree map to rebalance. **/
	/**
	 * Update the tree structure following a delete operation.
	 * Based on CLR implementation.
	 * @param x Parent of split line that has just been removed.  x changes (ascends) during this function as tree is fixed.
	 */
	private void fixAfterDeletion(SplitLine x) {   
		while (x != root && colorOf(x) == BLACK) {
			if (x == leftOf(parentOf(x))) {
				SplitLine sib = rightOf(parentOf(x));
				
				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}
				
				if (colorOf(leftOf(sib))  == BLACK &&
						colorOf(rightOf(sib)) == BLACK) {
					setColor(sib,  RED);
					x = parentOf(x);
				} else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} else { // symmetric
				SplitLine sib = leftOf(parentOf(x));
				
				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}
				
				if (colorOf(rightOf(sib)) == BLACK &&
						colorOf(leftOf(sib)) == BLACK) {
					setColor(sib,  RED);
					x = parentOf(x);
				} else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}
		
		setColor(x, BLACK);
	}

	/**
	 * ID for serialization purposes.
	 */
	private static final long serialVersionUID = 919286545866124006L;
	
	// OUR STUFF
	/**
	 * Object for debugging.  Checks for bounds, tree sizes, and tree properties done with this object.
	 */
	public final Debug debug = new Debug();
	
	/** Default value of minimum stuck line in X and Y directions. */
	public static final double defaultMinStuckValue = 0.001f;
	/** Default value of maximum stuck line in X and Y directions. */
	public static final double defaultMaxStuckValue = 0.999f;
	/** Default value of split line value for all bounded split lines. 
	 * For axes that have (2^n-1) split lines, this results in a uniformly spaced axis, if the tree is kept balanced. */
	public static final double defaultSplitValue = 0.5;
	/**
	 * Minimum stuck line for doing direct == comparisons 
	 * But NOT ACTUALLY IN THE TREE STRUCTURE!!
	 * Also stores the current minStuck Value, which is a world-space bound for the drawing canvas.
	 */
	protected SplitLine minStuckLine;
	/**
	 * Maximum stuck line for doing direct == comparisons 
	 * But NOT ACTUALLY IN THE TREE STRUCTURE!!
	 * Also stores the current maxStuck Value, which is a world-space bound for the drawing canvas.
	 */
	protected SplitLine maxStuckLine;   
	/**
	 * Useful quick index for the array returned from the getBounds() method.  
	 */
	public static final int minBound = 0;
	/**
	 * Useful quick index for the array returned from the getBounds() method.  
	 */
	public static final int maxBound = 1;
	/**
	 * Link to the accordion drawer using this SplitAxis object
	 */
	private AccordionDrawer ad;
	/**
	 * Whether this Split Axis is horizontal (true) or vertical (false).
	 */
	protected boolean horizontal;
	/**
	 * This contains the 'partitioned' list of splitLines to draw, including potentially 'fake' split lines that 
	 * represent the center line of a single cell (and whose absoluteValue is never referenced, used only for 
	 * the purposes of their min and max bounds.)  
	 */
	protected TreeSet partitionedList;
	
	/** 
	 * Used for {@link #resizeForest(AbstractRangeList, int, Hashtable, double)}
	 * true when the first range was to the right of the min stuck position
	 */
	static private boolean createMin = false;
	/** 
	 * Used for {@link #resizeForest(AbstractRangeList, int, Hashtable, double)}
	 * true when the last range was to the left of the max stuck position
	 */
	static private boolean createMax = false;
	
	/**
	 * Constructor for initializing split axes with pre-determined values.
	 * Calls {@link #initSplitValues(int, boolean)}
	 * @param ad Drawer that uses this split axis: {@link #ad}
	 * @param horizontal True if horizontal axis, false for vertical: {@link #horizontal}
	 * @param minStuckValue Position of minimum stuck line: {@link #minStuckLine}
	 * @param maxStuckValue Position of maximum stuck line: {@link #maxStuckLine}
	 * @param initLines Number of split lines to insert into the axis
	 * @param reset True: balance the split line tree after inserting initLines, false: do not balance, use default value for each split line: {@link #defaultSplitValue}
	 * @param staticLines True if using static split lines (TJ), false otherwise: {@link #staticLines}
	 */
	public SplitAxis(AccordionDrawer ad, SplitAxisLogger logger, boolean horizontal, 
			double minStuckValue, double maxStuckValue, int initLines, boolean reset,
			boolean staticLines)
	{
		this.ad = ad;
		this.horizontal = horizontal;
		this.staticLines = staticLines;
		this.setLogger(logger);
		
		if (staticLines)
		{
			minStuckLine = new StaticSplitLine(minStuckValue,null,null,false);
			maxStuckLine = new StaticSplitLine(maxStuckValue,null,null,false);			
		}
		else
		{
			minStuckLine = new SplitLine(minStuckValue,null, null, false);
			maxStuckLine = new SplitLine(maxStuckValue,null, null, false);
		}
		minStuckLine.setSubTreeSize(-1);
		maxStuckLine.setSubTreeSize(-1);
		
		if (initLines > 0)
			initSplitValues(initLines, staticLines);
		if (reset) {
			if (ad.uniformSplits[horizontal?0:1])
				resetSplitValues();
			else
				ad.resetSplitValues();
		}
	}
	
	/**
	 * Test constructor.
	 */
	public SplitAxis() { 
		double minStuckValue = 0.2;
		double maxStuckValue = 0.9999999999999;
		minStuckLine = new SplitLine(minStuckValue,null, null,false);
		maxStuckLine = new SplitLine(maxStuckValue,null, null,false);
		horizontal = false;
	}
	
	/**
	 * Returns the successor (next) of the specified Entry, or null if no such.
	 * Returns max stuck line if no successor elements in tree.
	 * @param t Split line to get successor for
	 * @return Successor split line, or max stuck line if given max stuck line as parameter
	 */
	private SplitLine successor(SplitLine t) {
		if (t == null)
			return null;
		// Two special cases: min stuck line and max stuck line
		if (t == maxStuckLine) { 
			System.out.println("Asking for successor to maxStuckLine!?  Returning maxStuckLine again.");
			return maxStuckLine;
		}
		if (t == minStuckLine) { 
			// Handle case if there are no splits in the tree . . .
			if (size == 0)
				return getMaxStuckLine();			
			return getMinLine();
		}
		if (t.getRightChild() != null) {  // Descend once right, then left all the way
			SplitLine p = t.getRightChild();
			while (p.getLeftChild() != null)
				p = p.getLeftChild();
			return p;  // If there is a right and no left, return right, otherwise return leftmost
			// This should never be null
		} else { // Ascend until node is a left child
			SplitLine p = t.getParent();
			SplitLine ch = t;
			while (p != null && ch == p.getRightChild()) {
				ch = p;
				p = p.getParent();
			}
			if (p == null)
				p = maxStuckLine; // started on max node
			return p; // Return first left child's parent following ascent  
		}
	}
	
	/**
	 * Returns the previous of the specified Entry, or null if no such.
	 * Returns minimum stuck line if no previous elements in tree.
	 * @param t Split line to get previous for
	 * @return Previous split line, or min stuck line if given min stuck line as parameter
	 */

	private SplitLine previous(SplitLine t) {
		if (t == null)
			return null;
		// Two special cases: min stuck line and max stuck line
		if (t == minStuckLine) { 
			System.out.println("Asking for previous to minStuckLine!?  Returning minStuckLine again.");
			return minStuckLine;
		}
		
		if (t == maxStuckLine) { 
			return getMaxLine();
		}
		
		if (t.getLeftChild() != null) {
			SplitLine p = t.getLeftChild();
			while (p.getRightChild() != null)
				p = p.getRightChild();
			return p;  // If there is a left and no right, return left, otherwise return rightmost
		} else {
			SplitLine p = t.getParent();
			SplitLine ch = t;
			while (p != null && ch == p.getLeftChild()) {
				ch = p;
				p = p.getParent();
			}
			if (p == null)
				p = minStuckLine;
			return p;  // Return next spatially adjacent to the left (or null if you are James)
		}
	}
	
	/** Based on CLR 
	 * Do a left rotate at the given node.  Helper function called by the fix functions.
	 * @param p Node to rotate at: this node becomes the left child 
	 * 			of its right child, and gains that child's left child 
	 * 			as its new right child. 
	 **/
	private void rotateLeft(SplitLine p) {
		SplitLine r = p.getRightChild();
		p.setRightChild(r.getLeftChild());
		p.subTreeSize -= r.subTreeSize; // Decrement tree size as we remove the right child
		if ( p.getRightChild() != null ) { 
			p.subTreeSize += p.getRightChild().subTreeSize; // Add the new number of children
			p.getRightChild().setParent(p);
		}
		r.setParent(p.getParent());
		if (p.getParent() == null)
			root = r;
		else if (p.getParent().getLeftChild() == p)
			p.getParent().setLeftChild(r);
		else
			p.getParent().setRightChild(r);
		
		r.setLeftChild(p);
		p.setParent(r);
		r.subTreeSize = (r.getRightChild() == null) ? 1 + r.getLeftChild().subTreeSize : 1 + r.getRightChild().subTreeSize + r.getLeftChild().subTreeSize;
		// Fix bounds
		setBounds(r);
		setBounds(r.getLeftChild()); // Fix r.left bounds
		if (p.getRightChild() != null)
			setBounds(p.getRightChild()); // Fix p.right bounds
	}
	
	/** Based on CLR 
	 * Do a right rotate at the given node.  Helper function called by the fix functions.
	 * @param p Node to rotate at: this node becomes the right child 
	 * 			of its left child, and gains that child's right child 
	 * 			as its new left child. 
	 **/
	private void rotateRight(SplitLine p) { // p is point of rotation
		SplitLine l = p.getLeftChild(); // Store p.left in l(eft)
		p.setLeftChild(l.getRightChild());
		p.subTreeSize -= l.subTreeSize;
		if ( p.getLeftChild() != null ) {
			p.subTreeSize += p.getLeftChild().subTreeSize;
			p.getLeftChild().setParent(p);  // If l's right child wasn't null, make sure its parent is set
		}
		l.setParent(p.getParent()); // Shouldn't need to touch this
		if (p.getParent() == null)
			root = l;
		else if (p.getParent().getRightChild() == p)
			p.getParent().setRightChild(l);
		else p.getParent().setLeftChild(l);
		l.setRightChild(p);
		p.setParent(l);
		l.subTreeSize = (l.getLeftChild() == null) ? 1 + l.getRightChild().subTreeSize : 1 + l.getLeftChild().subTreeSize + l.getRightChild().subTreeSize;
		// Fix bounds:
		setBounds(l);
		setBounds(l.getRightChild()); // Fix l.right bounds
		if (p.getLeftChild() != null)
			setBounds(p.getLeftChild());
	}
	
	/**
	 * Returns the next splitline greater than the parameter.  
	 * This can be maxStuckLine if line is the last line in the tree.  
	 * 
	 * @param line
	 * @return the next splitline greater than the parameter.  
	 */
	public SplitLine getNextSplit (SplitLine line) {
		if (line == minStuckLine) {
			SplitLine minLineInTree = getMinLine();
			if (minLineInTree == null)
				return maxStuckLine; // no split lines in tree, this split axis has no movable lines
			return minLineInTree;    		
		}
		if (line == maxStuckLine) {
			try { throw new Exception("Asking for one more than the maxStuckLine!?  Possible bug."); } catch (Exception e) { e.printStackTrace(); }
			return maxStuckLine;
		}
		
		SplitLine next = successor(line);
		getAbsoluteValue(next, ad.getFrameNum()); // never call computePlace directly
		
		// off by 1 fix, going beyond the second to last split line (a real line) gives a null
		// successor, make it the maxstuck instead
		if (next == null) next = maxStuckLine;
		
		return next;
	}
	
	/**
	 * Returns the splitLine previous to the parameter.
	 * This can be minStuckLine if line is the first movable line in the tree.  
	 * Note: wrapper call for the private previous() method.  
	 * @param line Line to get the previous split for
	 * @return The previous split line for the given split line
	 */
	public SplitLine getPreviousSplit (SplitLine line) {
		if (line == minStuckLine) {
			System.out.println("Asking for one less than the minStuckLine!?  Possible bug.");
			try { throw new Exception(); } catch (Exception e) { e.printStackTrace(); }
			return minStuckLine;    		
		}
		if (line == maxStuckLine) {
			SplitLine maxSplitLineInTree = getMaxLine();
			if (maxSplitLineInTree == null)
				return minStuckLine; // no split lines in tree, this split axis has no movable lines
			return maxSplitLineInTree;
		}
		
		SplitLine previous = previous(line);
		getAbsoluteValue(previous, ad.getFrameNum()); // never call computePlace directly
//		computePlaceThisFrame(previous,ad.getFrameNum());

		// off by 1 fix, going beyond the second split line (a real line) gives a null
		// previous line, make it the minstuck instead
		if (previous == null) previous = minStuckLine;
		
		return previous;
	}
	
	/**
	 * Returns the split line at the specified offset in the tree.  
	 * @param index The position of the splitLine in a 'flattened' view of the tree. 
	 * @return A splitline object
	 */
	public SplitLine getSplitFromIndex(int index ) {
		if (index < -1 || index > size) {
			Exception e = new Exception("Attempt to retrieve splitline with invalid index number: " + index + " Minstuck is -1 & MaxStuck is:" + getSplitIndex(getMaxStuckLine()));
			e.printStackTrace();
			return null;
		}
		
		if (index == -1 )
			return minStuckLine;
		if (index == size)
			return maxStuckLine;
		
		int myIndex = 0;
		if (root.getLeftChild() != null ) myIndex+= root.getLeftChild().subTreeSize;
		if ( myIndex == index ) {
			return root;
		} else if ( index < myIndex ) { 
			// Recurse left
			if (root.getLeftChild() != null)
				return getSplitFromIndexRecursive(index, 0, root.getLeftChild());
			else { 
				Exception e = new Exception("SplitLine not found at index: " + index + " This should never happen!");
				e.printStackTrace();
				return null;
			}
		} else { 
			// Recurse right
			if (root.getRightChild() != null )
				return getSplitFromIndexRecursive(index, myIndex+1, root.getRightChild());
			else {
				Exception e = new Exception("SplitLine not found at index: " + index + " This should never happen!");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 *  Recursive helper function for 'getSplitFromIndex()'.
	 * @param index - the index of the splitLine we are searching for
	 * @param fromLeft - how far we are from the left of the tree up until now
	 * @param node - current node in the tree
	 * @return the given split line from the initial index of interest
	 */
	private SplitLine getSplitFromIndexRecursive(int index, int fromLeft, SplitLine node ) { 
		int myIndex = fromLeft;
		if ( node.getLeftChild() != null )
			myIndex+= node.getLeftChild().subTreeSize;
		
		if (myIndex == index ) { 
			// Exit condition
			return node;
		} else if ( index < myIndex ) {
			// Recurse left
			if (node.getLeftChild() != null)
				return getSplitFromIndexRecursive(index, fromLeft, node.getLeftChild()); // Don't add myself
			else { 
				Exception e = new Exception("SplitLine not found at index: " + index + " This should never happen!");
				e.printStackTrace();
				return null;
			}
		} else {
			// Recurse right
			if (node.getRightChild() != null )
				return getSplitFromIndexRecursive(index, myIndex+1, node.getRightChild()); // Add my left subtree plus myself
			else {
				Exception e = new Exception("SplitLine not found at index: " + index + " This should never happen!");
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Repairs tree structure around the given split line following a delete operation.
	 * @param line Initial successor to the deleted split line 
	 */
	private void updateEchelonBounds(SplitLine line) {
		updateBoundLeftEchelon(line);
		updateBoundRightEchelon(line);
	}
	
	/**
	 * Repair left subtree following a delete
	 * @param line Initially the successor to the deleted split line, the children of this line will be repaired.
	 * @see #updateEchelonBounds(SplitLine)
	 */
	private void updateBoundLeftEchelon(SplitLine line) {
		if (line.getLeftChild() == null )
			return;
		if (line.getLeftChild().getRightChild() == null)
			return;
		
		SplitLine current = line.getLeftChild().getRightChild();
		
		while (current != null) { 
			current.setOpBound(line);
			current = current.getRightChild(); // Descend right down the echelon to the LEFT of line
		}
	}

	/**
	 * Repair right subtree following a delete
	 * @param line Initially the successor to the deleted split line, the children of this line will be repaired.
	 * @see #updateEchelonBounds(SplitLine)
	 */
	private void updateBoundRightEchelon(SplitLine line) {
		if (line.getRightChild() == null )
			return;
		if (line.getRightChild().getLeftChild() == null)
			return;
		
		SplitLine current = line.getRightChild().getLeftChild();
		
		while (current != null) { 
			current.setOpBound(line);
			current = current.getLeftChild(); // Descend left down the echelon to the RIGHT of line
		}
	}
	
	
	/**
	 * Delete node p, and then rebalance the tree.
	 * Code is derived from CLR red-black tree implementation
	 * @param p SplitLine to delete
	 */
	public void deleteEntry(SplitLine p) {
		decrementSize();
		
		// If strictly internal, copy successor's element to p and then make p
		// point to successor.  EG.  WE ARE ACTUALLY DELETING THE SUCCESSOR IN THIS
		// CASE.  
		// NOTE: this is potentially NASTY if things get added and not updated here.
		if (p.getLeftChild() != null && p.getRightChild() != null) {
			SplitLine s = successor (p);
			boolean delRoot = (p == root);
			SplitLine linkPlaceHolder;  
			
			////// OPTION 3
			// Swap left child
			linkPlaceHolder = p.getLeftChild();
			p.setLeftChild(s.getLeftChild());
			if (p.getLeftChild() != null)
				p.getLeftChild().setParent(p);
			s.setLeftChild(linkPlaceHolder);
			if (s.getLeftChild() != null)
				s.getLeftChild().setParent(s);
			// Swap right child
			linkPlaceHolder = p.getRightChild();
			p.setRightChild(s.getRightChild());
			if (p.getRightChild() != null)
				p.getRightChild().setParent(p);
			s.setRightChild(linkPlaceHolder);
			if (s.getRightChild() != null )
				s.getRightChild().setParent(s);
			// Swap parents
			linkPlaceHolder = p.getParent();
			p.setParent(s.getParent());
			if (p.getParent() != null) { 
				if (p.getParent().getLeftChild() == s)
					p.getParent().setLeftChild(p);
				else if (p.getParent().getRightChild() == s)
					p.getParent().setRightChild(p);
				else { 
					System.out.println("Something has gone very, very wrong.");
					Exception e = new Exception();
					e.printStackTrace();
					System.exit(0);
				}
			}
			s.setParent(linkPlaceHolder);
			if (s.getParent() != null) { 
				if (s.getParent().getLeftChild() == p)
					s.getParent().setLeftChild(s);
				else if (s.getParent().getRightChild() == p)
					s.getParent().setRightChild(s);
				else { 
					System.out.println("Something has gone very, very wrong.");
					Exception e = new Exception();
					e.printStackTrace();
					System.exit(0);
				}
			}
			// Unfortunately we still have to do a few copy by value ops...
			s.subTreeSize = p.subTreeSize;
			boolean colorHolder = s.color;
			s.color = p.color;
			p.color = colorHolder;
			// AND we need to fix bound stuff: 
			
			if (delRoot)
				root = s;
			
			setBounds(s);
			// TODO: updateEchelonBounds is too expensive!  potentially 2 (log n) expensive.  Is there a way to avoid this? 
			updateEchelonBounds(s);
			
			// NOW, swap parent and children links for successor & p 
			///////////// OPTION 1
//			s.copyContents(p); // Copy contents of s into p
			
			///////////// OPTION 2
			//            p.splitCount = s.splitCount;
			//            p.absoluteValue = s.absoluteValue;
			//            p.subTreeSize--; // Decrement subtree by one to account for deletion (to be done in else section of control structure below)
			
//			p = s;
		} // p has 2 children
		
		// Reduce tree size
		decrementTreeRecursive(p);
		
		// Start fixup at replacement node, if it exists.
		SplitLine replacement = (p.getLeftChild() != null ? p.getLeftChild() : p.getRightChild());
				
		if (replacement != null) {
			// Link replacement to parent
			replacement.setParent(p.getParent());
			if (p.getParent() == null)
				root = replacement;
			else if (p == p.getParent().getLeftChild())
				p.getParent().setLeftChild(replacement);
			else
				p.getParent().setRightChild(replacement);
			
			// Null out links so they are OK to use by fixAfterDeletion.
			p.setLeftChild(null);
			p.setRightChild(null);
			p.setParent(null);
			
			// Fix opBound
			setBounds(replacement);
			
			// Fix opBound
			setBounds(replacement);
			
			// Fix replacement
			if (p.color == BLACK)
				fixAfterDeletion(replacement);
		} else if (p.getParent() == null) { // return if we are the only node.
			root = null;
		} else { //  No children. Use self as phantom replacement and unlink.
			if (p.color == BLACK)
				fixAfterDeletion(p);
			
			if (p.getParent() != null) {
				if (p == p.getParent().getLeftChild())
					p.getParent().setLeftChild(null);
				else if (p == p.getParent().getRightChild())
					p.getParent().setRightChild(null);
				p.setParent(null);
			}
		}
	}
	
	/**
	 * Ascend recursively incrementing subtree counters as we go. 
	 * O(log n) operation.  Follows an add operation ({@link #putAt(SplitLine, SplitLine)}).
	 * @param line The current line in the recursive process to increment. 
	 */
	private void incrementTreeRecursive(SplitLine line ) { 
//		System.out.println("ITR line: " + line);
		if (line.getParent() == null) return;
		
		line.getParent().subTreeSize++;
		incrementTreeRecursive(line.getParent());
	}
	
	/**
	 * Ascend recursively decrementing subtree counters as we go. 
	 * Log(n) operation.  Follows a delete operation ({@link #deleteEntry(SplitLine)}).
	 * @param line The current line in the recursive process to decrement.
	 */
	private void decrementTreeRecursive(SplitLine line ) { 
		if (line.getParent() == null) return;
		
		line.getParent().subTreeSize--;
		decrementTreeRecursive(line.getParent());
	}
	
	/**
	 * We know which node of the tree we want to insert next to so
	 * the metricName is generated inside of this function.
	 * 
	 * If you are inserting the first splitline the adjacentSplit argument is ignored
	 * and can be null.
	 * 
	 * O(log n)
	 * 
	 * @param newSplit The new split line object.  
	 * @param adjacentSplit The split line you want to insert adjacent to.  (Left or Top depending on whether this is a vertical or horizontal split line.)  Inserting after minStuckLine is valid.
	 * @return true on success, false otherwise.
	 */
	public boolean putAt(SplitLine newSplit, SplitLine adjacentSplit) {
		if (newSplit == null) { 
			System.err.println("Cannot insert null split line.");
			return false;
		}
				
		if (root == null || root == newSplit) {
			incrementSize();
			root = newSplit;
			// Roots parent and opBound values stay null
			return true;
		}
		
		if (adjacentSplit == null ) { 
			System.err.println("Cannot insert adjacent to null split line.");
			return false;
		}
		
		if (adjacentSplit == minStuckLine) { 
			SplitLine nextLine = getNextSplit(adjacentSplit);
			if (nextLine.getLeftChild() != null) 
				System.err.println("Left child of first real line not null!?");
			
			incrementSize();
			nextLine.setLeftChild(newSplit);
			newSplit.setParent(nextLine);
			setBounds(newSplit);
			incrementTreeRecursive(newSplit);
			fixAfterInsertion(newSplit);
			
			return true;
		}
		
		// We are always inserting to the right of adjacentSplit
		if (adjacentSplit.getRightChild() == null) { 
			incrementSize();
			adjacentSplit.setRightChild(newSplit);
			newSplit.setParent(adjacentSplit);
			setBounds(newSplit);
			// ascend hierarchy incrementing subtree size counters (log n): incrementTreeRecursive. 
			// TODO: this is a naive implementation.  There is probably some way to 
			// move this functionality into the rotations so we aren't doing 2 log n for each insert
			incrementTreeRecursive(newSplit);
			fixAfterInsertion(adjacentSplit.getRightChild());
			return true;
		} else { 
			adjacentSplit = adjacentSplit.getRightChild();
			while (true) { 
				if (adjacentSplit.getLeftChild() != null ) { 
					adjacentSplit = adjacentSplit.getLeftChild(); // Descend left
				} else { 
					incrementSize();
					adjacentSplit.setLeftChild(newSplit);
					newSplit.setParent(adjacentSplit);
					setBounds(newSplit);
					// ascend hierarchy incrementing subtree size counters (log n): incrementTreeRecursive. 
					// TODO: this is a naive implementation.  There is probably some way to 
					// move this functionality into the rotations so we aren't doing 2 log n for each insert
					incrementTreeRecursive(newSplit);
					fixAfterInsertion(adjacentSplit.getLeftChild()); 
					return true;
				}
			}
		}
	}
	
	

	/**
	 * For debugging: compute positions of all split lines.  Only really needs to be called on all leaves, but both operations are O(n) with caching
	 * @param currentSL Current split line to compute, recurse on children of this split line
	 * @param frameNum frame number to use for computing locations
	 */
	private void computeAllSL(SplitLine currentSL, int frameNum)
	{
		computePlaceThisFrame(currentSL, frameNum);
		if (currentSL.getLeftChild() != null)
			computeAllSL(currentSL.getLeftChild(), frameNum);
		if (currentSL.getRightChild() != null)
			computeAllSL(currentSL.getRightChild(), frameNum);
	}
	
	
	/**
	 * Enqueue SplitTransitions to reset the entire display 
	 * back to its default position.  Queued entries are
	 * returned by means of the passed in parameter
	 * @param newToMove Hashtable to act as the current move queue.
	 * @param numSteps Number of steps to animate.
	 *
	 */
	public void animatedReset(Hashtable newToMove, int numSteps) { 
		if (root == null)
			return;
		
		MovingSplitLine [] toMove = new MovingSplitLine[size];
		final int frameNum = ad.getFrameNum();
		final double incr = (1d - minStuckLine.absoluteValue - (1 - maxStuckLine.absoluteValue)) / (size + 1);
		double currPos = minStuckLine.absoluteValue + incr;
		int i = 0;
		Iterator<SplitLine> it = iterator();		
		while (it.hasNext()) { 
			final SplitLine currLine = it.next();
			
			if (currLine == minStuckLine || currLine == maxStuckLine) continue;
			
			currLine.getValue(this, frameNum);
			toMove[i] = new MovingSplitLine(currLine, currPos);
			currPos+=incr;
			i++;
		}
		moveSplitLineSet(root, toMove, newToMove, numSteps);
	}
	

	/**
	 * Wrapper to call resetSplitValuesRecurse()
	 * 
	 * Must call computePlaceThisFrame before drawing anything after a reset.
	 * 
	 * Previous technique: O(n log n) behavior, is now O(n)
	 * 
	 */
	public void resetSplitValues() {
		if (root == null)
			return;		
		resetSplitValuesRecurse(root);
		return;
	}

	/**
	 * Recursively reset a subtree of split line values
	 * Previously n log n operation.  Made this o(n) by eliminating calls to getSplitIndex
	 * Note: this distributes split lines uniformly with split line indices, uses subtree sizes to place split lines accordingly
	 * @param splitline Current subtree root to reset.
	 */
	private void resetSplitValuesRecurse(SplitLine splitline) {

		int wholeTreeSize = splitline.subTreeSize;
		// 2 cases when a left or right child doesn't exist: subtree has only 1 or 2 splitlines
		if (wholeTreeSize == 1)
		{
			splitline.relativeValue = 1.0/2.0; // single split line in this subtree tree
		}
		else if (wholeTreeSize == 2) // 2 split lines:
			if (splitline.getLeftChild() != null)
			{
				splitline.relativeValue = 2.0/3.0; // allocate 2/3 of region for left side
				splitline.getLeftChild().relativeValue = 1.0/2.0; // and left child is uniformly spaced
			}
			else // must be a right child
			{
				splitline.relativeValue = 1.0/3.0; // allocate 1/3 of region for left side
				splitline.getRightChild().relativeValue = 1.0/2.0; // and right child is uniformly spaced
			}
		else // for 3 or more split lines (ASSERT: balanced trees with 3 or more split lines will have non-null left and right children):
		{
			int leftTreeSize = splitline.getLeftChild().subTreeSize;
			splitline.relativeValue = (leftTreeSize + 1.0) / (splitline.subTreeSize + 1.0);
			resetSplitValuesRecurse(splitline.getLeftChild());
			resetSplitValuesRecurse(splitline.getRightChild());
		}
	}
	
	/**
	 * Calculate the index number of any splitline from the left side of the tree.  
	 * WARNING: O(log n) operation. 
	 * @param line Line to compute index number for.
	 * @return Index of split line in tree, or Integer.MIN_VALUE if line is null
	 */
	public int getSplitIndex(SplitLine line ) { 
		if (line == null) { 
			Exception e = new NullPointerException("getSplitIndex on null line!"); 
			e.printStackTrace();
			return Integer.MIN_VALUE;
		}
		
		if (line == minStuckLine)
			return -1;
		
		if (line == maxStuckLine)
			return size;
		
		int lineIndex = (line.getLeftChild() != null) ? line.getLeftChild().subTreeSize : 0;
		SplitLine p = line.getParent();
		SplitLine current = line;
		
		while (p != null) { 
			if(current == p.getRightChild())
				lineIndex+=(p.getLeftChild() != null) ? p.getLeftChild().subTreeSize + 1 : 1;
			
			current = p;
			p = p.getParent();
		}
		return lineIndex;
	}
	
	
	/**
	 * This initializes a split line tree (empty) of size 'size'.  
	 * 
	 * O(2(n log n)) operation
	 * @param initSize Initial seed size of the split line hierarchy
	 * @param staticLines true if we are using static split lines for this axis, false for dynamic
	 */
	private void initSplitValues(int initSize, boolean staticLines) {
		if (initSize <= 0)
			return;
		if (staticLines)
		{
			size = initSize;
			root = new StaticSplitLine(-1.0, null, null, false);
			((StaticSplitLine)minStuckLine).setSplitIndex(-1);
			((StaticSplitLine)maxStuckLine).setSplitIndex(initSize);
			((StaticSplitLine)root).setSplitIndex(initSize/2);
			root.subTreeSize = size;
			initStaticSplitSubtree((StaticSplitLine)root);
			updateSubtreeSize(root);
		} else
		{
			SplitLine newroot = new SplitLine(-1.0, null, null, false);
			putAt(newroot,null);// insert root
			
			SplitLine newnode;
			SplitLine prevnode = root;
			for (int i = 1 ; i < initSize ; i++ ) {
				newnode = new SplitLine(-1.0,null, null, false);
				putAt(newnode,prevnode);
				prevnode = newnode;
			}
		}
	}

	/** array index for left bounds */
	protected final int LEFT = 0;
	/** array index for right bounds */
	protected final int RIGHT = 1;
	/**
	 * Gets the indices for the bounds of the given split line 
	 * @param currRoot Split line to compute bounding split lines
	 * @return pair of indices for {@link #LEFT} and {@link #RIGHT} bounds of this split line
	 */
	public int[] computeBoundIndices(StaticSplitLine currRoot)
	{
		boolean onLeft = currRoot.isLeftChild();
		int[] bounds = {0, 0};
		if (currRoot.getParent() == null) // set up the root
		{
			bounds[LEFT] = ((StaticSplitLine)minStuckLine).getSplitIndex();
			bounds[RIGHT] = ((StaticSplitLine)maxStuckLine).getSplitIndex();
		}
		else // non root node
		{
			if (onLeft)
			{
				bounds[RIGHT] = currRoot.getStaticParent().getSplitIndex();
				if (currRoot.getOffParentBound() == null)
					bounds[LEFT] = ((StaticSplitLine)minStuckLine).getSplitIndex();
				else
					bounds[LEFT] = currRoot.getOffParentBound().getSplitIndex();				
			}
			else
			{
				bounds[LEFT] = currRoot.getStaticParent().getSplitIndex(); 
				if (currRoot.getOffParentBound() == null)
					bounds[RIGHT] = ((StaticSplitLine)maxStuckLine).getSplitIndex();
				else
					bounds[RIGHT] = currRoot.getOffParentBound().getSplitIndex();
			}
		}
		return bounds;
	}
	
	/**
	 * Recursively initialize binary tree structure.
	 * Set properties of currRoot before this function
	 * @param currRoot Current subtree root split line that is being initialized.
	 */
	private void initStaticSplitSubtree(StaticSplitLine currRoot)
	{		
		int currIndex = currRoot.getSplitIndex();
		boolean onLeft = currRoot.isLeftChild();
		int[] bounds = computeBoundIndices(currRoot);
		StaticSplitLine newLeft = null, newRight = null;

		if (onLeft) // then left of current bounds left of left child, right child bounded by off parent of parent
		{
			if (currIndex - bounds[LEFT] > 1)
			{
				newLeft = new StaticSplitLine(-1, currRoot.getOffParentBound(), currRoot, false);
				currRoot.setLeftChild(newLeft);
				newLeft.setOpBound(currRoot.getOpBound());
				newLeft.setParent(currRoot);
				newLeft.setSplitIndex((currIndex + bounds[LEFT])/2);
				initStaticSplitSubtree(newLeft);
			}
			if (bounds[RIGHT] - currIndex > 1)
			{
				newRight = new StaticSplitLine(-1, currRoot, currRoot.getStaticParent(), false);
				currRoot.setRightChild(newRight);
				newRight.setOpBound(currRoot.getParent());
				newRight.setParent(currRoot);
				newRight.setSplitIndex((currIndex + bounds[RIGHT])/2);
				initStaticSplitSubtree(newRight);
			}
		}
		else // !onleft, current is on right of its parent
		{

			if (currIndex - bounds[LEFT] > 1)
			{
				newLeft = new StaticSplitLine(-1, currRoot, currRoot.getStaticParent(), false); 
				currRoot.setLeftChild(newLeft);
				newLeft.setOpBound(currRoot.getParent());
				newLeft.setParent(currRoot);
				newLeft.setSplitIndex((currIndex + bounds[LEFT])/2);
				initStaticSplitSubtree(newLeft);
			}
			
			if (bounds[RIGHT] - currIndex > 1)
			{
				newRight = new StaticSplitLine(-1, currRoot.getOffParentBound(), currRoot, false);
				currRoot.setRightChild(newRight);
				newRight.setOpBound(currRoot.getOpBound());
				newRight.setParent(currRoot);
				newRight.setSplitIndex((currIndex + bounds[RIGHT])/2);
				initStaticSplitSubtree(newRight);
			}
		}
		
		
		
	}
	
	/**
	 * Set the cell's boundaries given a known left (or top) split line for the cell.  This is for cells with a single unit in length/width.
	 * @param cell The cell whose splitline boundaries are being defined
	 * @param leftSplitLine The split line on the left (or top) side of the cell
	 */
	public void addCell(GridCell cell, SplitLine leftSplitLine) {
		int xy = horizontal ? 0 : 1;
		cell.minLine[xy] = leftSplitLine;
		cell.maxLine[xy] = successor(leftSplitLine);
	}
	
	/**
	 * Set the cell's boundaries given a known left (or top) split line for the cell.
	 * This is for cells with potentially many units in length/width.
	 * @param cell The cell whose splitline boundaries are being defined
	 * @param leftSplitLine The split line on the left (or top) side of the cell
	 * @param rightSplitLine The split line on the right (bottom) side of the cell
	 */
	public void addCell(GridCell cell, SplitLine leftSplitLine, SplitLine rightSplitLine) {
		int xy = horizontal ? 0 : 1;
		cell.minLine[xy] = leftSplitLine;
		cell.maxLine[xy] = rightSplitLine;
	}

		
	/**
	 * Returns the absolute position of the specified split line.
	 * This function caches the values for the given frame number.  
	 * 
	 * @param line Split line to get absolute position
	 * @param frameNum Current frame number
	 * @return Absolute position in world coordinates for the given split line.
	 */
	public double getAbsoluteValue(SplitLine line, int frameNum)
	{
		if (line == null)
		{
			System.err.println("Null absolute value check, could be minStuck or maxStuck, returning maxstuck");
			Exception e = new Exception();
			e.printStackTrace();
			return maxStuckLine.absoluteValue;
		}
		
		if (line == minStuckLine)
			return minStuckLine.absoluteValue;
		if (line == maxStuckLine)
			return maxStuckLine.absoluteValue;

				
		// Return cached value
		if (line.computedFrame == frameNum && line.absoluteValue > 0 && line.absoluteValue < 1)
			return line.absoluteValue;
		
		computePlaceThisFrame(line, frameNum);
		return line.absoluteValue;
	}
	
	/**
	 * Returns the absolute position of the split line at the specified offset 
	 * from the left of the tree, in world coordinates.  
	 * This function caches the values for the given frame number.
	 * 
	 * @param index Index of a split line to get the position for.
	 * @param frameNum Current frame number.
	 * @return Absolute position in world coordinates for the given split line.
	 */
	public double getAbsoluteValue(int index, int frameNum)
	{
		if (index < 0)
			return minStuckLine.absoluteValue;
		if (index >= size)
			return maxStuckLine.absoluteValue;
		
		SplitLine line = getSplitFromIndex(index);
		
		// Return cached value
		if (line.computedFrame == frameNum)
			return line.absoluteValue;
		else { 
			computePlaceThisFrame(line,frameNum);
			return line.absoluteValue;
		}		
	}
	
	
	/**
	 * Wrapper for getSplitFromAbsolute for places where we want the index number instead of a 
	 * pointer to the SplitLine object.  
	 * 
	 * Renamed from getSplitLineIndex.
	 * @param worldCoord World coordinate to search for a split line.
	 * @param pixelSize Current block size, in world-space coordinates.
	 * @param frameNum Current frame number.
	 * @return The index number of the split line, or -2 if you are outside of the stuck positions or there is an error.
	 */
	public int getSplitIndexFromAbsolute(double worldCoord, double pixelSize, int frameNum)
	{
		return getSplitIndex(getSplitFromAbsolute(worldCoord, pixelSize, frameNum));	
	}
	
	
	/**
	 * The max stuck value is a world space double value from 0 .. 1
	 * that defines where the max boundary split line is located
	 * in world space for this axis.  
	 * 
	 * @param f Value from [0,1] that places the max stuck value for this split axis.
	 */
	public void setMaxStuckValue(double f) {
		maxStuckLine.absoluteValue = f;
	}
	
	/**
	 * The min stuck value is a world space double value from 0 .. 1
	 * that defines where the min boundary split line is located
	 * in world space for this axis.  
	 * 
	 * @param f Value from [0,1] that places the min stuck value for this split axis.
	 */
	public void setMinStuckValue(double f) {
		minStuckLine.absoluteValue = f;
	}
	
	/**
	 * The max stuck value is a world space double value from 0 .. 1
	 * that defines where the max boundary split line is located
	 * in world space for this axis.  
	 * @return World-space position of max stuck line.
	 */
	public double getMaxStuckValue() {
		return maxStuckLine.absoluteValue;
	}
	
	/**
	 * The max stuck value is a world space double value from 0 .. 1
	 * that defines where the max boundary split line is located
	 * in world space for this axis.  
	 * 
	 * This line contains the max stuck value in its absoluteValue property.
	 * 
	 * @return Split line that represents the maximum boundary for this axis.
	 */
	public SplitLine getMaxStuckLine() {
		return maxStuckLine;
	}
	
	/**
	 * The min stuck value is a world space double value from 0 .. 1
	 * that defines where the min boundary split line is located
	 * in world space for this axis.  
	 * @return World-space position of min stuck line.
	 */
	public double getMinStuckValue() {
		return minStuckLine.absoluteValue; 
	}
	
	/**
	 * The min stuck value is a world space double value from 0 .. 1
	 * that defines where the min boundary split line is located
	 * in world space for this axis.  
	 * 
	 * This line contains the min stuck value in its absoluteValue property.
	 * 
	 * @return Split line that represents the minimum boundary for this axis.
	 */
	public SplitLine getMinStuckLine() {
		return minStuckLine;
	}
	
	
	/**
	 * Find a split line using absolute values to return the 
	 * minimum bounding split line at the screen position.  This uses the main split data 
	 * structures and NOT the partitioned list.
	 * This method may return the minStuckLine but never the maxStuckLine
	 * 
	 * @param worldCoords World coordinate to search for a split line.
	 * @param pixelSize Current block size, in world-space coordinates.
	 * @param frameNum Current frame number.
	 * @return Closest minimum split line to the given world-space coordinate
	 */
	public SplitLine getSplitFromAbsolute(double worldCoords, double pixelSize, int frameNum) {
		if (worldCoords < minStuckLine.absoluteValue || worldCoords > maxStuckLine.absoluteValue )
			return null;
		
		if (size == 0 )
			return minStuckLine;
		
		SplitLine min = minStuckLine;
		SplitLine max = maxStuckLine;
		SplitLine mid = root;
		
		// Check boundary condition for whether we should return minStuckLine
		final SplitLine minRealLine = getMinLine();
		final double minRealLinePos = minRealLine.getValue(this, frameNum);
		if (minRealLinePos > worldCoords)
			return minStuckLine;
		
		while ( getAbsoluteValue(max, frameNum) - getAbsoluteValue(min, frameNum) > pixelSize) {
			if (worldCoords < getAbsoluteValue(mid, frameNum)) {
				if ( mid.getLeftChild() != null ) { 
					max = mid;
					mid = mid.getLeftChild(); // Descend left
				} else {
					mid = minStuckLine;
					break;
				}
			} else {
				if (mid.getRightChild() != null ) { 
					// We MAY descend right, depends on whether the right child
					// is larger than what we are looking for.  Remember the 
					// objective here is to return the MAXIMUM POSSIBLE split line
					// that is still less than our position.  
					if (worldCoords < getAbsoluteValue(successor(mid), frameNum))
						break;
					
					min = mid;
					mid = mid.getRightChild();
				} else {
//					System.out.println("BROKE OUT ON RIGHT");	
					break;
				}
			}
		}
		
		if (worldCoords > getAbsoluteValue(min, frameNum) || worldCoords < getAbsoluteValue(max, frameNum))
			return mid;
		
		System.out.println("Error in getSplitLineIndex searching for the value: " + worldCoords);
		return null;
	}
	
	/**
	 * Get the line in the minimum position in the tree.  (NOT the minStuckLine.)  
	 * O(log n) (approx)
	 * 
	 * @see #getMinStuckLine() to get the minStuckLine 
	 * @return Left-most split line, the minimum non-stuck split line.
	 */
	public SplitLine getMinLine() { 
		if (root == null)
			return null;
//			return getMinStuckLine();
		
		SplitLine current = root;
		while (current.getLeftChild() != null)
			current = current.getLeftChild();
		
		return current;
	}
	
	/**
	 * Get the line in the maximum position in the tree.  (NOT the maxStuckLine.) 
	 * O(log n) (approx)
	 * 
	 * @return Right-most split line, the maximum non-stuck split line.
	 */
	public SplitLine getMaxLine() { 
		if (root == null)
			return null;
//			return getMaxStuckLine();
		
		SplitLine current = root;
		while (current.getRightChild() != null)
			current = current.getRightChild();
		
		return current;
	}
	
	/**
	 * This does the same thing as getSplitFromAbsolute and returns a minimum split line
	 * given an absolute position.  The only difference is that it doesn't stop when it 
	 * reaches minimum block size and it takes a screen space coordinate as a parameter.  
	 * TODO: these two methods should probably be merged.  
	 * 
	 * @param pixelPosition Screen space coordinate.
	 * @param frameNum Current frame number.
	 * @return Minimum split line adjacent to screen space coordinate.
	 */
	public SplitLine getMinLineForPixelValue(int pixelPosition, int frameNum) {
		int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
		double windowLocation = ad.s2w(pixelPosition, xy);
		return getMinLineForPixelValue(windowLocation, frameNum);
	}

	
	/**
	 * This does the same thing as getSplitFromAbsolute and returns a minimum split line
	 * given an absolute position.  The only difference is that it doesn't stop when it 
	 * reaches minimum block size and it takes a screen space coordinate as a parameter.  
	 * TODO: these two methods should probably be merged.  
	 * TODO: probably shouldn't take pixel-space coordinate 
	 *  
	 * @param worldLocation Screen space coordinate
	 * @param frameNum Current frame number
	 * @return Minimum split line adjacent to screen space coordinate.
	 */
	public SplitLine getMinLineForPixelValue(double worldLocation, int frameNum) {
		if (size == 0)
			return minStuckLine;

		SplitLine min = minStuckLine, max = maxStuckLine;
		SplitLine mid = root;
		double currLocation = getAbsoluteValue(mid, frameNum);
		while (mid != max)
		{
			if (worldLocation <= currLocation)
			{ // left
				max = mid;
				if ( mid.getLeftChild() != null )
					mid = mid.getLeftChild(); // Descend left
				else {
					break;
				}
			}
			else
			{ // right
				min = mid;
				if (mid.getRightChild() != null )
					mid = mid.getRightChild(); // Descend right
				else { 
					break;
				}
			}
			currLocation = getAbsoluteValue(mid, frameNum);
		}
		
		return min;
	}
	
	/**
	 * Returns the next line over (successor).  
	 * @param pixelPosition Screen-space coordinate
	 * @param frameNum Current frame number.
	 * @return Split line for the successor at the current pixel position.
	 */
	public SplitLine getMaxLineForPixelValue(int pixelPosition, int frameNum) {
		final int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
		final double windowLocation = ad.s2w(pixelPosition, xy);
		return successor(getMinLineForPixelValue(windowLocation, frameNum));
	}
	
	/**
	 * Returns the next line greater than the specified world space coordinate for 
	 * this axis.  
	 *   
	 * @param worldPosition World-space coordinate.
	 * @param frameNum Current frame number.
	 * @return Split line for the successor at the current world position.
	 */
	public SplitLine getMaxLineForPixelValue(double worldPosition, int frameNum) {
		return successor(getMinLineForPixelValue(worldPosition, frameNum));
	}
	
	/**
	 * Determines if this is a horizontal SplitAxis
	 * @return true if horizontal, false if vertical
	 */
	public boolean isHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns ths index of the root split line.
	 * @return The integer index to the current value of the root node.  
	 */
	public int getRootIndex() {
		return (root.getLeftChild() == null) ? 0 : root.getLeftChild().subTreeSize + 1;
	}
	
	/**
	 * Checks whether param is the root of this split axis.
	 * @param en Split line to test for root.
	 * @return true if the parameter is the root of the axis hierarchy
	 */
	private boolean isRoot(SplitLine en) { 
		return en == root;
	}
	
	/**
	 * Checks whether parameter is a left child of its parent.
	 * @param en Split line to test for being left child
	 * @return true if this split line has a parent and is the left child.
	 */
	private boolean isSplitLeftChild(SplitLine en)
	{
		if (isRoot(en)) 
			return false;		
		return en.getParent().getLeftChild() == en;
	}
	
	/**
	 * Computes a position for the given splitline for the frame number given.
	 * Frame numbers in the past are not supported (no memory), this is just to know when a splitline
	 * is out of date with the current view (caching, don't recompute positions when nothing moves)
	 * @param splitline splitline to compute a location for
	 * @param frameNum frame number to check for cached splitline positions
	 */
	public void computePlaceThisFrame(SplitLine splitline, int frameNum)
	{ 
		if (splitline == minStuckLine || splitline == maxStuckLine) { 
			return;
		}
			
		SplitLine[] bounds = getBounds(splitline, frameNum);
		final double minStuckValue = minStuckLine.absoluteValue;
		final double maxStuckValue = maxStuckLine.absoluteValue;
		
		if ( frameNum > splitline.computedFrame || !(splitline.absoluteValue > 0 && splitline.absoluteValue < 1))
		{
			if (!isRoot(splitline))
			{
				SplitLine parent = splitline.getParent();
				computePlaceThisFrame(parent, frameNum);
				if (isSplitLeftChild(splitline))
				{
					double parMinPosition = (bounds[minBound] == null) ? minStuckValue : bounds[minBound].getValue(this, frameNum); // absolute position of min line
					double parSplitPosition = size == 0 ? maxStuckValue : parent.getValue(this, frameNum);
					double range = parSplitPosition - parMinPosition; // size of parent min split
					splitline.absoluteValue = parMinPosition + range * splitline.relativeValue;
				} else { // right split child
					double parMaxPosition = (bounds[maxBound] == null) ? maxStuckValue : bounds[maxBound].getValue(this, frameNum); // absolute position of max line
					double parSplitPosition = size == 0 ? minStuckValue : parent.getValue(this, frameNum);
					double range = parMaxPosition - parSplitPosition; // size of parent max split
					splitline.absoluteValue = parSplitPosition + range * splitline.relativeValue;
				}
			} else { // root node, parent doesn't really exist, this cell is in position 0 so it is on the left of the imaginary parent
				double myMinPosition = minStuckValue;
				double myMaxPosition = maxStuckValue;
				double range = myMaxPosition - myMinPosition;
				splitline.absoluteValue = myMinPosition + range * splitline.relativeValue;
			}
			splitline.computedFrame = frameNum;
			if (AccordionDrawer.debugOutput && 
					(splitline.absoluteValue < minStuckValue || splitline.absoluteValue > maxStuckValue)) { 
				System.out.println("Bad absolute value: " + splitline.absoluteValue + " " + minStuckValue + " " + maxStuckValue + "(" + splitline + "/" + size + ")");
			}
		}
	}
	

	/**
	 * Recursively retrieves an pre-ordered list of the child splitlines of 
	 * the specified line (min -> max) 
	 * @param line Current split line being recursed through.
	 * @param list Recursive collector
	 * @return Value of list after adding child split lines.
	 */
	public List<SplitLine> getChildren (SplitLine line, List<SplitLine> list) { 
		if (line == null) 
			return list;
		
		final SplitLine leftChild = line.getLeftChild();
		final SplitLine rightChild = line.getRightChild();
		
		if (leftChild == null && rightChild == null) { 
			list.add(line);
			return list;
		}
		
		if (leftChild != null)
			getChildren(leftChild, list);
		else
			list.add(line);
		
		if (rightChild != null)
			getChildren(rightChild, list);
		return list;
	}
	
	/**
	 * Create list of SplitLines for this axis that are larger than the 
	 * specified blockSize: {@link AccordionDrawer#getPixelSize(int)}.
	 * Initializes and fills {@link #partitionedList}. 
	 * 
	 * @param frameNum Current frame number 
	 */
	public void makePixelRanges(int frameNum) {
		double pixelSize = ad.getPixelSize(horizontal ? AccordionDrawer.X : AccordionDrawer.Y);
		if (pixelSize < 0) {
			System.out.println("Error: pixelSize is negative!");
			return;
		}
		partitionedList = new TreeSet();
		
		if (size == 0)
			// Create a single dummy split line if our data set is empty
			partitionedList.add(new SplitLine(0.5, minStuckLine, maxStuckLine, true)); 
		else
			makePixelRangeRecursive(partitionedList, root, pixelSize, frameNum);
		
		if (LOGGING && getLogger() != null) { 
			try {
				getLogger().logEntry(minStuckLine, maxStuckLine, partitionedList);
			} catch (IOException e) {
				System.err.println("Error writing log entry!");
			}
		}
	}

	/**
	 * Create list of SplitLines for this axis that are larger than the 
	 * specified blockSize (ad.getPixelSize).
	 * Initializes and fills {@link #partitionedList}. 
	 * 
	 * @param frameNum Current frame number
	 * @param pixelSizeMultiple factor used to divide the size of a single pixel to determine block size.  
	 */
	public void makePixelRanges(double pixelSizeMultiple, int frameNum) {
		double pixelSize = ad.getPixelSize(horizontal ? AccordionDrawer.X : AccordionDrawer.Y) / pixelSizeMultiple;
		if (pixelSize < 0) {
			System.out.println("Error: pixelSize is negative!");
			return;
		}
		partitionedList = new TreeSet();
		
		if (size == 0)
			// Create a single dummy split line if our data set is empty
			if (staticLines)
				partitionedList.add(new StaticSplitLine(0.5, minStuckLine, maxStuckLine, true));
			else
				partitionedList.add(new SplitLine(0.5, minStuckLine, maxStuckLine, true)); 
		else
			makePixelRangeRecursive(partitionedList, root, pixelSize, frameNum);
				
		if (LOGGING && getLogger() != null) { 
			try {
				getLogger().logEntry(minStuckLine, maxStuckLine, partitionedList);
			} catch (IOException e) {
				System.err.println("Error writing log entry!");
			}
		}
	}
	
	/**
	 * This is the recursive call that builds the partition list for this SplitAxis object.  
	 * It descends the tree structure computing place this frame until it begins to produce 
	 * results which subtend pixelSize.  When this happens, recursion is finished.  
	 * 
	 * NOTE: You will notice two cases where 'Virtual' split lines are being added.  
	 * This is because the final 'leaf' layer of the tree is virtualized.  Drawing always
	 * takes place using the absolutePosition of a SplitLine's parent & opAncestor (their 
	 * equivelent of min and max bounds) and therefore nothing about the SplitLine object
	 * that is enqueued matters except that these two bounds are valid & have valid 
	 * absolutePositions.  See AccordionSequenceDrawer.drawRange for details.    
	 * 
	 * @param partitionSet TreeSet of SplitLines that represent the partition list
	 * @param line splitline, initially the root line then recursively left & right descends
	 * @param pixelSize minimal size of a block in absolute [0,1] size.  This is what stops the recursion from descending indefinitely. 
	 */
	private void makePixelRangeRecursive(TreeSet partitionSet, SplitLine line, double pixelSize, int frameNum) {
		SplitLine[] bounds = getBounds(line, frameNum);
		
		double[] positions = new double[2];
		//{getAbsoluteValue(bounds[minBound], frameNum),getAbsoluteValue(bounds[maxBound], frameNum)};
		if (bounds[minBound] == null)
			positions[minBound] =  minStuckLine.absoluteValue;
		else
			positions[minBound] = getAbsoluteValue(bounds[minBound], frameNum);
		if (bounds[maxBound] == null)
			positions[maxBound] = maxStuckLine.absoluteValue;
		else
			positions[maxBound] = getAbsoluteValue(bounds[maxBound], frameNum);
		if (positions[1] - positions[0] <= pixelSize) {
			getAbsoluteValue(line,frameNum);
			partitionSet.add(line);
		} else {
			if (line.getLeftChild() != null)
				makePixelRangeRecursive(partitionSet, line.getLeftChild(), pixelSize, frameNum);
			else // Create new virtual SplitLine, set left and right bounds
				// set the value of these virtual splitlines to a unique number
				//  - it shouldn't matter for anything but needs to be unique (+2 to avoid boundaries)
				// We are a left virtual child, so our opBound is our parent's left opBound
				// 
				if (staticLines)
				{
					StaticSplitLine newLeftSplit = new StaticSplitLine(-1, line, bounds[minBound], true);
					newLeftSplit.setSplitIndex(((StaticSplitLine)line).getSplitIndex());
					newLeftSplit.setParent(line);
					partitionSet.add(newLeftSplit);
				}
				else // use .size() to give a unique id to each splitline
					partitionSet.add(new SplitLine(positions[minBound] ,bounds[minBound],line, true));
			if (line.getRightChild() != null)
				makePixelRangeRecursive(partitionSet, line.getRightChild(), pixelSize, frameNum);
			else
				// We are a right virtual child, so our opBound is our parent's right opBound
				if (staticLines)
				{
					StaticSplitLine newRightSplit = null;
					newRightSplit = new StaticSplitLine(-1, bounds[maxBound], bounds[maxBound], true);
					if (bounds[maxBound] == null) // add last split line
					{	
						SplitLine nextToLast = getSplitFromIndex(size-1); 
						newRightSplit.setOpBound(nextToLast);
						newRightSplit.setSplitIndex(size);
					}
					else
					{
						newRightSplit.setSplitIndex(((StaticSplitLine)bounds[maxBound]).getSplitIndex());
					}
					newRightSplit.setParent(line);
					partitionSet.add(newRightSplit);
				}
				else // use .size() to give a unique split value to each new splitline
					partitionSet.add(new SplitLine(getAbsoluteValue(line,frameNum) ,line, bounds[maxBound], true));
		}
	}
	

	/**
	 * Resize the group by the inflation increment, in the given number of steps, and add the transitions to the
	 * hashtable for successive redraws.
	 * 
	 * 3 major steps:
	 *   1) compute "unshrinkable" space, and subtract that from the inflate increment.  Also computes component resize ratios for growing and shrinking regions.
	 *   2) handle edge cases where we either start with a range list on the stuck line, or shrink a region adjacent to a stuck line (min and max)
	 *   3) handle interior cases in the same way as 2), but with the generic for loop stucture
	 *   
	 *    After those steps, we run {@link #moveSplitLineSet(SplitLine, net.sourceforge.olduvai.accordiondrawer.SplitAxis.MovingSplitLine[], Hashtable, int)} to create transitions
	 *    from the sanely computed movement distances (stored in an array).
	 *   
	 * @param group Set of split adjacent split lines usually marked in the same color that are being resized
	 * @param numSteps The number of animated transition steps
	 * @param newToMove The new transition hash table
	 * @param inflateIncr The inflate increment.  All components of group will divide this world-space value among
	 * them, with larger subgroups growing more than smaller ones, size ratios are preserved. 
	 */
	public void resizeForest(AbstractRangeList group, int numSteps, Hashtable newToMove, double inflateIncr)
	{
		int frameNum = ad.getFrameNum();
		if (group == null || group.size() == 0) return; // no ranges
		SplitLine[] ranges = createIndexRanges(group); // sets createMin, createMax
		
		if (ranges == null) return; // range covers everything
		if (group.size() == 1 &&
			getAbsoluteValue(ranges[(1+(createMin?1:0))], frameNum) -
				getAbsoluteValue(ranges[0+(createMin?1:0)], frameNum) +
				inflateIncr < ad.minContextInside)
			return;
		double stuckRangeSize = getMaxStuckValue() - getMinStuckValue();
		
		double[] startValues = new double[ranges.length]; // where the splitlines start
		double[] endValues = new double[ranges.length]; // where the splitlines will go
		double[] extent = group.getSizesOfAllRanges(this, frameNum); // initial size of all ranges
		double noShrink = group.getUnshrinkableTotal(ad, this, frameNum); // total of all shrinking regions that will not shrink
		double oldTotalExtent = getTotalExtent(extent);
		double minGrowSize = stuckRangeSize - // stuck size 
							 noShrink; // room between ranges and peripheries
		if (oldTotalExtent + noShrink >= minGrowSize) // new potential size of ranges
		{
//			System.out.println("Too much squishing, not going to grow (maybe too many ranges? " + ranges.length + ")");
			return; // don't grow any more
		}

		double oldTotalNonExtent = stuckRangeSize - oldTotalExtent;
		
		for (int i = 0; i < ranges.length; i++)
		{
			startValues[i] = getAbsoluteValue(ranges[i], frameNum);
		}
		endValues[0] = startValues[0]; // stuck
		endValues[ranges.length-1] = startValues[ranges.length-1]; // stuck
		
		int numRealRanges = extent.length;
		int numRealNonRanges = numRealRanges - 1; // size if min/max stuck lines were in groups 
		if (createMin) // min wasn't initially a range line
			numRealNonRanges++;
		if (createMax) // max wasn't initially a range line
			numRealNonRanges++;
		
		int startAt = 0;
		int endAt = ranges.length - 1;
		// after this, startAt will point to the min splitline index of the minimum range to grow
		//  and endAt will point to the max splitline index of the maximum range to grow
		// if either createMin or Max, those ranges will be resized and the inflateIncr will be adjusted accordingly
	
		{
			if (oldTotalExtent + inflateIncr > minGrowSize)
				inflateIncr = minGrowSize - oldTotalExtent;
			// note: since inflateIncr is being used here it should not be altered after this point
			double newTotalExtent = oldTotalExtent + inflateIncr;
			double newTotalNonExtent = oldTotalNonExtent - inflateIncr;
			double totalExtentRatio = newTotalExtent/oldTotalExtent;
			double totalNonExtentRatio = newTotalNonExtent/oldTotalNonExtent;
			
			double firstRange = startValues[startAt+1]-startValues[startAt];
			double lastRange = startValues[endAt]-startValues[endAt-1];
			if (createMin)
			{
			// the periphery shouldn't be reduced if this is true:
				if (firstRange < ad.minContextPeriphery)
				{
//					System.out.println("Area before first range might be squished too small");
					endValues[startAt+1] = startValues[startAt+1];
				}
				else
				{ // adjust the periphery
					endValues[startAt+1] = startValues[startAt] + firstRange*totalNonExtentRatio;
					if (endValues[startAt+1]-endValues[startAt] < ad.minContextPeriphery)
						// note: this loses some exactness with the resizing by not refactoring wrt min periphery
						endValues[startAt+1] = endValues[startAt] + (double)ad.minContextPeriphery;
				}
				startAt++;
			}
			if (createMax)
			{
				if (lastRange < ad.minContextPeriphery)
				{
				//	note: this loses some exactness with the resizing by not refactoring wrt min periphery
//					System.out.println("Area after last range might be squished too small");
					endValues[endAt-1] = startValues[endAt-1];
				}
				else
				{
					endValues[endAt-1] = startValues[endAt] - lastRange*totalNonExtentRatio;
					if (endValues[endAt]-endValues[endAt-1] < ad.minContextPeriphery)
						// note: this loses some exactness with the resizing by not refactoring wrt min periphery
						endValues[endAt-1] = endValues[endAt] - (double)ad.minContextPeriphery;
				}
				endAt--;
			}
			for (int i = startAt; i <= endAt; i+=2)
			{ // ranges to grow start at i, end at i+1
				endValues[i+1] = startValues[i+1] - startValues[i];
				endValues[i+1] *= totalExtentRatio;
				endValues[i+1] += endValues[i]; // end of last range
			  // start of next range
				if (i+2 < endValues.length)
				{
					endValues[i+2] = startValues[i+2] - startValues[i+1];
					if (endValues[i+2] > ad.minContextInside)
						endValues[i+2] *= totalNonExtentRatio;
					endValues[i+2] += endValues[i+1];
				}
			}
		}
		
		// now resize the calculated ranges
		//System.out.println("START: " + doubleArrayToRangeString(startValues));
		//System.out.println("END: " + doubleArrayToRangeString(endValues));
		//System.out.println("DIFF: " + doubleArrayDiffsToString(startValues, endValues));
		
		MovingSplitLine [] toMove = new MovingSplitLine[ranges.length];
		
		for ( int i = 0 ; i < toMove.length ; i++) { 
			toMove[i] = new MovingSplitLine(ranges[i],endValues[i]);
		}
		
		moveSplitLineSet(root,toMove,newToMove,numSteps);
	}
	
	/**
	 * Helper function for resizeForest.  
	 * 
	 * create range of indices of split lines for ranges in group
	 * this includes the min/max stuck position split lines (if not already in a group)
	 * @param group Group to parse for split lines to retrieve indices
	 * @return Array of pairs of split line indices, 2 for each region (even/odd).
	 */
	private SplitLine[] createIndexRanges(AbstractRangeList group)
	{
		SplitLine[] rangesTemp = group.getSplitLines(this);
		if (group.size() == 1 && 
			rangesTemp[0] == minStuckLine && 
			rangesTemp[rangesTemp.length-1] == maxStuckLine)
			return null; // one range that covers the entire screen space 
		
		int minOffset = 0;
		createMin = false;
		createMax = false;
		int rangeSize = rangesTemp.length;
		if (rangesTemp[0] != minStuckLine) { // first range doesn't include the min stuck position\
//			System.out.println("difference between first range index and minstuckline: " + rangesTemp[0] + " != " + minStuckLine);
			rangeSize++;
			createMin = true;
			minOffset = 1;
		}
		if (rangesTemp[rangesTemp.length-1] != maxStuckLine) { // last range doesn't include the max stuck position
//			System.out.println("difference between last range index and maxstuckline: " + rangesTemp[rangesTemp.length-1] + " != " + maxStuckLine);
			rangeSize++;
			createMax = true;
		}
		SplitLine[] ranges = new SplitLine[rangeSize];
		for (int i = 0; i < rangesTemp.length; i++) {
			ranges[i + minOffset ] = rangesTemp[i]; 
		}
		if (createMin) ranges[0] = minStuckLine;
		if (createMax) ranges[ranges.length-1] = maxStuckLine;
		return ranges;		
	}

	/**
 	 * Returns the nearest split line to the specified world coordinate for this axis.  Note that this
 	 * differs from getSplitFromAbsolute which returns the largest line that has a value less than
 	 * the specified value.  This method may return a line with a value higher than that specified if
 	 * it is closer to the specified value. This uses the main split data structures and not the partitioned
 	 * list.
 	 *
 	 * @param worldCoords
 	 * @param pixelSize
 	 * @param frameNum
 	 * @return
 	 */
 	public SplitLine getNearestSplitFromAbsolute(double worldCoords, double pixelSize, int frameNum ) {
 		if (worldCoords < minStuckLine.absoluteValue || worldCoords > maxStuckLine.absoluteValue || size == 0)
 			return null;
 
 		// Start by getting the closest we can
 		SplitLine closestLine = getSplitFromAbsolute(worldCoords, pixelSize, frameNum);
 		if (closestLine == null)
 			return null;
 
 		while (getAbsoluteValue(closestLine, frameNum) < worldCoords && closestLine != maxStuckLine) {
 			closestLine = getNextSplit(closestLine);
 		}
 
 		if (closestLine == minStuckLine)
 			return minStuckLine;
 		else {
 			final SplitLine prevLine = getPreviousSplit(closestLine);
 			final double prevDist = Math.abs(getAbsoluteValue(prevLine, frameNum) - worldCoords);
 			final double dist = Math.abs(getAbsoluteValue(closestLine, frameNum) - worldCoords);
 			if (prevDist < dist)
 				closestLine = prevLine;
 		}
 		return closestLine;
 	}

	
	
	/**
	 * Helper function for resizeForest.  Computes the total extent between pairs of distances given as the parameter.
	 * @param extent Array of distances to add up.
	 * @return Sum of all extents.  This will be used to compute the non-extent, aka the squishable space.
	 */
	private static final double getTotalExtent(double[] extent)
	{
		double totalExtent = 0f;
		for (int i = 0; i < extent.length; i++)
		{
			totalExtent += extent[i];
		}
		return totalExtent;
	}
	
	
	/**
	 * Handles the case of the user drawing an interaction box and dragging on it.  
	 * 
	 * In this case, only one split line is actually being moved per axis, either the left, 
	 * right, top or bottom line depending on the axis and the direction of the drag motion.  
	 * 
	 * @param dragLine line to be moved
	 * @param dragPixelEnd screen coords of destination position
	 * @param fixedLine non-moving line in interaction box
	 * @param numSteps animation steps
	 * @param newToMove hashmap to be filled with animated transition steps
	 */
	public void moveLine(SplitLine dragLine, int dragPixelEnd, SplitLine fixedLine, int numSteps, Hashtable newToMove) {
		if (dragLine == fixedLine)
			return; // huh?
		if (dragLine == minStuckLine || dragLine == maxStuckLine) return; // split is one of the stuck lines
		
		final int frameNum = ad.getFrameNum();
		final int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
		
		SplitLine[] range = {minStuckLine, dragLine, fixedLine, maxStuckLine};
		double dragEnd = (double)ad.s2w(dragPixelEnd, xy);
		// shouldn't be able to drag the box out of the periphery range from the borders
		if (dragEnd < getMinStuckValue() + ad.minContextPeriphery ||
				dragEnd > getMaxStuckValue() - ad.minContextPeriphery)
			return;
		
		double staticEnd = getAbsoluteValue(fixedLine, frameNum);
		double dragStart = getAbsoluteValue(dragLine, frameNum);
		
		// shouldn't be able to drag the box too small, keep context inside
		if (Math.abs(dragEnd - dragStart) < ad.minContextInside)
			return;
		double[] startValue = {getMinStuckValue(), dragStart, staticEnd, getMaxStuckValue()};
		double[] endValue = {getMinStuckValue(), dragEnd, staticEnd, getMaxStuckValue()};
		if (dragStart > staticEnd) {
			range[1] = fixedLine;
			endValue[1] = staticEnd;
			startValue[1] = staticEnd;
			range[2] = dragLine;
			endValue[2] = dragEnd;
			startValue[2] = dragStart;
		}
		int min = 0, max = range.length - 1;
		while (range[min] == range[min+1]) min++; // at least ignore statics that are
		while (range[max] == range[max-1]) max--; // on a stuck line
		
		MovingSplitLine [] movingLines = new MovingSplitLine[max - min + 1]; 
		
		for ( int i = 0 ; i < movingLines.length ; i++ ) { 
			MovingSplitLine newMovingSplitLine = new MovingSplitLine(range[i+min],endValue[i+min]);
			movingLines[i] = newMovingSplitLine;
		}
		
//		System.out.println("Calling moveSplitLineSet(" + root + ", movingLines " + movingLines );
		moveSplitLineSet(root,movingLines,newToMove,numSteps);
	}
	
	/**
	 * Helper function for moveSplitLineSet.
	 * Gets new position of mid: finalMid = (initMid-initLeft)/(initRight-initLeft)*(finalRight-finalLeft)+finalLeft
	 * (translate, rescale, translate) 
	 * All params are absolute positions.
	 * @param initLeft Left line's initial absolute position.
	 * @param initMid Mid line's initial absolute position.
	 * @param initRight Right line's initial absolute position.
	 * @param finalLeft Left line's final absolute position.
	 * @param finalRight Right line's final absolute position.
	 * @return new final absolute value for initMid's split line ( = finalMid)
	 */
	private static final double computeFinalAbsolute( double initLeft, double initMid, double initRight, double finalLeft, double finalRight) {

		return (initMid - initLeft) / (initRight - initLeft) * (finalRight - finalLeft) + finalLeft;
	}
	
	/**
	 * Small number used to avoid division by 0 in {@link #computeFinalRelative(double, double, double)}.
	 */
	protected static final double small = 1e-12;
	/**
	 * Helper function for moveSplitLineSet. 
	 * All params are absolute positions.
	 * 
	 * @param finalLeft Final position for left split line
	 * @param finalMid Final position for mid split line
	 * @param finalRight Final position for right split line
	 * @return Final relative position (split line position between its bounds) for mid.
	 */
	private static final double computeFinalRelative(double finalLeft,double  finalMid,double  finalRight) {
		
		double result = (finalMid - finalLeft) / (finalRight - finalLeft);
		if (result == 0)
			result = small;
		else if (result == 1)
			result = 1 - small;
		else if (!(result > 0 && result < 1))
			result = small;
		return result;
		//(finalMid - finalLeft) / (finalRight - finalLeft);
	}
	
	/**
	 * This moves an array of split lines whose final positions have been specified 
	 * through user interation.  The array is toMove[].  This is a recursive function.
	 * On the first call, currRoot should always be the root of the tree.  
	 * 
	 * Note that the MovingSplitLine class is a simple wrapper class (inner class of SplitAxis)
	 * that wraps a SplitLine object with a double finalPosition.  
	 * 
	 * @param currRoot The root of the tree on the first call, left or right descends on recursive calls
	 * @param toMove The array of split lines with computed final absolute positions (specified by user or stuck lines)
	 * @param transitions The hashtable containing a list of SplitTransition objects that will be used to queue animations later.
	 * @param numSteps The number of steps of animation. 
	 */
	private void moveSplitLineSet(SplitLine currRoot, MovingSplitLine [] toMove, Hashtable transitions, int numSteps) { 
		if (toMove == null || toMove.length <= 2) // <= 2 means no region
			return;

		if (currRoot == null ) {
			return;
		}
		
		final int frameNum = ad.getFrameNum();
		/**
		 * Note: we are using SplitLine.absoluteValue as a comparator.  So this is valid iff
		 * computePlaceThisFrame has been called on currRoot & toMove[0..size-1].  This assumption
		 * should be valid.
		 */
		int currRootIndex = Arrays.binarySearch(toMove,currRoot); 
		MovingSplitLine [] leftToMove;
		MovingSplitLine [] rightToMove;

		int left,right; 
		double rootFinal;
		if ( currRootIndex < 0) { 	// Then we know currRoot index is not one of the lines to move 
									// but it falls inside of the range of lines to move
			right = currRootIndex * -1 - 1; // left better be > 0 !
			left = right - 1;  // right <= size  
			currRootIndex = left;
			
			if (left < 0 || right < 0 || currRootIndex < 0) { 
				System.err.println("WTF");
				return;
			}
			
			rootFinal = computeFinalAbsolute(
					toMove[left].line.getValue(this, frameNum),
					currRoot.getValue(this, frameNum),
					toMove[right].line.getValue(this, frameNum),
					toMove[left].finalAbsolutePos, 
					toMove[right].finalAbsolutePos);
		} else { // Our currRoot is moving
			left = currRootIndex - 1; 
			right = currRootIndex + 1;
			rootFinal = toMove[currRootIndex].finalAbsolutePos;
		}

		MovingSplitLine movingRoot = new MovingSplitLine(currRoot,rootFinal); 
		
		leftToMove = new MovingSplitLine [left + 2]; // + 2 to include boundaries
		rightToMove = new MovingSplitLine [toMove.length - 1 - right + 2];

		leftToMove[leftToMove.length - 1] = movingRoot;
		for ( int i = 0 ; i < leftToMove.length - 1 ; i++ ) { 
			leftToMove[i] = toMove[i];
		}
		
		rightToMove[0] = movingRoot;
		for ( int i = 1 ; i < rightToMove.length ; i++ ) { 
			rightToMove[i] = toMove[i+currRootIndex];
		}			
		
		SplitTransition tr = new SplitTransition(currRoot,
				computeFinalRelative(toMove[0].finalAbsolutePos,
						rootFinal,toMove[toMove.length-1].finalAbsolutePos),
						numSteps);
		transitions.put(tr,tr);

		moveSplitLineSet(currRoot.getLeftChild(), leftToMove, transitions, numSteps );
		moveSplitLineSet(currRoot.getRightChild(), rightToMove, transitions, numSteps );
	}
	
	
	/**
	 * Returns true if baseLine is previous to comparator
	 * O(2 log n)
	 * 
	 * @param baseLine split line to test
	 * @param comparator second split line to compare
	 * @return true if baseline < comparator
	 */
	public boolean lessThan(SplitLine baseLine, SplitLine comparator) { 
		int baseIndex = getSplitIndex(baseLine);
		int compareIndex = getSplitIndex(comparator);
		
		return (baseIndex < compareIndex);
	}
	
	/**
	 * Returns true if baseLine is a successor to comparator
	 * O(2 log n)
	 * 
	 * @param baseLine split line to test
	 * @param comparator second split line to compare
	 * @return true if baseline > comparator
	 */	
	public boolean greaterThan(SplitLine baseLine, SplitLine comparator) { 
		int baseIndex = getSplitIndex(baseLine);
		int compareIndex = getSplitIndex(comparator);
		
		return (baseIndex > compareIndex);
	}
	
	/**
	 * Depending on whether line is a left or right child node, set the 
	 * opBound value of line to be the left or right bound.  
	 * @param line Split line to repair, will get new opbound value
	 */
	private void setBounds(SplitLine line ) { 
		if (line == null)
			return;
		
		if (line == root) {
			line.setOpBound(null);
			return;
		}
		
		if (line.getParent().getLeftChild() == line ) {
			line.setOpBound(computeLeftBound(line));
		} else { 	
			line.setOpBound(computeRightBound(line));
		}
	}
	
	/**
	 * Gets the root split line for this axis.
	 * @return current root node of this tree.
	 */
	public SplitLine getRoot() { 
		return root;
	}
	
	/**
	 * Retrieve left and right first bounds into an array. 
	 * NOTE: this returns cached bound values.  
	 * @param line split line to get bounds from.
	 * @param framenum current frame number.
	 * @return array of size 2 containing left and right bounds respectively.  Uses helper functions.
	 */
	public SplitLine[] getBounds(SplitLine line, int framenum ) {
		
		if (line == null) { 
			Exception e = new Exception("getBounds on null split line");
			e.printStackTrace();
			return null;
		}
		
		SplitLine[] bounds = new SplitLine[2];
		
		bounds[0] = getMinBound(line, framenum);
		bounds[1] = getMaxBound(line, framenum);
		
		return bounds;
	}
	
	/**
	 * Some split lines are 'fake' leaf lines that are created to be loaded into the 
	 * partitioned set.  This detects "fake" leaf lines.   
	 * 
	 * @param line Line to be tested.
	 * @return true if the line is real, otherwise false if it is a fake leaf
	 */
	public boolean isReal ( SplitLine line ) { 		
		if (line == null) { 
			System.out.println("Fake line is null!?");
			return false;
		}
		
		if (line == minStuckLine || line == maxStuckLine)
			return true;
		
		if ( line == root ) { 
			return true;
		}
		
		SplitLine parent = line.getParent();
		if ( parent.getLeftChild() == line || parent.getRightChild() == line )
			return true;
		
		return false;
	}
	
	/**
	 * Retrieve the given line's left (min) Split Line bound.
	 * The bound is either the parent of this line, or the opbound (off parent bound). 
	 * 
	 * @param line Line to get min bound of.
	 * @param framenum Current frame number.
	 * @return Split line that is either the parent, the min stuck line, or the opbound of this line. Null on error.
	 */
	public SplitLine getMinBound(SplitLine line, int framenum) { 
		if ( line == root) { 
			return minStuckLine;
		} else if ( line == minStuckLine || line == maxStuckLine) { 
			System.err.println("Tried to get min bound of a stuck line!");
			return null;
		} else if ( line == null) { 
			System.err.println("Tried to get min bound of a null line!");
			return null;
		} else if (line.getParent() == null) { 
			System.err.println("getMinBound: Null parent for line:" + line);
		}
		
		if (line == line.getParent().getLeftChild()) { // this is a left child 
			return line.getOpBound();
		} else if (line == line.getParent().getRightChild()) { // right child
			return line.getParent();
		} else { // no children bound for the parent of this line, so this line is fake 
//			System.out.println("Fake minbound");
			// Input parameter is a fake line.  We use absolute values to determine relative positions of parent and off parent lines.
			if (line.getOpBound() != null && 
					getAbsoluteValue(line.getParent(), framenum) > getAbsoluteValue(line.getOpBound(), framenum)) {  
				return line.getOpBound();
			} else { 
				return  line.getParent();
			}			
		}
	}
	
	/**
	 * Get the index of the min bound split line for the given line.
	 * @param line Line to get min bound of.
	 * @param framenum Current frame number.
	 * @return Index of split line returned by {@link #getMinBound(SplitLine, int)}.
	 */
	public int getMinBoundI(SplitLine line, int framenum) { 
		SplitLine result = getMinBound(line, framenum);
		
		return getSplitIndex(result);
	}
	
	/**
	 * Retrieve the given line's right (max) Split Line bound.
	 * The bound is either the parent of this line, or the opbound (off parent bound). 
	 * 
	 * @param line Line to get max bound of.
	 * @param framenum Current frame number.
	 * @return Split line that is either the parent, the max stuck line, or the opbound of this line.  Null on error.
	 */
	public SplitLine getMaxBound(SplitLine line, int framenum) { 
		if (line == null) { 
			System.err.println("getMaxBound on null line");
			return null;
		} else if ( line == root) { 
			return maxStuckLine;
		} else if ( line == minStuckLine || line == maxStuckLine) { 
			System.err.println("Tried to get max bound of a stuck line: " + line);
			return null;
		} else if (line.getParent() == null) { 
			System.err.println("getMinBound: Null parent for line: " + line);
			return null;
		}
		
		if (line == line.getParent().getRightChild()) { 
			return line.getOpBound();
		} else if (line == line.getParent().getLeftChild() ) { 
			return line.getParent();
		} else { 
//			System.out.println("Fake maxbound");
			// We are a fake
			if (line.getOpBound() == null || 
					getAbsoluteValue(line.getParent(),framenum) > getAbsoluteValue(line.getOpBound(),framenum) ) {  
				return line.getParent();
			} else { 
				return  line.getOpBound();
			}			
		}
	}

	/**
	 * Get the index of the max bound split line for the given line.
	 * @param line Line to get max bound of.
	 * @param framenum Current frame number.
	 * @return Index of split line returned by {@link #getMaxBound(SplitLine, int)}.
	 */
	public int getMaxBoundI(SplitLine line, int framenum) { 
		SplitLine result = getMaxBound(line, framenum);
		
		return getSplitIndex(result);
	}

	/**
	 * Computes and Returns line's first right bound.  
	 * NOTE: YOU SHOULD USE getRightBound to retrieve cached version, this is only for initialization or verification/debugging.
	 * O(log n)
	 * @param line Line to compute right (max) bound of.
	 * @return first right bound, possibly the 'virtual' max stuck position.  Null if you pass in a null line.
	 */
	private SplitLine computeRightBound(SplitLine line) { 
		if (line == null || line == root )
			return null;
		
		SplitLine parent; 
		SplitLine gp; // Grand parent
		
		if ( line == line.getParent().getRightChild() ) { 
			// I am a right child
			while (true) { 
				parent = line.getParent();
				if (parent == root)
					return maxStuckLine; // Return the stuck position
				gp = parent.getParent();
				if (parent == gp.getLeftChild())
					return gp; // gp is our rightmost bound
				else
					line = parent; // Ascend tree
			}
		} else { 
			// I am a left child
			return line.getParent();
		}
	}
	
	/**
	 * Computes and returns line's first left bound. 
	 * NOTE: YOU SHOULD USE getLeftBound to retrieve cached version, this is only for initialization or verification/debugging.  
	 * O(log n).
	 * @param line Line to compute left (min) bound of. 
	 * @return first left bound, possibly the 'virtual' min stuck position.  Null if you pass in a null line.
	 */
	private SplitLine computeLeftBound(SplitLine line) { 
		if (line == null || line == root)
			return null;
		
		SplitLine parent; 
		SplitLine gp; // Grand parent
		
		if ( line == line.getParent().getLeftChild() ) { 
			// I am a left child
			while (true) { 
				parent = line.getParent();
				if (parent == root)
					return minStuckLine; // Return the stuck position
				gp = parent.getParent();
				if (parent == gp.getRightChild())
					return gp; // gp is our rightmost bound
				else
					line = parent; // Ascend tree
			}
		} else { 
			// I am a right child
			return line.getParent();
		}
	}
	
	/**
	 * Get the partitioned list, the list created prior to rendering to determine the visible objects and culling limit.
	 * @return Returns the partitionedList.
	 */
	public TreeSet getPartitionedList() {
		return partitionedList;
	}
	
	/**
	 * Get the number of split lines on the axis, not including min and max stuck lines. 
	 * @return number of splitlines on this axis, does not include minStuckLine or maxStuckLine
	 */
	public int getSize() { 
		return size;
	}

	/**
	 * Wrapper class for storing end position and Split line state for a moving split line.
	 * @author Peter McLachlan (spark343@cs.ubc.ca)
	 *
	 */
	protected class MovingSplitLine implements Comparable {
		/** Split line state variable. */
		private SplitLine line;
		/** Final position of the split line in world-space coordinates. */
		private double finalAbsolutePos;

		/**
		 * Use the split line comparison function to compare two moving split lines for similarity.
		 * @param arg0 Second split line to compare with.
		 * @return See {@link SplitLine#compareTo(Object)} for return values.
		 */
		public int compareTo(Object arg0) {
			return line.compareTo(arg0);
		}

		/**
		 * Constructor for moving split lines.
		 * @param line Line that is moving.
		 * @param finalAbsolutePos Final position in world-space coordinates for this line.  
		 * The current position is always stored by the split line (cached) or may be computed 
		 * with relative positions of a split line's ancestory.
		 */
		public MovingSplitLine(SplitLine line, double finalAbsolutePos) {
			this.line = line;
			this.finalAbsolutePos = finalAbsolutePos;
		}
		
		/**
		 * Display function for moving split lines.
		 * @return String representation of the moving split line.
		 */
		public String toString() { 
			return "MSL: " + line.toString() + " Final: " + finalAbsolutePos;
		}
		
	}

	/**
	 * Error printing function, displays string in stderr.
	 * @param s String to send to stderr, shortcut for System.err.println(String).
	 */
	protected static final void print (String s) { 
		System.err.println(s);
	}
	
	/**
	 * String output routine for splitaxes, displays {@link net.sourceforge.olduvai.accordiondrawer.SplitLine#toLongString()} for root of axis.
	 * @return String value for root.toLongString().
	 */
	public String toString() { 
		return root.toLongString();
	}
	
	// @Override
	// TODO: find out what sortedmap stuff was needed from here, all functions were stubs

	/**
	 * Recursively update the size of subtrees under the given current split line.
	 * O(n) operation, currRoot is initially the root node.
	 * @param currRoot Current split line being updated
	 */
	public void updateSubtreeSize(SplitLine currRoot)
	{
		SplitLine left = currRoot.getLeftChild();
		SplitLine right = currRoot.getRightChild();
		if (left != null)
			updateSubtreeSize(left);
		if (right != null)
			updateSubtreeSize(right);
		currRoot.subTreeSize = (left != null ? left.subTreeSize : 0) + 
		(right != null ? right.subTreeSize : 0) + 1;
	}


	/**
	 * Returns an iterator over the axis.  Note that this iterator 
	 * contains the minStuckLine and maxStuckLine.
	 * Uses the AxisIterator internal class. 
	 * 
	 * @return new AxisIterator object for this set of split lines.
	 */
	public Iterator<SplitLine> iterator() { 
		return new AxisIterator();
	}
	
	
	/**
	 * Specifies the log file to use for this split axis.  
	 * 
	 * If not null, implicitly enables logging unless LOGGING=false is set at compile time.
	 * 
	 * @param logger the logger to set
	 */
	public void setLogger(SplitAxisLogger logger) {
		this.logger = logger;
	}
	/**
	 * Retrieves the logger being used for this SplitAxis
	 * @return the logger
	 */
	public SplitAxisLogger getLogger() {
		return logger;
	}

	/**
	 * Iterator for the SplitAxis class of objects.
	 *   
	 * @author Peter McLachlan (spark343@cs.ubc.ca)
	 *
	 */
	public class AxisIterator implements Iterator<SplitLine> {
		/**
		 * Iterator state, the current pointed to split line.
		 */
		private SplitLine currLine = null;
		/**
		 * Set to {@link SplitAxis#modCount}, protection against a dynamic structure changing during the life of an iterator.
		 */
		private int fModCount;

		/**
		 * Creates iterator object, initializes state values.
		 *
		 */
		public AxisIterator () { 
			super ();
			this.fModCount = modCount;
		}
		
		/**
		 * Test state to see if there are split lines after the current line.
		 * @return True if there is a next line after {@link #currLine}.
		 */
		public boolean hasNext() {
			if (fModCount != modCount)
				throw new ConcurrentModificationException();
			return (currLine != maxStuckLine);
		}

		/**
		 * Get the next split line in the axis from the current value of {@link #currLine}.
		 */
		public SplitLine next() {
			if (fModCount != modCount)
				throw new ConcurrentModificationException();
			
			if (currLine == null)
				currLine = minStuckLine;
			else
				currLine = successor(currLine);
			
			return currLine;
		}

		/**
		 * Stub function.
		 */
		public void remove() {
			System.err.print("Remove not implemented on AxisIterator");
		}
	}

	/**
	 * Internal class for debugging the split axis class.
	 * @author Peter McLachlan (spark343@cs.ubc.ca)
	 *
	 */
	public class Debug {
		
		/**
		 * Debugging function to print the absolute values for the entire tree
		 * from left to right.
		 *
		 */
		public void printAllAbsoluteValues() { 
			SplitLine current = root;
			
			while (current.getLeftChild() != null) 
				current = current.getLeftChild();
			
			System.out.println("AV: " + getAbsoluteValue(current,10) );
			
			current = successor(current); 
			while (current != null && current != maxStuckLine) {
				System.out.println("AV: " + getAbsoluteValue(current,10) );
				current = successor(current);
			}
		}
		
		/**
		 * Verify that the split axis structure is valid.
		 * Starts checking at root, recursive calls with {@link #checkAllBoundsRecursive(SplitLine)}. 
		 *
		 */
		public void checkAllBounds() { 
			SplitLine current = root;
			
			if (size != 0 && current == null) { 
				print("Size is not 0 but root is NULL!"); 
				Exception e = new Exception();
				e.printStackTrace();
				System.exit(-21);
			}
			
			if ( current == null)
				return;
			
			if (current.getOpBound() != null) { 
				print("Root's op bound isn't null!");
			}
			
			checkAllBoundsRecursive(current.getLeftChild());
			checkAllBoundsRecursive(current.getRightChild());
		}

		/**
		 * Recursive part of {@link #checkAllBounds()}.
		 * A line's parent is one bound, the other bound is checked recursively.
		 * Recursion continues on children of the current line.
		 * @param line Current line to check bounds.
		 */
		private void checkAllBoundsRecursive(SplitLine line) { 
			if( line == null)
				return;
			
			SplitLine computedBound;
			
			if (line.getParent().getLeftChild() == line ) {
				computedBound = computeLeftBound(line);
			} else { 	
				computedBound = computeRightBound(line);
			}
			
			if (line.getOpBound() != computedBound )
				print("SplitLine: opBound is:" + line.getOpBound().absoluteValue + " should be: " + computedBound.absoluteValue );
			checkAllBoundsRecursive(line.getLeftChild());
			checkAllBoundsRecursive(line.getRightChild());
		}
		
		/**
		 * Check the size of the subtrees, recursively.
		 * Calls {@link #checkSubTreeSizesRecursive(SplitLine)} then adds results of left and right to itself (1).
		 *
		 */
		public void checkSubTreeSizes() { 
			if ( root == null)
				return;
			
			if (root.subTreeSize != size)
				print("Tree size and root subtree size don't match");
			
			int leftSize = (root.getLeftChild() != null) ? root.getLeftChild().subTreeSize : 0;
			int rightSize = (root.getRightChild() != null) ? root.getRightChild().subTreeSize : 0;
			
			if ( root.subTreeSize != ( leftSize + rightSize + 1))
				print("Root subtree size invalid!");
			
			checkSubTreeSizesRecursive(root.getLeftChild());
			checkSubTreeSizesRecursive(root.getRightChild());
		}
		
		/**
		 * Recursive part of {@link #checkSubTreeSizes()}.
		 * @param line Current line to check.
		 */
		private void checkSubTreeSizesRecursive(SplitLine line) { 
			if (line == null)
				return;
			
			int leftSize = (line.getLeftChild() != null) ? line.getLeftChild().subTreeSize : 0;
			int rightSize = (line.getRightChild() != null) ? line.getRightChild().subTreeSize : 0;
			
			if ( line.subTreeSize != ( leftSize + rightSize + 1))
				print("SplitLine " + line.absoluteValue + " subtree size invalid!");
			
			checkSubTreeSizesRecursive(line.getLeftChild());
			checkSubTreeSizesRecursive(line.getRightChild());			
		}
		
		/**
		 * Depending on whether line is a left or right child node, set the 
		 * opBound value of line to be the left or right bound.  
		 * 
		 * NOTE: THIS IS AN O(n) operation intended for debugging.
		 * 
		 * @param line Line to set bounds of.  Initially root, then this function is called recursively.
		 */
		public void setAllBounds(SplitLine line) { 
			if (line == null)
				return; // Exit condition
			
			if ( line == root ) { 
				// Just descend
				setAllBounds(line.getLeftChild());
				setAllBounds(line.getRightChild());
				return;
			}
			
			if (line.getParent().getLeftChild() == line ) {
				line.setOpBound(computeLeftBound(line));
			} else { 	
				line.setOpBound(computeRightBound(line));
			}
			
			setAllBounds(line.getLeftChild());
			setAllBounds(line.getRightChild());	
		}

		/**
		 * Get the next line, with the {@link SplitAxis#successor(SplitLine)} function supplied by the parent class.
		 * @param line Line to get next of.
		 * @return Next line, beside the input parameter.
		 */
		public SplitLine getSuccessor(SplitLine line) { 
			return successor(line);
		}
		
		/**
		 * Get the previous line, with the {@link SplitAxis#previous(SplitLine)} function supplied by the parent class.
		 * @param line Line to get previous of.
		 * @return Previous line, beside the input parameter.
		 */		public SplitLine getPrevious(SplitLine line) { 
			return previous(line);
		}
		
	}
	
}