
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

import net.sourceforge.olduvai.accordiondrawer.AccordionDrawer;
import net.sourceforge.olduvai.treejuxtaposer.drawer.*;



/**
 * Tree2Tree does the precomputation for each pair of trees. The two
 * precomputation tasks are computing the best corresponding node for
 * each node and building the range query data structures.
 *
 * @author  Tamara Munzner, Serdar Tasiran, Li Zhang, Yunhong Zhou
 * @version 2.1
 * @see     Tree
 * @see     RangeList
 */
class Tree2Tree {

	/** Arbitrary tree A for this comparison. */
	private Tree treeA;
	/** Arbitrary tree B for this comparison. */
	private Tree treeB;

	// for X2Y: an entry for each node in X if the subtree under X is a forest in Y
	/** Subtree hashtable for A->B.  Each node (table index) stores reference to an array of nodes that are marked
	 * if the subtree under the index node is marked.  The number of items is limited to {@link #SubtreeLeafCutoff}. */
	private Hashtable A2B;
	/** Subtree hashtable for B->A.  Each node (table index) stores reference to an array of nodes that are marked
	 * if the subtree under the index node is marked.  The number of items is limited to {@link #SubtreeLeafCutoff}. */
	private Hashtable B2A;

	/** Vector for A->B that stores the best corresponding nodes for
	 different levels. Each element of the vector is a hashmap that
	 is indexed by nodes */
	private Vector bestA2B;
	/** Vector for B->A that stores the best corresponding nodes for
	 different levels. Each element of the vector is a hashmap that
	 is indexed by nodes */
	private Vector bestB2A;

	// to deal with edge weights and leaves
	/** Minimum edge weight if they are used.  Edge weights of 0 cause problems, so this should be set higher. */
	private final float epsilon = 0.1f;

/**
 * Initialize the two vectors 
 * @param t1 Tree A/1 of this comparison object.
 * @param t2 Tree B/2 of this comparison object.
 * @param edgeweightLevels The number of edge weight levels used in the comparisons.  Defaults to 1 if not specified.
 */
	public Tree2Tree(Tree t1, Tree t2, int edgeweightLevels) {
		treeA = t1;
		treeB = t2;
		long start, current;
		bestA2B = new Vector(edgeweightLevels);
		bestB2A = new Vector(edgeweightLevels);

		start = System.currentTimeMillis();
		// initialization the data structures for answering the
		// queries of getBestCorrNode and isRangeInRange.
		onewayTreeCompare(treeA, treeB, bestA2B, edgeweightLevels);
		current = System.currentTimeMillis();
		if (AccordionDrawer.debugOutput)
			System.out.println("best match 1 total preprocessed: "+(current-start)/1000.0f+" sec");
		start = current;
		onewayTreeCompare(treeB, treeA, bestB2A, edgeweightLevels);
		current = System.currentTimeMillis();
		if (AccordionDrawer.debugOutput)
			System.out.println("best match 2 total preprocessed: "+(current-start)/1000.0f+" sec");
	}

	/**
	 * Adds the node to the hashtable, indexed by its key (as an Integer).
	 * @param node Node to insert.
	 * @param array Array of integers, storing list of keys found in the hashtable.
	 * @param hash Lookup table for nodes, indexed by their key.
	 */
	private void addNodeToForest(TreeNode node, ArrayList array, Hashtable hash)
	{
		Integer nodeInteger = new Integer(node.key);
		if (hash.get(nodeInteger) == null)
		{
			array.add(nodeInteger);
			hash.put(nodeInteger, node);
		}
	}
	/**
	 * Removes the node from the hashtable, indexed by its key (as an Integer).
	 * @param node Node to remove.
	 * @param array Array of integers, storing list of keys found in the hashtable.
	 * @param hash Lookup table for nodes, indexed by their key.
	 */
	private void removeNodeFromForest(TreeNode node, ArrayList array, Hashtable hash)
	{
		Integer nodeInteger = new Integer(node.key);
		if (hash.get(nodeInteger) == null)
		{
			array.remove(nodeInteger);
			hash.remove(nodeInteger);
		}
	}

