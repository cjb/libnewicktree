
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


import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

import javax.swing.JProgressBar;

import net.sourceforge.olduvai.treejuxtaposer.drawer.Tree;
import net.sourceforge.olduvai.treejuxtaposer.drawer.TreeNode;


/**
 * TreePairs store all the pairwise data structures needed for
 * structural comparison and visulization.
 *
 * @author Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
 * @version 2.2
 * @see net.sourceforge.olduvai.treejuxtaposer.TreeJuxtaposer
 * @see net.sourceforge.olduvai.treejuxtaposer.Tree2Tree
 * @see     Tree
 * @see     net.sourceforge.olduvai.accordiondrawer.GridCell
 *
 **/

public class TreePairs {

	/**
	 * List of "raw" trees, these are used in the hashtable.
	 */
	private ArrayList trees; 

	/**
	 * The hash map that stores all the Tree2Tree objects.   The
	 * pairs are indexed first by the key of one tree in the pair 
	 * and then by the key of the other tree in the pair. a better 
	 * solution would be to use the key pair to index each pair. but
	 *  since the number of trees is small and is likely in the
	 * range  fewer than 10, it does not really matter how we
	 * implement it.
	 */
	private HashMap pairs; 

	/**
	 * Constructor for tree pairs.  Makes the list (for raw trees) and hashtable (for T2T tree comparison pairs).
	 *
	 */
	public TreePairs() {
		trees = new ArrayList();
		pairs = new HashMap();
	}
	
	/**
	 * Add a new tree.
	 *
	 * Create the data structure between the new tree and all the
	 * previously added trees.
	 * @param newTree New tree that has been added to the application.
	 * @param edgeweightLevels The number of edge weight levels to process.
	 * @param pruneNewLeaves Unimplemented flag for removing unique leaves from processing tree comparisons, for difference marking that does not ascend when two leaf sets do not match.
	 **/
	public void addTree(Tree newTree, int edgeweightLevels, boolean pruneNewLeaves) {

		if(trees.size()>=1) {
			// we need to add all the pairs between newTree and all
			// the previously added trees.
			HashMap h = new HashMap();

			pairs.put(newTree, h); 

			// not currently implemented option that proved too slow with naive methods
			// attempted to remove leaves from trees prior to comparison processing
			if (pruneNewLeaves)
			{
				LinkedList newTreeLeaves = newTree.getLeaves(newTree.getRoot());
				
				TreeNode[] leafArray = (TreeNode[])newTreeLeaves.toArray();
				Arrays.sort(leafArray);
				for (int i = 0; i < trees.size(); i++)
				{
					Tree currTree = (Tree)trees.get(i);
					LinkedList currTreeLeaves = currTree.getLeaves(currTree.getRoot()); 
				}
			}
			else
			{
				Frame progFrame = null;
				progFrame = new Frame("Comparing new tree: " + newTree.getName());
	            progFrame.setLayout(new GridBagLayout());
	            JProgressBar jpb = new JProgressBar(JProgressBar.HORIZONTAL, 0, trees.size());
	            jpb.setStringPainted(true);
	            jpb.setString("Comparing " + (trees.size()+1));
	            progFrame.add(jpb);
	            progFrame.pack();
	            progFrame.setVisible(true);
			// construct the data structure between the newly added
			// tree and every previously added tree
			for(int i=0; i<trees.size(); i++) {
				Tree t = (Tree)trees.get(i);
				
				Tree2Tree t2t = new Tree2Tree(t,newTree,edgeweightLevels);
				h.put(t, t2t);
				// update iterated tree's pair list with the new tree
				HashMap ht = (HashMap)pairs.get(t);
				if(ht==null) {
					// this happens when there was only one tree in
					// the list 
					ht = new HashMap();
					ht.put(newTree, t2t);
					pairs.put(t, ht);
				} else
					ht.put(newTree, t2t);
				jpb.setValue(i+1);
				jpb.setStringPainted(true);
				jpb.setString("Compared " + (i+1) + "/" + (trees.size()+1));
			}
			progFrame.dispose();
			}
		}
		trees.add(newTree);
	}

