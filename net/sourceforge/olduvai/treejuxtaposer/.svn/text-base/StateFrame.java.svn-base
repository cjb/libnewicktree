package net.sourceforge.olduvai.treejuxtaposer;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JColorChooser;

import net.sourceforge.olduvai.treejuxtaposer.drawer.RangeList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Canvas;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;



/**
 * GUI for marking and current stretching/squishing group.
 * @author jslack
 *
 */
public class StateFrame extends JFrame implements ActionListener, MouseListener {

	/** Title for the state frame (aka groups). */
	private final static String title = "Groups";
	/** Spacing panel.  Empty. */
	private JPanel blank;  // for spacing, if needed
	/** Treejuxtaposer object, for referencing groups and other state. */
	private TreeJuxtaposer tj;
	/** Edge length of squares used to show the color for each marking group. */
	private static final int CANVAS_SIZE = 15;
	
	/** Active Panel
	 * Includes the following panels:
	 * 	grow direction,
	 * 	grow action,
	 * 	active group.
	 */
	private JPanel activePanel;
	/** Label for {@link #activePanel}. */
	private final static JLabel activePanelLabel = new JLabel("Active");
	/** Reset button for unmarking previously marked user groups. */
	private JButton resetButton;
	
	/** Grow direction panel (horizontal, vertical, or both). */
	private JPanel growDirectionPanel;
	/** Button group for {@link #growMode}. */
	private ButtonGroup growSelect;
	/** Radio buttons for stretching directions: horizontal, vertical, or both. */
	private JRadioButton growMode[];
	/** Label for {@link #growDirectionPanel}. */
	private final static JLabel growDirLabel = new JLabel("Direction");
	/** Grow labels for {@link #growMode}. */
	private final static String growLabel[] = {"Horizontal", "Vertical", "Both"};
	/** Horizontal growing. */
	public final static int H_MODE = 0;
	/** Vertical growing. */
	public final static int V_MODE = 1;
	/** Horizontal AND vertical growing at the same time. */
	public final static int B_MODE = 2;
	
	/* grow action panel */
	/** Grow/shrink buttons. */
	private JButton growButton[];
	/** Labels for grow and shrink for {@link #growButton}. */
	private final static String buttonLabel[] = {"Bigger", "Smaller"};
	/** Index for growing, for {@link #growButton}. */
	private final static int B_GROW = 0;
	/** Index for shrinking, for {@link #growButton}. */
	private final static int S_GROW = 1;
	
	/** Active group panel. */
	private JPanel activeGroupPanel;
	/** Active color swatches for currently growing group color selection. */
	private Canvas activeCanvas[];
	/** Group of buttons (one active at a time) for {@link #activeMode} set of buttons. */
	private ButtonGroup activeSelect;
	/** Buttons to select current active growing group, see {@link #activeLabel} for their names. */
	private JRadioButton activeMode[];

	/** Active group labels for user groups and automated marking.  User group titles are also reused for
	 * marking group labels in {@link #markLabel}.*/
	private final static String activeLabel[] = {
		"Group A", "Group B", "Group C", "Group D",
		"Group E", "Group F", "Group G", "Group H",
		"Mouse Over", "Differences", "Found", "LCA"};
	/** Titles for color manipulation dialogs (window title), which pop up after selecting a group color swatch (canvas {@link #markCanvas} or {@link #activeCanvas}). */
	static final private String colorTitle[] = {
			"'Group A' color selection",
			"'Group B' color selection",
			"'Group C' color selection",
			"'Group D' color selection",
			"'Group E' color selection",
			"'Group F' color selection",
			"'Group G' color selection",
			"'Group H' color selection",
			"'Mouse Over' color selection",
			"'Tree Differences' color selection",
			"'Found Nodes' color selection",
			" 'LCA' color selection"
	};
	/** 1st group (A) */
	public final static int GA_ACT = 0;
	/** 2nd group (B) */
	public final static int GB_ACT = 1;
	/** 3rd group (C) */
	public final static int GC_ACT = 2;
	/** 4th group (D) */
	public final static int GD_ACT = 3;
	/** 5th group (E) */
	public final static int GE_ACT = 4;
	/** 6th group (F) */
	public final static int GF_ACT = 5;
	/** 7th group (G) */
	public final static int GG_ACT = 6;
	/** 8th group (H) */
	public final static int GH_ACT = 7;
	/** Mouseover group number */
	public final static int M_ACT = 8;
	/** Automated difference group number */
	public final static int D_ACT = 9;
	/** Found (text search) group number */
	public final static int F_ACT = 10;
	/** Lowest common ancestor group number */
	public final static int LCA_ACT = 11;
	
