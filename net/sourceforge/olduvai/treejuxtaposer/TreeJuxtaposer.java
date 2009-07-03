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

package net.sourceforge.olduvai.treejuxtaposer;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import net.sourceforge.olduvai.accordiondrawer.*;
import net.sourceforge.olduvai.treejuxtaposer.drawer.*;


import java.io.*;


/**
 * A class representing a tree juxtaposer.  This is the main application
 * class, which controls all the drawer objects and trees within those drawers.
 * 
 * A TreeJuxtaposer takes a set of trees and build the data structures
 * for each pair for best corresponding nodes lookup and range
 * intersection queries.  The TreePairs class encapsulates all the
 * Tree2Tree classes required to relate pairs of trees in the list
 * "trees".
 * 
 *
 * @author Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
 * @version 2.1
 * @see     net.sourceforge.olduvai.treejuxtaposer.drawer.Tree
 * @see     net.sourceforge.olduvai.treejuxtaposer.drawer.TreeNode
 * @see     net.sourceforge.olduvai.accordiondrawer.CellGeom
 * @see     net.sourceforge.olduvai.treejuxtaposer.TreePairs
 * @see     net.sourceforge.olduvai.treejuxtaposer.drawer.AccordionTreeDrawer
 * @see     net.sourceforge.olduvai.treejuxtaposer.AccordionTreeDrawerFinal
 */


public class TreeJuxtaposer {

	/**
	 * Version string for this application.
	 */
	public static final String versionNumber = "2.1";

	/**
	 * Frame object for holding everything.
	 */
	protected Frame mainFrame;
	/**
	 * Title for the application.  This changes depending on what trees are loaded.
	 */
	private String title;

	/**
	 * State frame object, for manipulating which marking group is active for resizing or coloring.
	 */
	protected StateFrame stateFrame;
	/**
	 * Settings frame object, for changing font size, progressive rendering, and other options.
	 */
	protected SettingsFrame settingsFrame;
	/**
	 * Incremental search frame, for doing text searches.
	 */
	private IncrementalSearch searchFrame;
	/**
	 * Debugging frame, for reporting information that is meant to be hidden on releases.
	 */
	protected DebugFrame debugFrame;
	/**
	 * Drawing panel (canvas panel) for the main frame.
	 */
	private Panel drawPanel;
	/**
	 * Layout manager for the drawing panel.  Makes new tree drawers appear to the right of old ones.
	 */
	private GridLayout drawLayout;


	/**
	 * Resizeable array of AccordionTreeDrawerFinal objects (not trees, which are {@link #trees}).
	 */
	protected ArrayList treeDrawers;
	/**
	 * List of lists of tree drawers, each list is a row of drawers in the matrix layout.
	 */
	private ArrayList ATDMatrix;

	/**
	 * Resizeable array of Tree objects (not drawers, which are {@link #treeDrawers}).
	 */
	protected ArrayList trees;

	/**
	 * Rearrangable list of RangeLists (groups).  See {@link #groupsByIndex} for
	 * the marking group list by index.
	 */
	protected LinkedList groups;
	/**
	 * List of marking groups by index.  This is static once initialized.
	 * See {@link #groups} for the rearrangeable priority list.
	 */
	private Vector groupsByIndex;

	/**
	 * An array of TreePairs objects: 
	 * each TreePair contains the necessary data structures for
	 * the differences and node correspondences between nodes in a pair of trees.
	 **/
	private TreePairs TPs;

	/**
	 * Background color for the canvas.
	 */
	private Color backgroundColor;
	/**
	 * Default node color.
	 */
	private Color objectColor;
	/**
	 * Color for labels of normal (not highlighted) nodes.
	 */
	private Color labelColor;
	/**
	 * Color for label background of normal (not highlighted) nodes.
	 */
	private Color labelBackColor;
	/**
	 * Color for labels for highlighted nodes.
	 */
	private Color labelHiColor;
	/**
	 * Color for the label background for highlighted nodes.
	 */
	private Color labelBackHiColor;
	
	/**
	 * Number of tree objects.  Used to set the key for trees.
	 */
	protected static int treeCount;

	/**
	 * "Old" geometry from a previous flash action, which is stored in its original
	 * state in the pixel buffer.
	 */
	private CellGeom flashGeomOld;
	/**
	 * True for matrix mode, not supported.  Matrix mode is pair-wise differences
	 * between trees, and n^2 drawers for n loaded trees.  Trees on the diagonal
	 * are original trees, and differences are computed everywhere else.
	 */
	private boolean matrix;

	/**
	 * Thickness of tree edge lines for default non-marked nodes.
	 */
	protected int linethickness = 1;

	/**
	 * Number of arguments passed to application.
	 */
	private int treeArgs;

	/**
	 * Quasimode: true when resizing subtrees with linked navigation control.
	 */
	private boolean quasimode;
	/**
	 * The last used accordion tree drawer, which will be the drawer that gets focus if focus is regained after being lost.
	 */
	private AccordionTreeDrawer wantsFocusATD;

	/**
	 * Number of edge weights increments to compute.
	 */
	private int edgeweightLevels;
	/**
	 * Use a subset of the edge weight levels, to a maximum of this value.
	 */
	private int edgeweightLevelUsed;

	/**
	 * Object for controlling user interface functions directed from the top menu (FILE, HELP, etc), 
	 * to separate the menu functionality handling (string names, setup, etc) from the application.
	 */
	private UI ui;
	/**
	 * BCN filtering score for the entire tree.  Nodes with BCN less than this value are marked as different.
	 */
	private float bcnScore;

	/**
	 * List of TreeNodes that are marked with the LCA group color.
	 * Redundant with the LCA group?
	 */
	public ArrayList lcaNode = new ArrayList();


	/**
	 * Main constructor for the TJ object, initializes states, and user interfaces.
	 *
	 */
	public TreeJuxtaposer() {
		AccordionTreeDrawer.fullyQualified = false;
		matrix = false;
		// we use both swing and awt components,
		// so must disable the swing focus manager!
		FocusManager.disableSwingFocusManager();
		title="TreeJuxtaposer";
		ui = new UI(this, title);
		mainFrame = ui.getMainFrame();

		drawPanel = ui.getDrawPanel();
		searchFrame = ui.getSearchFrame();
		stateFrame = ui.getStateFrame();
		debugFrame = ui.getDebugFrame();
		settingsFrame = ui.getSettingsFrame();   

		treeDrawers = new ArrayList();
		ATDMatrix = new	ArrayList(); 
		trees = new ArrayList();
		groups = new LinkedList();
		groupsByIndex = new Vector(7);

		backgroundColor = Color.getHSBColor(0.0f/360f,0f,1f);
		// remember that rbcol is xor'ed against backcolor, 
		// so set to opposite of desired color...
		objectColor = Color.getHSBColor(0.0f/360f,.0f,0.15f);
		labelColor = Color.getHSBColor(0f/360f,0f,0f);
		labelBackColor = Color.getHSBColor(0.0f/360f,0.0f,1f);
		labelHiColor = Color.getHSBColor(0.0f/360f,.0f,0.15f);
		labelBackHiColor = Color.getHSBColor(36f/360f,1f,1f);

		Color flashCol = StateFrame.initialColor[StateFrame.M_ACT]; //orange
		Color foundCol = StateFrame.initialColor[StateFrame.F_ACT]; //pink
		Color diffCol = StateFrame.initialColor[StateFrame.D_ACT]; //red
		Color lcaCol = StateFrame.initialColor[StateFrame.LCA_ACT]; // green
		//Color diffCol = new Color(87, 152, 64); // dark green, maybe good for contest?

		TPs = new TreePairs();
		treeCount = 0;

		RangeList diffGroup = addGroup(StateFrame.D_ACT, true);
		diffGroup.setColor(diffCol);

		for (int currMarkGroup = StateFrame.GA_ACT; currMarkGroup <= StateFrame.GH_ACT; currMarkGroup++)
		{
			RangeList markGroup = addGroup(currMarkGroup, false);
			markGroup.setColor(StateFrame.initialColor[currMarkGroup]);	 
		}

		RangeList flashGroup = addGroup(StateFrame.M_ACT, false);
		flashGroup.setColor(flashCol);
		// enabled by default, unset since should be off
		flashGroup.setEnabled(false);

		RangeList foundGroup = addGroup(StateFrame.F_ACT, false);
		foundGroup.setColor(foundCol);
		RangeList LCAGroup = addGroup(StateFrame.LCA_ACT, false);
		LCAGroup.setColor(lcaCol);
				
		for (int currMarkGroup = StateFrame.GH_ACT;
			currMarkGroup >= StateFrame.GA_ACT; currMarkGroup--)
		{
			setGroupPriority(currMarkGroup);
		}

		settingsFrame.setStructDiff(true);

		quasimode = false;
		wantsFocusATD = null;

		edgeweightLevels = 1;
		edgeweightLevelUsed = 0;
		bcnScore = 1.0f;
	}

