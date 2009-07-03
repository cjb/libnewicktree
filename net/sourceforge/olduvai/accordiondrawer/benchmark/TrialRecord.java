/**
 * 
 */
package net.sourceforge.olduvai.accordiondrawer.benchmark;

class TrialRecord { 
	public static final int SEARCH_TRIAL = 0;
	public static final int MODIFY_TRIAL = 1;
	
	private static final String TYPESTRING (int trialType) { 
		if (trialType == SEARCH_TRIAL) { 
			return "SEARCH";
		} else { 
			return "MODIFY";
		}
	}

	
	public TrialRecord (int trialType, int trialSize, int numOps, int adds, long initHeapSize, long finalHeapSize, long duration) { 
		this.trialType = trialType;
		this.trialSize = trialSize;
		this.numOps = numOps;
		this.adds = adds;
		this.initHeapSize = initHeapSize;
		this.finalHeapSize = finalHeapSize;
		this.duration = duration;
	}
	
	public int trialType;
	public int trialSize;
	public int adds;
	public int numOps;
	public long initHeapSize;
	public long finalHeapSize;
	public long duration;
	
	public static final String printHeader () { 
		return "Trial type, Trial size, type1, Number of operations, Initial heap size, Final heap size, Trial duration";
	}
		
	static final String comma = ",";
	public String toString () { 
		String result = TYPESTRING(trialType) + comma + trialSize + comma + adds + comma + numOps + comma + initHeapSize + comma + finalHeapSize + comma + duration;		
		return result;
	}
}