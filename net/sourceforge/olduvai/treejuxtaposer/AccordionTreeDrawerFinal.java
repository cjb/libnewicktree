
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
import java.awt.event.*;

import javax.media.opengl.GL;
import javax.swing.JProgressBar;

import net.sourceforge.olduvai.accordiondrawer.*;
import net.sourceforge.olduvai.treejuxtaposer.drawer.*;



/**
 * A class that implements the abstract methods left in AccordionTreeDrawer, mainly user interface and application-specific hooks for TreeJuxtaposer.
 *
 * @author  Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou, James Slack
 * @version 2.1
 * @see     TreeJuxtaposer
 * @see     AccordionTreeDrawer
 * @see		AccordionDrawer
 */
public class AccordionTreeDrawerFinal extends AccordionTreeDrawer {

	/** The treejuxtaposer object for referencing the program instance that responds to user actions found in this class. */
	private TreeJuxtaposer tj;
	/** The X/Y pixel location of a mouse dragging action at the start. */
	private int dragStart[] = new int[2];
	/** The X/Y pixel location of a mouse dragging action at it's current position. */
	private int dragEnd[] = new int[2];
	/** The current 'focus' object under the cursor, a TreeNode. */
	private TreeNode mouseOverNode;

	// which direction to grow
	/** HORIZ direction X */
	protected final static int HORIZ = StateFrame.H_MODE;
	/** VERT direction Y */
	protected final static int VERT = StateFrame.V_MODE;
	/** BOTH X and Y directions */
	private final static int ALL = StateFrame.B_MODE;
	/** Growing direction, default to {@value #VERT} */
	private int growDirectionDefault = VERT;
	
	/** After a user selects 'a' for all groups, keytarget will be set to this value.
	 * The next key can be a 'c' for clearing the marks from all groups. */
	private final static int ALL_GROUPS = 500;

	// does the selection (marking) of a node select just the node or the subtree
	/** Mark mode: single node */
	private final static int NODE = StateFrame.N_RES;
	/** Mark mode: subtree */
	private final static int SUBTREE = StateFrame.S_RES;
	/** Mark mode default: {@value #SUBTREE}*/
	private int selectionResolutionDefault = SUBTREE;

	/** Store previous keystroke if a developer key action happened (".") */
	private int keytarget;
	// developer mode for keytarget only
	/** Developer mode for keytarget */
	private final static int DEVELOPER = 11;
	// only let the keytarget and actionmode be set to NONE
	// any other state that can be seen externally should have real state (default value)
	/** value of NONE for {@link #keytarget} and {@link #actionmode} */
	private final static int NONE = 12;
	/** Default key target is Mouse over interaction M_ACT in StateFrame */
	private final static int keytargetDefault = StateFrame.M_ACT;

	/** state of interaction:
	 * {@value #MOUSEOVER}, {@value #ST_FREEMOVE}, {@value #ST_FREEMOVEAGAIN}, {@value #ST_RESHAPE},
	 * {@value #RECT_CREATE}, {@value #RECT_FREEMOVE}, {@value #RECT_FREEMOVEAGAIN}, {@value #RECT_RESHAPE},
	 * {@value #MOVE_STUCKPOS}
	 */
	private int actionmode;
	/** ActionMode: Base state of cursor interaction, no action buttons pushed, any node that is under the cursor will be highlighted. */
	private final static int MOUSEOVER = 0;
	/** ActionMode: Free move state with shift pressed */
	private final static int ST_FREEMOVE = 1;
	/** ActionMode: Free move state with shift pressed following a reshape */
	private final static int ST_FREEMOVEAGAIN = 2;
	/** ActionMode: Free move state with shift pressed during a mouse drag event */
	private final static int ST_RESHAPE = 3;
	/** ActionMode: Mouse drag to create an interaction box */
	private final static int RECT_CREATE = 4;
	/** ActionMode: Mouse movement with active interaction box (no drag) */
	private final static int RECT_FREEMOVE = 5;
	/** ActionMode: Mouse movement after dragging the active interaction box (no current drag) */
	private final static int RECT_FREEMOVEAGAIN = 6;
	/** ActionMode: Mouse dragging movement with active interaction box */
	private final static int RECT_RESHAPE = 7;
	/** ActionMode: default to mouseover action */
	private final static int actionmodeDefault = MOUSEOVER;
	/** ActionMode: move a stuck line (hidden feature) */
	private final static int MOVE_STUCKPOS = 11;

	/**
	 * true: do jump cuts, false: use the number default number of steps in each animation
	 */
	protected boolean jump;
	/** The currently picked node (flash drawing). */
	private TreeNode pickedNode;
	/** The previously picked node (flash drawing). */
	private TreeNode pickedNodeOld;

