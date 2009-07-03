/*
 * Created on 12-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.olduvai.treejuxtaposer;

import java.awt.GraphicsConfiguration;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;


/**
 * Debugging class.  The debugging window isn't supported by releases, and access to this
 * class is intended to be removed from the menu panel for releases.  This window displays some values of interest, usually
 * for mouse-over, some other state as well. The chunk size label for controlling block size is
 * currently found on this window, but shouldn't be required for general purpose use.
 * @author jslack
 */
public class DebugFrame extends JFrame implements ChangeListener {

	private TreeJuxtaposer tj;
	static final private String resultType[] = {"Active Group",
		"Action Mode",
		"Selection Resolution",
		"Grow Direction",
		"Current Mark",
		"Navigation",
		"BCN Score",
		"Depth Test",
		"Show Tree",
		"Show Grid"
		};
	static final public int ACT_TYPE = 0;
	static final public int ACT_MODE = 1;
	static final public int ACT_SELECTION = 2;
	static final public int ACT_GROWDIR = 3;
	static final public int M_GROUP = 4;
	static final public int NAV_TYPE = 5;
	static final public int BCN_SCORE = 6;
	static final public int DEPTH_TEST = 7;
	static final public int SHOW_TREE = 8;
	static final public int SHOW_GRID = 9;
	public JTextField result[];
	private JLabel resultLabel[];
	
	static private JSlider chunkSizeForDrawing;
	static private String chunkSizeString = "chunks: ";
	static private JLabel chunkSizeLabel;
	
	// store the state here?
	static public JCheckBox showGrid;
	static public JCheckBox showTree;
	
	/* assume action types (strings and integer indices) provided in StateFrame */
	
	/* action mode section */
	static final public String actionModeLabel[] = {"MOUSEOVER",
		"ST_FREEMOVE",
		"ST_FREEMOVEAGAIN",
		"ST_RESHAPE",
		"RECT_CREATE",
		"RECT_FREEMOVE",
		"RECT_FREEMOVEAGAIN",
		"RECT_RESHAPE",
		null,
		null,
		null,
		"MOVE_STUCKPOS"
	};
	/* integer indices for action mode strings provided in AccordionTreeDrawerFinal */
	
	/* action target section
	 * this is split into grow direction (the first 3 items)
	 * and selection resolution (the last 2 items)
	 */
	static final public String actionTargetLabel[] = {"Horizontal",
		"Vertical",
		"Horizontal and Vertical",
		"Node",
		"Subtree"
	};
	static final int H_TARGET = 0;
	static final int V_TARGET = 1;
	static final int B_TARGET = 2;
	static final int NODE_TARGET = 3;
	static final int SUBTREE_TARGET = 4;
	
	/* assume mark group (strings and integer indices) provided in StateFrame */

	/**
	 * @throws java.awt.HeadlessException
	 */
	public DebugFrame(TreeJuxtaposer tj)  {
		super();
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	/**
	 * @param gc
	 */
	public DebugFrame(TreeJuxtaposer tj, GraphicsConfiguration gc) {
		super(gc);
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	/**
	 * @param title
	 * @throws java.awt.HeadlessException
	 */
	public DebugFrame(TreeJuxtaposer tj, String title)  {
		super(title);
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	/**
	 * @param title
	 * @param gc
	 */
	public DebugFrame(TreeJuxtaposer tj, String title, GraphicsConfiguration gc) {
		super(title, gc);
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	private void initComponents()
	{
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		result = new JTextField[resultType.length];
		resultLabel = new JLabel[resultType.length];
		for (int i = 0; i < resultType.length; i++)
		{
			gbc.gridx = 0;
			gbc.gridy = i;
			resultLabel[i] = new JLabel(resultType[i]);
			getContentPane().add(resultLabel[i], gbc);
			
			gbc.gridx = 1;
			result[i] = new JTextField(25);
			result[i].setEditable(false);
			getContentPane().add(result[i], gbc);
		}
		gbc.gridx = 2;
		
		gbc.gridy = SHOW_TREE;
		showTree = new JCheckBox();
		showTree.setSelected(true);
		showTree.addChangeListener(this);
		getContentPane().add(showTree, gbc);
		
		gbc.gridy = SHOW_GRID;
		showGrid = new JCheckBox();
		showGrid.setSelected(false);
		showGrid.addChangeListener(this);
		getContentPane().add(showGrid, gbc);
		
		gbc.gridx = 1;
		gbc.gridy++;
		chunkSizeForDrawing = new JSlider(1, AccordionDrawer.dequeueChunkPerTimeCheck * 5, AccordionDrawer.dequeueChunkPerTimeCheck);
//		chunkSizeForDrawing.setName(chunkSizeString+chunkSizeForDrawing.getValue());
		chunkSizeForDrawing.addChangeListener(this);
		getContentPane().add(chunkSizeForDrawing, gbc);
		gbc.gridx = 0;
		chunkSizeLabel = new JLabel(chunkSizeString + chunkSizeForDrawing.getValue());
		getContentPane().add(chunkSizeLabel, gbc);
		// no drawers yet, can't call this here
		//	tj.observe();
		
		pack();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		Object obj = e.getSource();
		if (obj == chunkSizeForDrawing)
		{
			int value = chunkSizeForDrawing.getValue();
			AccordionDrawer.dequeueChunkPerTimeCheck = value;
			chunkSizeLabel.setText(chunkSizeString + value);
		}
		else if (obj == showTree)
		{
			tj.setDrawGeoms(showTree.isSelected());
		}
		else if (obj == showGrid)
		{
			tj.setDrawGrid(showGrid.isSelected());
		}
		
	}
	
}
