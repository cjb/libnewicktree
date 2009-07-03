package net.sourceforge.olduvai.accordiondrawer.benchmark;

import java.util.Random;

import net.sourceforge.olduvai.accordiondrawer.SplitAxis;
import net.sourceforge.olduvai.accordiondrawer.SplitLine;

public class SplitAxisBenchmark {
	static int initTrialSize = 50000;
	static int opCount = 10000; 
	static int incrementBy = 50000;
	static int stopAt = 5000000;
	static double addPercent = .50;
	static int numRepeats = 3;

	/**
	 * Sleep this number of seconds between trials
	 */
	static int sleepSeconds = 1;

	static Random rand;

	static final SplitAxis initAxis (int axisSize) { 
		SplitAxis axis = new SplitAxis();						
		// Stick in root
		SplitLine rootLine = new SplitLine();
		axis.putAt(rootLine, null);
		// Initialize to initSize: 
		int curSize = axis.getSize();
		for (int i = 0 ; i < axisSize - 1; i++) { 
			SplitLine addLine = new SplitLine();			
			int insertat = rand.nextInt(curSize);
			SplitLine selectLine = axis.getSplitFromIndex(insertat);
			axis.putAt(addLine, selectLine);
			curSize++;
		}
		return axis;
	}


	static final TrialRecord searchTrial (int trialSize) { 
		flushGC();
		SplitAxis axis = initAxis(trialSize);
		final int curSize = axis.getSize();
		final long initHeapSize = memSize();

		int [] randomints = new int [opCount];
		// pregenerate random ints so we don't have to do it in the loop
		for (int i = 0 ; i < opCount ; i++ ) { 
			randomints[i] = rand.nextInt(curSize);
		}

		long beginTime = System.currentTimeMillis();
		for (int i = 0 ; i < opCount ; i++) {
			axis.getSplitFromIndex(randomints[i]);
		}
		long endTime = System.currentTimeMillis();		
		axis = null; 
		return(new TrialRecord(TrialRecord.SEARCH_TRIAL, trialSize , opCount , -1,  initHeapSize, initHeapSize, (endTime - beginTime)));
	}


	static final TrialRecord modifyTrial (int trialSize) { 
		flushGC();

		// must pregenerate modCount random numbers into an array!  
		final boolean addRemove [] = new boolean [opCount];
		for (int i = 0 ; i < opCount ; i++) { 
			if (rand.nextDouble() <= addPercent ) { 
				addRemove[i] = true;
			} else { 
				addRemove[i] = false;
			}
		}

		SplitAxis axis = initAxis(trialSize);
		final long initHeapSize = memSize();

		int addCount = 0;
		int curSize = axis.getSize() - 1;
		long beginTime = System.currentTimeMillis();
		for (int i = 0 ; i < opCount ; i++) { 
			if (addRemove[i]) { 
				axis.putAt(new SplitLine(), axis.getSplitFromIndex(rand.nextInt(curSize)));
				addCount++; 
				curSize++;
			} else { 
				// Delete randomly selected line
				axis.deleteEntry(axis.getSplitFromIndex(rand.nextInt(curSize)));	
				curSize--;
			}
		}
		long endTime = System.currentTimeMillis();

//		axis.debug.checkSubTreeSizes();
//		axis.debug.checkAllBounds();

		axis = null; 
		return(new TrialRecord(TrialRecord.MODIFY_TRIAL, trialSize , opCount , addCount,  initHeapSize, initHeapSize, (endTime - beginTime)));
	}

	static final void flushGC() { 
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
	}

	static final long memSize () { 
		long mem0 = Runtime.getRuntime().totalMemory() -
		Runtime.getRuntime().freeMemory();
//		System.out.println("Memory usage after init: " + mem0);
		return mem0;
	}

	public SplitAxisBenchmark() {
		super();
		rand = new Random(System.currentTimeMillis());
		System.out.println(TrialRecord.printHeader());

		// Do a useless trial just to get things primed 
		modifyTrial(initTrialSize);

		final int totalTrials = (((stopAt - initTrialSize) / incrementBy) + 1) * numRepeats;
//		System.err.println("Total trials = " + totalTrials);

		int currentTrialSize = initTrialSize;
		for (int i = 0 ; i < totalTrials ; i++) { 
//			System.err.println("-- Begin try " + i);
			System.out.println(searchTrial(currentTrialSize).toString());
			System.out.println(modifyTrial(currentTrialSize).toString());
//			System.err.println("--   End try " + i);

			if (i % numRepeats == numRepeats - 1) {
//				System.err.println("!! next trial");
				currentTrialSize += incrementBy;
			}
			try {
				flushGC();
				Thread.sleep(sleepSeconds * 1000);
				flushGC();
			} catch (InterruptedException e) {
				System.err.println("How dare ye interrupt me slumber.");
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) { 
			try { 
				parseArgs(args);
			} catch (Exception e) { 			
				System.err.println("Invalid parameters");
				printCorrectUsage ();
				System.exit(-1);
			}
		}
		new SplitAxisBenchmark();
	}

	static final private void printCorrectUsage() { 
		System.out.println("Correct usage: SplitAxisBenchmark [ -initSize XX ] [ -opCount XX ] [ -incrementBy XX ] [ -stopAt XX ] [ -addPercent XX ] [ -numRepeats XX ] [ -sleepSeconds XX ]" );
	}
	
	static final String INIT_PARAM = "-initSize";
	static final String OP_PARAM = "-opCount";
	static final String INCREMENT_PARAM ="-incrementBy";
	static final String STOPAT_PARAM = "-stopAt";
	static final String ADDPERCENT_PARAM = "-addPercent";
	static final String NUMREPEATS_PARAM = "-numRepeats";
	static final String SLEEPSECONDS_PARAM = "-sleepSeconds";

	final private static void parseArgs (String[] args) throws Exception { 

		for (int i = 0 ; i < args.length ; i++) { 
			if (args[i].equals(INIT_PARAM)) { 
				initTrialSize = Integer.parseInt(args[++i]);
			} else if (args[i].equals(OP_PARAM)) { 
				opCount = Integer.parseInt(args[++i]);				
			}else if (args[i].equals(INCREMENT_PARAM)) { 
				incrementBy = Integer.parseInt(args[++i]);
			}else if (args[i].equals(STOPAT_PARAM)) { 
				stopAt = Integer.parseInt(args[++i]);
			}else if (args[i].equals(ADDPERCENT_PARAM)) { 
				addPercent = Integer.parseInt(args[++i]) * .01;
			}else if (args[i].equals(NUMREPEATS_PARAM)) { 
				numRepeats = Integer.parseInt(args[++i]);
			} else if (args[i].equals(SLEEPSECONDS_PARAM)) { 
				sleepSeconds = Integer.parseInt(args[++i]);
			} else { 
				System.err.println("Unrecognized argument: " + args[i]);
				printCorrectUsage();
				System.exit(-1);
			}
		}
	}

}