	/**
	 * Constructor for a drawer with defined key and mouse interaction methods, and bindings from the drawer to
	 * the TreeJuxtaposer application.
	 * @param t Tree for this drawer.
	 * @param w width of the canvas in pixels.
	 * @param h height of the canvas in pixels.
	 * @param thetj TreeJuxtaposer object that uses this drawer.
	 */
	public AccordionTreeDrawerFinal(Tree t, int w, int h, TreeJuxtaposer thetj) {
		super(t, w, h);
		tj = thetj;
		tj.stateFrame.activeMarkAction(StateFrame.GA_ACT);
		tj.stateFrame.activeModeAction(StateFrame.M_ACT);
		actionmode = actionmodeDefault;
		keytarget = NONE;
		tj.stateFrame.growModeAction(growDirectionDefault);
		tj.stateFrame.activeResolutionAction(selectionResolutionDefault);
		jump = false;
		baseBox = null;
		splitAxis[X].setMaxStuckValue(0.7f); // so you can see the names on the leaves
		setMouseMoveAnimSteps(1, 3); // 1 step animations (jump) when mouse moves in progressive rendering mode
		// 3 step animations when moving in non-progressive rendering

	}

	/**
	 * Wrapper function for {@link TreeJuxtaposer#getColorsForRange(int, int, AccordionTreeDrawer)}
	 */
	public ArrayList getColorsForRange(int objmin, int objmax) {
		return tj.getColorsForRange(objmin, objmax, (AccordionTreeDrawer) this);
	}

	/**
	 * Make an InteractionBox from the GridCells nearest to the box of the cursor
	 * position, and update the view with a flash draw.
	 */
	private void drawActiveSubtreeBox() {
		// turn off original flashbox
		InteractionBox baseBox;
		flashBox = null;
		setFlash(null, rubberbandColor, -1, -1, true);
		CellGeom fg = null;
		if (tj.stateFrame.getActionGroup() == StateFrame.M_ACT) {
			baseBox = makeBox(pickedNode); 
			if (null == baseBox) {
				return;
			}
			flashBox = baseBox;
			if (null != pickedNode)
				fg = pickedNode;
		} else {
			ArrayList forestRoots = tj.getGroupForest(tj.stateFrame.getActionGroup(), this); // array of TreeNodes
			if (forestRoots.isEmpty()) {
				actionmodeReset();
				return;
			}
			TreeNode n = (TreeNode)(forestRoots.get(0));
			baseBox = makeBox(n); // just use the first one, for now
			if (null == baseBox) {
				return;
			}
			fg = n;
			flashBox = baseBox;
		}
		flashBoxWidth=3f;
		if (fg == null) fg = tree.getRoot();
		tj.setFocus(fg, this);
		setFlash(fg, rubberbandColor,-1, -1, true);
	}

	/**
	 * Initialize or reset the state of all object state.
	 * Return all flags to a known stable state, unset all interaction boxes.
	 */
	public void actionmodeReset() {
//		System.out.println("action mode reset");
		// reason: set action mode to mouseover when exiting other modes
		actionmode = MOUSEOVER;

		baseBox = null;
		flashBox = null;
		pickedNode = null;
		pickedNodeOld = null;
		flashBoxWidth = 1f;
		tj.doFlashGeom(null, StateFrame.M_ACT, this, mouseNow[X], mouseNow[Y]);

		keytarget = NONE; // squash keys in state machine
		tj.setQuasimode(false);
		mouseover(mouseNow[X], mouseNow[Y]);
	}

	/**
	 * Simple cursor location update and picking, called by {@link #mouseMoved(MouseEvent)}.
	 * @param x cursor horizontal position.
	 * @param y cursor vertical position.
	 */
	private void mouseover(int x, int y) {     
		TreeNode pickedGeom = (TreeNode)pickGeom(x,y);
		if (null == pickedGeom) return;
		mouseOverNode = pickedGeom;
		pickedNode = pickedGeom;
		if (pickedNode == pickedNodeOld) return;


		pickedNodeOld = pickedNode;
		tj.doFlashGeom(pickedNode, StateFrame.M_ACT, this, x, y);
		tj.debugFrame.result[DebugFrame.NAV_TYPE].setText(pickedNode.toString());
		tj.debugFrame.result[DebugFrame.BCN_SCORE].setText((pickedNode.getBcnScore()).toString());

	}