	/**
	 * Get the Tree2Tree table for t1 and t2 differences.  First gets the hashmap for t1 (from {@link #pairs})
	 * then gets the T2T in that hashmap for t2 (the differences of t2 based on t1).
	 * @param t1 First tree object.  Index in {@link #trees} for hashmap.
	 * @param t2 Second tree object.  Index in the recovered hashmap for t1 for the {@link Tree2Tree} object.
	 * @return Tree2Tree that corresponds to the differences in t1, with respect to t2.
	 */
	public Tree2Tree getPair(Tree t1, Tree t2)
	{
		HashMap h = (HashMap) pairs.get(t1);
		if (h == null)
			return null;
		return (Tree2Tree) h.get(t2);
	}

	/**
	 * Remove a tree from the list of trees.  Does not remove the relevant tree pairs.
	 * @param deletedTree Tree to delete from the set of trees.  
	 */
	public void removeTree(Tree deletedTree)
	{
		trees.remove(deletedTree);
	}

	/**
	 * Computes the node in Tree "other" whose set of descendant
	 * leaves best matches that of TreeNode n in Tree "source"
	 * 
	 * The best match is the node n' maximizing the following score
	 * | S(n) Intersection S(n') | / | S(n) Union S(n') | 
	 * 
	 * where S(n) is the set of leaves that are descendants of node n.
	 * 
	 * @see      Tree
	 * @see      TreeNode
	 * @see      Tree2Tree.NodeScorePair
	 * @param source Tree that contains the node of interest.
	 * @param n The node that we are looking up the best matching node for.
	 * @param other Target tree to look up the node in.
	 * @param el Number of edge weight levels to use.
	 * @return
	 // TODO: here 
	 */
	public TreeNode getBestCorrNode(Tree source, TreeNode n, Tree other, int el)
	{
		if (source == other)
			return n;
		if (null == n)
			return null;
		Tree2Tree t2t = getPair(source, other);
		if (t2t == null)
			return null;
		return t2t.getBestCorrNode(source, n, other, el);
	}

	/**
	 * Get the list of nodes for the given node that will be highlighted if the node is selected.
	 * BCN are not always unique, some nodes have equally good matches for best nodes.
	 * @param source Source tree that contains the given tree node
	 * @param n The node to look up the list of best nodes for, found in the source tree
	 * @param other The target tree that will be referenced for the best nodes that match the given node.
	 * @param edgeweightLevel The edge weight level to use for looking up matches.
	 * @return The list of nodes in other (target tree) that match the input node the best.
	 */
	public ArrayList getBestNodeList(Tree source, TreeNode n, Tree other, int edgeweightLevel)
	{
		if (null == n)
			return null;
		Tree2Tree t2t = getPair(source, other);
		if (t2t == null)
			return null;
		ArrayList returnValue = t2t.getCorrRange(source, n, other, edgeweightLevel);
		return returnValue;
	}

	/**
	 * Retrieves the matching score for the node in Tree "other" whose
	 * set of descendant leaves best matches that of TreeNode n in
	 * Tree "source".
	 * 
	 * The matching score between nodes n and n' is computed as
	 * follows:
	 *
	 * | S(n) Intersection S(n') | / | S(n) Union S(n') | 
	 * 
	 * where S(n) is the set of leaves that are descendants of node n.
	 * 
	 * @see      Tree
	 * @see      TreeNode
	 * @see      Tree2Tree.NodeScorePair
	 * 
	 * @param source Source tree, in which the tree node of interest is found.
	 * @param n Node of interest, the score with respect to the second tree is returned.
	 * @param other The target tree, or the tree that the given node's score will be found.
	 * @param edgeweightLevel The edge weight level for the lookup.
	 * @return The score, as stored by index of the node, in the tree to tree object, from the pair table for the two input trees.
	 */
	public float getBestCorrNodeScore(Tree source, TreeNode n, Tree other, int edgeweightLevel) {

		if (source == other) return 1.0f;
		Tree2Tree t2t = getPair(source, other);
		if(t2t==null) return 0.0f;
		return t2t.getBestCorrNodeScore(source, n, other, edgeweightLevel);
	}

}
