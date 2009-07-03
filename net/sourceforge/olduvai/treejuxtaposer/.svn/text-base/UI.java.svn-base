/*
 * Created on 2003-10-22
 * User Interface part of code for TreeJuxtaposer 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package net.sourceforge.olduvai.treejuxtaposer;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.FileChooserUI;

import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;
import net.sourceforge.olduvai.treejuxtaposer.drawer.Tree;


import java.io.*;
import java.util.Iterator;

/**
 * User interface class that externalizes most of the menu setup for the top
 * menu bar, and the main implementation of the dialog to add new trees.
 * @author jeffrey 
 */

public class UI implements WindowListener, ActionListener
{
	/** Main drawing frame that has the menu bar at the top, and holds the drawing panel. */
	private Frame mainFrame;
	/** State frame for marking and active group manipulation. */
	private StateFrame stateFrame;
	/** Settings frame for options such as line width and font size. */
	private SettingsFrame settingsFrame;
	/** Incremental search frame. */
	private IncrementalSearch searchFrame;
	/** Debug frame. */
	private DebugFrame debugFrame;
	/** Drawing panel that hold canvas objects. */
	private Panel drawPanel;
	/** Layout for drawing panel. */
	private GridLayout drawLayout;
	/** Toolkit used to get screen dimensions. */
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	/** Read actual screen dimensions. */
	private Dimension screendim = toolkit.getScreenSize();

	/** Detector for initialization. */
//	private boolean firstTime = true;
	/** Current directory used for file loaders (saving and opening trees). */
	public File currDir = new File(System.getProperty("user.home"));
	
	/* Variables and constants for the menu bar */
	/** Menu bar object, to be later initialized with menu objects. */
	private MenuBar menuBar;
	/** List of all menu items, to be later initialized with constants. */
	private MenuItem menuItem[][];
	/** Font type for menu. */
	private static final String MENUFONTTYPE = "Helvetica";
	/** Font style for menu. */
	private static final int MENUFONTSTYLE = Font.BOLD;
	/** Font size for menu. */
	private static final int MENUFONTSIZE = 10;

	/** Top-level menu bar string constants. */
	private static final String menuHeader[] = { "File", "Find", "Tools", "Help" };
	/** Index for File menu. */
	private static final int FILEMENU = 0;
	/** Index for Find menu. */
	private static final int FINDMENU = 1;
	/** Index for Tools menu. */
	private static final int TOOLMENU = 2;
	/** Index for Help menu. */
	private static final int HELPMENU = 3;

	/**
	 * Array of top level menus: File, Find, Tool, Help.
	 */
	private static final Menu menu[] = { new Menu(menuHeader[FILEMENU]),
			new Menu(menuHeader[FINDMENU]), new Menu(menuHeader[TOOLMENU]),
			new Menu(menuHeader[HELPMENU]), };

	/* component-level menu bar constants
	 * string to describe the menu options, null is for a separator
	 * boolean to describe whether menu option is active
	 * integer for enumerating menu options
	 */

	/** Strings for File menu section. */
	private static final String fileMenu[] = { "Open", "Save", "Remove", null, "Quit" };
	/** Flags to indicate if menu options are active (true) or spaces (false). */
	private static final boolean fileActive[] = { true, true, true, false, true };
	/** Index for File->Open option. */
	private static final int OPENOPT = 0;
	/** Index for File->Save option. */
	private static final int SAVEOPT = 1;
	/** Index for File->Remove option. */
	private static final int REMOVEOPT = 2;
	/** Index for File->Quit option. */
	private static final int QUITOPT = 4;

	/** Strings for Find menu section. */
	private static final String findMenu[] = { "Find" };
	/** Flags to indicate if menu options are active (true) or spaces (false). */
	private static final boolean findActive[] = { true };
	/** Index for Find->Find option. */
	private static final int FINDOPT = 0;