	/**
	 * Called back when the mouse re-enters the main frame, gives focus to the last
	 * drawer to have it.
	 * @param atd The last drawer that had focus, prior to the cursor leaving.  For
	 * remembering how to undraw an old flash draw.
	 */
	protected void wantsFocusInQuasi(AccordionTreeDrawer atd) {
		wantsFocusATD = atd;
	}

	/**
	 * Turns the quasimode to the value of "on".  Also sets the focus ({@link #wantsFocusATD}) if quasimode
	 * was on and is being turned off.
	 * @param on
	 */
	protected void setQuasimode(boolean on) {
		if (quasimode == true && on == false && wantsFocusATD != null) {
			wantsFocusATD.getCanvas().requestFocus();
		}
		quasimode = on;
		wantsFocusATD = null;
	}
	/**
	 * Returns state of the quasi-mode flag.
	 * @return True if quasimode is set, false if the quasimode is not set.
	 */
	protected boolean getQuasimode() { return quasimode; }

	/**
	 * Update the title bar for the application.  The title will be a version string, followed by the names of the trees in order.
	 *
	 */
	public void updateTitle() {
		title = "TreeJuxtaposer v" + versionNumber;
		if (trees.size() > 0)
		{
			title += ": ";
			for (int i = 0; i < treeDrawers.size(); i++)
			{
				String currTree = ((Tree)trees.get(i)).getName();
				int lastSlash = Math.max(currTree.lastIndexOf('/'), currTree.lastIndexOf('\\'));
				title += currTree.substring(lastSlash+1) + " "; 
			}
			title = title.substring(0, title.length()-1); // remove trailing space
		}
		mainFrame.setTitle(title);
	}


	/**
	 * Adds a new tree to "trees", performs all the pairwise tree
	 * comparisons and stores results.
	 * @param newTree New tree to add.  Each existing tree will be compared with this new one.
	 *
	 * @see net.sourceforge.olduvai.treejuxtaposer.drawer.Tree
	 * @see TreePairs#addTree(Tree, int, boolean)
	 */
	public void addTree(Tree newTree) {
		newTree.setKey(treeCount);
		treeCount++;

		int height, width;
		int hgap = 8;
		int vgap = 8;
		if(1 ==treeCount) {
			height = 540; 
			width = 640;
			// hardwired assumption of controlPanel placement, must
			// change if we move controlPanel! 10/10/02 TMM
//			int controlWidth = controlPanel.getWidth();
//			width = (controlWidth > width) ? controlWidth : width;

			if (matrix) {
				// set this very small so that it doesn't override the
				// small windows later - when loading multiple files
				// from command line, after we set the initial size it
				// doesn't seem to be possible to shrink things
				// afterward.

				height = 10;
				width = 10;
			}
			drawPanel.setSize(width, height);
//			mainFrame.setSize(width, height+controlPanel.getHeight());
			mainFrame.setSize(width, height);
			mainFrame.validate();
		} else {
			int numTrees = (treeArgs > treeCount) ? treeArgs : treeCount;

			if (matrix) { 

				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Dimension screendim = toolkit.getScreenSize();
				mainFrame.setSize(screendim.width-10, screendim.height-30);

				mainFrame.setLocation(10,30);
				int cph = 100;
				width = (screendim.width - 20 - hgap*(numTrees-1))/numTrees;
				height = (screendim.height - 30 - cph - vgap*(numTrees-1))/numTrees;
				drawPanel.setSize(screendim.width, screendim.height-cph);
				drawLayout = new GridLayout(0,numTrees,hgap,vgap);
				drawPanel.setLayout( drawLayout );
				drawPanel.validate();
				mainFrame.validate();
			} else {

				// please set this to 0 here in order to avoid the 'invalid-drawable'
				// problem on macosx
				// I think the problem is, that the canvas set its own w and h 
				// so don't mess with that guy. he is awkward and don't likes
				// sizes other then 0 at startup - Yes I know that this is strange...
				height = 0;// drawPanel.getHeight();
				width = 0;//drawPanel.getWidth()/numTrees;

				mainFrame.validate();

			}
		}	

		/**
		 * Construct the data structures for structural comparison and
		 * highlight
		 **/

		trees.add(newTree);
		addNamesToSearchList();

		/**
		 * Create and add the drawer(s) for the tree
		 **/
		if (matrix) {
			TPs.addTree(newTree, edgeweightLevels, false);
			// matrix mode is n by n grid, where n is total number of
			// trees (like scatterplot matrix). there are n rows where
			// each tree is drawn n times: once with no highlights in
			// column that corresponds to own row (i.e. the diagonal),
			// and highlighted by the tree corresponding to that
			// column. example diagram for three trees: capital letter
			// is tree drawn, lower case is which tree it's
			// highlighted against.

			// A   A/b  A/c
			// B/a B    B/c
			// C/a C/b  C

			// because of the way the gridlayout works, we can't just
			// add new components because there's no way to rearrange
			// or insert at specific locations. so just clear the
			// panel and then insert everything in the correct order.

			// reshape before removing, otherwise command is ignored.
			// nope, this doesn't work either.
			Iterator tdIter = treeDrawers.iterator();
			while (tdIter.hasNext()) {
				AccordionTreeDrawer atd = (AccordionTreeDrawer)tdIter.next();
				atd.reshape(atd.getCanvasDrawable(), 10, 10, width,height);
				atd.requestRedraw();
			}
			drawPanel.removeAll();
			ArrayList newRow = new ArrayList();
			ATDMatrix.add(newRow);
			for (int row = 0; row < treeCount; row++ ) {
				ArrayList ATDRow = (ArrayList) ATDMatrix.get(row);
				for (int col = 0; col < treeCount; col++) {
					if (row >= treeCount-1 || col >= treeCount-1) {
						// add new one
						Tree treeToUse;
						if (row == treeCount-1 && 0 == col) {
							// use new tree for the first new ATD on bottom row
							treeToUse = newTree;
						} else {
							// clone the tree, this isn't the first new ATD
							treeToUse = new Tree((Tree)((AccordionTreeDrawer)ATDRow.get(0)).tree);
							treeToUse.setKey(treeCount);
							TPs.addTree(treeToUse, edgeweightLevels, false);
						}
						AccordionTreeDrawer newATD = (AccordionTreeDrawer) addATD(treeToUse, width, height);
						ATDRow.add(newATD);
					}
				}
			}
			// can't set structural differences until all the new ATDs have been made
			for (int row = 0; row < treeCount; row++ ) {
				ArrayList ATDRow = (ArrayList) ATDMatrix.get(row);
				for (int col = 0; col < treeCount; col++) {
					ArrayList ATDCol = (ArrayList) ATDMatrix.get(col);
					if (settingsFrame.isDiffOn()) {
						if (row != col && (row >= treeCount-1 || col >= treeCount-1 )) {
							AccordionTreeDrawer atdA = (AccordionTreeDrawer)ATDRow.get(col);
							AccordionTreeDrawer atdB = (AccordionTreeDrawer)ATDCol.get(row);
							doStructuralDiff(atdA, atdB, StateFrame.D_ACT);

							Tree2Tree t2t = TPs.getPair(atdA.tree, atdB.tree);  // new subtree => forest preprocessing:
							t2t.subtree2Forest(atdA, atdB, edgeweightLevels); //    matrix mode untested
						}
					}
					AccordionTreeDrawer ATD = (AccordionTreeDrawer)ATDRow.get(col);
					drawPanel.add(ATD.getCanvas());
				}
			}
		} else {
			addATD(newTree, width, height);
			TPs.addTree(newTree, edgeweightLevels, false);
			addOneTreeStructuralDiffs();
			Component c = ((AccordionTreeDrawer)treeDrawers.get(treeCount-1)).getCanvas();
			drawPanel.add(c);
		}

		drawPanel.validate();
		mainFrame.validate();
		mainFrame.repaint();
		drawPanel.repaint();
	}
	
