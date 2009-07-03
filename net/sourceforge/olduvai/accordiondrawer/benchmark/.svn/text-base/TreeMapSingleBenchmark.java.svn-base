package net.sourceforge.olduvai.accordiondrawer.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class TreeMapSingleBenchmark {
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
	
	// Static fields
	static int type = MODIFY_TYPE;
	static int warmup = 50000;
	static int trialSize = 50000;
	static int opCount = 10000; 
	static int sleepSeconds = 1;
	static double addPercent = .50;
	static int numRepeats = 3;
	static Random rand;

	static final TreeSet<Integer> initAxis (int trialSize) { 
		TreeSet<Integer> map = new TreeSet<Integer>();
		
		for (int i = 0 ; i < trialSize ; i++) {
			map.add(new Integer(i));
		}
		return map;
	}

	static final TrialRecord modifyTrial (int trialSize) { 
		TreeSet<Integer> axis = initAxis(trialSize);

		final long initHeapSize = memSize();
		
		int addCount = 0;
		int curSize = axis.size() - 1;
		long beginTime = System.currentTimeMillis();
		for (int i = 0 ; i < opCount ; i++) { 
			axis.add(new Integer(curSize + 1));
			curSize++;
		}
		long endTime = System.currentTimeMillis();
		
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

	public TreeMapSingleBenchmark() {
		super();
		rand = new Random(System.currentTimeMillis());
		
		modifyTrial(trialSize);
		flushGC();
		try {
			// Give a second to try and make sure GC is completed before moving on
			Thread.sleep(sleepSeconds * 1000);
		} catch (InterruptedException e) {
			System.err.println("Away with ye, scurvy dog.");
		}

		System.out.println(modifyTrial(trialSize).toString());
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
		new TreeMapSingleBenchmark();
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
