package net.sourceforge.olduvai.accordiondrawer;

import java.util.TreeSet;

/**
 * An instance of a SplitLine.  Note that the comparator uses the absoluteValue 
 * of this object.  This code also implements the TreeSet lookup hack, which allows
 * for the retrieving of an object in the TreeSet using the 'contains' method call 
 * on the tree.  Note that this hack is not thread safe.
 * This class must be kept lightweight with respect to state, as there may be many of them in some applications.
 */
public class SplitLine implements Comparable {
	
	/** State for fake lines or real lines.  Fake lines are not in the treeset, but are added as needed during partitioning. */
	private boolean isFakeLine = false; // remove later, just for debugging

	/** Global static variable for retrieving a split line from the split axis.
	 * See {@link #compareTo(Object)}, which does not have a return value, but sets a value for {@link #splitLineFound} if this is true. */
	public static boolean getSplitLine = false;
	/** Global static variable for storing a found split line from the split axis.
	 * See {@link #compareTo(Object)}, which does not have a return value, but sets this if {@link #getSplitLine} is true. */
	public static SplitLine splitLineFound = null;
	
	// binary tree left and right children
	/** Left child of this split line, null if the child is null (possible leaf is right is also null). */
	private SplitLine leftChild = null;
	/** Right child of this split line, null if the child is null (possible leaf is left is also null). */    
	private SplitLine rightChild = null;
    /** Parent of this split line, null if this is the root.  Set to some real splitline for virtual split lines, null for stuck lines. */
    private SplitLine parent; // My parent. null if root?
    /** Off-parent bound of this split line, null if this is the root.  Set to some real splitline for virtual split lines, null for stuck lines. 
     * An example off parent is the first left parent of a node that is the right child of it's direct parent.
     * This, with the parent split line, bounds the movement of this split line in world space, and are used to compute {@link #absoluteValue} with the hierarchy of {@link #relativeValue} */
	private SplitLine opBound = null;  // Opposite bound from parent, also null if root?
	/** State for red-black tree. Either {@link SplitAxis#BLACK} or {@link SplitAxis#RED}. */
    protected boolean color = SplitAxis.BLACK;

	/**
	 * Relative value, between 0 and 1, between {@link #opBound} and {@link #parent}, the bounds for this split line.
	 */
    protected double relativeValue;
    /**
     * Absolute value, between 0 and 1, between {@link SplitAxis#minStuckLine} and {@link SplitAxis#maxStuckLine}.
     */
    protected double absoluteValue;
    /** Current frame for rendering, monotonically increasing, for determining validity of {@link #absoluteValue} (caching). */
    protected int computedFrame;
	/**
	 *  How many nodes are below me, including me (1 for leaves).
	 */
    protected int subTreeSize;
    
    /**
     * An object to assist with aggregation when multiple splitcells are hidden under a single 
     * pixel.  Stores {@link CellGeom} or other application-specific objects.  
     */
	public Object cullingObject; 
	
	/**
	 * An object that stores a 'row' of data.  LRAC-specific purpose.
	 * TODO: identify this purpose, the type of object stored here.
	 */
	public Object rowObject;  

	/** Accessor for parent object. 
	 * @return Split line that is the {@link #parent}. */
	public SplitLine getParent () { 
		return parent;
	}
	/** Modifier for parent object. For dynamic split lines.  
	 * @param s new {@link #parent} for this line.
	*/
	public void setParent(SplitLine s)
	{
		parent = s;
	}

	/** Accessor for left child. 
	 * @return Split line that is the {@link #leftChild}.
	 */
	public SplitLine getLeftChild () { 
		return leftChild;
	}
	/** Modifier for left child.
	 * @param s New {@link #leftChild} for this line.
	 * */
	public void setLeftChild(SplitLine s)
	{
		leftChild = s;
	}
	/** Accessor for right child. 
	 * @return Split line that is the {@link #rightChild}.
	 */
	public SplitLine getRightChild () { 
		return rightChild;
	}
	/** Modifier for right child.
	 * @param s New {@link #rightChild} for this line.
	 * */
	public void setRightChild(SplitLine s)
	{
		rightChild = s;
	}
	
