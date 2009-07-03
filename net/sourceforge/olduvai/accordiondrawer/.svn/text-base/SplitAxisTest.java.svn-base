package net.sourceforge.olduvai.accordiondrawer;

import java.util.Random;

/**
 * Test class for Split Axis debugging.
 */
public class SplitAxisTest {

	/**
	 * Test function.  Creates a new split axis and performs operations.
	 *
	 */
	public SplitAxisTest() {
		super();
		
//		final int initSize = 7500;
		final int modCount = 2100000;
		
		SplitAxis axis = new SplitAxis();
//		axis.initSplitValues(initSize);

		float addPercent = .9f;
		Random rand = new Random();
		
		int addCount = 1;
		
		SplitLine remLine;
		int curSize = 0;
		int insertat = 0;
		
		// Stick in root
		SplitLine rootLine = new SplitLine();
		rootLine.absoluteValue = 0;
		axis.putAt(rootLine, null);

//		// Stick in 5 more items
//		for (int i = 1 ; i < 7 ; i++) { 
//			addLine = new SplitLine(); 
//			addLine.absoluteValue = i;
//			axis.putAt(addLine, axis.getMaxLine());
//		}
//		axis.debug.checkSubTreeSizes();
//		axis.debug.checkAllBounds();
//		System.out.println("Added!");
//		
//		// now lets remove root twice . . .
//		System.out.println("Remove 1");
//		axis.deleteEntry(axis.getRoot());
//		axis.debug.checkSubTreeSizes();
//		axis.debug.checkAllBounds();
//		System.out.println("Remove 2");
//		axis.deleteEntry(axis.getRoot());
//		axis.debug.checkSubTreeSizes();
//		axis.debug.checkAllBounds();
		
		for (int i = 0 ; i < modCount ; i++) { 
			curSize = axis.getSize();
//			System.out.println("Current size:" + curSize);
			
			if (rand.nextFloat() < addPercent || curSize == 0) { 
				// Add
				SplitLine addLine = new SplitLine();
				addLine.absoluteValue = addCount;
				addCount++; 
				
				SplitLine selectLine = null;
				if (rand.nextFloat() < .1 ) { 
					selectLine = axis.getMinStuckLine();
				} else { 
					insertat = rand.nextInt(curSize);
	//				System.out.println("Inserting at position:" + insertat + " root is: " + axis.getSplitIndex(axis.getRoot()));
					selectLine = axis.getSplitFromIndex(insertat);
				}
//				

//				selectLine = axis.getSplitFromIndex((curSize / 2));
//				System.out.println("Add line at:" + selectLine);
				axis.putAt(addLine, selectLine);
//				axis.debug.checkAllBounds();
				
				addLine = null;
				selectLine = null;
			} else { 
				// Delete randomly selected line
				remLine = axis.getSplitFromIndex(rand.nextInt(curSize));
//				System.out.println("Remove line: " + remLine);
				axis.deleteEntry(remLine);
//				axis.debug.checkAllBounds();
				
				remLine = null;				
			}
		}
		
//		axis.debug.printAllAbsoluteValues();
		axis.debug.checkSubTreeSizes();
		axis.debug.checkAllBounds();
//				
		System.out.println("Done!");
	}

	/**
	 * Starts the split axis test.  No arguments.
	 * @param args Not used, simple test function with no input parameters.
	 */
	public static void main(String[] args) {
		SplitAxisTest test = new SplitAxisTest();

	}

}