	/**
	 * Box stretching function used by {@link #mouseDragged(MouseEvent)}.
	 * @param stretchBox Box that is stretching, following a call to {@link InteractionBox#updateDrag(int[])}.
	 * @param doOther if true, will also stretch interaction boxes in other drawers, for linked navigation.
	 */
	private void reshaperectangle(InteractionBox stretchBox, boolean doOther) {

		if (null == stretchBox) {
			actionmodeReset();
			return;
		}

		// CRFP: return 2 integers, pixel differences for baseBox movable lines in X and Y
		int[] rectBox = createRectFromPick(stretchBox);

		if (rectBox == null)
			return;
		int numAnimSteps = getMouseMoveAnimSteps();
		while (keepMoving())
		{
			if (debugOutput)
				System.out.println(" (rRect) ending transitions");
			endAllTransitions(); // end transitions, new queue is going to be swapped in
		}
		Hashtable newToMove = new Hashtable();
		for (int xy = X; xy <= Y; xy++)
		{
			splitAxis[xy].moveLine(stretchBox.moveLines[xy], // the split to move
					rectBox[xy], // move it to the end position
					stretchBox.stuckLines[xy], // the split to stay
					numAnimSteps, newToMove);
		}
		toMove = newToMove; // swap in new queue

		incrementFrameNumber(); // so compute place this frame will update, this can be moved to after transitions are created
		if (!keepMoving())
		{
			if (debugOutput)
				System.out.println(" (rRect) nothing was added to move queue, so requesting redraw");
//			requestRedraw();
		}


		if (doOther
				&& null != stretchBox.item) { // linked navigation to other trees

			double changeRatio[] = new double[2];
			for (int xy = 0; xy < 2; xy++) {
				SplitAxis axis = getSplitAxis(xy);
				int movement = stretchBox.dragEnd[xy] - stretchBox.oldDragEnd[xy];
				double stuckPos = axis.getAbsoluteValue(stretchBox.stuckLines[xy], frameNum);
				double movePos = axis.getAbsoluteValue(stretchBox.moveLines[xy], frameNum);
				if (stuckPos > movePos)
					movement *= -1;
				changeRatio[xy] = s2w(movement, xy);

			}
			TreeNode n = (TreeNode)stretchBox.item;
			tj.resizeRectOthers(n, changeRatio, numAnimSteps, this);
			tj.requestRedrawAll();
		}
		else
			requestRedraw(); // redraw the frame, this will enact the movements in the move queue
		flashBox = stretchBox;
		setFlash(pickedNode, rubberbandColor, mouseNow[X], mouseNow[Y], true);
	}

	/**
	 * Mouse entry function.  When mouse enters, focus the canvas window, which brings the canvas to the front of other windows.
	 * @param e Mouse event object.
	 */
	public void mouseEntered(MouseEvent e) {
		if (mouseOutOfWindow(e))
			return;
		if (!tj.getQuasimode()) canvas.requestFocus();
		else tj.wantsFocusInQuasi(this);
	}

	/**
	 * Mouse exit function, for when mouse leaves drawing canvas' parent.
	 * When mouse leaves, transfer focus (key events listeners etc) to the new window.
	 * @param e Mouse event object.
	 */
	public void mouseExited(MouseEvent e) {
		if (mouseOutOfWindow(e))
			return;
		if (!tj.getQuasimode()) canvas.transferFocus();
	}

	/**
	 * Helper to determine if the cursor is out of the canvas.
	 * @param e Mouse event, for cursor position.
	 * @return true if cursor is outside drawing window.
	 */
	private boolean mouseOutOfWindow(MouseEvent e)
	{
		return e.getX() <= 0 || e.getX() >= getWinMax(X) || 
		e.getY() <= 0 || e.getY() >= getWinMax(Y);
	}

	/**
	 * Helper function to determine if the cursor is close to a stuck line, within pickFuzz.
	 * @return enumerated value of state of cursor with respect to the stuck lines.
	 */
	private int closeToStuck()
	{
		double mouse[] = {s2w(mouseNow[X], X), s2w(mouseNow[Y], Y)};
		double fuzz[] = {s2w(pickFuzz, Y), s2w(pickFuzz, Y)};
		for (int xy = X; xy <= Y; xy++)
		{
			if (Math.abs(mouse[xy] - splitAxis[xy].getMinStuckValue()) < fuzz[xy])
			{
				return xy + MIN_STUCK_X;
			}
			else if (Math.abs(mouse[xy] - splitAxis[xy].getMaxStuckValue()) < fuzz[xy])
			{
				return xy + MAX_STUCK_X;
			}
		}
		return FAR_FROM_STUCK;
	}

	/**
	 * Mouse click function, currently not fully supported, but can be used to move stuck positions.
	 * @param e The mouse click event.
	 */
	public void mouseClicked(MouseEvent e) {
		if (mouseOutOfWindow(e))
			return;
		int stuckValue = closeToStuck();
		if (MOUSEOVER == actionmode && stuckValue != FAR_FROM_STUCK)
		{
			baseBox = createBoxFromCells(stuckValue);
			flashBox = baseBox;
			setFlash(null, rubberbandColor, mouseNow[X], mouseNow[Y], true);
			actionmode = MOVE_STUCKPOS;
		}
		else if (RECT_FREEMOVE == actionmode || RECT_FREEMOVEAGAIN == actionmode ||
				ST_FREEMOVE == actionmode || ST_FREEMOVEAGAIN == actionmode) {
			// throw away return value, we just need the check of
			// whether faraway click means revert to mouseover
			if (null == baseBox) {
				actionmodeReset();
			}

		}
		mouseNow[X] = e.getX();
		mouseNow[Y] = e.getY();

		if (keepDrawing())
			requestRedraw();
	}
	
