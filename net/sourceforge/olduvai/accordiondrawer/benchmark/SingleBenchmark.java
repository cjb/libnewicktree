package net.sourceforge.olduvai.accordiondrawer.benchmark;

import java.util.Random;

import net.sourceforge.olduvai.accordiondrawer.SplitAxis;
import net.sourceforge.olduvai.accordiondrawer.SplitLine;

public class SingleBenchmark {
	static final String TYPE_PARAM = "-type";
	static final String WARMUP_PARAM = "-warmup";
	static final String INIT_PARAM = "-initSize";
	static final String OP_PARAM = "-opCount";
	static final String ADDPERCENT_PARAM = "-addPercent";
	static final String NUMREPEATS_PARAM = "-numRepeats";
	static final String SLEEPSECONDS_PARAM = "-sleepSeconds";
	static final int SEARCH_TYPE = 1;
	static final int MODIFY_TYPE = 0;	
	static final String SEARCH_MATCH = "search";
	static final String MODIFY_MATCH = "modify";
	static final long TARGET_HEAP_SIZE = 352000000l;	
	
	// Static fields
	static int type = MODIFY_TYPE;
	static int warmup = 50000;
	static int trialSize = 50000;
	static int opCount = 10000; 
	static int sleepSeconds = 1;
	static double addPercent = .50;
	static int numRepeats = 3;
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


	final TrialRecord searchTrial (int trialSize, boolean dryrun) { 
		SplitAxis axis = initAxis(trialSize);
		final int curSize = axis.getSize();

		int [] randomints = new int [opCount];
		// pregenerate random ints so we don't have to do it in the loop
		for (int i = 0 ; i < opCount ; i++ ) { 
			randomints[i] = rand.nextInt(curSize);
		}

		final long initHeapSize = memSize();
		long finalHeapSize = initHeapSize;
//		if (! dryrun)
//			finalHeapSize = allocateGarbage(initHeapSize);
		
		long beginTime = System.currentTimeMillis();
		for (int i = 0 ; i < opCount ; i++) {
			axis.getSplitFromIndex(randomints[i]);
		}
		long endTime = System.currentTimeMillis();		
		axis = null; 
		return(new TrialRecord(TrialRecord.SEARCH_TRIAL, trialSize , opCount , -1,  initHeapSize, finalHeapSize, (endTime - beginTime)));
	}

	int [] heapGarbage;
	final long allocateGarbage (long startHeapSize) { 
		long toAllocate = (TARGET_HEAP_SIZE - startHeapSize) / 4;  // divide by four to get bytes
		if (toAllocate <= 0) 
			return startHeapSize;
		heapGarbage = new int [(int) toAllocate];
		final long postGarbage = memSize();
		return postGarbage;
	}

	final TrialRecord modifyTrial (int trialSize, boolean dryrun) { 
		SplitAxis axis = initAxis(trialSize);
//		System.out.println("Trial size: " + trialSize);
		final long initHeapSize = memSize();
		
		// Create all new objects in advance
		final int numSplitsToCreate = (int) (opCount);
		SplitLine [] newSplits = new SplitLine [numSplitsToCreate];
		for (int i = 0 ; i < numSplitsToCreate ; i++) { 
			newSplits[i] = new SplitLine();
		}
		final long finalHeapSize = memSize();
				
		int addCount = 0;
		int curSize = axis.getSize() - 1;
		long beginTime = System.currentTimeMillis();
		for (int i = 0 ; i < opCount ; i++) { 
			if (rand.nextDouble() <= addPercent) { 
				axis.putAt(newSplits[addCount], axis.getSplitFromIndex(rand.nextInt(curSize)));
				addCount++; 
				curSize++;
			} else { 
				// Delete randomly selected line
				axis.deleteEntry(axis.getSplitFromIndex(rand.nextInt(curSize)));	
				curSize--;
			}
		}
		long endTime = System.currentTimeMillis();
		
		axis = null; 
		return(new TrialRecord(TrialRecord.MODIFY_TRIAL, trialSize , opCount , addCount,  initHeapSize, finalHeapSize, (endTime - beginTime)));
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

	public SingleBenchmark() {
		super();
		rand = new Random(System.currentTimeMillis());
//		System.out.println(TrialRecord.printHeader());

		// Do a useless trial just to get things primed 
		modifyTrial(warmup, true);
//		modifyTrial(warmup);
//		flushGC();
		try {
			// Give a second to try and make sure GC is completed before moving on
			Thread.sleep(sleepSeconds * 1000);
		} catch (InterruptedException e) {
			System.err.println("Away with ye, scurvy dog.");
		}

		if (type == MODIFY_TYPE)
			System.out.println(modifyTrial(trialSize, false).toString());
		else
			System.out.println(searchTrial(trialSize, false).toString());
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
		new SingleBenchmark();
	}

	static final private void printCorrectUsage() { 
		System.out.println("Correct usage: SingleBenchmark [ -type {search || modify} ] [ -warmup XX ] [ -initSize XX ] [ -opCount XX ] [ -addPercent XX ] [ -numRepeats XX ] [ -sleepSeconds XX ]" );
	}


	
	final private static void parseArgs (String[] args) throws Exception { 

		for (int i = 0 ; i < args.length ; i++) { 
			if (args[i].equals(TYPE_PARAM)) {
				final String t = args[++i];
				if (t.toLowerCase().equals(MODIFY_MATCH))
					type = MODIFY_TYPE;
				else
					type = SEARCH_TYPE;
			} else if (args[i].equals(INIT_PARAM)) { 
				trialSize = Integer.parseInt(args[++i]);
			} else if (args[i].equals(WARMUP_PARAM)) { 
				warmup = Integer.parseInt(args[++i]);				
			} else if (args[i].equals(OP_PARAM)) { 
				opCount = Integer.parseInt(args[++i]);				
			} else if (args[i].equals(ADDPERCENT_PARAM)) { 
				addPercent = Integer.parseInt(args[++i]) * .01;
			} else if (args[i].equals(NUMREPEATS_PARAM)) { 
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
