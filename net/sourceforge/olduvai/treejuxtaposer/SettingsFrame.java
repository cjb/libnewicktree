/*
 * Created on 12-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.olduvai.treejuxtaposer;

import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;
import net.sourceforge.olduvai.treejuxtaposer.drawer.RangeList;



/**
 * GUI for settings.  This class is heavily parameterized to support changes and additional settings options that may be added later.
 * @author jslack
 *
 */
public class SettingsFrame extends JFrame implements ActionListener, ChangeListener {

	/** Treejuxtaposer object for referencing state and all drawers. */
	private TreeJuxtaposer tj;
	/** Title for this frame. */
	private final static String title = "Settings";
	/** Objects on the left side of the GUI. Sliders, font sizes, check boxes for linked navigation and progressive rendering. */
	private JPanel leftPanel;
	/** Objects on the right side of the GUI. Check boxes for diffs, labels, dimming, and snapshot button. */
	private JPanel rightPanel;

	/** Slider for line width.  Also changes the block size. */
	private JSlider linewidthSlider;
	/** Label for {@link #linewidthSlider}. */
	private final static JLabel linewidthLabel = new JLabel("Line Width");
	/** Slider for label density. */
	private JSlider labelDensitySlider;
	/** Label for {@link #labelDensitySlider}. */
	private final static JLabel labelDensityLabel = new JLabel("Label Density");
	/** Maximum value for label density. */
	private int labelDensityMax = 100;
	/** Slider for controling the BCN threshold for differences. */
	private JSlider bcnScoreSlider;
	/** Label for {@link #bcnScoreSlider}. */
	private final static JLabel bcnScoreLabel = new JLabel("BCN Score");
//	/** Slider for controling the angle of labels.  Not implemented currently. */
//	private JSlider labelAngleSlider;
//	/** Label for {@link #labelAngleSlider}. */
//	private final static JLabel labelAngleLabel = new JLabel("Label Angle");
	/** Main label for font size control. */
	private final static JLabel fontLabel = new JLabel("Fonts");
	/** Min/Max labels for font size controls. */
	private final static String fontString[] = {"Minimum", "Maximum"};
	/** Text labels for font strings {@link #fontString}. */
	private JTextField fontEntry[];
	/** Buttons for font size control (smaller/bigger min/max). */
	private JButton fontButton[];
	/** Index for Min font.  OR by {@link #LEFT_MASK} or {@link #RIGHT_MASK} to get smaller or bigger button. */
	private final static int MIN_FONT = 0;
	/** Index for Max font.  OR by {@link #LEFT_MASK} or {@link #RIGHT_MASK} to get smaller or bigger button. */
	private final static int MAX_FONT = 1;
	/** Mask to OR by to access left side labels for fonts. */
	private final static int LEFT_MASK = 0;
	/** Mask to OR by to access right side labels for fonts. */
	private final static int RIGHT_MASK = 2;
	
	/** Minimum legal font size. */
	private final static int MIN_FONT_THRESHOLD = 5;
	/** Maximum legal font size. */
	private final static int MAX_FONT_THRESHOLD = 50;
	/** Checkbox for linked navigation. */
	public JCheckBox linkCheck;
	/** String for {@link #linkCheck} checkbox. */
	private final static String linkString = "Linked Navigation";
	/** Title for toggle boxes in {@link #showCheck}. */
	private final static JLabel showLabel = new JLabel("Show");
	/** Toggle boxes for diffs and labels. */
	private JCheckBox showCheck[];
	/** Strings for checkbox {@link #showCheck}. */
	private final static String showString[] = {"Differences", "Labels"};
	/** Index for diffs in {@link #showString}*/
	private final static int SHOW_DIFF = 0;
	/** Index for labels string in {@link #showString}. */
	private final static int SHOW_LABEL = 1;
	
	/** Dimming label title for both check boxes. */
	private final static JLabel dimLabel = new JLabel("Dimming");
	/** Dimming check boxes. */
	private JCheckBox dimCheck[];
	/** Dimming strings for {@link #dimCheck} checkbox. */
	private final static String dimString[] = {"Marked", "Unmarked"};
	/** Index of Marked dimming option for dimming strings in {@link #dimString}. */
	private final static int DIM_MARK = 0;
	/** Index of Unmarked dimming option for dimming strings in {@link #dimString}. */
	private final static int DIM_UNMARK = 1;
	