	/** User-defined marking group panel. */
	private JPanel markPanel;
	/** Label for mark panel {@link #markPanel}. */
	private final static JLabel markPanelLabel = new JLabel("Mark");
	
	/** User-defined marking group radio buttons ({@link #markMode} group), only one mark active at a time. */
	private ButtonGroup markSelect;
	/** Buttons for user-defined group marks. */
	private JRadioButton markMode[];
	/** Panel for {@link #markMode}, user-defined group marks.*/
	private JPanel markModePanel;
	/** Label array for user-defined mark names.  Uses {@link #activeLabel} names. */
	private final static String markLabel[] = {activeLabel[0],
		activeLabel[1], activeLabel[2], activeLabel[3], activeLabel[4],
		activeLabel[5], activeLabel[6], activeLabel[7]};
	/** Canvases for user-defined marks.  */
	private Canvas markCanvas[];
	/** Labels for clearing user group buttons {@link #clearButton}. */
	private final static String clearLabel[] = {"Clear group", "Clear all"};
	/** "Clear group" label and button index in {@link #clearButton}. */
	private final static int G_CLEAR = 0;
	/** "Clear all" label and button index in {@link #clearButton}. */
	private final static int A_CLEAR = 1;
	/** Buttons for clearing, indexed by group clear {@link #G_CLEAR} and clear all {@link #A_CLEAR}.*/
	private JButton clearButton[];
	
	/** Mark resolution (node or subtree marking) label. */
	private final static JLabel resolutionTitle = new JLabel("Mark Resolution");
	/** Panel for marking resolution. */
	private JPanel resolutionPanel;
	/** Button group for marking resolution, so only one of {@link #resolutionMode} is selected. */
	private ButtonGroup markResolution;
	/** Label for node {@link #N_RES} and subtree {@link #S_RES} marking. */
	private final static String resolutionLabel[] = {"Node", "Subtree"};
	/** Node marking mode. Compare to {@link #S_RES}. */
	public final static int N_RES = 0;
	/** Subtree marking mode. Compare to {@link #N_RES}. */
	public final static int S_RES = 1;
	/** Resolution mode, for node {@link #N_RES} or subtree {@link #S_RES} marking modes. */
	private JRadioButton resolutionMode[];
	
	/** Label for least common ancestor checkboxes. */
	private final static String LCA = "LCA";
	/** For growing by LCA when checked. */
	private JCheckBox LCAGroup; 
	/** For marking by LCA (ascend tree from marks to their LCA while this is checked) */
	public JCheckBox LCAMark; 

	/**
	 * Initial colors for groups.
	 */
	public final static Color initialColor[] = {
		Color.getHSBColor(200f/360f, 0.81f, 0.75f),  // group A blue
		Color.getHSBColor(140f/360f, 0.71f, 0.67f),  // group B green
		Color.getHSBColor(184f/360f, 0.98f, 0.84f),  // group C teal
		Color.getHSBColor(277f/360f, 0.33f, 0.75f),  // group D purple
		Color.getHSBColor(60f/360f, 0.79f, 0.78f),  // group E gold
		Color.getHSBColor(11f/360f, 0.41f, 0.62f),  // group F brown
		Color.getHSBColor(324f/360f, 0.35f, 0.91f),  // group G = found
		Color.getHSBColor(3f/360f, 0.77f, 0.87f),  // group H = diff
		Color.getHSBColor(31f/360f, 0.84f, 1f),  // mouse over orange
		Color.getHSBColor(3f/360f, 0.77f, 0.87f),  // differences red
		//new Color(87, 152, 64), // dark green
		Color.getHSBColor(324f/360f, 0.35f, 0.91f),  // found pink
		 new Color(87, 152, 64) // lca dark green
	};
	private Color color[];
	