	/**
	 * Helper to process the last tree as a special case, do diffs for tree n-1 wrt each of the others.
	 * Uses {@link #doOneDiff(int, int)} with each tree vs the last tree.
	 *
	 */
	public void addOneTreeStructuralDiffs()
	{
		if (settingsFrame.isDiffOn())
		{
			for (int i = 0; i < treeDrawers.size()-1; i++) {
				doOneDiff(i, treeCount - 1);
			}
		}
	}
	
	/**
	 * Complete do-over of structural diffs with all pairs of all trees.
	 * Must be done after a node has been relabeled (leaf nodes, to be exact) to get accurate differences.
	 * Uses {@link #doOneDiff(int, int)} with each possible pair of trees.
	 *
	 */
	public void redoStructuralDiffs()
	{
		if (settingsFrame.isDiffOn())
		{
			for (int i = 0; i < treeDrawers.size(); i++)
				for (int j = i+1; j < treeDrawers.size(); j++)
					doOneDiff(i, j);
		}
	}
	
	/**
	 * A single diff process.  Used by {@link #redoStructuralDiffs()} and {@link #addOneTreeStructuralDiffs()}.
	 * @param oneIndex Index of first tree.
	 * @param twoIndex Index of second tree.
	 */
	public void doOneDiff(int oneIndex, int twoIndex)
	{
		AccordionTreeDrawer atdA = (AccordionTreeDrawer)treeDrawers.get(oneIndex);
		AccordionTreeDrawer atdB = (AccordionTreeDrawer)treeDrawers.get(twoIndex);
		doStructuralDiff(atdA, atdB, StateFrame.D_ACT);
		doStructuralDiff(atdB, atdA, StateFrame.D_ACT);
		Tree2Tree t2t = TPs.getPair(atdA.tree, atdB.tree); // new subtree => forest preprocessing:
		t2t.subtree2Forest(atdA, atdB, edgeweightLevels);
	}

	/**
	 * Utility function to make new AccordionTreeDrawer, set initial
	 * parameters and do treeDrawer list bookkeeping.
	 * @param newTree Tree that will belong to the new drawer.
	 * @param width Width of the new drawer window/canvas.
	 * @param height Height of the new drawer window/canvas.
	 * @return New drawer object for the given tree.
	 */
    private AccordionTreeDrawerFinal addATD(Tree newTree,int width,int height) {
	AccordionTreeDrawerFinal ATD = new AccordionTreeDrawerFinal(newTree, width, height, this);
		ATD.setBackgroundColor(backgroundColor);
		ATD.setObjectColor(objectColor);
		ATD.setLabelColor(labelColor);
		ATD.setLabelBackColor(labelBackColor);
		ATD.setLabelHiColor(labelHiColor);
		ATD.setLabelBackHiColor(labelBackHiColor);
		ATD.setRubberbandColor(Color.getHSBColor(0.0f/360f,.0f,0.3f));
		ATD.setKey(treeDrawers.size());
		ATD.setLineThickness(linethickness); // no redraw
		treeDrawers.add(ATD);
		return ATD;
	}

	/**
	 * Delete a tree from "trees", clean up all the data structures
	 * constructed for tree comparisons.
	 * @param treeNums List of indices of trees to delete.
	 *
	 */
	public void deleteTrees(int[] treeNums) {

//		System.out.println("VM memory before operation: "+Runtime.getRuntime().freeMemory()) ;
//		System.out.println("Total memory before operation: "+Runtime.getRuntime().totalMemory()) ;

		for (int i = treeNums.length-1; i >= 0; i--)
		{
			Tree currTree = (Tree)trees.get(treeNums[i]);
			AccordionTreeDrawer atd = (AccordionTreeDrawer)treeDrawers.get(treeNums[i]);
			if (atd.tree != currTree)
			{
				System.out.println("Error: mismatched tree/drawer in delete: " + currTree + " != " + atd.tree);
				continue;
			}
			Component c = atd.getCanvas();
			TPs.removeTree(currTree);
			currTree.close();
			trees.remove(currTree);
			atd.shutdown();
			treeDrawers.remove(atd);
			drawPanel.remove(c);
			treeCount--;
		}
		drawPanel.validate();
	}

	/**
	 * Post-processing following a tree delete.
	 * Clears marked groups and recomputes differences on all tree pairs.
	 *
	 */
	public void postDeleteTrees()
	{

		addNamesToSearchList();
		for (int i=0; i<groups.size(); i++)
			clearGroup(i); 

		TPs = new TreePairs();
		for (int i = 0; i < treeDrawers.size(); i++)
		{
			AccordionTreeDrawerFinal atdA = (AccordionTreeDrawerFinal)treeDrawers.get(i);
			atdA.getTree().setUpNameLists();
			TPs.addTree(atdA.tree, edgeweightLevels, false);
		}
		
		redoStructuralDiffs();

//		System.gc();
//		System.out.println("VM memory after operation: "+Runtime.getRuntime().freeMemory()) ;
//		System.out.println("Total memory after operation: "+Runtime.getRuntime().totalMemory()) ;       
		updateTitle();
		requestRedrawAll();
	}

	/**
	 * Fetch a tree by its name.  This is done by linear search as we
	 * do not expect to have many trees in memory.
	 * @param name String representation of a tree by its file name.
	 * @return Tree object that matches the string, or null if not found.
	 *
	 */
	public Tree getTreeByName(String name) {

		for(int i=0; i<trees.size(); i++) {
			Tree t = (Tree)trees.get(i);
			if(t.getName().equals(name)) return t;
		}
		return null;	
	}


	/**
	 * Wrapper for calling the add tree option in the {@link UI} object.
	 *
	 */
	protected void addAction(){
		ui.addAction();
	}

	/**
	 * Wrapper for calling the quit option in the {@link UI} object.
	 *
	 */
	protected void quitAction()
	{
		ui.quitAction();
	}

	/**
	 * Initiate a redraw in all drawer objects by calling {@link AccordionDrawer#requestRedraw()}.
	 *
	 */
	public void requestRedrawAll() {
		Iterator tdIter = treeDrawers.iterator();
		while (tdIter.hasNext()) {
			AccordionTreeDrawer atd = (AccordionTreeDrawer)tdIter.next();
			atd.requestRedraw();
		}

	}

	/**
	 * Detect loading status of tree files, initially set to false, sets to true
	 * after all trees have been loaded and first drawing cycles can start.
	 * Avoids any attempts by the drawing thread to pre-emptively draw before grids have been consructed.
	 * @return True: all data files have been loaded and trees may be drawn, false: data still being loaded/processed, drawing is delayed.
	 */
	public boolean isLoaded()
	{
		return AccordionDrawer.loaded;

	}


	// resets the leaves and recreates the node hash set and search array
	/**
	 * Initialize the searching dialog with the set of tree node labels.
	 */
	public void addNamesToSearchList()
	{
		// hash set has constant time verification that a similarly named node has already been added
		// a tree set is also capable of doing this job, but requires rebalancing
		HashSet hn;
		if (trees.size() > 0)
			hn = new HashSet(((Tree)trees.get(0)).nodes.size() * 2);
		else
			hn = new HashSet();
		int i;
		for(i=0; i<trees.size(); i++) {
			Tree t = (Tree)trees.get(i);
			for(int j=0; j<t.nodes.size(); j++) {
				String n = ((TreeNode)t.nodes.get(j)).getName();
				if(n != null && n.length()>0)
					hn.add(n);
			}
		}
		i=0;
		String s[] = new String[hn.size()];
		Iterator it = hn.iterator();
		while (it.hasNext()) {
			s[i] = (String) it.next();
			i++;
		}
		Arrays.sort(s); // one sort instead of making s a treeset which would sort with every add
		searchFrame.initializeList(new ArrayList(Arrays.asList(s)));
	}

	/**
	 * Add a group to the set of all markable groups {@link #groups}.  
	 * @param thisTreeOnly True if the marks for this group apply only to a single tree. 
	 * @return The group that was added
	 */
	public RangeList addGroup(int groupNum, boolean thisTreeOnly) {
		int groupCount = groups.size();
		RangeList g = new RangeList(groupCount, groupNum);
		g.setEnabled(true);
		g.setThisTreeOnly(thisTreeOnly);
		if (AccordionDrawer.debugOutput)
			System.out.println("adding group " + groupCount);
		groups.add(g); // priority ordering (linear search only)
		groupsByIndex.add(g); // permanent ordering (recover with group.key)
		return g;
	}