	/** Button for taking snapshots */
	private JButton snapshotButton;
	/** String for snapshot label */
	private final static String snapshotLabel = "EPS Snapshot"; 
	/** Checkbox for progressive rendering toggle */
	private JCheckBox progressiveOn;
	/** String for progressive rendering toggle */
	private final static String toggleProgRenderString = "Progressive Rendering";
	/**
	 * Constructor.  Calls {@link #initComponents()} to create GUI.  
	 * @throws java.awt.HeadlessException
	 */
	public SettingsFrame(TreeJuxtaposer tj)  {
		super();
		this.tj = tj;
		this.setResizable(false);
		initComponents();
	}

	/**
	 * Initialize components and set up GUI.
	 * This is only run as part of the constructor, which sets up the main state variables for this class.
	 */
	private void initComponents()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		this.setTitle(title);
		((JFrame)this).setLocation(10,30);
		this.getContentPane().setLayout(new GridBagLayout());
		
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridy = 0;
		
		leftPanel = new JPanel(new GridBagLayout());
		linewidthSlider = new JSlider(JSlider.HORIZONTAL,1,4,(int)tj.linethickness);
		linewidthSlider.setPreferredSize(new Dimension(80, linewidthSlider.getPreferredSize().height * 2));
		linewidthSlider.setMajorTickSpacing(1);
		linewidthSlider.setSnapToTicks(true);
		linewidthSlider.setPaintTicks(true);
		linewidthSlider.addChangeListener(this);
		
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		leftPanel.add(linewidthSlider, gbc);
		gbc.gridy = 1;
		leftPanel.add(linewidthLabel, gbc);
		
		labelDensitySlider = new JSlider(JSlider.HORIZONTAL,0,labelDensityMax,90);
		labelDensitySlider.setPreferredSize(new Dimension(150, labelDensitySlider.getPreferredSize().height * 2));
		labelDensitySlider.setMajorTickSpacing(25);
		labelDensitySlider.setSnapToTicks(false);
		labelDensitySlider.setPaintTicks(true);
		labelDensitySlider.addChangeListener(this);
		
// best corresponding score slider		
		bcnScoreSlider = new JSlider(JSlider.HORIZONTAL,0,10,10);
       bcnScoreSlider.setPreferredSize(new Dimension(100, bcnScoreSlider.getPreferredSize().height * 2));
       bcnScoreSlider.setMajorTickSpacing( 1);
       bcnScoreSlider.setSnapToTicks(false);
       bcnScoreSlider.setPaintTicks(true);
       bcnScoreSlider.addChangeListener(this);
       bcnScoreSlider.setInverted(true);
		
//	   labelAngleSlider = new JSlider(JSlider.HORIZONTAL,0,60,30);
//	   labelAngleSlider.setPreferredSize(new Dimension(100, labelAngleSlider.getPreferredSize().height * 2));
//	   labelAngleSlider.setMajorTickSpacing( 15);
//	   labelAngleSlider.setSnapToTicks(false);
//	   labelAngleSlider.setPaintTicks(true);
//	   labelAngleSlider.addChangeListener(this);
		
		gbc.ipady = 0;
		gbc.gridx = 4;
		gbc.gridwidth = 4;
		gbc.gridy = 0;
		leftPanel.add(labelDensitySlider, gbc);
		gbc.gridy = 1;
		leftPanel.add(labelDensityLabel, gbc);
		
        gbc.gridx=0;
        gbc.gridy=3;
        gbc.gridwidth=2;
   
        leftPanel.add(bcnScoreSlider, gbc);
        gbc.gridy=4;
        leftPanel.add(bcnScoreLabel, gbc);
		
		gbc.gridx=4;
		gbc.gridy=3;
		gbc.gridwidth=2;
   
//		leftPanel.add(labelAngleSlider, gbc);
//		gbc.gridy=4;
//		leftPanel.add(labelAngleLabel, gbc);
		
