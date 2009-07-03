package net.sourceforge.olduvai.treejuxtaposer;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import net.sourceforge.olduvai.treejuxtaposer.drawer.*;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * Incremental search functionality and GUI class.  Typing letter by letter will search
 * a cache of results and display matches.  The cache grows over time without bound.
 * @author jslack
 *
 */
public class IncrementalSearch extends JFrame 
	implements KeyListener, ListSelectionListener, ActionListener  {

	/** Initial text in comment box {@link #searchStatus}. */
	private static String initialText = "No nodes selected";
	/** Pane for picking results that can be scrolled. */
	private JScrollPane resultsPane;
	/** List of results that can be picked that is put into the text field {@link #results}. */
	private JList searchResults;
	/** Pane for the search status {@link #searchStatus}. */
	private JScrollPane statusPane;
	/** Comment box that displays the number of things that match a user selection. */
	private JTextArea searchStatus;
	/** Text that is put into the results pane {@link #resultsPane}. */
	private JTextField results;
	/** Cache of results, indexed by substrings of previously found. */
	private Hashtable prefix;
	/** The TJ for this set of found results, for calling updates to the drawers. */
	private TreeJuxtaposer tj;
	/** The number of items in the list of results {@link #results}. */
	private int numFound;
	
	/**
	 * Reset button
	 */
	private JButton resetButton;

	/**  Highlight search results found if under (or equal to) this threshold */
	private static final int returnThreshold = (int)10e6;

	/**
	 * Initialize the search box with default values.
	 * @param tj The TJ for this search box.
	 */
	public IncrementalSearch(TreeJuxtaposer tj)
	{
		this.tj = tj;
		doUI();
		initializeList(null);
		results.setText("");
	}

	/**
	 * Initialize the search box with a list of names.
	 * @param tj The TJ for this search box.
	 * @param initialList A list of names to use with search.  Used mostly for testing.
	 */
	public IncrementalSearch(TreeJuxtaposer tj, ArrayList initialList)
	{
		this.tj = tj;
		doUI();
		initializeList(initialList);
		results.setText("");
	}

	/**
	 * Set up the user interface.
	 */
	private void doUI()
	{
		GridBagConstraints gbc;
		getContentPane().setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		results = new JTextField();
		gbc = new GridBagConstraints(0,0,1,1,1.0,1.0,
				GridBagConstraints.NORTH,
				GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0,0);
		results.setEditable(true);
		searchResults = new JList();
		resultsPane = new JScrollPane(searchResults);
		resultsPane.setMinimumSize(new Dimension(250, 300));
		resultsPane.setPreferredSize(new Dimension(250, 500));
		resultsPane.setMaximumSize(new Dimension(1024, 1600));
		searchStatus = new JTextArea(1, 23);
		searchStatus.setEditable(false);
		searchStatus.setBackground(this.getBackground());
		searchStatus.setText(initialText);
		statusPane = new JScrollPane(searchStatus);
		getContentPane().add(resultsPane, gbc);
		gbc = new GridBagConstraints(0,1,1,1,1.0,0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0,0);

		results.setMinimumSize(new Dimension(250, 24));
		results.setPreferredSize(new Dimension(250, 24));
		results.setMaximumSize(new Dimension(1024, 24));
		getContentPane().add(results, gbc);
		gbc = new GridBagConstraints(0,2,1,1,1.0,1.0,
				GridBagConstraints.SOUTH,
				GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0,0);		
		getContentPane().add(statusPane, gbc);
		results.addKeyListener(this);
		this.setResizable(true);
		resultsPane.revalidate();
		searchResults.addListSelectionListener(this);
		
		gbc = new GridBagConstraints(0,3,1,1,1.0,1.0,
				GridBagConstraints.SOUTH,
				GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0,0);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		
		getContentPane().add(resetButton, gbc);
		this.pack();
	}

	/**
	 * Initialize nodes in the search list if the input is not null.
	 * @param nameList List of names to put into the search dialog.  If this is null, do nothing.
	 * TJ ({@link TreeJuxtaposer#addNamesToSearchList()}) will call this function again and set this list to the list of named nodes after laying out the tree. 
	 */
	public void initializeList(ArrayList nameList)
	{
		prefix = new Hashtable();
		if (nameList == null)
		{
//			System.out.println("Remember to call IncrementalSearch.initializeList");
			return;
		}
		else
		{
			prefix.put("", nameList);
			searchResults.setListData(nameList.toArray());
			numFound = nameList.size();
		}
	}

	/** Set to true for just matching prefixes, false for matching anywhere in the string. */
	private static boolean matchPrefixOnly = false; 
	
	/**
	 * Search hashtable ({@link #prefix}) for the given text.  If not in the hashtable, use result of
	 * string except for last character and reduce that list with the full text input.
	 * Recursion is done first, so all prefixes of the text should be in the hash table.
	 * Called by 
	 * @param text Text string to search for in the hash, to recover a list of matches.
	 * @return List of matching strings for the given text input.
	 */
	private ArrayList recursiveSearch(String text)
	{
		if (text.length() == 0)
		{
			return (ArrayList)prefix.get(text); // full list for empty string
		}
		String smaller = text.substring(0, text.length() - 1);
		ArrayList returnValue = (ArrayList)prefix.get(text);
		if (returnValue != null && returnValue.size() == 0) // prefix had no matches, so full text will not either
			return returnValue;
		if (returnValue == null)
		{
			ArrayList temp = recursiveSearch(smaller);
			returnValue = new ArrayList();
			int textLength = text.length();
			for (int i = 0; i < temp.size(); i++)
			{
				String FQName = (String)temp.get(i);
				String tempString = tj.getLabelByFQName(FQName);
				
				if (matchPrefixOnly)
				{
					if (tempString.length() >= textLength &&
							tempString.toLowerCase().startsWith(text))
					{
						returnValue.add(FQName);
					}
				}
				else
				{
					if (tempString == null)
						System.out.println("debug");
					if (tempString.length() >= textLength &&
							tempString.toLowerCase().lastIndexOf(text) != -1)
					{
						returnValue.add(FQName); // add TreeNode to returnValue
					}
				}
			}
			// add to the hashtable here
			prefix.put(text, returnValue);
		}
		return returnValue;
	}

	/**
	 * Check cache for previous results that match text input.  If the text has no
	 * match, call {@link #recursiveSearch(String)}, which will fill in the cache to
	 * get a correct result.  This is called by the {@link #keyReleased(KeyEvent)} event handler.
	 * @return List of items from the cache (cache is updated with {@link #recursiveSearch(String)} if input text is not found).
	 */
	private ArrayList partialResult()
	{
		ArrayList returnValue = null;
		String resultString = results.getText().toLowerCase();
		returnValue = (ArrayList)prefix.get(resultString);
		if (returnValue == null)
		{
			returnValue = recursiveSearch(resultString);
		}
		return returnValue;
	}

	/**
	 * Reset the search results to empty, redraws in all drawers.
	 *
	 */
	public void resetSearch()
	{
		searchResults.clearSelection();
		searchStatus.setText(initialText);
		results.setText("");
		ArrayList searchResultArray = partialResult();
		searchResults.setListData(searchResultArray.toArray());
		
		Iterator tdIter = tj.treeDrawers.iterator();
		while (tdIter.hasNext())
		{
			AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tdIter.next();
			atd.requestRedraw();
		}
	}


	/**
	 * Static test function, standalone testing on this class.
	 * @param args Input, not used
	 */
	public static void main(String[] args) {
		ArrayList testSet = new ArrayList();
		testSet.add(new String("Nicotiana"));
		testSet.add(new String("Campanula"));
		testSet.add(new String("Scaevola"));
		testSet.add(new String("Dasyphyllum"));
		testSet.add(new String("Stokesia"));
		testSet.add(new String("Dimorphotheca"));
		testSet.add(new String("Senecio"));
		testSet.add(new String("Gerbera"));
		testSet.add(new String("Gazania"));
		testSet.add(new String("Echinops"));
		testSet.add(new String("Felicia"));
		testSet.add(new String("Tagetes"));
		testSet.add(new String("Chromolaena"));
		testSet.add(new String("Blennosperma"));
		testSet.add(new String("Coreopsis"));
		testSet.add(new String("Vernonia"));
		testSet.add(new String("Cacosmia"));
		testSet.add(new String("Cichorium"));
		testSet.add(new String("Achillea"));
		testSet.add(new String("Carthamnus"));
		testSet.add(new String("Flaveria"));
		testSet.add(new String("Piptocarpa"));
		testSet.add(new String("Helianthus"));
		testSet.add(new String("Tragopogon"));
		testSet.add(new String("Chrysanthemum"));
		testSet.add(new String("Eupatorium"));
		testSet.add(new String("Lactuca"));
		testSet.add(new String("Barnadesia"));
		IncrementalSearch is;
		is = new IncrementalSearch(null);
		is.setVisible(true);
		is.initializeList(testSet);
	}

	/**
	 * List change detection function.  Updates highlighting on tree (changes to selected items reflected in tree),
	 * and modifies status line to indicate number of items selected ({@link #searchStatus}).
	 * @param evt List change event, on the list {@link #searchResults}.  This is ignored in place of re-reading the list content.
	 */
	public void valueChanged(ListSelectionEvent evt)
	{
		Object selected[] = searchResults.getSelectedValues();
		numFound = searchResults.getModel().getSize(); // maybe redundant
		searchStatus.setText("Selected " + selected.length + " of " + numFound + " nodes");
		if (tj != null && selected.length <= returnThreshold)
		{
			tj.clearGroup(StateFrame.F_ACT);
			for (int i = 0; i < selected.length; i++)
			{
				Iterator tdIter = tj.treeDrawers.iterator();
				while (tdIter.hasNext())
				{
					AccordionTreeDrawerFinal atd = (AccordionTreeDrawerFinal)tdIter.next();
					TreeNode n = atd.getNodeByName((String)selected[i]);
					if (n == null) { continue; }
					int key = n.getKey();
					tj.addNodesToGroup(key, key, StateFrame.F_ACT, atd);
				}
			}
		}
		if (selected.length > returnThreshold)
			tj.clearGroup(StateFrame.F_ACT);
		tj.requestRedrawAll();
	}

	/**
	 * Triggers the search function after a key is released.  The caret position (text cursor)
	 * is stored and recovered after text is entered to properly support deletes, copy-paste, and 
	 * normal typing.
	 * @param evt Key release event.
	 */
	public void keyReleased(KeyEvent evt)
	{
		int caretPos;
		caretPos = results.getCaretPosition();
		ArrayList searchResultArray = partialResult();
		String partialName = results.getText();
		if (partialName.length() == 0)
		{
			resetSearch();
			return;
		}		
		searchResults.setListData(searchResultArray.toArray());
		searchStatus.setText("Search matched " + searchResultArray.size() + " nodes");
		numFound = searchResults.getModel().getSize();
		results.setText(partialName);
		results.setCaretPosition(caretPos);
		if (numFound < returnThreshold)
		{
			searchResults.setSelectionInterval(0, numFound  - 1);
		}
		else
			resetSearch();
	}

	/**
	 * Stub function, use {@link #keyReleased(KeyEvent)} events to fully support copy/paste/delete/typing.
	 * @param e Key typing event.
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Stub function, use {@link #keyReleased(KeyEvent)} events to fully support copy/paste/delete/typing.
	 * @param e Key press event.
	 */
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * Action Listener for the reset button.
	 * @param arg0 Event that triggered the reset button.
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == resetButton)
		{
			resetSearch();
		}
	}


}