	/** Strings for Tools menu section. */
//	private static final String toolMenu[] = { "Groups", "Settings", "Debug"}; // DEBUG VERSION
	private static final String toolMenu[] = { "Groups", "Settings"};
	/** Flags to indicate if menu options are active (true) or spaces (false). */
//	private static final boolean toolActive[] = { true, true, true}; DEBUG VERSION
	private static final boolean toolActive[] = { true, true, false};
	/** Index for Tools->Groups option. */
	private static final int GROUPOPT = 0;
	/** Index for Tools->Settings option. */
	private static final int SETTINGOPT = 1;
	/** Index for Tools->Debug option. */
	private static final int DEBUGOPT = 2;

	/** Strings for the help menu section. */
	private static final String helpMenu[] = { "Read Me" };
	/** Flags to indicate if menu options are active (true) or spaces (false). */
	private static final boolean helpActive[] = { true };
	/** Index for Help->Readme option. */
	private static final int ABOUTOPT = 0;

	// over-declared to make construction shorter
	/** Collection of all menu strings. Order defines real menu order in the bar. */
	private static final String allMenu[][] = { fileMenu, findMenu, toolMenu, helpMenu };
	/** Collection of sets of flags that define if a string is selectable or a space. */
	private static final boolean allActive[][] = { fileActive, findActive, toolActive,
			helpActive };
	/** Files that are selected for reading trees, used in the addTree function. */
	private File[] files;
	/** TreeJuxtaposer object. */
	private TreeJuxtaposer tj;

	/**
	 * Initializes the drawing surface and menu bar on the main frame.
	 * @param tj TreeJuxtaposer object.
	 * @param title Initial title for the main frame window.
	 */
	public UI(TreeJuxtaposer tj, String title) {
		this.tj = tj;

		mainFrame = new Frame(title);
		mainFrame.setLocation(10, 30);
		mainFrame.setLayout(new BorderLayout());

		drawPanel = new Panel();
		drawLayout = new GridLayout(1, 0, 8, 0);
		drawPanel.setLayout(drawLayout);

		mainFrame.add(drawPanel, BorderLayout.CENTER);

		mainFrame.addWindowListener(this);

		stateFrame = new StateFrame(tj);
		settingsFrame = new SettingsFrame(tj);
		searchFrame = new IncrementalSearch(tj);
		debugFrame = new DebugFrame(tj);
		addMenuBar();

		menuBar.setFont(new Font(MENUFONTTYPE, MENUFONTSTYLE, MENUFONTSIZE));
	}
	