		gbc.gridy=4;
		snapshotButton = new JButton(snapshotLabel);
		snapshotButton.addActionListener(this);
		leftPanel.add(snapshotButton, gbc);

		gbc.gridy = 5;
		gbc.ipady = 10;
		leftPanel.add(new JPanel(), gbc);
		gbc.ipady = 0;
		gbc.gridy = 6;
		gbc.gridx = 0;
		gbc.gridwidth = 7;
		leftPanel.add(fontLabel, gbc);
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		fontButton = new JButton[fontString.length * 2];
		fontEntry = new JTextField[fontString.length];
		for (int i = 0; i < fontString.length; i++)
		{
			fontButton[i | LEFT_MASK] = new JButton("<");

			fontButton[i | RIGHT_MASK] = new JButton(">");
			fontEntry[i] = new JTextField(3);
			gbc.gridx = (i * 4);
			leftPanel.add(fontButton[i | LEFT_MASK], gbc);
			gbc.gridx += 1;
			leftPanel.add(fontEntry[i], gbc);
			gbc.gridy += 1;
			gbc.gridx -= 1;
			gbc.gridwidth = 3;
			leftPanel.add(new JLabel(fontString[i]), gbc);
			gbc.gridwidth = 1;
			gbc.gridy -= 1;
			gbc.gridx += 2;
			leftPanel.add(fontButton[i | RIGHT_MASK], gbc);
			gbc.ipadx = 10;
			gbc.gridx += 1;
			if (i < fontString.length - 1)
				leftPanel.add(new JPanel(), gbc);
			gbc.ipadx = 0;
		}

		fontButton[MIN_FONT | LEFT_MASK].addActionListener(this);
		fontButton[MIN_FONT | RIGHT_MASK].addActionListener(this);
		fontButton[MAX_FONT | LEFT_MASK].addActionListener(this);
		fontButton[MAX_FONT | RIGHT_MASK].addActionListener(this);

				
		gbc.gridx = 0;
		gbc.gridwidth = 7;
		gbc.gridy++;
		JPanel linkCheckSpacer = new JPanel();
		linkCheckSpacer.setPreferredSize(new Dimension(1, 40));
		leftPanel.add(linkCheckSpacer, gbc);
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy++;
		linkCheck = new JCheckBox(linkString);
		linkCheck.addActionListener(this);
		leftPanel.add(linkCheck, gbc);
		
		
		gbc.gridwidth = 4;
		gbc.gridx+=3;
		progressiveOn = new JCheckBox(toggleProgRenderString, true);
		progressiveOn.addActionListener(this);
		leftPanel.add(progressiveOn, gbc);
		
		gbc.gridwidth = 1;
		
		
		
		
		JPanel middlePanel = new JPanel();
		middlePanel.setPreferredSize(new Dimension(20, 1));
		
		rightPanel = new JPanel(new GridBagLayout());
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		rightPanel.add(showLabel, gbc);
		gbc.gridy++;
		showCheck = new JCheckBox[showString.length];
		for (int i = 0; i < showString.length; i++)
		{
			showCheck[i] = new JCheckBox(showString[i]);
			showCheck[i].addActionListener(this);
			showCheck[i].setSelected(true); // default show differences and labels
			rightPanel.add(showCheck[i], gbc);
			gbc.gridy++;
		}
		
		JPanel checkBoxSpacer = new JPanel();
		checkBoxSpacer.setPreferredSize(new Dimension(1, 10));
		rightPanel.add(checkBoxSpacer, gbc);
		gbc.gridy++;
		
		rightPanel.add(dimLabel, gbc);
		gbc.gridy++;
		dimCheck = new JCheckBox[dimString.length];
		for (int i = 0; i < dimString.length; i++)
		{
			dimCheck[i] = new JCheckBox(dimString[i]);
			dimCheck[i].addActionListener(this);
			rightPanel.add(dimCheck[i], gbc);
			gbc.gridy++;
		}
		
		rightPanel.add(new JPanel(), gbc); // spacer
		gbc.gridy++;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		getContentPane().add(leftPanel, gbc);
		