	/**
	 * Return an ArrayList of a reduced number of elements from ArrayList
	 * based on the number of leaves; bigger subtrees are kept, ties broken
	 * by taking any subtree with maximum number of leaves not already in forest.
	 * This function allows us to store top-level nodes of large trees without having
	 * to keep references to all nodes that we would have to mark.  Descendant nodes in the
	 * marked subtree will also be checked, so the reduced list does not have to be exhaustive.
	 * @param node List of nodes that would be marked (forest) if a subtree is marked (referenced by marking the root of this subtree).
	 * @param cutoff Limit on number of nodes stored for each subtree.  Currently uses {@link #SubtreeLeafCutoff} defined value.
	 * @return A reduced list of nodes from the original set, which is not as large as the cutoff value.
	 */
	private ArrayList reduceNodeListToCutoff(ArrayList node, int cutoff)
	{
		int subtreeSize[] = new int[node.size()];
		for (int i = 0; i < node.size(); i++)
		{
			subtreeSize[i] = ((TreeNode)node.get(i)).numberLeaves;
		}
		Arrays.sort(subtreeSize); // sort array in ascending order
		int subtreeSizeCutoff = subtreeSize[node.size() - cutoff] + 1; // find (cutoff + 1) subtree size
		ArrayList returnList = new ArrayList();
		for (int i = 0; i < node.size(); i++)
		{
			TreeNode currentNode = (TreeNode)node.get(i);
			if (currentNode.numberLeaves >= subtreeSizeCutoff)
				returnList.add(currentNode);
		}
		subtreeSizeCutoff--;
		for (int i = 0; i < node.size() && returnList.size() < cutoff; i++)
		{
			TreeNode currentNode = (TreeNode)node.get(i);
			if (currentNode.numberLeaves == subtreeSizeCutoff)
				returnList.add(currentNode);
		}
		return returnList;
	}

	/**
	 * Compute the forest of marked nodes in treeY, for subtrees under every node in treeX.
	 * This precomputation avoids the on-the-fly tree traversals for complex BCN matchings
	 * (BCN is not 1-to-1, so each node that ties the best score is marked).  The forest
	 * is reduced to the cutoff value (constant {@link #SubtreeLeafCutoff}) to prevent
	 * storage of each node for large subtrees; results for descendants fill in missing
	 * values as needed.
	 * @param X2Y BCN forest table for treeX to treeY (input empty, filled by this function).
	 * @param treeX First tree, each node in this tree is processed into a forest.
	 * @param treeY Second tree, nodes are located that match under subtree of node in treeX, and stored in the BCN table.
	 * @param atdY Drawer for treeY.
	 * @param cutoff Cutoff value for forest size, currently set to {@link #SubtreeLeafCutoff}.
	 */
	private void computeForest(Hashtable X2Y, Tree treeX, Tree treeY,
			AccordionTreeDrawer atdY, int cutoff)
	{
		//posorder = children first, then parents
		for (TreeNode nX = treeX.getLeftmostLeaf(); nX != null; nX = nX.posorderNext)
		{
			ArrayList currListX = null;
			TreeNode nY = getBestCorrNode(treeX, nX, treeY, 0);
			if (nY == null) // no BCN for nX, give up
				continue;
			Hashtable nXHash = null;

			if (!nX.isLeaf()) // add children of nY to currListY
				//  (iff child is BCN of a descendent of nX)
			{
				currListX = new ArrayList();
				nXHash = new Hashtable();
				addNodeToForest(nY, currListX, nXHash);
				// add nY reference (sort later)
				for (int i = 0; i < nX.numberChildren(); i++) {
					TreeNode nXChild = nX.getChild(i);
					TreeNode nYChild = getBestCorrNode(treeX, nXChild, treeY, 0);
					ArrayList nXChildList = (ArrayList)X2Y.get(nXChild);
					if (nXChildList != null) {
						for (int j = 0; j < nXChildList.size(); j++) // go through forest of nXChild
						{
							TreeNode nYj = (TreeNode)nXChildList.get(j);
							if (nY.getMin() > nYj.getMin() || nY.getMax() < nYj.getMax())
								// nY is not an ancestor of nYj; nYj could be an ancestor of nY
								addNodeToForest(nYj, currListX, nXHash);
							if (nYj.getMin() <= nY.getMin() && nYj.getMax() >= nY.getMax())
								// nYj is an ancestor of nY, remove nY
								removeNodeFromForest(nY, currListX, nXHash);
						}
					}
					// add child
					if (nYChild != null && !nY.isAncestorOf(nYChild))
						addNodeToForest(nYChild, currListX, nXHash);

					// reduce (to cutoff) large forests under children of current node
					if (nXChildList != null && nXChildList.size() > cutoff)
					{
						ArrayList newNXChildList = reduceNodeListToCutoff(nXChildList, cutoff);
						X2Y.remove(nXChild);
						X2Y.put(nXChild, newNXChildList);
					}
				}
			}
			ArrayList finalArrayX = new ArrayList(); // final forest calculation

			// sort nBChildList by key
			if (currListX != null) {
				Object tempObjects[] = currListX.toArray();
				Integer tempArray[] = new Integer[tempObjects.length];
				for (int i = 0; i < tempObjects.length; i++)
					tempArray[i] = (Integer)tempObjects[i];
				Arrays.sort(tempArray);
				for (int i = 0; i < tempArray.length; i++)
				{
					TreeNode node = (TreeNode)nXHash.get(tempArray[i]);
					if (node != null)
					{
						finalArrayX.add(node);
						nXHash.remove(tempArray[i]);
					}
				}
			}

			// add finalArrayX to X2Y referenced by nX if finalArrayX has more than one element
			if (finalArrayX != null && finalArrayX.size() > 1)
				X2Y.put(nX, finalArrayX);
		} // end of current nX
	}

