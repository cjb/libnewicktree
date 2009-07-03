/*
 * Created on May 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.sourceforge.olduvai.accordiondrawer;

import java.awt.Color;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represents a list of ranges along ONE axis.  
 * A range is a set of indices min,max that represent cells 
 * in the SplitLine grid.  
 *   
 * @author hilde, jslack
 */
public abstract class AbstractRangeList {
	
	/** The drawing color for this range list, if appropriate. */
	protected Color color;
	
	/** Resizeable Set of RangeInTree objects.  Usually a treeset but that isn't required.  */
	protected Set ranges;
	
	/** True when this group is active, and will be drawn.  Setting to false will not draw this group. */
	protected boolean enabled;
	
	/** Unique integer key for this group */
	protected int key;
	
	/**
	 * Application-specific accessor for split line indices that surround this range.
	 * In TJ, we use indices in the set of leaves, in SJ it could either be a range of
	 * Sequences or a range of Nucleotides. 
	 * @param horizontal For SJ, horizontal = true for nucleotide ranges, false for sequences
	 * @return Indices to a pair of split lines that surround the range.
	 */
	abstract public int[] getSplitIndices(boolean horizontal);
	
	/**
	 * Analagous to {@link #getSplitIndices(boolean)} but returns the list of lines instead.
	 * 
	 * @param axis Axis that contains this list of ranges
	 * @return A pair of split lines that surround each range in this list.
	 */
	public SplitLine[] getSplitLines(SplitAxis axis)  { 
		
		SplitLine[] returnArray = new SplitLine[ranges.size() * 2];
		Iterator iter = ranges.iterator();
		int i = 0;
		while (iter.hasNext())
		{
			DrawableRange currRange = (DrawableRange)iter.next(); 
			returnArray[i*2] = axis.getSplitFromIndex(currRange.getMin()-1);
			returnArray[i*2+1] = axis.getSplitFromIndex(currRange.getMax());
//			System.out.println("indices for currRange " + currRange + ": " + returnArray[i*2] + ", " + returnArray[i*2+1]);
			i++;
		}
		return returnArray;
	}
	/**
	 * For each range in {@link #ranges}, store the size in world coordinates
	 * @param splitAxis The split axis that the range set represents
	 * @param frameNum The current frame number
	 * @return An array of range sizes, in world coordinates.
	 */
	abstract public double[] getSizesOfAllRanges(SplitAxis splitAxis, int frameNum);
	/**
	 * Return the quantity of world space that may not be shrunk by a stretching operation.
	 * @param ad The accordion drawer to use for growing/shrinking 
	 * @param splitAxis The axis (X or Y) for growing/shrinking
	 * @param frameNum Current frame number
	 * @return The total world space that may not be shrunk after stretching.
	 */
	abstract public double getUnshrinkableTotal(AccordionDrawer ad, SplitAxis splitAxis, int frameNum); // total of all shrinking regions that will not shrink
	/**
	 * Cuts the ranges into a subset of only ranges in the given drawer.
	 * @param d The drawer we wish to get the ranges in.
	 * @return A new range list populated with only ranges from the given drawer
	 */
	abstract public AbstractRangeList onlyThisAD(AccordionDrawer d);
	/**
	 * Reverses the ranges so we may grow the unselected ranges, performing a shrink on the selected ranges.
	 * @param xy The axis direction for shrinking.
	 * @param ad The drawer for shrinking (contains the split axis)
	 * @return The reverse of the ranges, or the list of unselected ranges.
	 */
	abstract public AbstractRangeList flipRangeToShrink(int xy, AccordionDrawer ad);
	/**
	 * Sets the color of the marked ranges.
	 * @param c The color to set the ranges, {@link #color}.
	 */
	public void setColor(Color c) {color = new Color(c.getRed(), c.getGreen(), c.getBlue(), key);}
	/**
	 * Accessor for the range color, {@link #color}
	 * @return The value of {@link #color}
	 */
	public Color getColor() {return color;}
	/**
	 * Accessor for the key value, {@link #key}
	 * @return The value of {@link #key}
	 */
	public int getKey() { return key; }
	/**
	 * Sets the key to the given value.  Also sets the alpha of {@link #color} to {@link #key}, so we may later recover the depth of the current color, which we store in the alpha channel.
	 * @param key The new value of {@link #key}, and the new value of the {@link #color} alpha channel.
	 */
	public void setKey(int key) { this.key = key; setColor(color);  }
	/**
	 * Accessor for the number of ranges in the set of ranges {@link #ranges}.
	 * @return The number of objects in {@link #ranges}, which are the selected ranges.
	 */
	public int size() { return ranges.size(); }
	/**
	 * Sets the value of {@link #enabled}, turning on (true) or off (false) the rendering of the group defined by this abstract range list.
	 * @param enabled The new value of {@link #enabled}.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/** Accessor for the value of {@link #enabled} 
	 * @return The value of {@link #enabled} */
	public boolean isEnabled() {
		return enabled;
	}
}