	/**
	 * Mouse press function, for updating interaction box events, setting up new boxes or stretching a created one.
	 * @param e The mouse event, for updating the interaction box.
	 */
	public void mousePressed(MouseEvent e) {
		if (mouseOutOfWindow(e))
			return;

		mouseNow[X] = e.getX();
		mouseNow[Y] = e.getY();
		for (int xy = 0; xy < 2; xy++) {
			dragStart[xy] = mouseNow[xy];
			dragEnd[xy] = mouseNow[xy];
		}
		if (flashBox != null)
			flashBox.setDragPoints(dragStart, dragEnd);

		if (MOUSEOVER == actionmode) {
			{		
				tj.clearDrawers();
			}
			actionmode = RECT_CREATE;
		}
		if (RECT_FREEMOVEAGAIN == actionmode) {
			actionmode = RECT_RESHAPE;
		} else if (RECT_FREEMOVE == actionmode) {
			if (null != flashBox) {
				actionmode = RECT_RESHAPE;
			} else {
				// no box yet. drag one out.
				flashBoxWidth=3f;
				actionmode = RECT_CREATE;
			}
		} else if (ST_FREEMOVE == actionmode) {
			actionmode = ST_RESHAPE;
		} else if (ST_FREEMOVEAGAIN == actionmode) {
			actionmode = ST_RESHAPE;
		}
	}

	/**
	 * Mouse release function, for updating mouse position events, such as highlighting.
	 * @param e The mouse event, for updating interaction boxes.
	 */
	public void mouseReleased(MouseEvent e) {
		if (RECT_RESHAPE == actionmode || ST_RESHAPE == actionmode)
		{
			if (baseBox != null)
				baseBox.undraw();
			if (flashBox != null)
				flashBox.undraw();
			actionmodeReset();
			return;
		}
		if (RECT_CREATE == actionmode) {
			actionmode = RECT_FREEMOVE;
		} else if (RECT_RESHAPE == actionmode || MOVE_STUCKPOS == actionmode) {
		} else if (actionmode == ST_RESHAPE) {
			actionmode = ST_FREEMOVEAGAIN;
		}
		if (keepDrawing())
			requestRedraw();
	}
	
	/**
	 * Mouse drag function, for updating mouse position events during a mouse press, such as stretching.
	 * @param e The mouse event, for updating positions of interaction boxes.
	 */
	public void mouseDragged(MouseEvent e) {
		mouseNow[X] = e.getX();
		mouseNow[Y] = e.getY();
		dragEnd[X] = mouseNow[X];
		dragEnd[Y] = mouseNow[Y];

		if (mouseNow[X] == mousePrev[X] && 
				mouseNow[Y] == mousePrev[Y]) return; // was exit the while loop, no return inside function
		if (flashBox != null)
		{
			flashBox.updateDrag(dragEnd);
		} 

		if (ST_FREEMOVE == actionmode)
			actionmode = ST_RESHAPE;
		
		if (ST_RESHAPE == actionmode) {
			reshaperectangle(flashBox, linkednav);
		}
		else if (actionmode == MOVE_STUCKPOS)
		{
			moveStuckPosition(flashBox);
		} else if ((RECT_CREATE == actionmode)) {
			tj.setQuasimode(true);
			flashBox = null;
			flashBoxWidth=3f;
			flashBox = createBoxFromCells(dragStart, dragEnd);
			setFlash(null, rubberbandColor, mouseNow[X], mouseNow[Y], true);
		} else if (RECT_RESHAPE == actionmode) {
			reshaperectangle(flashBox, false);
		}

		// modification by jeffrey, state machine change for control key vs. drag start

		else if(RECT_FREEMOVE == actionmode){
			flashBoxWidth=3f;
			flashBox = createBoxFromCells(dragStart, dragEnd);
//			baseBox = createBoxFromCells(dragStart, dragEnd);
//			flashBox = baseBox;
			setFlash(null, rubberbandColor, e.getX(), e.getY(), true);        
		}
		mousePrev[X] = mouseNow[X];
		mousePrev[Y] = mouseNow[Y];
	}

	/**
	 * Mouse movement function, for updating mouse position events, such as highlighting.
	 * Calls {@link #mouseover(int, int)}.
	 * @param e The mouse event, for updating the position.
	 */
	public void mouseMoved(MouseEvent e) {
		if (mouseOutOfWindow(e))
			return;

		mouseNow[X] = e.getX();
		mouseNow[Y] = e.getY();
		if (MOUSEOVER == actionmode)
		{
			mouseover(e.getX(), e.getY());
		}
		mousePrev[X] = mouseNow[X];
		mousePrev[Y] = mouseNow[Y];
	}

	/** Used when hitting a key should trigger an atomic action.
	 * Alternative is a {@link #keyPressed(KeyEvent)}/{@link #keyReleased(KeyEvent)} pair that could be used in
	 * conjunction with mouseclicks.
	 *  */
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();

