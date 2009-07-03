package net.sourceforge.olduvai.treejuxtaposer;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.olduvai.treejuxtaposer.drawer.Tree;
import net.sourceforge.olduvai.treejuxtaposer.drawer.TreeNode;

/**
 * Static utility class to write a tree to a file.  Does not create an object, no constructor.
 * Newick files are created, but the functions here could be used to create a Nexus
 * file writer.
 * @author jslack
 *
 */
public class TreeWriter extends JFrame implements ActionListener {

	/** End of tree character. */
    private final static char lineTerminator = ';';
    /** End of tree character. */
    private final static char treeTerminator = lineTerminator;
    /** Open bracket to wrap children of a subtree root. */
    private final static char openBracket = '(';
    /** Close bracket to wrap children of a subtree root. */
    private final static char closeBracket = ')';
    /** Place between children of a subtree root. */
    private final static char childSeparator = ',';
    /** Quote object to wrap node names. */
    private final static char doubleQuote = '"';
    /** UNUSED separator for placing more information after the node name. */
    private final static char infoSeparator = ':';
    /** UNUSED single quote character. */
    private final static char quote = '\'';

	/**
	 * Writes Tree to file by name of fileName.
	 * @param t Tree to be written to file.
	 * @param fileName output file name.
	 */
	static public void writeTree(Tree t, String fileName)
	{
		try
		{
			FileWriter treeFileWriter = new FileWriter(fileName);
			BufferedWriter writer = new BufferedWriter(treeFileWriter);
			nodeWriter(t.getRoot(), writer);
			writer.write(treeTerminator);
			writer.close();
		}
		catch (IOException ioe)
		{
			System.err.println("Could not find file: " + fileName);
		}
	}
	
	/**
	 * Write a single node to the writer object, after calling the subtree writer on each child.
	 * All nodes are quoted with {@link #doubleQuote}.
	 * @param currNode Node to write.
	 * @param writer Writer object used to send the node and its subtree to file.
	 * @throws IOException
	 */
	static private void nodeWriter(TreeNode currNode, BufferedWriter writer) throws IOException
	{
		if (currNode.numberChildren() > 0)
		{
			subtreeWriter(currNode, writer);
		}
		if (currNode.getName().length() > 0)
			writer.write(doubleQuote + currNode.getName() + doubleQuote);		
	}
	
	/**
	 * Wrap a series of nodes in brackets, then call the node writer on each child of the input subtree root.
	 * @param subtreeRoot Root of the subtree to write.  This node itself is not written by this function.
	 * @param writer Writer object used to send the subtree to file.
	 * @throws IOException
	 */
	static private void subtreeWriter(TreeNode subtreeRoot, BufferedWriter writer)
	throws IOException
	{
		writer.write(openBracket);
		for (int i = 0; i < subtreeRoot.numberChildren() - 1; i++)
		{
			nodeWriter(subtreeRoot.getChild(i), writer);
			writer.write(childSeparator);
		}
		nodeWriter(subtreeRoot.getChild(subtreeRoot.numberChildren()-1), writer);
		writer.write(closeBracket);
	}

	/** TreeJuxtaposer used to reset the differences and search dialog with {@link TreeJuxtaposer#postDeleteTrees()}. */
	private TreeJuxtaposer tj;
	/** Node that is being renamed. */
	private TreeNode renameNode;
	/** OK button that resets the differences and search dialog. */
	private JButton OkButton = new JButton("OK");
	/** Cancel button that does not change the node and does not reset differences. */
	private JButton CancelButton = new JButton("Cancel");
	/** Label for the text field. */
	private JLabel dialogLabel = new JLabel("New label for");
	/** Text area to read new label name. */
	private JTextField newLabel = new JTextField();
	
	/**
	 * Start a tree writer dialog that allows a user to enter a new name for a node.
	 * Reinitializes the differences and search dialog to start a new cache.
	 * Nodes that were previously labeled and match other nodes in other trees do not
	 * affect the identically named nodes, which may cause issues with the differences
	 * and search dialog if they are not reset.
	 * @param renameNode Node that is being renamed, does not know it's own tree.
	 * @param tj TreeJuxtaposer object, for referencing the search dialog.
	 */
	public TreeWriter(TreeNode renameNode, TreeJuxtaposer tj)
	{
		this.renameNode = renameNode;
		this.tj = tj;
		initGUI();
	}
	
	/**
	 * Initialize the GUI for changing the label of a node.  This action causes
	 * differences and search dialogs to be updated.
	 *
	 */
	private void initGUI()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		this.setTitle("Rename " + renameNode.label);
		((JFrame)this).setLocation(10,30);
		this.getContentPane().setLayout(new GridBagLayout());

		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.gridy = 0;
		
		this.add(dialogLabel, gbc);
		
		gbc.gridy++;
		
		this.add(newLabel, gbc);
		newLabel.setText(renameNode.label);
		newLabel.setMinimumSize(new Dimension(250, 24));
		newLabel.setPreferredSize(new Dimension(250, 24));
		newLabel.setMaximumSize(new Dimension(1024, 24));
		
		gbc.gridy++;
		gbc.gridwidth = 1;
		
		this.add(CancelButton, gbc);
		CancelButton.addActionListener(this);
		
		// spacer
		gbc.gridx++;
		this.add(new JPanel(), gbc);
		gbc.gridx++;
		
		this.add(OkButton, gbc);
		OkButton.addActionListener(this);
		
		pack();
		setVisible(true);
		
	}
	
	/**
	 * Capture button press for newly renamed node.
	 * @param arg0 Button press, either OK or cancel.
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == OkButton)
		{
			renameNode.label = newLabel.getText();
			renameNode.setName(renameNode.label);
			tj.postDeleteTrees();
			dispose();
		}
		else // cancel or kill window
		{
			dispose();
		}
	}
}