		// for spacing
		gbc.gridx = 1;
		gbc.gridy = 0;
		getContentPane().add(middlePanel, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		getContentPane().add(rightPanel, gbc);
				
		pack();
	}

	/**
	 * Other state changes not detectable by {@link #actionPerformed(ActionEvent)}, such as slider movement.
	 * @param evt State change event.
	 */
	public void stateChanged(ChangeEvent evt)
	{
		Object obj = evt.getSource();
		if (obj == linewidthSlider)
		{
			int linewidth = linewidthSlider.getValue();
			Iterator tdIter = tj.treeDrawers.iterator();
			while (tdIter.hasNext()) {
				AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tdIter.next();
				atd.setLineThickness(linewidth);
				atd.newPixelDiv(linewidth, AccordionTreeDrawerFinal.Y);
				atd.newPixelDiv(linewidth, AccordionTreeDrawerFinal.X);
				atd.requestRedraw();
			}
		}
		else if (obj == labelDensitySlider)
		{
			
			float labelBuffer = labelDensityMax-(float)labelDensitySlider.getValue();
//			System.out.println("changing label density to " + labelBuffer);
			Iterator tdIter = tj.treeDrawers.iterator();
			while (tdIter.hasNext()) {
				AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tdIter.next();
				atd.setLabelBuffer((int)labelBuffer);
				atd.requestRedraw();
			}
		}
		else if (obj == bcnScoreSlider)
		{
			float bcnScore = (float)bcnScoreSlider.getValue();
			tj.setBcnScore(bcnScore/10);
            tj.clearGroup(StateFrame.D_ACT);
            for (int i = 0; i < tj.treeDrawers.size()-1; i++) {
			AccordionTreeDrawerFinal atdA = (AccordionTreeDrawerFinal)tj.treeDrawers.get(i);
			AccordionTreeDrawerFinal atdB = (AccordionTreeDrawerFinal)tj.treeDrawers.get(TreeJuxtaposer.treeCount-1);
			tj.doStructuralDiff(atdA, atdB, StateFrame.D_ACT);
			tj.doStructuralDiff(atdB, atdA, StateFrame.D_ACT);
					tj.requestRedrawAll();
				
			} 
				
			} 
//		else if(obj == labelAngleSlider)
//	     	{
//				float angle = (float)labelAngleSlider.getValue();
//				if(angle <=10.0) angle = 10.0f;
//				Iterator tdIter = tj.treeDrawers.iterator();
//				while (tdIter.hasNext()) {
//						AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tdIter.next();
//				    
//						atd.requestRedraw();
//					}
//	    	}
	}

	/**
	 * Update state of UI after a user event.
	 * @param evt The user event.
	 */
	public void actionPerformed(ActionEvent evt)
	{
		Object obj = evt.getSource();
		AccordionTreeDrawerFinal atd = null;
		if (tj.treeDrawers != null && tj.treeDrawers.size() > 0)
			atd = (AccordionTreeDrawerFinal)tj.treeDrawers.get(0);
			
		if (obj == fontButton[MIN_FONT | LEFT_MASK]) {
			if (atd == null) return;
			if (atd.minFontHeight > MIN_FONT_THRESHOLD)
				tj.decreaseMinFontHeight();
			fontEntry[MIN_FONT].setText(new Integer(atd.minFontHeight).toString());
		}
		else if (obj == fontButton[MAX_FONT | RIGHT_MASK]) {
			if (atd == null) return;
			if (atd.maxFontHeight < MAX_FONT_THRESHOLD)
				tj.increaseMaxFontHeight();
			fontEntry[MAX_FONT].setText(new Integer(atd.maxFontHeight).toString());
		}
		else if (obj == fontButton[MIN_FONT | RIGHT_MASK]) {
			if (atd == null) return;
			if (atd.minFontHeight < atd.maxFontHeight)
				tj.increaseMinFontHeight();
			fontEntry[MIN_FONT].setText(new Integer(atd.minFontHeight).toString());
		}
		else if (obj == fontButton[MAX_FONT | LEFT_MASK]) {
			if (atd == null) return;
			if (atd.maxFontHeight > atd.minFontHeight)
				tj.decreaseMaxFontHeight();
			fontEntry[MAX_FONT].setText(new Integer(atd.maxFontHeight).toString());
		}
		else if (obj == linkCheck)
		{
			tj.setLinkedNav(linkCheck.isSelected());
		}
		else if (obj == dimCheck[DIM_MARK])
		{
			tj.setDimColors(dimCheck[DIM_MARK].isSelected());
		}
		else if (obj == dimCheck[DIM_UNMARK])
		{
			tj.setDimBrite(dimCheck[DIM_UNMARK].isSelected());
		}
		else if (obj == showCheck[SHOW_DIFF])
		{ // does a redraw call, not redundant
			setStructDiff(showCheck[SHOW_DIFF].isSelected());
		}
		else if (obj == showCheck[SHOW_LABEL])
		{ // does a redraw
			setLabelsOn(showCheck[SHOW_LABEL].isSelected());
		}
		else if (obj == snapshotButton)
		{
			AccordionDrawer.doSnapshot(tj.treeDrawers);
		}
		else if (obj == progressiveOn)
		{
			tj.setProgressiveOn(progressiveOn.isSelected());
		}
		else
		{
			System.out.println("Unknown event: " + obj);
		}
	}
	