	/**
	 * Change the priority of user mark groups, to layer the most important (latest)
	 * changed marks over previously changed groups.
	 * @param group Group to make the newest top-drawn, all others shift down 1 spot.
	 */
	public void setGroupPriority(int group) {
		final int markGroupLength = StateFrame.GH_ACT - StateFrame.GA_ACT + 1;
		RangeList g = getGroupByID(group);
		if (group <= markGroupLength) {
			groups.remove(g);
			groups.add(1,g); // put it first of changeable ones
			
			// update keys/alpha for marked colors
			Iterator iter = groups.listIterator(1);
			int currKey = 0;
			while (currKey < markGroupLength)
			{
				RangeList currGroup = (RangeList)iter.next();
				currGroup.setKey(currKey++);
			}
		}
	}


	/**
	 * Clear state in all accordion drawers.
	 * Does not erase the drawn edges, only resets interaction boxes.
	 *
	 */
	public void clearDrawers()
	{
		Iterator atdIter = treeDrawers.iterator();
		if( treeDrawers.size() > 1)
			while( atdIter.hasNext())
			{
				AccordionTreeDrawerFinal a = (AccordionTreeDrawerFinal) atdIter.next();

				if( a.baseBox != null)
				{
					if( a.getFlashBox() != null )
						a.getFlashBox().undraw();
					a.baseBox = null;
					a.flashBox = null;
					a.actionmodeReset();
				}		
			}
	}

	// deliberately don't have removeGroup method. if you don't want
	// it anymore, disable it. why? we want the index in the groups
	// vector to match the key in the rangelist. maybe there's a less
	// hardline way to do this?...

	// what sort of error checking should we do here if group is out
	// of bounds? be silent, return null where appropriate? print out
	// error message? throw an informative exception? ...

	/**
	 * Ascend from the given node and add nodes to the marking group if the BCN of ancestors have been marked before.
	 * Stop looping when a node is found to be marked (assume that ancestor checks have already been done in that case).
	 * @param atd Directly marked tree, where the direct marks were made that led to finding the indirectly marked input node/tree combination.
	 * @param currTree Indirectly marked tree that contains the given indirectly marked node.
	 * @param n Indirectly marked node that is found in currTree.
	 * @param addToGroup Group that marked nodes will be added to (and checked for previous marks).
	 */
	private void ascendTreeMark(AccordionTreeDrawer atd, AccordionTreeDrawer currTree, 
			TreeNode n, RangeList addToGroup)
	{
		// ascend currTree (from n, the bcn of directlymarked, to the root) until no more marking needs to be done
		TreeNode currIndirectNode = n.parent;
		if (currIndirectNode == null) return;
		TreeNode currDirectNode = TPs.getBestCorrNode(currTree.tree, currIndirectNode, atd.tree, edgeweightLevelUsed);
		if (currDirectNode == null) {return;}
		boolean addedAlready = addToGroup.isThisRangeInList(currIndirectNode.key, currIndirectNode.key, currTree);
		boolean fitsIntoDirect = addToGroup.isThisRangeInList(currDirectNode.key, currDirectNode.key, atd);
		while (!addedAlready && fitsIntoDirect)
		{
			addToGroup.addRange(currIndirectNode.key, currIndirectNode.key, currTree);
			currIndirectNode = n.parent;
			if (currIndirectNode == null)
				break;
			currDirectNode = TPs.getBestCorrNode(atd.tree, currIndirectNode, currTree.tree, edgeweightLevelUsed);
			if (currDirectNode == null)
				break;

			addedAlready = addToGroup.isThisRangeInList(currIndirectNode.key, currIndirectNode.key, currTree);
			fitsIntoDirect = addToGroup.isThisRangeInList(currDirectNode.key, currDirectNode.key, atd);
		}
	}

	/**
	 * Recursively descend subtree and mark descendants, including BCN for each node marked in each drawer.
	 * This takes a drawer target and a seed node, and descends the subtree of that node, looking for previously marked
	 * nodes.  The backwards checking done here reflects the original implementation of BCN from TJ 1.0.
	 * @param atd Drawer that marked node (subtree) is in, used to reference the tree pair object
	 * @param currTree Target tree (drawer), where the node to be marked (indirectly marked node) is
	 * @param n The BCN of a node in the indirectly marked tree (belonging to currTree), that will have its children (and additional descendants) examined for marking potential.
	 * @param addToGroup Marking group to add the indirectly marked nodes.
	 */
	private void descendTreeMark(AccordionTreeDrawer atd, AccordionTreeDrawer currTree,
			TreeNode n, RangeList addToGroup)
	{
		// descend currTree (from n to leaves recursively), until no more marking is needed
		for (int i = 0; i < n.numberChildren(); i++)
		{
			TreeNode currChild = n.getChild(i);
			TreeNode currDirectNode = TPs.getBestCorrNode(currTree.tree, currChild, atd.tree, edgeweightLevelUsed);
			if (currDirectNode == null) {continue;}
			boolean addedAlready = addToGroup.isThisRangeInList(currChild.key, currChild.key, currTree);
			boolean fitsIntoDirect = addToGroup.isThisRangeInList(currDirectNode.key, currDirectNode.key, atd);
			if (!addedAlready && fitsIntoDirect)
			{
				addToGroup.addRange(currChild.key, currChild.key, currTree);
				descendTreeMark(atd, currTree, currChild, addToGroup);
			}
		}
	}