		if (e.isShiftDown() && keytarget != DEVELOPER)
		{
			tj.setQuasimode(false);
			actionmodeReset();
		}
		if (keytarget != DEVELOPER)
		{
			if (key == 'a') {
				keytarget = ALL_GROUPS;
				tj.stateFrame.growModeAction(ALL);
				tj.stateFrame.activeResolutionAction(SUBTREE);
			} else if (key == 'd') {
				tj.stateFrame.activeModeAction(StateFrame.D_ACT);
			} else if (key == 'f') {
				tj.stateFrame.activeModeAction(StateFrame.M_ACT);
			} else if (key == 'h') {
				tj.stateFrame.growModeAction(HORIZ);
			} else if (key == 'l') {
				tj.stateFrame.activeModeAction(StateFrame.F_ACT);
			} else if (key == 'n') {
				tj.stateFrame.activeResolutionAction(NODE);
			} else if (key == 'v') {
				tj.stateFrame.growModeAction(VERT);
			} else if (key == 'x') {
				new TreeWriter(mouseOverNode, tj);
			} else if (key == '.' || key == '>') { // with or w/o shift
				keytarget = DEVELOPER;
			} else if (key == 'g') {
				if (keytarget >= StateFrame.GA_ACT &&
						keytarget <= StateFrame.GH_ACT)
				{
					tj.stateFrame.activeMarkAction(keytarget);
				}
				else {
					int newMark = tj.stateFrame.getMarkGroup() + 1;
					if (newMark > StateFrame.GH_ACT) 
						tj.stateFrame.activeMarkAction(StateFrame.GA_ACT);
					else
						tj.stateFrame.activeMarkAction(newMark);

					tj.setGroupPriority(tj.stateFrame.getMarkGroup()); 
				}
			} else if (key == 'k') {
				tj.settingsFrame.setLinkedNavigation(!tj.settingsFrame.linkCheck.isSelected());
			} else if (key == 'r' || key == 'R') {
				tj.reset();
				return;
			} else if (key == 't') {
				drawActiveSubtreeBox();
				actionmode = ST_FREEMOVE;
				if (tj.stateFrame.getActionGroup() == StateFrame.M_ACT) 
					if (pickedNode != null) tj.setFocus(pickedNode, this);
					else {
						ArrayList forestRoots = tj.getGroupForest(tj.stateFrame.getActionGroup(), this); // array of TreeNodes
						if (!forestRoots.isEmpty()) 
							tj.setFocus(((TreeNode)forestRoots.get(0)), this);
					}
			} else if (key == 'b' || key == 'B' || key == 's' || key == 'S') {
				boolean grow=(key == 'b' || key == 'B');
				int numSteps = (jump) ? 1 : getNumAnimSteps();
				if (linkednav)
					tj.resizeGroup(tj.getGroupByID(tj.stateFrame.getActionGroup()), numSteps, grow);
				else 
					tj.resizeGroup(tj.getGroupByID(tj.stateFrame.getActionGroup()), numSteps, grow, this);
				if (tj.stateFrame.getActionGroup() == StateFrame.M_ACT)
				{
					if (pickedNode != null)
						tj.setFocus(pickedNode, this);
					else
					{
						ArrayList forestRoots = tj.getGroupForest(tj.stateFrame.getActionGroup(), this); // array of TreeNodes
						if (!forestRoots.isEmpty()) 
							tj.setFocus(((TreeNode)forestRoots.get(0)), this);
					}
				}
			} else if (key == 'm' || key == 'M') {
				if (pickedNode != null) {
					if (key == 'm')	
						tj.unmarkGroup(tj.stateFrame.getMarkGroup());
					else
						// silly shift problem
						tj.clearDrawers();
					boolean selectSubtree= tj.stateFrame.activeResolution == SUBTREE;
					tj.doSelectGeom(pickedNode, selectSubtree, tj.stateFrame.getMarkGroup(), this);
					tj.setFocus(pickedNode, this);
					tj.lcaNode.add(pickedNode);
					if(tj.stateFrame.LCAMark.isSelected())
						tj.doLCAGeom(StateFrame.LCA_ACT, this);
					keytarget = tj.stateFrame.getMarkGroup();
				}
			} else if (key == 'c') {
				if (keytarget >= StateFrame.GA_ACT &&
						keytarget <= StateFrame.GH_ACT)
					tj.unmarkGroup(keytarget);
				else if (keytarget == ALL_GROUPS)
					for (int currMark = StateFrame.GA_ACT; currMark <= StateFrame.GH_ACT; currMark++)
						tj.unmarkGroup(currMark);
				tj.requestRedrawAll();
			} else if (key == 'u') {
				tj.settingsFrame.setStructDiff(!tj.settingsFrame.isDiffOn());
			} else if (key == 'w') {
				tj.settingsFrame.setLabelsOn(!tj.settingsFrame.areLabelsOn());
			} else if (key == 'e') {
				actionmodeReset();
			}
			tj.requestRedrawAll();
		} else { // developer
			if (key == 'N') {
				tj.requestRedrawAll();
			} else if (key == 'g') {
				tj.toggleDrawGrid();
			} else if (key == 'T') {
				tj.toggleDrawGeoms();
				requestRedraw();
			} else if (key == 'H') {
				tj.toggleNoFlash();
			} else if (key == 'R') {
				tj.toggleLabelPosRight();
			} else if (key == 'w') {
				tj.increaseLineThickness();
			} else if (key == 'W') {
				tj.decreaseLineThickness();
			} else if (key == 'm') {
				tj.increaseLabelBuffer(X);
			} else if (key == 'M') {
				tj.decreaseLabelBuffer(X);
			} else if (key == 'x') {
				tj.increaseMaxFontHeight();
			} else if (key == 'X') {
				tj.decreaseMaxFontHeight();
			} else if (key == 'e') {
				tj.increaseMinFontHeight();		    
			} else if (key == 'E') {
				tj.decreaseMinFontHeight();		    
			} else if (key == 'B') {
				tj.toggleLabelDrawBack();
			} else if (key == 'D') {
				tj.toggleDimBrite();
			} else if (key == 'C') {
				tj.toggleDimColors();
			} else if (key == 'j') {
				jump = !jump;
			} else if (key == 'z') {
				tj.toggleDrawSplits();
			} else if (key == 'O') {
//				tj.toggleCacheRange();
			} else if (key == 't') {
//				tj.toggleCheckTime();
			} else if (key == 'J') {
				stats.flush();
				System.out.println("flushed stats\n");
			} else if (key == 'S') {
				tj.toggleDumpStats();
			} else if (key == 'F') {
				// the second display loop is so that the entire scene gets drawn, before going on to next iteration
				setDumpStats(true);
				setDumpStats(false);
				System.out.println("done gathering stats");
			}
//			setFlash(pickedNode, tj.getGroupColor(StateFrame.M_ACT), mouseNow[X], mouseNow[Y], true);
			tj.requestRedrawAll();
		}
		