	/**
	 * Post process updates of UI after a user action on the settings.
	 * Updates font sizes (min/max), linked navigation state, and dimming (marked/unmarked).
	 * Other state updates have been migrated to act on the state of the widgets, not using TJ for holding state at all.
	 */
	public void prepareToShow()
	{
		if (tj != null && 
				tj.treeDrawers != null &&
				tj.treeDrawers.size() > 0)
			{
				AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tj.treeDrawers.get(0);
				fontEntry[MIN_FONT].setText(new Integer(atd.minFontHeight).toString());
				fontEntry[MAX_FONT].setText(new Integer(atd.maxFontHeight).toString());
				linkCheck.setSelected(tj.getLinkedNav());
				dimCheck[DIM_MARK].setSelected(tj.getDimColors());
				dimCheck[DIM_UNMARK].setSelected(tj.getDimBrite());	
			}
	}
	
	/**
	 * Activate/deactivate state of drawing diffs.
	 * @param on If true, diffs will be highlighted.  False does not highlight diffs.
	 */	
	public void setStructDiff(boolean on)
	{
		boolean previouslyOn = showCheck[SHOW_DIFF].isSelected();
		showCheck[SHOW_DIFF].setSelected(on);
		RangeList diffGroup = tj.getGroupByID(StateFrame.D_ACT);
		diffGroup.setEnabled(on);
//		if (!previouslyOn && on)
		{
			tj.requestRedrawAll();
		}
	}
	/**
	 * Flag check for testing if diffs are on. Replaces state machine.
	 * @return State of diff flag, {@link #showCheck}[{@link #SHOW_DIFF}].
	 */
	public boolean isDiffOn()
	{
		return showCheck[SHOW_DIFF].isSelected();
	}
	
	/**
	 * Activate/deactivate state of drawing labels.
	 * @param on If true, labels will be drawn.  False does not draw labels.
	 */
	public void setLabelsOn(boolean on)
	{
		tj.setDrawLabels(on);
		boolean previouslyOn = showCheck[SHOW_DIFF].isSelected();
		showCheck[SHOW_LABEL].setSelected(on);
//		if(!previouslyOn && on)
		{
			tj.requestRedrawAll();
		}
	}
	
	/**
	 * Activate/deactivate state of linked navigation.
	 * Does not perform redraw.
	 * @param on If true, do linked navigation with quasi-mode box dragging.
	 * False does not do linked navigation.
	 */
	public void setLinkedNavigation(boolean on)
	{
		tj.setLinkedNav(on);
		linkCheck.setSelected(on);
	}

	/**
	 * Flag check for testing if labels are on. Replaces state machine.
	 * @return State of label flag, {@link #showCheck}[{@link #SHOW_LABEL}].
	 */
	public boolean areLabelsOn()
	{
		return showCheck[SHOW_LABEL].isSelected();
	}

}