	/**
	 * Constructor for StateFrame.
	 * @param tj The tree juxtaposer to reference for state and drawers.
	 * @throws HeadlessException
	 */
	public StateFrame(TreeJuxtaposer tj)  {
		super();
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	/**
	 * Initialize GUI and set up listeners.
	 *
	 */
	private void initComponents()
	{
	GridBagConstraints gbc = new GridBagConstraints();
	this.setTitle(title);
	((JFrame)this).setLocation(10,30);
	this.getContentPane().setLayout(new GridBagLayout());

	/* Active Panel */
	activePanel = new JPanel(new GridBagLayout());
	activePanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.gridx = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.gridy = 0;
	//activePanel.add(activePanelLabel, gbc);

	
	/* Grow direction panel */
	growDirectionPanel = new JPanel(new GridBagLayout());
	//growDirectionPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	//gbc.gridwidth = growLabel.length;
	//growDirectionPanel.add(growDirLabel, gbc);
	growButton = new JButton[buttonLabel.length];
	for (int i = 0; i < buttonLabel.length; i++)
	{
		growButton[i] = new JButton(buttonLabel[i]);
		gbc.gridx = i;
		growDirectionPanel.add(growButton[i], gbc);
	}

	
	growMode = new JRadioButton[growLabel.length];
	growSelect = new ButtonGroup();
	gbc.gridy = 0;
	for (int i = 0; i < growLabel.length; i++)
	{
		growMode[i] = new JRadioButton(growLabel[i]);
		growSelect.add(growMode[i]);
		gbc.gridx = i + buttonLabel.length;
		growDirectionPanel.add(growMode[i], gbc);
	}
	
	LCAGroup = new JCheckBox(LCA, false);
	gbc.gridx = buttonLabel.length + 2*growLabel.length;
	gbc.anchor = GridBagConstraints.EAST;
//	growDirectionPanel.add(LCAGroup, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
	
	resetButton = new JButton("Reset");
	gbc.gridx++;
	gbc.anchor = GridBagConstraints.WEST;
	growDirectionPanel.add(resetButton, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
	
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.fill = GridBagConstraints.BOTH;
	activePanel.add(growDirectionPanel, gbc);
	gbc.fill = GridBagConstraints.NONE;
	
	/* Active Group Panel */
	activeGroupPanel = new JPanel(new GridBagLayout());
	//activeGroupPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	activeCanvas = new Canvas[activeLabel.length];
	activeMode = new JRadioButton[activeLabel.length];
	activeSelect = new ButtonGroup();
	color = new Color[activeLabel.length];
	gbc.gridy = 0;
	
	final int activeRows = 3;
	for (int i = 0; i < activeLabel.length; i++)
	{
		color[i] = new Color(initialColor[i].getRGB());
		gbc.gridy = i / (activeLabel.length / activeRows);
		gbc.gridx = 3 * (i % (activeLabel.length / activeRows));
		// put in radio buttons
		activeMode[i] = new JRadioButton(activeLabel[i]);
		activeSelect.add(activeMode[i]);
		gbc.ipadx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx += 1;
		activeGroupPanel.add(activeMode[i], gbc);
		activeCanvas[i] = new Canvas();
		activeCanvas[i].setBackground(color[i]);
		activeCanvas[i].setSize(CANVAS_SIZE, CANVAS_SIZE);
		gbc.gridx -= 1;
		gbc.anchor = GridBagConstraints.EAST;
		JPanel canvasPanel = new JPanel();
		canvasPanel.add(activeCanvas[i]);
		canvasPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		activeGroupPanel.add(canvasPanel, gbc);
		//activeGroupPanel.add(activeCanvas[i], gbc);
		gbc.ipadx = 10;
		gbc.gridx += 2;
		blank = new JPanel();
		activeGroupPanel.add(blank, gbc);
	}
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.ipadx = 0;
	activeMode[M_ACT].setSelected(true);
	gbc.gridy = 0;
	gbc.gridx = 1;
	gbc.fill = GridBagConstraints.BOTH;
	activePanel.add(activeGroupPanel, gbc);
	gbc.fill = GridBagConstraints.NONE;
	
	/* Active Panel events */
	resetButton.addActionListener(this);
	
	growMode[H_MODE].addActionListener(this);
	growMode[V_MODE].addActionListener(this);
	growMode[B_MODE].addActionListener(this);
	
	growButton[B_GROW].addActionListener(this);
	growButton[S_GROW].addActionListener(this);
	
	activeMode[GA_ACT].addActionListener(this);
	activeMode[GB_ACT].addActionListener(this);
	activeMode[GC_ACT].addActionListener(this);
	activeMode[GD_ACT].addActionListener(this);
	activeMode[GE_ACT].addActionListener(this);
	activeMode[GF_ACT].addActionListener(this);
	activeMode[GG_ACT].addActionListener(this);
	activeMode[GH_ACT].addActionListener(this);
	activeMode[M_ACT].addActionListener(this);
	activeMode[D_ACT].addActionListener(this);
	activeMode[F_ACT].addActionListener(this);
	activeMode[LCA_ACT].addActionListener(this);
	
	activeCanvas[GA_ACT].addMouseListener(this);
	activeCanvas[GB_ACT].addMouseListener(this);
	activeCanvas[GC_ACT].addMouseListener(this);
	activeCanvas[GD_ACT].addMouseListener(this);
	activeCanvas[GE_ACT].addMouseListener(this);
	activeCanvas[GF_ACT].addMouseListener(this);
	activeCanvas[GG_ACT].addMouseListener(this);
	activeCanvas[GH_ACT].addMouseListener(this);
	activeCanvas[M_ACT].addMouseListener(this);
	activeCanvas[D_ACT].addMouseListener(this);
	activeCanvas[F_ACT].addMouseListener(this);
	activeCanvas[LCA_ACT].addMouseListener(this);
	LCAGroup.addActionListener(this);

	/* Mark Panel */
	markPanel = new JPanel(new GridBagLayout());
	markPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.gridx = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.gridy = 0;
	gbc.anchor = GridBagConstraints.WEST;
	markPanel.add(markPanelLabel, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
	
	markModePanel = new JPanel(new GridBagLayout());
	//markModePanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	markSelect = new ButtonGroup();
	markMode = new JRadioButton[markLabel.length];
	markCanvas = new Canvas[markLabel.length];
	final int markRows = 2;
	for (int i = 0; i < markLabel.length; i++)
	{
		markMode[i] = new JRadioButton(markLabel[i]);
		markCanvas[i] = new Canvas();
		markCanvas[i].setBackground(color[i]);
		markCanvas[i].setSize(CANVAS_SIZE, CANVAS_SIZE);
		markSelect.add(markMode[i]);
		gbc.gridx = (i % (markLabel.length / markRows)) * 3;
		gbc.ipadx = 0;
		gbc.gridy = i / (markLabel.length / markRows);
		gbc.anchor = GridBagConstraints.EAST;
		JPanel canvasPanel = new JPanel();
		canvasPanel.add(markCanvas[i]);
		canvasPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		markModePanel.add(canvasPanel, gbc);
		//markModePanel.add(markCanvas[i], gbc);
		gbc.gridx += 1;
		gbc.anchor = GridBagConstraints.WEST;
		markModePanel.add(markMode[i], gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx += 1;
		gbc.ipadx = 10;
		markModePanel.add(new JPanel(), gbc); // blank space
	}
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.gridwidth = 10;
	gbc.fill = GridBagConstraints.BOTH;
	markPanel.add(markModePanel, gbc);
	gbc.fill = GridBagConstraints.NONE;
	gbc.gridwidth = 1;
	gbc.ipadx = 0;
	clearButton = new JButton[clearLabel.length];
	gbc.gridy = 2;
	gbc.fill = GridBagConstraints.NONE;
	for (int i = 0; i < clearLabel.length; i++)
	{
		clearButton[i] = new JButton(clearLabel[i]);
		gbc.gridx = i;
		markPanel.add(clearButton[i], gbc);
	}
	gbc.gridx = clearLabel.length;
	markPanel.add(new JPanel(), gbc);

	resolutionPanel = new JPanel();
	//resolutionPanel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
	markResolution = new ButtonGroup();
	resolutionMode = new JRadioButton[resolutionLabel.length];
	gbc.gridy = 0;
	gbc.gridx = 0;
	resolutionPanel.add(resolutionTitle, gbc);
	for (int i = 0; i < resolutionLabel.length; i++)
	{
		resolutionMode[i] = new JRadioButton(resolutionLabel[i]);
		markResolution.add(resolutionMode[i]);
		gbc.gridx = i + 1;
		resolutionPanel.add(resolutionMode[i], gbc);
	}
	
	LCAMark = new JCheckBox(LCA, false);
	gbc.gridx = gbc.gridx + 1;
	gbc.anchor = GridBagConstraints.EAST;
	resolutionPanel.add(LCAMark, gbc);
	gbc.anchor = GridBagConstraints.CENTER;
		
	gbc.gridx = clearLabel.length + 1;
	gbc.gridy = 2;
	markPanel.add(resolutionPanel, gbc);

	/* Mark panel events */
	markCanvas[GA_ACT].addMouseListener(this);
	markCanvas[GB_ACT].addMouseListener(this);
	markCanvas[GC_ACT].addMouseListener(this);
	markCanvas[GD_ACT].addMouseListener(this);
	markCanvas[GE_ACT].addMouseListener(this);
	markCanvas[GF_ACT].addMouseListener(this);
	markCanvas[GG_ACT].addMouseListener(this);
	markCanvas[GH_ACT].addMouseListener(this);
	
	clearButton[G_CLEAR].addActionListener(this);
	clearButton[A_CLEAR].addActionListener(this);
	
	markMode[GA_ACT].addActionListener(this);
	markMode[GB_ACT].addActionListener(this);
	markMode[GC_ACT].addActionListener(this);
	markMode[GD_ACT].addActionListener(this);
	markMode[GE_ACT].addActionListener(this);
	markMode[GF_ACT].addActionListener(this);
	markMode[GG_ACT].addActionListener(this);
	markMode[GH_ACT].addActionListener(this);

	resolutionMode[N_RES].addActionListener(this);
	resolutionMode[S_RES].addActionListener(this);
	
	LCAMark.addActionListener(this);
	
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.gridx = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.BOTH;
	getContentPane().add(activePanel, gbc);
	gbc.gridy = 1;
	getContentPane().add(markPanel, gbc);
	markMode[GA_ACT].setSelected(true);
	pack();
	
	}

	/**
	 * Handler for user-modifying grow direction not through the GUI.
	 * @param mode One of the defined directions horizontal {@link #H_MODE}, both {@link #B_MODE}, or vertical {@link #V_MODE}.
	 */
	protected void growModeAction(int mode)
	{
		growMode[mode].setSelected(true);
		growDirection = mode;		
	}

	/** The current group that will be grown on an automated action, such as grow/shrink. */
	protected int activeGrow;
	/** The current group that will be marked on user-directed marking actions. */
	protected int activeMark;
	/** The current mode for growing, either vertical, horizontal, or both directions simultaneously. */
	protected int growDirection;
	/** The current mark mode for internal nodes: either {@link #N_RES} or {@link #S_RES} (node, subtree) */
	protected int activeResolution;
	
	/**
	 * Sets the current active group (local state {@link #activeGrow}) to automatically grow/shrink or perform other actions on.
	 * Compare to current mark group, which is the group that a selected region will be marked as.
	 * The mode may be any group (user, diff, found, mouse-over, LCA) while the mark is only user.
	 * @param mode Any staticly valued group such as user groups, diff, found, mouse-over and LCA.
	 */
	protected void activeModeAction(int mode)
	{
		activeGrow = mode;
		activeMode[mode].setSelected(true);
	}
	
	/**
	 * Sets the current marking group (local state {@link #activeMark}) to mark when user selects 'm' to highlight nodes or subtrees.
	 * Compare to current active group, which is the group that will be grown with automated actions.
	 * The mode may be any user marking group only, while the active group may be any other group as well.
	 * The group priority is then adjusted to make the last marked group visible over all others.
	 * Difference marks are always top priority over user groups.
	 * @param mode Any user marking group ({@link #GA_ACT} to {@link #GH_ACT}).
	 */
	protected void activeMarkAction(int mode)
	{
		activeMark = mode; // TreeJuxtaposer.markGroup[mode];
		markMode[mode].setSelected(true);
		tj.setGroupPriority(mode); //TreeJuxtaposer.markGroup[mode]);
	}

	/**
	 * Set the user group marking resolution to the given mode.
	 * Modes are node {@link #N_RES} and subtree {@link #S_RES}.
	 * @param mode One of node {@link #N_RES} or subtree {@link #S_RES}, the marking resolution.
	 */
	protected void activeResolutionAction(int mode)
	{
		resolutionMode[mode].setSelected(true);
		activeResolution = mode;
	}
	
	/**
	 * Clear all user marked groups, and LCA mark checkbox.
	 */
	private void unmarkAllGroups()
	{
		tj.unmarkGroup(GA_ACT); //TreeJuxtaposer.markGroup[GA_ACT]);
		tj.unmarkGroup(GB_ACT);
		tj.unmarkGroup(GC_ACT);
		tj.unmarkGroup(GD_ACT);
		tj.unmarkGroup(GE_ACT);
		tj.unmarkGroup(GF_ACT);
		tj.unmarkGroup(GG_ACT);
		tj.unmarkGroup(GH_ACT);

		tj.unmarkGroup(LCA_ACT); //TreeJuxtaposer.lcaGroup);
		setLCAMark(false);
	}

	/**
	 * Button press action listener.
	 * @param evt Event to decipher.
	 */
	public void actionPerformed(ActionEvent evt)
	{
		Object obj = evt.getSource();
		if (obj == resetButton)
		{
			tj.reset();
		}
		else if (obj == growMode[H_MODE] ||
			obj == growMode[V_MODE] ||
			obj == growMode[B_MODE])
		{
			int modeNum = H_MODE;
			if (obj == growMode[V_MODE]) modeNum = V_MODE;
			else if (obj == growMode[B_MODE]) modeNum = B_MODE;
			growModeAction(modeNum);
		}
		
		// bigger or smaller button
		else if (obj == growButton[B_GROW] ||
			obj == growButton[S_GROW])
		{
			if (tj.treeDrawers.size() == 0)
				return;
			AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tj.treeDrawers.get(0);
			int numSteps = (atd.jump) ? 1 : atd.getNumAnimSteps();
			tj.resizeGroup(tj.getGroupByID(activeGrow), numSteps, obj == growButton[B_GROW]);
			tj.requestRedrawAll();
		}
		
		// active group changed
		else if (obj == activeMode[GA_ACT] ||
			obj == activeMode[GB_ACT] ||
			obj == activeMode[GC_ACT] ||
			obj == activeMode[GD_ACT] ||
			obj == activeMode[GE_ACT] ||
			obj == activeMode[GF_ACT] ||
			obj == activeMode[GG_ACT] ||
			obj == activeMode[GH_ACT] ||
			obj == activeMode[M_ACT] ||
			obj == activeMode[F_ACT] ||
			obj == activeMode[D_ACT] ||
			obj == activeMode[LCA_ACT])
		{
			activeGrow = GA_ACT;
			((JRadioButton)obj).setSelected(true);
			if (obj == activeMode[GB_ACT]) activeGrow = GB_ACT;
			else if (obj == activeMode[GC_ACT]) activeGrow = GC_ACT;
			else if (obj == activeMode[GD_ACT]) activeGrow = GD_ACT;
			else if (obj == activeMode[GE_ACT]) activeGrow = GE_ACT;
			else if (obj == activeMode[GF_ACT]) activeGrow = GF_ACT;
			else if (obj == activeMode[GG_ACT]) activeGrow = GG_ACT;
			else if (obj == activeMode[GH_ACT]) activeGrow = GH_ACT;
			else if (obj == activeMode[M_ACT]) activeGrow = M_ACT;
			else if (obj == activeMode[F_ACT]) activeGrow = F_ACT;
			else if (obj == activeMode[D_ACT]) activeGrow = D_ACT;
			else if (obj == activeMode[LCA_ACT]) activeGrow = LCA_ACT;
			activeModeAction(activeGrow);
			tj.requestRedrawAll();
		}	
		else if (obj == clearButton[G_CLEAR])
		{
			if (markMode[GA_ACT].isSelected()) tj.unmarkGroup(GA_ACT); //TreeJuxtaposer.markGroup[GA_ACT]);
			else if (markMode[GB_ACT].isSelected()) tj.unmarkGroup(GB_ACT);
			else if (markMode[GC_ACT].isSelected()) tj.unmarkGroup(GC_ACT);
			else if (markMode[GD_ACT].isSelected()) tj.unmarkGroup(GD_ACT);
			else if (markMode[GE_ACT].isSelected()) tj.unmarkGroup(GE_ACT);
			else if (markMode[GF_ACT].isSelected()) tj.unmarkGroup(GF_ACT);
			else if (markMode[GG_ACT].isSelected()) tj.unmarkGroup(GG_ACT);
			else if (markMode[GH_ACT].isSelected()) tj.unmarkGroup(GH_ACT);
			else unmarkAllGroups();
			tj.requestRedrawAll();
		}
		else if (obj == clearButton[A_CLEAR])
		{
			unmarkAllGroups();
			tj.lcaNode.clear();
			tj.requestRedrawAll();
		}
		else if (obj == markMode[GA_ACT] ||
			obj == markMode[GB_ACT] ||
			obj == markMode[GC_ACT] ||
			obj == markMode[GD_ACT] ||
			obj == markMode[GE_ACT] ||
			obj == markMode[GF_ACT] ||
			obj == markMode[GG_ACT] ||
			obj == markMode[GH_ACT])
		{
			int mode = GA_ACT;
			if (obj == markMode[GB_ACT]) mode = GB_ACT;
			else if (obj == markMode[GC_ACT]) mode = GC_ACT;
			else if (obj == markMode[GD_ACT]) mode = GD_ACT;
			else if (obj == markMode[GE_ACT]) mode = GE_ACT;
			else if (obj == markMode[GF_ACT]) mode = GF_ACT;
			else if (obj == markMode[GG_ACT]) mode = GG_ACT;
			else if (obj == markMode[GH_ACT]) mode = GH_ACT;
			activeMarkAction(mode);
			tj.requestRedrawAll();
		}	
		else if (obj == resolutionMode[N_RES] ||
			obj == resolutionMode[S_RES])
		{
			if (obj == resolutionMode[N_RES])
				activeResolutionAction(N_RES);
			else
				activeResolutionAction(S_RES);
		}
		else if (obj == LCAMark)
		{
			if(LCAMark.isSelected())			
			    for(int i=0; i<tj.treeDrawers.size(); i++)
			    tj.doLCAGeom(LCA_ACT, (AccordionTreeDrawerFinal)tj.treeDrawers.get(i));			
			else
			     tj.unmarkGroup(LCA_ACT);

			tj.requestRedrawAll();
			
		}
		else if (obj == LCAGroup)
		{
			System.out.println("LCAGroup: " + LCAGroup.isSelected());
		}
	}

	/**
	 * Set state of LCA marking checkbox {@link #LCAMark}.
	 * @param on Set to true if we want to mark objects in the LCA group color.
	 */
	protected void setLCAMark(boolean on) { LCAMark.setSelected(on); }
	/** Test to see if LCA mark checkbox {@link #LCAMark} is selected. 
	 * @return True if LCA objects are being selected for marking. */
	protected boolean getLCAMark() { return LCAMark.isSelected(); }
	/**
	 * Set state of LCA group checkbox {@link #LCAGroup}.
	 * @param on Set to true if we want to grow the LCA group with the bigger and smaller buttons.
	 */
	protected void setLCAGroup(boolean on) { LCAGroup.setSelected(on); }
	/** Test to see if LCA group checkbox {@link #LCAGroup} is selected. 
	 * @return True if LCA is being selected for grows. */
	protected boolean getLCAGroup() { return LCAGroup.isSelected(); }

	/** Mouse click handling on canvas objects (color swatches for marked groups).
	 * Opens a color manipulation dialog and redraws if a new color is chosen.
	 * @param evt Mouse click event, only detected on canvas objects {@link #markCanvas} or {@link #activeCanvas}.
	 * */
	public void mouseClicked(MouseEvent evt)
	{
		Object obj = evt.getSource();
		if (obj == activeCanvas[GA_ACT] || obj == markCanvas[GA_ACT] ||
			obj == activeCanvas[GB_ACT] || obj == markCanvas[GB_ACT] ||
			obj == activeCanvas[GC_ACT] || obj == markCanvas[GC_ACT] ||
			obj == activeCanvas[GD_ACT] || obj == markCanvas[GD_ACT] ||
			obj == activeCanvas[GE_ACT] || obj == markCanvas[GE_ACT] ||
			obj == activeCanvas[GF_ACT] || obj == markCanvas[GF_ACT] ||
			obj == activeCanvas[GG_ACT] || obj == markCanvas[GG_ACT] ||
			obj == activeCanvas[GH_ACT] || obj == markCanvas[GH_ACT] ||
			obj == activeCanvas[M_ACT] ||
			obj == activeCanvas[D_ACT] ||
			obj == activeCanvas[F_ACT] ||
		    obj == activeCanvas[LCA_ACT]) 
		{
			int group = GA_ACT;
//			int tjgroup = TreeJuxtaposer.markGroup[GA_ACT];
			if (obj == activeCanvas[GB_ACT] || obj == markCanvas[GB_ACT])
				{ group = GB_ACT; } //tjgroup = TreeJuxtaposer.markGroup[GB_ACT]; }
			else if (obj == activeCanvas[GC_ACT] || obj == markCanvas[GC_ACT])
				{ group = GC_ACT; }
			else if (obj == activeCanvas[GD_ACT] || obj == markCanvas[GD_ACT])
				{ group = GD_ACT; }
			else if (obj == activeCanvas[GE_ACT] || obj == markCanvas[GE_ACT])
				{ group = GE_ACT; }
			else if (obj == activeCanvas[GF_ACT] || obj == markCanvas[GF_ACT])
				{ group = GF_ACT; }
			else if (obj == activeCanvas[GG_ACT] || obj == markCanvas[GG_ACT])
				{ group = GG_ACT; }
			else if (obj == activeCanvas[GH_ACT] || obj == markCanvas[GH_ACT])
				{ group = GH_ACT; }
			else if (obj == activeCanvas[M_ACT])
				{ group = M_ACT; }
			else if (obj == activeCanvas[D_ACT])
				{ group = D_ACT; }
			else if (obj == activeCanvas[F_ACT])
				{ group = F_ACT; } 
			else if (obj == activeCanvas[LCA_ACT])
				{ group = LCA_ACT; } 	
			Color newColor = JColorChooser.showDialog(activePanel, colorTitle[group], color[group]);
			if (newColor != null)
			{
				color[group] = newColor;
				activeCanvas[group].setBackground(color[group]);
				if (group == GA_ACT || group == GB_ACT || group == GC_ACT || group == GD_ACT ||
						group == GE_ACT || group == GF_ACT || group == GG_ACT || group == GH_ACT)
					markCanvas[group].setBackground(color[group]);
				RangeList groupObject = tj.getGroupByID(group);
				groupObject.setColor(color[group]);
				tj.requestRedrawAll();
			}
		}
	}

	/**
     * Returns the group number that is supposed to grow when grow option (bigger/smaller) is pressed.
     * One of: user groups (A-H, or 1-8) {@link #GA_ACT}, {@link #GB_ACT}, {@link #GC_ACT}, {@link #GD_ACT}, {@link #GE_ACT}, {@link #GF_ACT}, {@link #GG_ACT}, {@link #GH_ACT},
     * or differences {@link #D_ACT}, mouseover {@link #M_ACT}, found items {@link #F_ACT}, or least common ancestor {@link #LCA_ACT}. 
     * @return A group number that corresponds to the active growing group.
     */
    public int getActionGroup()
    {
    	// check groups A through H
    	if (activeMode[GA_ACT].isSelected())
    		return GA_ACT;
    	else if (activeMode[GB_ACT].isSelected())
    		return GB_ACT;
    	else if (activeMode[GC_ACT].isSelected())
    		return GC_ACT;
    	else if (activeMode[GD_ACT].isSelected())
    		return GD_ACT;
    	else if (activeMode[GE_ACT].isSelected())
    		return GE_ACT;
    	else if (activeMode[GF_ACT].isSelected())
    		return GF_ACT;
    	else if (activeMode[GG_ACT].isSelected())
    		return GG_ACT;
    	else if (activeMode[GH_ACT].isSelected())
    		return GH_ACT;
    	// check Found nodes
    	else if (activeMode[F_ACT].isSelected())
    		return F_ACT;
    	// check Difference marks
    	else if (activeMode[D_ACT].isSelected())
    		return D_ACT;
    	// check Mouse-over
    	else if (activeMode[M_ACT].isSelected())
    		return M_ACT;
    	// check LCA marks
    	else if (activeMode[LCA_ACT].isSelected())
    		return LCA_ACT;

    	// default mouse-over flash drawing selected
    	return M_ACT;
    }
    
    /**
     * Return the number corresponding to the active mark group.
     * One of the user groups (A-H, or 1-8): 
     * {@link #GA_ACT}, {@link #GB_ACT}, {@link #GC_ACT}, {@link #GD_ACT}, {@link #GE_ACT}, {@link #GF_ACT}, {@link #GG_ACT}, {@link #GH_ACT}.
     * @return The current active group.
     */
    protected int getMarkGroup()
    {
    	return activeMark;
    }

    /**
     * Return true if growing horizontal, or in both directions.
     * @return False if only growing in vertical direction.
     */
    protected boolean growHorizontal()
    {
    	return growDirection != AccordionTreeDrawerFinal.VERT;
    }
    /**
     * Return true if growing vertical, or in both directions.
     * @return False if only growing in horizontal direction.
     */
    protected boolean growVertical()
    {
		return growDirection != AccordionTreeDrawerFinal.HORIZ;
    }
    
	/**
	 * Stub function.  Mouse listener used for detecting canvas (color swatch) clicks and this is another mouse function that the mouse listener requires.
	 * @param evt Mouse event object.
	 */
	public void mouseEntered(MouseEvent evt) {}
	/**
	 * Stub function.  Mouse listener used for detecting canvas (color swatch) clicks and this is another mouse function that the mouse listener requires.
	 * @param evt Mouse event object.
	 */
	public void mouseExited(MouseEvent evt) {}
	/**
	 * Stub function.  Mouse listener used for detecting canvas (color swatch) clicks and this is another mouse function that the mouse listener requires.
	 * @param evt Mouse event object.
	 */
	public void mousePressed(MouseEvent evt) {}
	/**
	 * Stub function.  Mouse listener used for detecting canvas (color swatch) clicks and this is another mouse function that the mouse listener requires.
	 * @param evt Mouse event object.
	 */
	public void mouseReleased(MouseEvent evt) {}
	
}