		if (key >= '0' && key <= '7') {
			keytarget = StateFrame.GA_ACT + key - '0';
		} else if (key != '.' && key != '>' // squish the keytarget if not developer key 
			&& key != 'a') // or all groups key
			keytarget = NONE;
	}

	/**
	 * Called by any of the arrow action functions, to update the drawer with a flash draw.
	 * @param originalNode Starting tree node.
	 * @param newNode New tree node to highlight.
	 */
	private void newMouseOver(TreeNode originalNode, TreeNode newNode)
	{
		if( newNode == null ) return;
		tj.clearGroup(StateFrame.M_ACT);
		tj.addNodesToGroup(newNode.key, newNode.key, StateFrame.M_ACT, this);
		tj.doFlashGeom(newNode, StateFrame.M_ACT, this, canvas.getX(), canvas.getY());
		this.pickedNode = newNode;
	}

	/** Find the child index of the originalNode as a child of the parent node
	 * 
	 * @param parentNode Parent node of the original node
	 * @param originalNode Node to get child index of
	 * @return the index of the original node in the parent's list of children
	 */
	private int findChildNumber(TreeNode parentNode, TreeNode originalNode)
	{
		int originalNodeChildNumber = 0; // number of original node (plus 1 actually)
		while (originalNodeChildNumber < parentNode.numberChildren() &&
				parentNode.getChild(originalNodeChildNumber++) != originalNode);
		if (originalNodeChildNumber > parentNode.numberChildren())
		{
			System.out.println("Child couldn't find self from parent");
		}
		return originalNodeChildNumber;
	}

	/**
	 Find the right aligned cell that is below (above) the original node, using the parent node as reference.
	The last parameter (boolean) is used to determine whether to search down (true) or up (false)
	@param parentNode parent of the original node
	@param originalNode the originating node to find a cousin of
	@param originalNodeChildNumber the index of the original node in the list of its parents children
	@param goingDown true for down arrow action, false for up arrow
	@return the cousin tree node of the original node, as best as possible for up and down arrow actions.  Previous
	movements are forgotten, so up-down actions may not return to original node. 
	*/
	private TreeNode rightAlignFind(TreeNode parentNode, TreeNode originalNode, int originalNodeChildNumber, boolean goingDown)
	{
		TreeNode temp = parentNode.getChild(originalNodeChildNumber);
		TreeNode prev = temp;
		final int TEMP = 0;
		final int ORIG = 1;
		double rightX[];
		rightX = new double[2];
		rightX[ORIG] = originalNode.getMaxY();
		rightX[TEMP] = temp.getMaxY();
		while (rightX[TEMP]  < rightX[ORIG] && temp.numberChildren() > 0)
		{
			prev = temp;
			if (goingDown)
				temp = temp.getChild(0);
			else
				temp = temp.getChild(temp.numberChildren() - 1);
			rightX[TEMP] = temp.getMaxY();
		}
		if (rightX[TEMP] == rightX[ORIG] || temp.numberChildren() == 0)
			return temp;
		else
			return prev;
	}

	/**
	 * Function to handle a left arrow action by the user.
	 *
	 */
	public void flashLeft()
	{
		RangeList flashGroup = tj.getGroupByID(StateFrame.M_ACT);
		if (flashGroup.size() == 0)
			return;
		RangeInTree rangeInTree =
			(RangeInTree) flashGroup.getFirst();
		int firstNodeKey = rangeInTree.getMin();
		TreeNode originalNode, parentNode;
		originalNode = tree.getNodeByKey(firstNodeKey);
		if (originalNode == null
				|| (parentNode = originalNode.parent()) == null)
			return;
		mouseOverNode = parentNode; // keep the level for up/down traversal
		newMouseOver(originalNode, parentNode);
	}

