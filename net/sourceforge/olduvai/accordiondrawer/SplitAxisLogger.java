/**
 * 
 */
package net.sourceforge.olduvai.accordiondrawer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

/**
 * Any lines starting with ## are interpreted by the player to be header information and are
 * ignored. 
 * 
 * 
 * @author peter
 *
 */
public class SplitAxisLogger {
	public static final int X = 0;
	public static final int Y = 1;
	
	private static final long TIMERINTERVAL = 1000 * 60 * 1; // run every 1 minute
	
	final private File writeFile;
	final protected BufferedWriter bufferedWriter;
	final int type;
	
	final private StringBuilder builder = new StringBuilder();
	
	final Timer timer; 
	
	protected String fullPath; 
	
	final String path;
	
	String filename;
	
	boolean canWrite = true;
	
	/**
	 * @param prefix for log filename
	 * @param path Path to output file in filesystem
	 * @param type {@link #X} or {@link #Y} 
	 */
	public SplitAxisLogger(String filename, String path, int type) throws IOException { 
		this.type = type;
		fullPath = path + "/" + filename + "_" + getAxisName() + ".log" ;
		timer = new Timer("Logging timer " + getAxisName());
		this.filename = filename;
		this.path = path;
		
		// create path if it doesn't exist
		new File(path).mkdir();
		
		// Attempt to create file in specified path
		writeFile = new File(fullPath);
		writeFile.createNewFile();
		
		if (writeFile.canWrite()) { 
			this.bufferedWriter = new BufferedWriter(new FileWriter(writeFile));
		} else { 
			throw new IOException("Unable to write to file");
		}
		writeHeader();
		
		timer.schedule(new TimerTask () {
			public void run() {
				try {
					bufferedWriter.flush();
				} catch (IOException e) {
					System.err.println("Flush timer unable to write IO log for log " + getAxisName() );
				}
			}} , TIMERINTERVAL, TIMERINTERVAL);
	}

	private String getAxisName () { 
		return ((type == 0) ? "X" : "Y");
	}
	
	double [] prevValues;
	
	/**
	 * Create a new log entry.  
	 * 
	 * @param minStuckLine
	 * @param maxStuckLine
	 * @param partitionedSet
	 * @throws IOException
	 */
	public void logEntry(SplitLine minStuckLine, SplitLine maxStuckLine, TreeSet partitionedSet) throws IOException { 
		if (! canWrite) { 
			System.err.println("Attempt to write to a closed log file: " + this);
			return;
		}
		
		final long time = System.currentTimeMillis();
		builder.setLength(0); // clear the builder
		
		double [] newValues = new double [partitionedSet.size()];
		int i = 0;
		for (final Object o : partitionedSet) {
			final SplitLine s = (SplitLine) o;
			newValues[i] = s.getCachedValue();
			builder.append("," + newValues[i]);
			i++;
		}		
		
		if (prevValues != null && newValues.length == prevValues.length) { 
			// same length, they may be ronidentical, let's check
			boolean same = true;
			for (int j = 0 ; j < newValues.length ; j++) { 
				if (newValues[j] != prevValues[j]) { 
					same = false;
					break;
				}
			}
			if (same) // lines are identical
				return;
		}
		
		prevValues = newValues;
		bufferedWriter.write(time + builder.toString() + "," + maxStuckLine.getCachedValue() + "\n");
	}
	
	/**
	 * May be overridden to log custom header information
	 * @throws IOException
	 */
	public void writeHeader () throws IOException { 
		bufferedWriter.write("## timestamp, minLine, maxLine, partitionedLineList...\n");
	}
	
	
	/**
	 * Flushes and closes the log file, then gzips.  
	 * @throws IOException
	 */
	public void cleanup () throws  IOException { 
		canWrite = false; 
		
		timer.cancel();
		
		bufferedWriter.flush();
		bufferedWriter.close();
		
		final String newFileName = filename + ".gz";
		final String newFullPath = fullPath + ".gz";
		GZIPOutputStream outgzWriter = new GZIPOutputStream(new FileOutputStream(newFullPath));
		outgzWriter.write(readLogFile().getBytes());
		outgzWriter.close();
		
		this.filename = newFileName;
		this.fullPath = newFullPath;
	}

	
	/**
	 * Reads in the log file as it currently exists (performs a flush to ensure latest updates 
	 * are available.)  
	 * @return
	 * @throws IOException
	 */
	public String readLogFile () throws IOException { 
		if (canWrite)
			bufferedWriter.flush();
		final File readFile = new File(fullPath);
		
		final FileReader fr = new FileReader(readFile);
		final BufferedReader br = new BufferedReader(fr);
		StringBuilder sb = new StringBuilder();
		String line;
		while ( (line = br.readLine()) != null ) {
			sb.append(line + "\n");
		}
		fr.close();
		return sb.toString();
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
//		cleanup();
	}
	public String getFullPath() {
		return fullPath;
	}

	public String getPath() {
		return path;
	}

	public String getFilename() {
		return filename;
	}

	public void flush() throws IOException {
		if (canWrite)
			bufferedWriter.flush();
	}
	
	
}
