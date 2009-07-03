
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
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.olduvai.treejuxtaposer.drawer.*;



/**
 * Tree removal panel.  Opens a dialog with a list of trees that are opened, if there
 * are more than one, and selects them all.  Users may unselect trees they want to
 * keep, or cancel.
 * @author james
 *
 */
public class TreeRemovalPanel extends JDialog implements ActionListener {
	
	/** The ok button. */
	private JButton okButton;
	/** The cancel button. */
	private JButton cancelButton;
	/** List of trees from the TJ object. */
	private JList treeList;
	/** The TJ object used to get the tree names. */
	private TreeJuxtaposer tj; 
	/** Indices that have been selected for deleting. */
	private int[] indices = null;
    
	/**
	 * Creates a tree removal panel from the current active trees drawn in the input TJ.
	 * Opens a delete panel and automatically selects all trees for delete.
	 * @param tj_ TJ to examine for trees to delete.
	 */
	public TreeRemovalPanel(TreeJuxtaposer tj_) {
	
	tj = tj_;
	setTitle("Remove trees");
	Container contentPanel = getContentPane();
	JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new BorderLayout());
	
	Object[] possibleValues = new Object[tj.trees.size()];
	for(int i=0; i<tj.trees.size(); i++) 
		possibleValues[i] = ((Tree)(tj.trees.get(i))).getName();
		
	treeList = new JList(possibleValues);
	treeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	JScrollPane listScrollPane = new JScrollPane(treeList);
	contentPanel.add(listScrollPane, BorderLayout.NORTH);
	
	indices = new int[tj.trees.size()];
	for(int i=0; i<indices.length;i++)
		   indices[i] =i;
	treeList.setSelectedIndices(indices);
	
	okButton = new JButton("OK");
	okButton.addActionListener(this); 
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(this);
	controlPanel.add(okButton, BorderLayout.WEST);
	controlPanel.add(cancelButton, BorderLayout.EAST);
	contentPanel.add(controlPanel, BorderLayout.SOUTH);
	}
    
	/**
	 * Action listener callback.
	 * @param e Detect the only actions possible, the ok button being pushed (do the 
	 * delete) or cancel (do nothing).
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object ea = e.getSource();
		if (ea == okButton)
		{
			tj.deleteTrees(treeList.getSelectedIndices());
			tj.postDeleteTrees();
		}
		dispose();
	}
}