	// add nodes indirectly marked in all trees
	/**
	 * Add nodes indirectly marked in each tree.  The best corresponding node lookups
	 * use the previously initialized results from {@link Tree2Tree}.
	 * @param min Minimum node in the range of nodes to add.
	 * @param max Maximum node in the range of nodes to add.
	 * @param group Group to add nodes in range to.
	 * @param atd Drawer that contained tree for the given range of marked nodes.
	 */
	public void addNodesToGroup(int min, int max, int group, AccordionTreeDrawer atd) {
		RangeList addToGroup = getGroupByID(group); 

		addToGroup.addRange(min, max, atd);
		// must add the nodes from all other trees as well
		Iterator iter = treeDrawers.iterator();
		while (iter.hasNext())
		{
			AccordionTreeDrawer currTree = (AccordionTreeDrawer)iter.next();
			if (currTree != atd) // ignore the directly marked tree
			{
				for (int i = min; i <= max; i++) // over all directly marked nodes
				{
					TreeNode directlyMarked = atd.getNodeByKey(i);
					TreeNode n = TPs.getBestCorrNode(atd.tree, directlyMarked, currTree.tree, edgeweightLevelUsed);
					if (n != null)
					{
						// the key represents the current subtree selected; all children of the bcn of this subtree are added to the range
						// the subtree selected also needs to be walked to make sure all potential forest created with bcn's is handled
						if (!addToGroup.isThisRangeInList(n.key, n.key, currTree))
						{
							addToGroup.addRange(n.key, n.key, currTree);
							ascendTreeMark(atd, currTree, n, addToGroup);
							descendTreeMark(atd, currTree, n, addToGroup);

							if(!stateFrame.LCAMark.isSelected())
								addToGroup.addRange(n.key, n.key, currTree);
							else
								addToGroup.addRange(n.key, n.getMax(), currTree);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the color for the chosen group.
	 * @param group Group ID, from {@link StateFrame} constants for group ID.
	 * @return Color stored by the marking group, in its RangeList object.
	 */
	public Color getGroupColor(int group) { 
		return getGroupByID(group).getColor();
	}

	/**
	 * Force clear the contents of the given group without unmarking the LCA group.
	 * @param group Key of group to clear.
	 */
	public void clearGroup(int group) {
		RangeList clearGroup = getGroupByID(group);
		clearGroup.clear();
	}

	/**
	 * Remove LCA marked objects from the given group, then clear the contents of the group, which
	 * will force the previously marked objects to not be marked by that group's color.
	 * @param group Key of the group to unmark.
	 */
	public void unmarkGroup(int group) {
		RangeList groupList = (RangeList)groupsByIndex.get(group); 
		RangeInTree rit;
		if(groupList.getNumRanges() != 0)
		{
			rit = groupList.getFirst();
			for (int i=0; i<lcaNode.size(); i++)
			{
				if(((TreeNode)lcaNode.get(i)).getKey() == rit.getMin() )
				{
					lcaNode.remove(i);			
				}
			}
		}
		if(stateFrame.LCAMark.isSelected())
			doLCAGeom(StateFrame.LCA_ACT, (AccordionTreeDrawer)treeDrawers.get(0));
		clearGroup(group);
	}

	/**
	 * Get the marked group (a rangelist) by the given id tag.
	 * @param groupID ID of the marking group, as defined in {@link StateFrame}.
	 * @return A valid marked group object, that contains a list of ranges of tree nodes
	 * and a color that they should be marked in.
	 */
	protected RangeList getGroupByID(int groupID)
	{
		Iterator iter = groups.iterator();
		RangeList currGroup = null;
		while (iter.hasNext())
		{
			currGroup = (RangeList)iter.next();
			if (currGroup.groupID() == groupID)
				break;
		}
		return currGroup;
	}

	/**
	 * Seed the marked groups for the given accordion drawer.
	 * Add one leaf node in each marked subtree, or internal nodes for individually marked nodes.
	 * A skeleton is drawn for marked nodes, so we don't waste time rendering entire marked subtrees,
	 * but draw a single path from one of the deepest nodes (such as a leaf) to the root.
	 * This gives fast visual indications of group location during partially drawn frames of
	 * a progressively rendered scene.
	 * @param atd Drawer to use for group marking lookup.
	 */
	private void seedGroups(AccordionTreeDrawerFinal atd)
	{
		Iterator GroupIter = groups.iterator();
		while (GroupIter.hasNext())
		{
			RangeList group = (RangeList)((RangeList)GroupIter.next()).onlyThisAD(atd);
			if (group.isEnabled())
			{
				Iterator RangeIter = group.getRanges().iterator();
				int count=0;
				while (RangeIter.hasNext())
				{ // seed all ranges for now, add count restriction back later if necessary && count < 5) {
					RangeInTree r = (RangeInTree) RangeIter.next();
					int min = r.getMin(), max = r.getMax();
					TreeNode rootNode;
					{
						do
						{
							rootNode = atd.getNodeByKey(min);
							atd.addToDrawQueue(rootNode);
							min = rootNode.rightmostLeaf.key+1;
						}
						while (min <= max);
					}
					count++;
				}
			}
		}
	}

	/**
	 * Get the split line boundaries for the given split line, properly ordered in the return value [min, max].
	 * @param sl Split line to get movement restricting split lines.
	 * @param maxSplitIndex Maximum split index for the seeding range.
	 * @return Pair of indices that are positions of split lines that bound the input split line.
	 */
	private int[] getStaticSeedBounds(StaticSplitLine sl, int maxSplitIndex)
	{
		int thisIndex = sl.getSplitIndex();
		int[] bounds = {thisIndex - 1, thisIndex}; // correct for leaves only, fix for internal nodes
		if (sl.isRoot())
		{
			bounds[0] = -1;
			bounds[1] = maxSplitIndex;
			return bounds;
		}
		if (sl.getLeftChild() != null && sl.getRightChild() != null) // this is not a leaf
			// otherwise (is a leaf) fall through and use the initialized values
		{

			if (sl.isLeftChild())
			{
				bounds[1] = sl.getStaticParent().getSplitIndex();
				if (sl.getOffParentBound() == null)
					bounds[0] = -1;
				else
					bounds[0] = sl.getOffParentBound().getSplitIndex();
			}
			else // is right child
			{
				bounds[0] = sl.getStaticParent().getSplitIndex();
				if (sl.getOffParentBound() == null)
					bounds[1] = maxSplitIndex;
				else
					bounds[1] = sl.getOffParentBound().getSplitIndex();
			}
		}
		return bounds;
	}

	/**
	 * Seed the queue for the given drawer.
	 * Order matters: first the interaction box, then marked groups, then everything else.
	 * @param atd Drawer that we are seeding.
	 */
	protected void seedQueue(AccordionTreeDrawerFinal atd) {
		if (AccordionDrawer.debugOutput)
			System.out.println("seeding queue");
		atd.clearQueue();
		InteractionBox bbox = atd.baseBox;
		int firstRange[] = {-1, -1};
		if (bbox != null)
		{
			firstRange[0] = ((StaticSplitLine)bbox.getMinLine(AccordionDrawer.Y)).getSplitIndex();
			firstRange[1] = ((StaticSplitLine)bbox.getMaxLine(AccordionDrawer.Y)).getSplitIndex();
		}

		seedGroups(atd);

		Vector drawQueue = atd.getToDrawQ();
		int numSeededGroups = drawQueue.size();

		TreeSet listofleaves = atd.getListOfLeaves();
		Iterator iter = listofleaves.iterator();
		while (iter.hasNext())
		{
			StaticSplitLine sl = (StaticSplitLine)iter.next();
			int bounds[] = getStaticSeedBounds(sl, atd.getSplitAxis(AccordionDrawer.Y).getSize());
			if (sl.getSplitIndex() > firstRange[0] && bounds[0] < firstRange[1])
				drawQueue.add(numSeededGroups, new RangeInTree(bounds[0], bounds[1], atd)); // higher priority
			else
				drawQueue.add(new RangeInTree(bounds[0], bounds[1], atd));
		}
		if (AccordionDrawer.debugOutput)
			System.out.println("seeded: " + drawQueue.size() + " frame: " + atd.getFrameNum());
		if (atd.basePass || atd.groupPass) // reverse queue, linear but only for taking pictures
		{
			Vector temp = new Vector(drawQueue);
			drawQueue.clear();
			while (temp.size() > 0)
				drawQueue.add(temp.remove(temp.size()-1));
		}

	}


	/** 
	 * Get the list of colors for a range of tree nodes.
	 * A built-in group precedence ordering determines which group color
	wins out. This function stops when it finds the first valid drawing color.
	We guarantee that the results of this function are necessary to represent the accurate
	drawing of the tree, as if all nodes are drawn, but only one representative object is drawn in this range.
	 * 
	 * @param objMin Minimum value for object keys in range.
	 * @param objMax Maximum value for object keys in range.
	 * @param callingTreeDrawer Tree drawer to use for object index lookups.
	 * @return List of colors, each color representing a group that is between objMin and objMax.
	 */
	protected ArrayList getColorsForRange(int objMin, int objMax,
			AccordionTreeDrawer callingTreeDrawer) {
		ArrayList returnList = new ArrayList();
		RangeInTree fakeRange =
			new RangeInTree(objMin, objMax, callingTreeDrawer);
		RangeInTree.doAdj = false;
		Iterator GroupIter = groups.iterator();
		RangeList.returnObject = true;
		while (GroupIter.hasNext()) { // && returnList.size() < 1) {
			RangeList group = (RangeList) GroupIter.next();
			if (group.isEnabled() && group.getRanges().contains(fakeRange)) {
				RangeInTree foundRange = RangeList.matchRange;
				if (foundRange != null && group.getColor() != null)
				{
					returnList.add(group.getColor());
				}
			}
		}
		RangeList.returnObject = false;
		RangeInTree.doAdj = true;
		return returnList;
	}

	/** 
	 * Callback function from cursor movement highlighting.
	 * Acts as a toggle on group members in the flash drawing group: turn off (remove) old ones, turn on
	 * (add) new ones.  In the drawing function {@link AccordionTreeDrawer#flashDraw()}, do lightweight frontbuffer drawing per item not whole
	 * screen redraw.
	 * @param flashGeom Geometry (tree node) that we are currently highlighting with the cursor.
	 * @param group Index for the flash drawing group.
	 * @param thisTree Tree drawer that the geometry is within.
	 * @param x Cursor X position.
	 * @param y Cursor Y position.
	 * @see Tree
	 * @see TreeNode
	 * @see RangeList
	 *
	 */
	protected void doFlashGeom(CellGeom flashGeom, int group, 
			AccordionTreeDrawerFinal thisTree, int x, int y) {
		RangeList theGroup = getGroupByID(group);
		TreeNode flashNode;
		if (null == flashGeom) {
			flashNode = null;
		} else {
			// flashGeom is an edge. get its associated node instead.
			flashNode = thisTree.getNodeByKey(flashGeom.getKey());
		}
		if (flashNode == flashGeomOld) return;
		// we're still doing the group book-keeping so that we know
		// which tree the item is attached to. we just don't want to
		// enable the group, since then we're left with yellow boxes
		// that aren't easy to clean up with this frontbuffer trick!
		clearGroup(group);
		if (null != flashNode)
			addNodesToGroup(flashNode.getKey(), flashNode.getKey(), group, thisTree);
		Iterator tdIter = treeDrawers.iterator();
		while (tdIter.hasNext()) {
			AccordionTreeDrawer atd = (AccordionTreeDrawer)tdIter.next();
			// we're assuming flashGeom is a TreeNode, since we only put nodes in above (not edges)
			TreeNode bcn = (null == flashNode) ? null :
				TPs.getBestCorrNode(thisTree.tree, flashNode, atd.tree, edgeweightLevelUsed);
			boolean sametree = thisTree.tree == atd.tree;
			if (!sametree)
				atd.setFlash((CellGeom)bcn,theGroup.getColor(), -1, -1, sametree);
			else
				atd.setFlash((CellGeom)bcn,theGroup.getColor(), x, y, sametree);
			if (null == flashNode) { // tell it to turn off
				atd.setFlash(null,theGroup.getColor(), x, y, sametree);
			}
		}
		flashGeomOld = flashNode;
	}

	/**
	 * Wrapper for adding nodes to a group when user selects a subtree or node for user-directed marking.
	 * @param g TreeNode that was marked.
	 * @param selectSubtree True if subtree marking mode is on (false for single node marking).
	 * @param group Index of group to add node(s) to.
	 * @param thisTree Tree in which the node was marked.
	 */
	protected void doSelectGeom(TreeNode g, boolean selectSubtree, int group, AccordionTreeDrawer thisTree) {
		if (AccordionDrawer.debugOutput)
			System.out.println("key to select: "+g.getKey());
		int which = (true == selectSubtree) ? g.getMax() : g.getKey();
		addNodesToGroup(g.getKey(), which, group, thisTree);
//		System.out.println("adding nodes to group " + group + " :: GA_ACT = " + StateFrame.GA_ACT);
	}

	/**
	 * Find the LCA skeleton (minimal paths) for the nodes marked while the LCA box is active.
	 * @param group LCA group key.
	 * @param thisTree Tree drawer to use for searching for the best LCA skeleton.
	 */
	protected void doLCAGeom(int group, AccordionTreeDrawer thisTree)
	{
		clearGroup(group);
		if(lcaNode.size()<2)
		{
			System.out.println("need 2 or more nodes to have a LCA");
			return; 
		}
		Iterator it = lcaNode.iterator();
		TreeNode tn1; 
		tn1= (TreeNode)lcaNode.get(0); 
		TreeNode lca = tn1;
		while(it.hasNext())
			lca = lca.lca((TreeNode)it.next());

		for(int i=0; i<lcaNode.size(); i++)
		{
			TreeNode current = (TreeNode)lcaNode.get(i);
			while(current != null && current.getKey() != lca.getKey())
			{
				addNodesToGroup(current.getKey(), current.getKey(), group, thisTree);
				current=current.parent;
			}
		}
		addNodesToGroup(lca.getKey(), lca.getKey(), group, thisTree);
	}

	/**
	 * Compute best matching nodes between two trees
	 *
	 * A node X is perfectly matched by a node Y if they have exactly
	 * the same leaf set.  The matching for a pair of nodes is the ratio between the number of common nodes
	 * (nodes in each set) to the number of total nodes (nodes in both sets). 
	 *
	 * @param atd1 First tree drawer to use as the basis of comparison.
	 * @param atd2 Second tree drawer to compare with the first.
	 * @param group Group number for computed differences.
	 * @see Tree
	 * @see TreeNode
	 * @see TreePairs
	 **/
	protected void doStructuralDiff(AccordionTreeDrawer atd1, AccordionTreeDrawer atd2, int group) {
		Tree t1 = atd1.getTree();
		Tree t2 = atd2.getTree();
		RangeList groupList = getGroupByID(group);
		for(int i = 0; i < t1.nodes.size(); i++) {
			TreeNode n = (TreeNode)t1.nodes.get(i);
			float score = TPs.getBestCorrNodeScore(t1, n, t2, edgeweightLevelUsed);
			n.setBcnScore(score);
			if (score < getBcnScore()) {
				groupList.addRange(n.getKey(), n.getKey(), atd1);
			}
		}
	}

	/**
	 * Set the focus, for each drawer, to the given tree node in the given drawer.
	 * @param g Tree node to focus on.
	 * @param atd Drawer that has the tree node.
	 */
	protected void setFocus(CellGeom g,  AccordionTreeDrawer atd) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) {
			AccordionTreeDrawer thisTree = (AccordionTreeDrawer)i.next();
			TreeNode n = atd.tree.getNodeByKey(g.getKey());
			if (thisTree != atd) {
				n = TPs.getBestCorrNode(atd.tree, n, thisTree.tree, edgeweightLevelUsed); 
				if (n != null) thisTree.setFocusCell(n.getCell());
//				else System.out.println("bad focus");
			} else {
				thisTree.setFocusCell(n.getCell());
			}
		}
	}

	/**
	 * Return the label for a node, given its full name.
	 * Search each tree for the node, and return the label of the first match.
	 * @param FQName The full name of a node.
	 * @return The label that is shown for the found node, or null if the full name was not found.
	 */
	public String getLabelByFQName(String FQName)
	{
		String returnValue = null;
		TreeNode n;
		Tree t;
		Iterator i = treeDrawers.iterator();
		while (i.hasNext())
		{
			t = ((AccordionTreeDrawer)i.next()).getTree();
			if ((n = t.getNodeByName(FQName)) != null)
			{
				returnValue = n.label;
			}
		}
		return returnValue;
	}

	/**
	 * Set state of the non-colored node rendering, either dimmed edges according to a node's tree depth (true) or all black (false).
	 * @param on True if we draw deeper nodes more grey (default), false for solid black coloring.
	 */
	public void setDimBrite(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setDimBrite(on);
	}
	/**
	 * Toggle state of the non-colored node rendering, either dimmed edges according to a node's tree depth (true) or all black (false).
	 */
	public void toggleDimBrite() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDimBrite();
	}
	/**
	 * Get state of the non-colored node rendering, either dimmed edges according to a node's tree depth (true) or all black (false).
	 * @return True if we draw deeper nodes more grey (default), false for solid black coloring.
	 */
	public boolean getDimBrite() {
		if (treeDrawers.size() > 0)
			return ((AccordionTreeDrawer)treeDrawers.get(0)).dimbrite;
		return false;
	}

	/**
	 * Set state of the color rendering, either dimmed colors according to a node's tree depth (true) or equal saturation (false).
	 * @param on True if we draw deeper nodes dimmer, false for solid coloring (default).
	 */
	public void setDimColors(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setDimColors(on);
	}
	/**
	 * Toggle state of the color rendering, either dimmed colors according to a node's tree depth (true) or equal saturation (false).
	 */
	public void toggleDimColors() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDimColors();
	}
	/**
	 * Get state of the color rendering, either dimmed colors according to a node's tree depth (true) or equal saturation (false).
	 * @return True if we draw deeper nodes dimmer, false for solid coloring (default).
	 */
	public boolean getDimColors() {
		if (treeDrawers.size() > 0)
			return ((AccordionTreeDrawer)treeDrawers.get(0)).dimcolors;
		return false;
	}

	/**
	 * Toggle debug state of sending rendering time information or other real-time information
	 * to a log file.
	 * @see AccordionDrawer#dumpstats 
	 */
	public void toggleDumpStats() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDumpStats();
	}

	/**
	 * Increment the label buffer (the minimum distance between two labels) in the given direction.
	 * @param xy Direction of interest.
	 */
	public void increaseLabelBuffer(int xy) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).increaseLabelBuffer(xy);
	}
	/**
	 * Decrement the label buffer (the minimum distance between two labels) in the given direction.
	 * @param xy Direction of interest.
	 */
	public void decreaseLabelBuffer(int xy) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).decreaseLabelBuffer(xy);
	}

	/**
	 * Toggle the drawing of the label background (for non-highlighted nodes).  Default is off (false).
	 *
	 */
	public void toggleLabelDrawBack() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleLabelDrawBack();
	}

	/**
	 * Toggle for drawing labels on the right side of edges, or by the node junction between tree edges.
	 *
	 */
	public void toggleLabelPosRight() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleLabelPosRight();
	}

	/**
	 * Toggle for drawing split lines (grid lines).
	 *
	 */
	public void toggleDrawSplits() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDrawSplits();
	}

	/**
	 * Increment the width of tree edge lines in each drawer.
	 *
	 */
	public void increaseLineThickness() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).increaseLineThickness();
	}
	/**
	 * Decrement the width of tree edge lines in each drawer.
	 *
	 */
	public void decreaseLineThickness() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).decreaseLineThickness();
	}

	/**
	 * Set the state of linked navigation to the given value.  All drawers are updated to the value.
	 * @param on New value for linked navigation: true = do linked navigation.
	 */
	public void setLinkedNav(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setLinkedNav(on);
	}
	/**
	 * Toggle the state of linked navigation in all drawers.
	 *
	 */
	public void toggleLinkedNav() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleLinkedNav();
	}
	/**
	 * Get the state of the linked navigation flag, which is stored in the first tree drawer,
	 * {@link AccordionDrawer#linkednav}.
	 * @return State of linked navigation, true for linked (default), false for no linked navigation.
	 */
	public boolean getLinkedNav() {
		if (treeDrawers.size() > 0)
			return ((AccordionTreeDrawer)treeDrawers.get(0)).linkednav;
		return false;
	}

	/**
	 * Increment the maximum font height in each drawer.
	 *
	 */
	public void increaseMaxFontHeight() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).increaseMaxFontHeight();
	}
	/**
	 * Decrement the maximum font height in each drawer.
	 *
	 */
	public void decreaseMaxFontHeight() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).decreaseMaxFontHeight();
	}

	/**
	 * Increment the minimum font height in each drawer.
	 *
	 */
	public void increaseMinFontHeight() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).increaseMinFontHeight();
	}
	/**
	 * Decrement the minimum font height by one in each drawer.
	 *
	 */
	public void decreaseMinFontHeight() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).decreaseMinFontHeight();
	}

	/**
	 * Toggle state of flash (mouse-over) drawing on all drawers.
	 *
	 */
	public void toggleNoFlash() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleNoFlash();
	}

	/**
	 * Turn on/off tree labels.
	 * @param on True: draw labels for each visible tree node (default).  False: do not draw the tree labels.
	 */
	public void setDrawLabels(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setDrawLabels(on);
	}
	
	/**
	 * Turn on/off tree nodes.
	 * @param on True: draw edges for each visible tree node (default).  False: do not draw the tree nodes.
	 */
	public void setDrawGeoms(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setDrawGeoms(on);
	}
	/**
	 * Toggle state of drawing tree nodes (geoms).
	 *
	 */
	public void toggleDrawGeoms() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDrawGeoms();
	}

	/**
	 * Turn on/off grid drawing.
	 * @param on True: draw the grid lines for all trees.  False: no grid (default).
	 */
	public void setDrawGrid(boolean on) {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).setDrawGrid(on);
	}
	/**
	 * Toggle state of drawing grid on all drawers.
	 *
	 */
	public void toggleDrawGrid() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).toggleDrawGrid();
	}

	/**
	 * Test to see if LCA group resize is active.
	 * @return True if the LCA group will be resized if group resizing is selected.
	 */
	public boolean getLCAGroup() { return stateFrame.getLCAGroup(); }

	/** Get state of LCA marking from the {@link #stateFrame}.
	 * @return True if LCA marking is active. */
	public boolean getLCAMark() { return stateFrame.getLCAMark(); }


	/**
	 * Reset iterator: resets all drawers.
	 *
	 */
	public void reset() {
		Iterator i = treeDrawers.iterator();
		while (i.hasNext()) 
			((AccordionTreeDrawer)i.next()).reset();
	}

	/**
	 * 
	 * Return an array of tree nodes to resize per group range item.
	 * 
	 * @param groupindex Index of group (from {@link StateFrame#getActionGroup()}) to resize.
	 * @param atd Drawer to resize in.  Objects returned will all be from this drawer.
	 * @return List of top level tree nodes (forest roots) to resize.
	 */
	 public ArrayList getGroupForest(int groupindex, AccordionTreeDrawer atd) {
		RangeList group = getGroupByID(groupindex);
		ArrayList forestRoots = new ArrayList();
		Iterator groupIter = group.getRanges().iterator();
		while (groupIter.hasNext()) {
			RangeInTree r = (RangeInTree) groupIter.next();
			// if item is singleton, item.max == item.min
			AccordionTreeDrawer ratd = r.getTree();
			Tree thisTree = ratd.tree;
			int key = r.getMin(); // if it's a range, min is the top node.
			TreeNode tn = thisTree.getNodeByKey(key);
			if (thisTree == atd.tree) {
				forestRoots.add(tn);
			} else if (!group.isThisTreeOnly()) {
				// get list of nodes that match subtree rooted by tn
				ArrayList list = TPs.getBestNodeList(thisTree, tn, atd.tree, edgeweightLevelUsed);
				if (list == null) // save space in hash, no forest
					forestRoots.add(TPs.getBestCorrNode(thisTree, tn, atd.tree, edgeweightLevelUsed));
				else
					for (int i = 0; i < list.size(); i++)
					{
						forestRoots.add((TreeNode)list.get(i));
					}
			}
		}
		return forestRoots;
	 }
	 
	 /**
	  * Resize the group in the given drawer.
	  * @param group Marked list of objects to grow.
	  * @param numSteps Number of animation steps.
	  * @param grow Growing if true, shrinking if false.
	  * @param atd Drawer to resize in.
	  */
	 public void resizeGroup(RangeList group, int numSteps, boolean grow, AccordionTreeDrawer atd) {
		 {
			 atd.endAllTransitions();
			 Hashtable newToMove = new Hashtable();
			 if (stateFrame.growHorizontal())
			 {
				 RangeList horizGroup = (RangeList) group.onlyThisAD(atd);
				 if (!grow)
				 {
					 horizGroup = (RangeList) horizGroup.nodeKeyToSplitLine(AccordionDrawer.X, true).flipRangeToShrink(AccordionDrawer.X, atd);
				 }
				 else
					 horizGroup = horizGroup.nodeKeyToSplitLine(AccordionDrawer.X, true);
				 atd.getSplitAxis(AccordionDrawer.X).resizeForest(horizGroup, numSteps, newToMove, atd.getInflateIncr());
			 }
			 if (stateFrame.growVertical()) 
			 {
				 RangeList vertGroup = (RangeList) group.onlyThisAD(atd);
				 if (!grow)
				 {
					 vertGroup =
						 (RangeList) vertGroup.nodeKeyToSplitLine(
								 AccordionDrawer.Y, false).flipRangeToShrink(
										 AccordionDrawer.Y, atd);
				 }
				 else
					 vertGroup = vertGroup.nodeKeyToSplitLine(AccordionDrawer.Y, false);
				 SplitAxis splitAxisY = atd.getSplitAxis(AccordionDrawer.Y);
				 splitAxisY.makePixelRanges(1/10, atd.getFrameNum());

				 atd.getSplitAxis(AccordionDrawer.Y).resizeForest(
						 vertGroup, numSteps, newToMove, atd.getInflateIncr());

			 }
			 atd.toMove = newToMove;
		 }
	 }

	 /**
	  * Grow a marked group of objects in all drawers.
	  * Calls {@link #resizeGroup(RangeList, int, boolean, AccordionTreeDrawer)} on each drawer.
	  * @param group Marked list of objects to grow.
	  * @param numSteps Number of animation steps.
	  * @param grow Growing if true, shrinking if false.
	  */
	 public void resizeGroup (RangeList group, int numSteps, boolean grow)
	 {
		 Iterator tdIter = treeDrawers.iterator();
		 while (tdIter.hasNext())
		 {
			 AccordionTreeDrawer atd = (AccordionTreeDrawer) tdIter.next();
			 resizeGroup(group, numSteps, grow, atd);
		 }
	 }

	 /**
	  * Linked navigation function to iterate changes over all drawers.
	  * This only works with a single node resizing that has a best corresponding node
	  * in other drawers.
	  * @param tn Linking node that was selected for resizing.
	  * @param changeRatio Size of drag in X and Y (0 and 1) for the navigation.
	  * @param numAnimSteps Number of steps to animate through.
	  * @param thisatd Do not resize this drawer (where the node tn is found) since it is already done.
	  */
	 public void resizeRectOthers(TreeNode tn, double[] changeRatio, int numAnimSteps,
			 AccordionTreeDrawer thisatd) {
		 if (null == tn)
			 return;

		 Iterator tdIter = treeDrawers.iterator();
		 while (tdIter.hasNext()) {
			 AccordionTreeDrawer atd = (AccordionTreeDrawer)tdIter.next();
			 while (atd.keepMoving())
			 {
				 if (AccordionDrawer.debugOutput)
					 System.out.println(" (rzRO) killing transitions");
				 atd.endAllTransitions(); // end transitions, new queue is going to be swapped in
			 }
			 Hashtable newToMove = new Hashtable();
			 if (thisatd != atd) {
				 TreeNode bcn = TPs.getBestCorrNode(thisatd.tree, tn, atd.tree, edgeweightLevelUsed);
				 if (null != bcn) {
					 TreeNode minLeaf = bcn.leftmostLeaf;
					 TreeNode maxLeaf = bcn.rightmostLeaf;
					 RangeList range;// = new RangeList[2];
					 
					 // fake range resize in X
					 range = new RangeList(98, -1);
					 range.addRange(((StaticSplitLine)bcn.getCell().getMinLine(AccordionDrawer.X)).getSplitIndex()+1,
							 ((StaticSplitLine)minLeaf.getCell().getMaxLine(AccordionDrawer.X)).getSplitIndex(), atd);
					 atd.getSplitAxis(AccordionDrawer.X).resizeForest(range, numAnimSteps, newToMove, changeRatio[AccordionDrawer.X]);

					 // fake range resize in Y
					 range = new RangeList(99, -1);
					 range.addRange(((StaticSplitLine)minLeaf.getCell().getMinLine(AccordionDrawer.Y)).getSplitIndex()+1,
							 ((StaticSplitLine)maxLeaf.getCell().getMaxLine(AccordionDrawer.Y)).getSplitIndex(), atd);
					 atd.getSplitAxis(AccordionDrawer.Y).resizeForest(range, numAnimSteps, newToMove, changeRatio[AccordionDrawer.Y]);
					 
					 if (AccordionDrawer.debugOutput)
						 System.out.println("incrementing frame from resizeRectOthers");
					 atd.incrementFrameNumber(); // so compute place this frame will update, this can be moved to after transitions are created
					 atd.toMove = newToMove;
				 }
			 }
		 }
	 }

	 /** For loading specific nexus trees from the command line (or all trees if vector is null).
	  * 
	  * @param fileName Name of the nexus file.
	  * @param nexusNumbers List of indices to load from the nexus file.  All trees are loaded if this is null.
	  */
	 protected void loadTree(String fileName, Vector nexusNumbers)
	 {
		 try
		 {
			 long start, loaded, processed;
			 start  = System.currentTimeMillis();

			 TreeParser parser = new TreeParser(new BufferedReader(new FileReader(fileName)));
			 ArrayList trees = parser.nexusTokenize(nexusNumbers, null); // does postprocess too
			 loaded  = System.currentTimeMillis();
			 if (AccordionDrawer.debugOutput)
				 System.out.println("load/parse time " + (loaded-start)/1000.0f + " sec");
			 if (trees == null)
			 {
				 System.err.println("No trees found for file: " + fileName);
				 return;
			 }
			 Iterator iter = trees.iterator();
			 while (iter.hasNext())
			 {
			 	 addTree((Tree)iter.next());
			 }
			 updateTitle();

			 processed  = System.currentTimeMillis();
			 if (AccordionDrawer.debugOutput)
				 System.out.println("preprocess time " + (processed -loaded)/1000.0f + " sec");

		 }
		 catch (FileNotFoundException e)
		 {
			 System.out.println("Error: can't find file: " + fileName);
		 }
	 }

	 /**
	  * Load a newick tree.  This is called following the general loading function {@link #loadTree(String)} if nexus is not detected.
	  * @param fileName File name for the newick tree.
	  */
	 private void loadNewickTree(String fileName)
	 {
		 try
		 {
			 long start, loaded, processed;
			 start  = System.currentTimeMillis();

			 File f = new File(fileName);
			 TreeParser parser = new TreeParser(new BufferedReader(new FileReader(fileName)));
			 Tree t = parser.tokenize(f.length(), f.getName(), null); // does postprocess too
			 loaded  = System.currentTimeMillis();
			 if (AccordionDrawer.debugOutput)
				 System.out.println("load/parse time " + (loaded-start)/1000.0f + " sec");

			 t.setFileName(fileName);
			 addTree(t);
			 updateTitle();

			 processed  = System.currentTimeMillis();
			 if (AccordionDrawer.debugOutput)
				 System.out.println("preprocess time " + (processed -loaded)/1000.0f + " sec");

		 }
		 catch (FileNotFoundException e)
		 {
			 System.out.println("Error: can't find file: " + fileName);
		 }
	 }

	 /**
	  * Wrapper for loading all type of supported trees.  Nexus detection done first, then
	  * fall back to newick.  Tree is loaded into a new tree drawer.
	  * @param fname File name to load.
	  * @throws FileNotFoundException
	  */
	 protected void loadTree(String fname) throws FileNotFoundException
	 {
		 TreeParser tp = new TreeParser(new BufferedReader(new FileReader(fname)));
		 if (tp.isNexusFile(fname))
		 {
			 ArrayList treeNames = TreeParser.nexusFileTreeNames(fname);
			 Vector treeNumbers = null;
			 if (treeNames.size() > 1)
				 treeNumbers = tp.chooseNames(treeNames);
			 loadTree(fname, treeNumbers);
		 }
		 else
		 {
			 loadNewickTree(fname);
		 }
	 }

	 /**
	  * Main application function.  See README for commandline arguments.
	  * @param args See README file for full set of supported command line arguments.
	  */
	 public static void main (String[] args) {

		 TreeJuxtaposer tj = new TreeJuxtaposer();
		 tj.mainFrame.setSize(300,300);

		 // get rid of annoying sync warnings
		 System.setProperty("java.util.prefs.syncInterval","2000000");

		 int i=0;
		 AccordionDrawer.loaded = false;
		 while (i<args.length) {
			 if (args[i].equals("-f")) // flag to fully qualify node names
				 AccordionTreeDrawer.fullyQualified = true; // default is false
			 else if (args[i].equals("-nostructdiff")) {
				 tj.settingsFrame.setStructDiff(false);
			 } else if (args[i].equals("-matrix")) {
				 tj.matrix = true;
			 } else if (args[i].equals("-L")) {
				 tj.edgeweightLevels = (new Integer(args[++i])).intValue();
				 tj.edgeweightLevelUsed = (new Integer(args[++i])).intValue();
				 if(tj.edgeweightLevelUsed>=tj.edgeweightLevels)
					 tj.edgeweightLevelUsed = tj.edgeweightLevels-1;
				 else if(tj.edgeweightLevelUsed<0)
					 tj.edgeweightLevelUsed = 0;
			 } else if (args[i].equals("-x")) {
				 i++;
				 String filename = args[i++];
				 Vector numbers = new Vector();
				 while(i < args.length){
					 String s = args[i];
					 Integer n;
					 try{
						 n = Integer.valueOf(s);
					 }catch(NumberFormatException ne){
						 break;
					 }
					 numbers.add(n);
					 i++;
				 }
				 tj.loadTree(filename, numbers);

				 // to offset the next operation i++
				 i--;  

			 } else {
				 try
				 {
					 tj.loadTree(args[i]);
				 } catch (FileNotFoundException ex) {
					 System.out.println("File not found: " + args[i] + " (" + ex + ")");
				 }
				 tj.treeArgs = args.length-i;
			 }
			 i++;
		 }

		 if (args.length < 1)
			 // assume webstart or no args 
			 tj.ui.currDir = new File(System.getProperty("user.home"));
		 else
			 // assume command line
			 tj.ui.currDir = new File(System.getProperty("user.dir"));

		 tj.updateTitle();

		 tj.mainFrame.setVisible(true);
		 AccordionDrawer.loaded = true;

	 }

	 /**
	  * Sets bcn filtering score, shows differences if nodeBCNScore < {@link #bcnScore}.
	  * Set by the slider in settings.
	  * @param score New filtering score.
	  */
	 public void setBcnScore(float score)
	 {
		 bcnScore = score;
	 }
	 /**
	  * Access the bcn filtering score for this tree.
	  * @return BCN filtering score, stored in this class as well as on the slider.
	  */
	 public float getBcnScore()
	 {
		 return bcnScore;
	 }

	 /**
	  * Toggles progressive rendering.  Changes state for each drawer for this application.
	  * @param on New state of progressive rendering: true == active.
	  */
	 public void setProgressiveOn(boolean on)
	 {
		 Iterator iter = treeDrawers.iterator();
		 while (iter.hasNext())
		 {
			 AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)iter.next();
			 atd.ignoreProgressive = !on;
		 }
	 }

};