	/**
	 * Default split line constructor.
	 * Use all default (impossible in some cases) values for state.
	 * No parameter constructor initializes absolute value to the (invalid) value of -1.0 
	 * which should be fixed on the next compute place this frame.  Use the SplitAxis.putAt()
	 * method to add this to the tree, at which point the parent and opParent values are 
	 * set by the SplitAxis class.  
	 *
	 */
	public SplitLine() {
//		System.out.println("Creating new splitline, no args");
        this.absoluteValue = -1.0;
        this.parent = null;
        this.opBound = null;
        subTreeSize = 1; // I am the only node in my subtree to begin with.
        computedFrame = -1;
        relativeValue = SplitAxis.defaultSplitValue;
	}
	    
    /**
     * Make a new splitline with given key, value, and parent, and with
     * <tt>null</tt> child links, and BLACK color.
     * This is used to create "dummy" split lines.
     * A new SplitLine object. Generally the absoluteValue set here is meaningless 
	 * since the first time SplitAxis.computePlaceThisFrame is run it will get 
	 * changed anyway.  The relativeValue is set based on SplitAxis.defaultSplitValue
	 * which would typically be .5 if the red-black tree is perfectly balanced.  
	 * 
	 * @param isVirtual true for virtual split lines, created for rendering but not added to the split axis hierarchy.
     * @param value initial {@link #absoluteValue} value for this split line.  Will be used to place and determine relative value.
     * @param parent {@link #parent} split line
     * @param opAncestor opposite split line, {@link #opBound}
     */
    public SplitLine(double value, SplitLine parent, SplitLine opAncestor, boolean isVirtual) {
    	isFakeLine = isVirtual;
        this.absoluteValue = value;
        // for virtual lines: we want the parent to be a lower node in the tree than the other bound, and not a stuck line 
        if (!isVirtual || (parent != null && // virtual line has a parent, 
        		parent.subTreeSize != -1 && // and parent isn't a stuck line
        		opAncestor != null && // opAncestor can't be null TODO: check this to make sure it works
        		(parent.subTreeSize < opAncestor.subTreeSize || opAncestor.subTreeSize == -1))) // and parent is descendant of opbound, opbound isn't a stuck
        {
//        	System.out.println("II keeping parent");
        	this.parent = parent;
        	this.opBound = opAncestor;
        }
        else // either parent is a stuck line, or opbound is lower than parent
        {
//        	System.out.println("II using opbound " + parent + " " + parent.subTreeSize + " " + opAncestor.subTreeSize);
        	this.parent = opAncestor;
        	this.opBound = parent;
        }
        subTreeSize = 1; // I am the only node in my subtree to begin with.
        computedFrame = -1;
        relativeValue = SplitAxis.defaultSplitValue;
    }


    /**
     * Returns the absolute value of this splitLine.  Computes the place of the line for the current frame.
     * @param axis the axis to use for computing the value for this line.
     * @param frameNum the frame number to check if computing the position is required (or if cache is safe)
     * @return the absolute location of this split line on screen (used for drawing)
     */
    public double getValue(SplitAxis axis, int frameNum) {
    	if (this == axis.minStuckLine)
    		return axis.minStuckLine.absoluteValue;
    	else if (this == axis.maxStuckLine)
    		return axis.maxStuckLine.absoluteValue;
    	
    	if (frameNum > computedFrame)
    		axis.computePlaceThisFrame(this, frameNum);
        return absoluteValue;
    }
    
    /**
     * Returns the cached value of the line without checking if it is up to date.
     * {@link #getValue(SplitAxis, int)} is much safer if the real position of the line is required.
     * @return The value of {@link #absoluteValue}.
     */
    public double getCachedValue() { 
    	return absoluteValue;
    }

    /**
     * Note: natural ordering is NOT consistent with equals.
     * Natural ordering uses absoluteValue, equals uses splitCount (metricName).
     * @param o Second object to test for equality.
     * @return True if the object and this split line have identical cached absolute positions.
     */
    public boolean equals(Object o) {   	
    	return this.compareTo(o) == 0;
    }

    /** String representation of this split line.
     * @return debugging string used to identify the split line. */
    public String toString() {
    	
    	return
    	//" Rel: " + relativeValue + "AV: " + 
    	"(" + absoluteValue + "@" + hashCode()%1000 + ")";
    }
    
