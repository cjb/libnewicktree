

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

package net.sourceforge.olduvai.treejuxtaposer.drawer;
import java.util.*;

/**
 * Leaf comparator, compares pair of leaves by leaf index values.
 * @author Tamara
 *
 */
public class LeafComparator implements Comparator {
    /**
     * Subtract o2.leafindex from o1.leafindex to get comparator value that is negative (o1 < o2), positive (o2 < o1) or zero (equal)
     * @param o1 first leaf
     * @param o2 second leaf
     */
    public int compare(Object o1, Object o2) {
	int a =	((TreeNode) o1).getLindex();
	int b = ((TreeNode) o2).getLindex();
	return (a-b);
    }
}