/**
 * Function to handle a right arrow action by the user.
 *
 */
	public void flashRight()
	{
		RangeList flashGroup = tj.getGroupByID(StateFrame.M_ACT); 
		if (flashGroup.size() == 0)
			return;
		RangeInTree rangeInTree =
			(RangeInTree) flashGroup.getFirst();
		int firstNodeKey = rangeInTree.getMin();
		TreeNode originalNode, childNode = null;
		originalNode = tree.getNodeByKey(firstNodeKey);
		if (originalNode == null
				|| originalNode.numberChildren() == 0
				|| (childNode = originalNode.getChild(0)) == null)
			return;
		mouseOverNode = childNode; // keep the level for up/down traversal
		newMouseOver(originalNode, childNode);
	}


	/**
	 * Function to handle a down arrow action by the user.
	 */
	public void flashDown()
	{
		RangeInTree rangeInTree =
			(RangeInTree) tj.getGroupByID(StateFrame.M_ACT).getFirst();
		int firstNodeKey = rangeInTree.getMin();
		TreeNode prevParentNode = null,
		parentNode = null,
		originalNode,
		downNode = null;
		originalNode = tree.getNodeByKey(firstNodeKey);
		downNode = originalNode; // default downNode, idempotent action
		if (originalNode == null
				|| (parentNode = originalNode.parent()) == null)
			return; // no parent (root) or no highlight
		int originalNodeChildNumber = findChildNumber(parentNode, originalNode);
		// number of original node (plus 1 actually)
		if (originalNodeChildNumber > parentNode.numberChildren())
			return; // can't find self
		if (originalNodeChildNumber < parentNode.numberChildren()) {
			// become maximal bigger-numbered-sibling's descendent
			//  - of the immediate parent, find the next sibling and traverse down
			//    until the last node that has a right edge less than the right edge of
			//    the original node is found (so traversal through the leaves of a right-
			//    aligned tree is possible since it is better than falling back to the root)
			downNode =
				rightAlignFind(
						parentNode,
						mouseOverNode,
						originalNodeChildNumber,
						true);
		} else {
			// process cousins
			prevParentNode = parentNode.parent;
			int parentAsChildNum = 0;
			while (prevParentNode != null) {
				parentAsChildNum = findChildNumber(prevParentNode, parentNode);
				if (parentAsChildNum > prevParentNode.numberChildren())
					return;
				if (parentAsChildNum < prevParentNode.numberChildren()
						|| prevParentNode.parent == null)
					break;
				parentNode = prevParentNode;
				prevParentNode = prevParentNode.parent;
			}
			if (prevParentNode == null
					|| parentAsChildNum == prevParentNode.numberChildren())
				return;
			downNode =
				rightAlignFind(
						prevParentNode,
						mouseOverNode,
						parentAsChildNum,
						true);
		}
		newMouseOver(originalNode, downNode);
	}

	/**
	 * Function to handle an up arrow action by the user.
	 */
	public void flashUp()
	{
		RangeInTree rangeInTree = tj.getGroupByID(StateFrame.M_ACT).getFirst();
		int firstNodeKey = rangeInTree.getMin();
		TreeNode prevParentNode = null,
		parentNode = null,
		originalNode,
		upNode = null;
		originalNode = tree.getNodeByKey(firstNodeKey);
		upNode = originalNode; // default upNode, idempotent action
		if (originalNode == null || (parentNode = originalNode.parent()) == null)
			return; // no parent (root) or no highlight
		int originalNodeChildNumber = findChildNumber(parentNode, originalNode);
		// number of original node (plus 1 actually)
		if (originalNodeChildNumber > parentNode.numberChildren())
			return; // can't find self
		if (originalNodeChildNumber > 1) {
			// become maximal smaller-numbered-sibling's descendent
			//  - of the immediate parent, find the previous sibling and traverse up
			//    until the last node that has a right edge less than the right edge of
			//    the original node is found (so traversal through the leaves of a right-
			//    aligned tree is possible since it is better than falling back to the root)
			upNode =
				rightAlignFind(
						parentNode,
						mouseOverNode,
						originalNodeChildNumber - 2,
						false);
		} else {
			// process cousins
			prevParentNode = parentNode.parent;
			int parentAsChildNum = 0;
			while (prevParentNode != null) {

				parentAsChildNum = findChildNumber(prevParentNode, parentNode);
				if (parentAsChildNum > prevParentNode.numberChildren())
					return; // can't find parent from prev parent
				if ((parentAsChildNum > 1
						&& parentAsChildNum <= prevParentNode.numberChildren())
						|| prevParentNode.parent == null)
					break; // found good common descendent to traverse down
				parentNode = prevParentNode;
				prevParentNode = prevParentNode.parent;
			}
			if (prevParentNode == null || parentAsChildNum == 1)
				return;
			upNode =
				rightAlignFind(
						prevParentNode,
						mouseOverNode,
						parentAsChildNum - 2,
						false);
		}
		newMouseOver(originalNode, upNode);
	}
	
	/**
	 * Capture the press of a key.  Used to detect arrow cursor movements or a user pressing one of the control keys (eg. shift, control, alt).
	 * @param e The key event.
	 */
	public void keyPressed(KeyEvent e) {
		if (tj.getQuasimode() || keytarget == DEVELOPER) return;
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_LEFT)
		{
			flashLeft();
		}
		else if (code == KeyEvent.VK_RIGHT)
		{
			flashRight();
		}
		else if (code == KeyEvent.VK_DOWN)
		{
			flashDown();
		}

		else if (code == KeyEvent.VK_UP)
		{
			flashUp();
		}
		else 
			if (KeyEvent.VK_CONTROL == code || KeyEvent.VK_ALT == code) {
				tj.setQuasimode(true);
				flashBox = null;
				setFlash(null, rubberbandColor, mouseNow[X], mouseNow[Y], true);
				actionmode = RECT_FREEMOVE;
			}  else if (KeyEvent.VK_SHIFT == code) {
				if (keytarget >= StateFrame.GA_ACT && 
						keytarget <= StateFrame.GH_ACT)
					tj.stateFrame.activeMarkAction(keytarget);
				else tj.stateFrame.activeMarkAction(StateFrame.GA_ACT);
				drawActiveSubtreeBox();
				if (tj.stateFrame.getActionGroup() == StateFrame.M_ACT) {
					if (null != pickedNode) {
						tj.setFocus(pickedNode, this);
						tj.setQuasimode(true);
						drawActiveSubtreeBox();
						actionmode = ST_FREEMOVE;
					}
				} else {
					tj.setQuasimode(true);
					drawActiveSubtreeBox();
					actionmode = ST_FREEMOVE;
					ArrayList forestRoots = tj.getGroupForest(tj.stateFrame.getActionGroup(), this); // array of TreeNodes
					if (!forestRoots.isEmpty()) 
						tj.setFocus(((TreeNode)forestRoots.get(0)), this);
				}
				System.out.println("keytarget reset in shift pressed");
				keytarget = NONE;
			}
	}

	/**
	 * Capture the release of a keystroke.  Used to set action mode when a user releases one of the control keys (eg. shift, control, alt).
	 * @param e The key event.
	 */
	public void keyReleased(KeyEvent e) {
		if (keytarget == DEVELOPER) return;
		int code = e.getKeyCode();
		if (KeyEvent.VK_SHIFT == code || KeyEvent.VK_CONTROL == code || KeyEvent.VK_ALT == code) {
			tj.setQuasimode(false);
			actionmodeReset();
		}
	}

	/**
	 * Wrapper function for {@link TreeJuxtaposer#seedQueue(AccordionTreeDrawerFinal)}.
	 */
	public void seedQueue() {
		tj.seedQueue(this);
	}

	/**
	 * Wrapper for TreeJuxtaposer call to {@link TreeJuxtaposer#getColorsForRange(int, int, AccordionTreeDrawer)}.  Get the color for a given TreeNode.
	 * @param c TreeNode to get color for.
	 * @return an array of colors that this node is to be drawn in, in drawing priority.
	 */
	public ArrayList getColorsForCellGeom(CellGeom c){
		return tj.getColorsForRange(c.getKey(), c.getKey(), this);
	}

	/**
	 * Get the range list (marked group) by the given key
	 * @param key range ID
	 * @return RangeList marked group that corresponds to the given key.
	 */
	public RangeList getRangeByKey(int key)
	{
		RangeList group = tj.getGroupByID(key);
		return group;
	}

	/**
	 * Stub function.
	 */
	protected void customGLInit(GL gl) {
	}
	
	/**
	 * Convert tree into name used by screen shot writer.
	 * @return Truncated name of file to remove directory location.
	 */
	public String toString()
	{
		int lastSlash = Math.max(tree.getName().lastIndexOf('/'), tree.getName().lastIndexOf('\\'));
		return tree.getName().substring(lastSlash+1);
	}

};