	/** Restriction on the maximum number of nodes to store for any subtree's forest of best corresponding nodes. 
	 * This doesn't have to be big, if a subtree is marked, only the top nodes in the forest hierarchy need to be
	 * detected, since children of the marked subtree should contain more information about what is marked.  Storing
	 * all marks for all subtrees would be quadratic storage. */
	private final int SubtreeLeafCutoff = (int)100; // allow at most "cutoff" items in a node's forest array

	/**
	 * Preprocessing: calculate and store forests that correspond to subtrees.
	 * A2B/B2A: hash tables that map nodes (roots of subtrees) from A/B to forests of nodes (subtrees) in B/A
	 * All BCN work is done in computeForest, called both ways in this function (A->B and B->A).
	 * @param atdA Drawer for tree A.
	 * @param atdB Drawer for tree B.
	 * @param eL number of edge weight levels to compute (not used).
	 */
	protected void subtree2Forest(AccordionTreeDrawer atdA, AccordionTreeDrawer atdB, int eL)
	{
		
		float start = System.currentTimeMillis();
		A2B = new Hashtable();
		computeForest(A2B, treeA, treeB, atdB, SubtreeLeafCutoff);
		B2A = new Hashtable();
		computeForest(B2A, treeB, treeA, atdA, SubtreeLeafCutoff);
		float time = (System.currentTimeMillis() - start) / 1000;
		if (AccordionDrawer.debugOutput)
			System.out.println("Time to preprocess forest pair: " + time);
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
	 * @param source Source tree that contains the node being looked up in the other tree.
	 * @param n Node being looked up in the target tree.
	 * @param other Target tree to find the best node for the input node.
	 * @param el Edge length weight to use for lookup.
	 * 
	 * @see      Tree
	 * @see      TreeNode
	 * @see      NodeScorePair
	 */
	protected TreeNode getBestCorrNode(Tree source, TreeNode n, Tree other, int el) {
		HashMap h = null;
		if((source == treeA)&&(other == treeB))
		{
			h = (HashMap)bestA2B.elementAt(el);
		}
		else if((source == treeB)&&(other == treeA))
		{
			h = (HashMap)bestB2A.elementAt(el);
		}
		if (h == null) return null;

		NodeScorePair p = ((NodeScorePair)(h.get(n)));
		if (p != null)
			return p.node;
		return null;
	}

	/**
	 * Identify input trees as {@link #treeA} or {@link #treeB} and look up the best score
	 * from the BCN of the input node from A to tree B.
	 * @param source Tree that contains the target node.
	 * @param n The target node.
	 * @param other Second tree, for identifying the appropriate hashmap (either {@link #bestA2B} or {@link #bestB2A}).
	 * @param el The edge weight length to use for lookups.
	 * @return The score of the BCN of n in B.
	 */
	protected float getBestCorrNodeScore(Tree source, TreeNode n, Tree other, int el) {

		if((source == treeA)&&(other == treeB))
			return ((NodeScorePair)(((HashMap)(bestA2B.elementAt(el))).get(n))).score;
		else if((source == treeB)&&(other == treeA))
			return ((NodeScorePair)(((HashMap)(bestB2A.elementAt(el))).get(n))).score;
		else return -1.0f;
	}

	// return an ArrayList of RangeInTree objects that correspond to n from source
	/**
	 * Identify input trees as {@link #treeA} or {@link #treeB} and look up the best matching nodes
	 * of the input node from A in tree B.
	 * @param source Tree that contains the target node.
	 * @param n The target node.
	 * @param other Second tree, for identifying the appropriate hashmap (either {@link #bestA2B} or {@link #bestB2A}).
	 * @param el The edge weight length to use for lookups.
	 * @return The list of nodes in the second tree that best match the input node.  Many matches possible since the BCN is not 1-to-1.
	 */
	protected ArrayList getCorrRange(Tree source, TreeNode n, Tree other, int el)
	{
		if ((source == treeA)&&(other == treeB))
			return (ArrayList)A2B.get(n);
		else if ((source == treeB) && (other == treeA))
			return (ArrayList)B2A.get(n);
		return null;
	}

	/**
	 * Container class for returning best corresponding node+score pair.
	 *
	 */
	private class NodeScorePair {
		/** Tree node reference. */
		private TreeNode node = null;
		/** Score for the tree node, with respect to a second node (known by the calling function). */
		private float score = 0.0f;
		/**
		 * Constructor for node+score pair.
		 * @param n Tree node reference.
		 * @param s Score for the tree node, with respect to a second node (known by the calling function).
		 */
		private NodeScorePair(TreeNode n, float s) { node = n; score = s; }
		/**
		 * Debugging output.
		 * @return Node and score in a string.
		 */
		public String toString() {
			return "[" + node + " " + score + "]";
		};
	};

	/** 
	 * Attachment to a node that is needed as temporary data structure
	 * when computing best corresponding nodes.  Caches results from level 0
	 * processing to be used in higher levels.
	 **/
	private class TmpD {
		private int tmpScore = 0;
		private float uSum = 0;
		private float lSum = 0;
		private TreeNode tmpParent = null;
		private TreeNode tmpPosorderNext = null;
	};

	/**
	 *
	 * For each node on Tree t1, computes the best matching node in
	 * Tree t2 and stores it in Vector v12.
	 * 
	 * @param t1 The first tree
	 * @param t2 The second tree
	 * @param v12 Vector to store the hashmaps (t1:t2 and t2:t1) for matching node pairs for T1 vs T2.  Each edge level has a hashmap of node-node pairs in the vector.
	 * @param edgeweightLevels Number of edge weight levels to store
	 * @see      Tree
	 * @see      #computeBestMatch(TreeNode, Tree, Tree, float, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])
	 * @see      NodeScorePair
	 */
	private void onewayTreeCompare(Tree t1, Tree t2, Vector v12, int edgeweightLevels) {

		TmpD[] tmpData = new TmpD[t2.nodes.size()];

		// allocate the temporary data needed. we will reuse
		// the temporary data for each node. hopefully, this saves
		// us some time.
		for(int i=0; i<t2.nodes.size(); i++) {
			tmpData[i] = new TmpD();
		}
		HashMap h12 = new HashMap();

		long start, current;
		// special case for level 0 since we know how to deal with 
		// it
		start = System.currentTimeMillis();
		for(int i=0; i<t1.nodes.size(); i++) {
			TreeNode n = (TreeNode) t1.nodes.get(i);
			NodeScorePair p = getBestNodeScorePair(n, t1, t2, tmpData);
			h12.put(n, p);
		}
		current = System.currentTimeMillis();
		if (AccordionDrawer.debugOutput)
			System.out.println("best match level 0 preprocessed: "+(current-start)/1000.0f+" sec");
		start = current;

		v12.add(h12);
		// higher than 0 edge weight levels
		for(int el=1; el<edgeweightLevels; el++) {
			h12 = new HashMap();
			for(int i=0; i<t1.nodes.size(); i++) {
				TreeNode n = (TreeNode) t1.nodes.get(i);
				NodeScorePair p = computeBestMatch(n, t1, t2, el*1.0f/(edgeweightLevels-1), tmpData);
				h12.put(n, p);
				// System.out.println(n.leftmostLeaf.getName()+","+n.rightmostLeaf.getName()+":"+p.node.leftmostLeaf.getName()+","+p.node.rightmostLeaf.getName()+" "+p.score);
			}
			current = System.currentTimeMillis();
			if (AccordionDrawer.debugOutput)
				System.out.println("best match level "+el+" preprocessed: "+(current-start)/1000.0f+" sec");
			start = current;
			v12.add(h12);
		}
	}

	/**
	 * Find the best corresponding node for a given node sourceNode:sourceTree, in the
	 * target tree targetTree.  This is the level 0 processing and preprocessing of the array
	 * used to compute higher edge levels in {@link #computeBestMatch(TreeNode, Tree, Tree, float, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])}.
	 * 
	 * How to compute the best corresponding node for each node:
	 *
	 * node B is the best corresponding node of node A if it maximizes
	 *
             | L(A) U L(B)|
            ----------------
             | L(A) n L(B)|

	  where L(A),L(B) represent the set of leaves underneath the
	  node A and node B respectively.

	  For the description of the algorithm, see 
	  Li Zhang. On Matching Nodes in Two Trees.
	 * @param sourceNode Node of interest in sourceTree, get the corresponding {@link NodeScorePair} in targetTree.
	 * @param sourceTree Tree that has sourceNode.
	 * @param targetTree Tree to look up a corresponding node, wrt to sourceNode.
	 * @param tmpData Array initialized by level 0 processing ({@link #getBestNodeScorePair(TreeNode, Tree, Tree, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])}), used to compute best nodes.
	 * @return A node and it's score as a {@link NodeScorePair} that best corresponds to sourceNode.
	 **/
	private NodeScorePair getBestNodeScorePair(TreeNode sourceNode, Tree sourceTree,
			Tree targetTree, TmpD[] tmpData) {

		LinkedList mleaves = new LinkedList();
		Object[] la;
		// gather all leaves from target tree that are under sourceNode into mleaves
		for(int currLeafIndex = sourceNode.leftmostLeaf.getLindex();
		currLeafIndex <= sourceNode.rightmostLeaf.getLindex();
		currLeafIndex++) {
			TreeNode n = sourceTree.getLeaf(currLeafIndex);

			TreeNode nn = targetTree.getNodeByName(n.getName());
			if(nn!=null) {
				if(nn.isLeaf()) {
					tmpData[nn.key].tmpScore = 1;
					mleaves.add(nn);
				}
			}
		}	
		if(mleaves.size()==0) { // no matching leaves for sourceNode, return null/0, always mark this node as different
			return new NodeScorePair(null, 0.0f);
		} else if(mleaves.size()==1) { // single leaf match for node or subtree, return matchingNode/(1/numleaves)
			// TODO: (interior names don't usually match leaves) This needs to be tested
			return new NodeScorePair((TreeNode)(mleaves.get(0)), 1.0f/sourceNode.numberLeaves);
		}

		la = mleaves.toArray();
		Arrays.sort(la, new LeafComparator());

		// build the simplified spanning tree by repeated bay traversal.
		// the following precedure can be probably best understood by 

		TreeNode tmpPosorderStart = (TreeNode)(la[0]);
		TreeNode lastNode = (TreeNode)(la[la.length-1]);
		TreeNode tmpRoot = tmpPosorderStart.leafLca(lastNode);

		tmpData[tmpPosorderStart.key].tmpParent = tmpRoot;
		tmpData[lastNode.key].tmpParent = tmpRoot;	
		tmpData[tmpPosorderStart.key].tmpPosorderNext = lastNode;
		tmpData[lastNode.key].tmpPosorderNext = tmpRoot;

		Stack bay = new Stack();
		TreeNode prev, pprev,a;

		bay.push(tmpRoot); // root on the bottom of the stack
		bay.push(tmpPosorderStart); // "leftmost" leaf on top

		// initialize tmpdata array with nodes from target tree
		for(int i=1; i<la.length-1; i++) {

			TreeNode nn = (TreeNode) (la[i]);
			if(nn.getLindex() == ((TreeNode)la[i-1]).getLindex())
				continue; // no duplication, please
			a = nn.leafLca((TreeNode)(la[i-1]));
			prev = null;
			pprev = null;

			while(!bay.empty()) {
				pprev = prev;
				prev = (TreeNode) (bay.peek());
				if(prev.isAncestorOf(a)) {
					if(prev==a) bay.pop();
					break;
				}
				bay.pop();
			}

			if(bay.empty()) {
				a = nn.leafLca(lastNode);
				if(a==prev) {
					// not a binary tree
					tmpData[nn.key].tmpParent = prev;
					tmpData[pprev.key].tmpPosorderNext = nn;

					// we need to deal with non-binary tree and when
					// the node inserted is a child of the root
					if(prev==tmpRoot)
						tmpData[nn.key].tmpPosorderNext = lastNode;
					else
						tmpData[nn.key].tmpPosorderNext = a;
				} else {

					// the node is inserted to the right branch
					// and creat the new tmpRoot
					tmpData[a.key].tmpParent = tmpData[lastNode.key].tmpParent;
					tmpData[lastNode.key].tmpParent = a;
					tmpData[nn.key].tmpParent = a;
					tmpData[a.key].tmpPosorderNext = 
						tmpData[lastNode.key].tmpPosorderNext;
					tmpData[pprev.key].tmpPosorderNext = nn;
					tmpData[nn.key].tmpPosorderNext = lastNode;
					tmpData[lastNode.key].tmpPosorderNext = a;
					tmpRoot = a;
				}
			} else {
				if(a==prev) {
					tmpData[nn.key].tmpParent = prev;
					tmpData[pprev.key].tmpPosorderNext = nn;
					tmpData[nn.key].tmpPosorderNext = a;
				} else {
					tmpData[a.key].tmpParent = tmpData[pprev.key].tmpParent;
					tmpData[pprev.key].tmpParent = a;
					tmpData[nn.key].tmpParent = a;

					tmpData[a.key].tmpPosorderNext = tmpData[pprev.key].tmpPosorderNext;
					tmpData[pprev.key].tmpPosorderNext = nn;
					tmpData[nn.key].tmpPosorderNext = a;
				}
			}
			bay.push(a);
			bay.push(nn);
		}

		// walk up in the tree, compute the score of each node in the
		// spanning tree, and find the maximum score
		TreeNode match = null;
		float matchScore = 0.0f;
		float currentMatchScore;
		int sizeofUnion;
		final boolean pruneLeaves = false;
		// accumulate scores into tmpData array
		// the final score for each TmpD item will be the number of leaves in the intersection
		for(TreeNode n = tmpPosorderStart;
			n!=null;
			n=tmpData[n.key].tmpPosorderNext) {
			int fakeTargetLeaves = n.numberLeaves;
			if (pruneLeaves && !n.isLeaf()) // looking for nodes with leaf children that have no matches
			{
				LinkedList leaves = targetTree.getLeaves(n); // fetch the leaves for this node n
				System.out.println(n + " leaves: " + leaves);
				Iterator iter = leaves.iterator();
				while (iter.hasNext())
				{
					TreeNode currLeaf = (TreeNode)iter.next();
					if (tmpData[currLeaf.key].tmpScore == 0) // a leaf with no match in source
					{
						System.out.println((sourceNode) +" "+ (sourceNode.cell.drawer.getKey()) + " missing target " + currLeaf);
						fakeTargetLeaves--; // miss a node here to cover a leaf that has been added
					}
				}
			}
			int fakeSourceLeaves = sourceNode.numberLeaves;
			if (pruneLeaves && !sourceNode.isLeaf()) // removing leaves under source that were added
			{
				LinkedList leaves = sourceTree.getLeaves(sourceNode);
				Iterator iter = leaves.iterator();
				while (iter.hasNext())
				{
					TreeNode currLeaf = (TreeNode)iter.next();
					if (targetTree.getNodeByName(currLeaf.getName()) == null) // no matching leaf in target
					{
						System.out.println(sourceNode + " missing source " + currLeaf);
						fakeSourceLeaves--; // miss a node here to cover a leaf that has been added
					}
				}
			}
			
			sizeofUnion = fakeSourceLeaves+fakeTargetLeaves-tmpData[n.key].tmpScore;
			currentMatchScore = (float)(tmpData[n.key].tmpScore*1.0/sizeofUnion);
			if(matchScore<currentMatchScore) {
				match = n; 
				matchScore = currentMatchScore;
			}
			if(matchScore == 1.0f) break;
			TreeNode np = tmpData[n.key].tmpParent;
			if(np != null) {
				tmpData[np.key].tmpScore += tmpData[n.key].tmpScore;
			}
		}
		// cleanup the temporary data
		TreeNode n = tmpPosorderStart;
		while(n!=null) {
			tmpData[n.key].tmpScore = 0;
			TreeNode nn = tmpData[n.key].tmpPosorderNext;
			tmpData[n.key].tmpPosorderNext = null;
			tmpData[n.key].tmpParent = null;
			n = nn;
		}
		return new NodeScorePair(match, matchScore);
	}



	/**
	 * Compute the best match for sourceNode from sourceTree in the targetTree.
	 * Only for tree comparisons with more than one edge weight level (this function is
	 * for levels 1 and higher, the default level 0 is done by {@link #getBestNodeScorePair(TreeNode, Tree, Tree, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])},
	 * which also initializes the arrays used in this function.
	 * @param sourceNode Node of interest in sourceTree, get the corresponding {@link NodeScorePair} in targetTree.
	 * @param sourceTree Tree that has sourceNode.
	 * @param targetTree Tree to look up a corresponding node, wrt to sourceNode.
	 * @param edgeCoefficient Edge level coefficient.  Non-zero as level 0 is done in {@link #getBestNodeScorePair(TreeNode, Tree, Tree, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])}.
	 * @param tmpData Array initialized by level 0 processing ({@link #getBestNodeScorePair(TreeNode, Tree, Tree, net.sourceforge.olduvai.treejuxtaposer.Tree2Tree.TmpD[])}), used to compute best nodes.
	 * @return A node and it's score as a {@link NodeScorePair} that best corresponds to sourceNode.
	 */
	private NodeScorePair computeBestMatch(TreeNode sourceNode, Tree sourceTree,
			Tree targetTree, float edgeCoefficient, TmpD[] tmpData) {

		// compute the path length to each leaf node
		HashMap h = new HashMap();
		float tSum = 0;

		for (int currLeafIndex = sourceNode.leftmostLeaf.getLindex();
		currLeafIndex <= sourceNode.rightmostLeaf.getLindex();
		currLeafIndex++)
		{
			TreeNode currSourceLeaf = sourceTree.getLeaf(currLeafIndex);
			TreeNode p = currSourceLeaf;
			float pathLen = epsilon;
			while(p!=sourceNode)
			{
				pathLen += p.weight;
				p = p.parent;
			}
			float pathLenA = (float)Math.pow(pathLen, edgeCoefficient);
			// float pathLenA = Math.exp(alpha*Math.log(pathLen));
			h.put(currSourceLeaf.getName(), new Double(pathLenA));
			tSum += pathLenA;
		}

		// prepare tmpData, reset 
		for(int i=0; i<tmpData.length; i++) {
			TmpD tmpd = tmpData[i];
			tmpd.uSum = 0;
			// assume that the leaves are not presented in each node
			// and then correct the assumption later.
			tmpd.lSum = tSum;
		}

		// for each leaf node of t, accumulate the score bottom up
		// along the path to the root.
		for (int currTargetLeafIndex = 0;
		currTargetLeafIndex < targetTree.getLeafCount();
		currTargetLeafIndex++)
		{
			TreeNode currTargetLeaf = targetTree.getLeaf(currTargetLeafIndex);
			float pathLen = epsilon;
			Double tpl = (Double)(h.get(currTargetLeaf.getName()));
			TreeNode p = currTargetLeaf;
			if(tpl == null) {
				// the leaf is not in the subtree rooted at an
				while(p!=null) {
					// tmpData[p.key].uSum += 0;
					tmpData[p.key].lSum += Math.pow(pathLen, edgeCoefficient);
					// tmpData[p.key].lSum += Math.exp(alpha*Math.log(pathLen));
					pathLen += p.weight;
					p = p.parent;
				}
			} else {
				float plen = tpl.floatValue();
				while(p!=null) {
					TmpD tmpd = tmpData[p.key];
					tmpd.lSum -= plen;
					float plenp = (float)Math.pow(pathLen, edgeCoefficient);
					// float plenp = Math.exp(alpha*Math.log(pathLen));
					if(plenp < plen) {
						tmpd.uSum += plenp;
						tmpd.lSum += plen;
					} else {
						tmpd.uSum += plen;
						tmpd.lSum += plenp;
					}
					pathLen += p.weight;
					p = p.parent;
				}

			}

		}

		// traverse the tree and find the best match (highest uSum/lSum)
		TreeNode match = null;
		float matchScore = 0.0f;
		float currentMatchScore;

		for(int i=0; i<targetTree.nodes.size(); i++) {
			TreeNode n = (TreeNode)(targetTree.nodes.get(i));
			TmpD tmpd = tmpData[n.key];
			currentMatchScore = tmpd.uSum/tmpd.lSum;
			if(matchScore<currentMatchScore) {
				match = n; 
				matchScore = currentMatchScore;
			}
			if(matchScore == 1.0f) break;
		}
		return new NodeScorePair(match, matchScore);
	}
};