    /**
     * Recursive descent print: For printing the split axis tree under this node (recursive).
     * Very inefficient and time consuming for large trees.
     * Danger: used by {@link SplitAxis#print(String)} on the root node.
     * @return recursive concatenation of axis tree below this node
     */
    public String toLongString() {
    	String leftTree = "null";
    	String rightTree = "null";
    	if (leftChild != null)
    		leftTree = leftChild.toLongString();
    	if (rightChild != null)
    		rightTree = rightChild.toLongString();
    	return toString() + "(" + leftTree + ":" + rightTree + ")";
    }

    /**
     * Compares absolute values of two SplitLines.  
     * NOTE: this comparison is only valid AFTER you have called 
     * 'computeplacethisframe'!!
     * 
     * Note, if 'getSplitLine' static boolean is set to true it will set the
     * splitLineFound static pointer either to the exact match or the nearest
     * match less than the absoluteValue of the object passed in.  
     * 
     * @param o Second split line to test position.
     * @return -1 for this lesser than o, +1 for greater than, 0 for equality.
     */
	public int compareTo(Object o) {
//		if (getSplitLine)
//			System.out.println("Break");
				
		SplitLine ob = (SplitLine) o;
		if (getSplitLine && splitLineFound == null)
			splitLineFound = ob;
		
		if ( absoluteValue < ob.absoluteValue ) 
			return -1;
		else if ( absoluteValue > ob.absoluteValue ) {
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
	 * @param searchTree Tree of split lines to test, usually a partitioned set of split lines (set covers whole space between {@link SplitAxis#minStuckLine} and {@link SplitAxis#maxStuckLine}.
	 * @param input Split line to locate within searchTree (should bounded by exactly one split line in the tree)
	 * @return The split line in searchTree that contains the given split line.
	 */
	public static SplitLine getOverlapSplitCell(TreeSet searchTree, SplitLine input) {
		SplitLine first = (SplitLine) searchTree.first();
		if (input.absoluteValue <= first.absoluteValue)
			return first; // give up, return first split line in searchtree
			
		SplitLine.getSplitLine = true;
		SplitLine.splitLineFound = null;
		searchTree.contains(input);

//			System.out.println("Exact match!");
		
		SplitLine.getSplitLine = false;
		return SplitLine.splitLineFound;
	}

	/**
	 * Accessor for {@link #subTreeSize}.
	 * @return Returns the subTreeSize.
	 */
	public int getSubTreeSize() {
		return subTreeSize;
	}

	/**
	 * Modifier for {@link #subTreeSize}.
	 * @param subTreeSize The subTreeSize to set.
	 */
	public void setSubTreeSize(int subTreeSize) {
		this.subTreeSize = subTreeSize;
	}

	/**
	 * Accessor for {@link #cullingObject}.  Should be cast later for application-specific use.
	 * @return Returns the cullingObject.
	 */
	public Object getCullingObject() {
		return cullingObject;
	}

	/**
	 * Modifier for {@link #cullingObject}.  Generic object, application-specific use for culling.
	 * @param cullingObject The cullingObject to set.
	 */
	public void setCullingObject(Object cullingObject) {
		this.cullingObject = cullingObject;
	}

	/**
	 * Accessor for {@link #rowObject}.
	 * @return Returns the rowObject.
	 */
	public Object getRowObject() {
		return rowObject;
	}

	/**
	 * Modifier for {@link #rowObject}.
	 * @param rowObject The rowObject to set.
	 */
	public void setRowObject(Object rowObject) {
		this.rowObject = rowObject;
	}
	
	/**
	 * Test function for internal nodes, true if this line is a left child of its immediate parent.
	 * @return True if this split line is to the left of its parent.
	 * Already known that this is not the root.
	 */
	public boolean isLeftChild()
	{
		if (parent == null)
			return false; // root is right child (arbitrary)
		return parent.leftChild == this;
	}

	/**
	 * Accessor for {@link #opBound}.
	 * @return Returns the opBound.
	 */
	public SplitLine getOpBound() {
		return opBound;
	}

	/**
	 * Modifier for {@link #opBound}.
	 * @param opBound The opBound to set.
	 */
	public void setOpBound(SplitLine opBound) {
		this.opBound = opBound;
	}
	
	/** Root test. 
	 * @return True if this is the root node, which means the parent of this node is unset.
	 * */
	public boolean isRoot()
	{
		return parent == null;
	}

	
}