	/**
	 * Action to perform when the window is closing.  Halts the program.
	 * @param event Window event.
	 */
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}

	/**
	 * Action to perform on a close operation.  Halts the program.
	 * @param event Window event.
	 */
	public void windowClosed(WindowEvent event) {
		System.exit(0);
	}

	/**
	 * Action to perform on an activation.  Currently does nothing.
	 * @param event Window event.
	 */
	public void windowActivated(WindowEvent event) {
//	    System.out.println("WIN activated");
	}

	/**
	 * Action to perform on an deiconification.  Requests a redraw.
	 * @param event Window event.
	 */
	public void windowDeiconified(WindowEvent event) {
		tj.requestRedrawAll();
	}

	/**
	 * Action to perform on an open.  Currently does nothing.
	 * @param event Window event.
	 */
	public void windowOpened(WindowEvent event) {
//		tj.requestRedrawAll();
//	    System.out.println("WIN opened");
	}

	/**
	 * Action to perform on an deactivation.  Currently does nothing.
	 * @param event Window event.
	 */
	public void windowDeactivated(WindowEvent event) {
//	    System.out.println("WIN deactivated");
	}

	/**
	 * Action to perform on an iconification.  Currently does nothing.
	 * @param event Window event.
	 */
	public void windowIconified(WindowEvent event) {
//	    System.out.println("WIN iconified");
	}

	/**
	 * Set up the menu bar, with all the items that are defined with the string
	 * externalization.
	 *
	 */
	private void addMenuBar()
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		menuBar = new MenuBar();
		menuItem = new MenuItem[menuHeader.length][];
		for (int i = 0; i < menuHeader.length; i++) {
			menuItem[i] = new MenuItem[allMenu[i].length];
			for (int j = 0; j < allMenu[i].length; j++) {
				if (allMenu[i][j] == null)
					menu[i].addSeparator();
				else
				{
					menuItem[i][j] = new MenuItem(allMenu[i][j]);
					menuItem[i][j].setEnabled(allActive[i][j]);
					menuItem[i][j].addActionListener(this);
					menu[i].add(menuItem[i][j]);
				}
			}
			menuBar.add(menu[i]);
		}
		mainFrame.setMenuBar((MenuBar) menuBar);
	}

	/**
	 * Return the main frame object for holding the drawing panel.  Used to hold
	 * the control panel, which does not exist anymore.
	 * @return The main frame object.
	 */
	protected Frame getMainFrame() {
		return mainFrame;
	}

	/**
	 * Return the drawing panel, which holds all the canvas objects that are the drawers.
	 * @return The drawing panel.
	 */
	protected Panel getDrawPanel() {
		return drawPanel;
	}

	/**
	 * Get the drawing layout manager.
	 * @return The drawing layout, for positioning new drawers on the canvas.
	 */
	protected GridLayout getDrawLayout() {
		return drawLayout;
	}

	/**
	 * Get the search panel frame {@link IncrementalSearch}.
	 * @return The search panel frame.
	 */
	protected IncrementalSearch getSearchFrame() {
		return searchFrame;
	}

	/**
	 * Get the settings frame {@link SettingsFrame}.
	 * @return The settings frame.
	 */
	protected SettingsFrame getSettingsFrame() {
		return settingsFrame;
	}

	/** Get the debug frame {@link DebugFrame}. 
	 * @return The debug frame.
	 * */
	protected DebugFrame getDebugFrame() {
		return debugFrame;
	}

	/** Get the state frame {@link StateFrame}.
	 * @return The state frame. */
	protected StateFrame getStateFrame() {
		return stateFrame;
	}

	/**
	 * Add a new tree action.
	 *
	 */
	protected void addAction() {
		//fc.setCurrentDirectory();
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory(currDir);
		int returnVal = fc.showOpenDialog(mainFrame);
		AccordionDrawer.loaded = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//				File file = fc.getSelectedFile();
			//				File[] files = fc.getSelectedFiles();
			currDir = fc.getCurrentDirectory();
			files = fc.getSelectedFiles();
			for (int i = 0; i < files.length; i++)
			{
				String s = null;
				try {
					s = files[i].getCanonicalPath();

					if (AccordionDrawer.debugOutput)
					    System.out.println("YZ File.getCanonicalPath() fname: " + s);
					tj.loadTree(s);
				} catch (FileNotFoundException ex) {
					System.out.println("File not found: " + s + " (" + ex + ")");
				}
				catch (IOException e)
				{
				    System.out.println("I/O Exception: " + s + " (" + e + ")");
				}
			}
		}
		AccordionDrawer.loaded = true;
		tj.mainFrame.setVisible(true);
	}

	/**
	 * Triggers a new tree removal panel {@link TreeRemovalPanel}.
	 *
	 */
	protected void removeAction() {
		TreeRemovalPanel trp = new TreeRemovalPanel(tj);
		trp.pack();
		trp.setLocation(500, 0);
		trp.setVisible(true);
	}
	
	/**
	 * Performs a save operation with the current trees.
	 *
	 */
	protected void saveAction(){
		JFileChooser filechooser = new JFileChooser();
		filechooser.setCurrentDirectory(currDir);
		
		filechooser.setMultiSelectionEnabled(false);
		int returnVal = filechooser.showSaveDialog(mainFrame);
		AccordionDrawer.loaded = false;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			currDir = filechooser.getCurrentDirectory();
			File saveFile = filechooser.getSelectedFile();
			File saveDirectory = saveFile.getParentFile();
			Iterator iter = tj.trees.iterator();
			
			String fileSep = System.getProperty("file.separator"); // "/" or "\"
			String extSep = ".";
			String saveName = saveFile.getAbsolutePath();
			
			String ext;
			String truncName;
			
			if (saveName.lastIndexOf(extSep) > -1)
			{
			  ext = saveFile.getName().substring(saveFile.getName().lastIndexOf(extSep)+1);
			  truncName = saveName.substring(saveName.lastIndexOf(fileSep)+1, saveName.lastIndexOf(extSep));
			}
			else
			{
				ext = "";
				truncName = saveName.substring(saveName.lastIndexOf(fileSep)+1);
			}
			
			int counter = 0;
			while (iter.hasNext())
			{
				Tree currTree = (Tree)iter.next();
				
				TreeWriter.writeTree(currTree, saveDirectory.getAbsolutePath() + 
						System.getProperty("file.separator") + 
						truncName + (counter>0? "_"+counter : "" ) + // "", "_1", "_2", etc 
						(ext.length() > 0 ? extSep : "") + ext);
						
				counter++;
			}
		}
		AccordionDrawer.loaded = true;
	}

	/**
	 * Kills the application with an exit. 
	 *
	 */
	protected void quitAction() {
		// add other actions if needed when the quit menu option is selected
		System.exit(0);
	}

	/**
	 * Menu listener.
	 * @param e Menu event, parsed and handled here.
	 */
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj == menuItem[FILEMENU][OPENOPT]) {
			tj.addAction();
			tj.requestRedrawAll();
		} else if (obj == menuItem[FILEMENU][SAVEOPT]) {
			saveAction();
			tj.requestRedrawAll();
		} else if (obj == menuItem[FILEMENU][REMOVEOPT]) {
			removeAction();
		} else if (obj == menuItem[FILEMENU][QUITOPT]) {
			tj.quitAction();
		} else if (obj == menuItem[FINDMENU][FINDOPT]) {
			searchFrame.setLocation(screendim.width - searchFrame.getWidth()
					- 10, 30);
			searchFrame.setVisible(true);
		} else if (obj == menuItem[TOOLMENU][GROUPOPT]) {
			if (mainFrame.getLocation().y + mainFrame.getHeight() + 30
					+ stateFrame.getHeight() < screendim.height)
				stateFrame.setLocation(10, mainFrame.getLocation().y
						+ mainFrame.getHeight() + 30);
			else
				stateFrame.setLocation(10, screendim.height
						- stateFrame.getHeight());
			stateFrame.setVisible(true);
		} else if (obj == menuItem[TOOLMENU][SETTINGOPT]) {
			settingsFrame.prepareToShow();
			if (stateFrame.getLocation().y == 30) // default loaction for stateFrame
				settingsFrame.setLocation(10, screendim.height
						- settingsFrame.getHeight() - 30);
			else if ((screendim.height < mainFrame.getHeight()
					+ stateFrame.getHeight() + settingsFrame.getHeight())
					|| (screendim.height < stateFrame.getLocation().y
							+ stateFrame.getHeight()
							+ settingsFrame.getHeight()))

				settingsFrame.setLocation(20 + stateFrame.getWidth(),
						stateFrame.getLocation().y);

			else
				settingsFrame.setLocation(10, stateFrame.getLocation().y
						+ stateFrame.getHeight());

			settingsFrame.setVisible(true);
		} else if (menuItem[TOOLMENU].length > 2 && obj == menuItem[TOOLMENU][DEBUGOPT]) {
			if (settingsFrame.getLocation().y == 30)
				debugFrame.setLocation(10 + settingsFrame.getWidth(),
						screendim.height - debugFrame.getHeight() - 30);

			else if (screendim.width < settingsFrame.getLocation().x
					+ settingsFrame.getWidth() + debugFrame.getWidth())

				debugFrame.setLocation(settingsFrame.getLocation().x,
						settingsFrame.getLocation().y - debugFrame.getHeight());

			else
				debugFrame.setLocation(10 + settingsFrame.getLocation().x
						+ settingsFrame.getWidth(),
						settingsFrame.getLocation().y);

			debugFrame.setVisible(true);
		} else if (obj == menuItem[HELPMENU][ABOUTOPT]) {
			Help.showAboutFrame();
		} else
			System.out.println("unknown action performed: " + obj);
	}

}