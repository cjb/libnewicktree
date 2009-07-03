package net.sourceforge.olduvai.accordiondrawer.logplayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("unchecked")
public abstract class LogPlayer extends JFrame implements KeyListener, WindowListener {
	protected static final int X = 0;
	protected static final int Y = 1;
	
	static final String LOGFILESUFFIX = ".log";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final String TITLE = "Log player";
	
	static final String PLAYTEXT = "Play";
	static final String PAUSETEXT = "Pause";
	
	
	protected class LogEntry implements  Comparable {

		final protected long timestamp;
		final protected double [] values;
		
		public long getTimestamp() { 
			return timestamp;
		}
		
		public double [] getValues () { 
			return values;
		}
		
		public LogEntry(long timestamp, double [] values) { 
			this.timestamp = timestamp;
			this.values = values;
		}
		
		public int compareTo(Object o) { 
			if (o instanceof Long)
				return new Long(timestamp).compareTo((Long) o); 
			final LogEntry l = (LogEntry) o;
			if (this.timestamp == l.timestamp)
				return 0; 
			else if (this.timestamp < l.timestamp)
				return -1;
			else if (this.timestamp > l.timestamp)
				return 1;
			return 0;
		}
	}
	
	protected ArrayList<LogEntry> [] entries = new ArrayList [2]; 
	
	final Timer timer = new Timer("Log playback timer");
	TimerTask tt;
	boolean disableSlider = false;
	
	/**
	 * Store index of current grid coordinates for each axis {@link #Y} and {@link #Y}
	 */
	int [] currentIndex = new int [2];
	
	long currentTimeStamp; 
	
	long delay = 2;
	long advanceAmount = 2;

	final JButton playButton;
	final JSlider slider;
	
	/**
	 * If set to true, exit when the window is closed.  
	 */
	boolean standalone = false;
	
	protected final JPanel drawPanel = new JPanel() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			
//			g2.drawLine(0, 0, getWidth(), getHeight());
			for (int axis = X ; axis <= Y ; axis++)
				drawLines(g2, getCurrentValues(axis), axis);
			
			drawCustom(g);
		}
		
		private void drawLines(Graphics2D g2d, double [] values, int axis) { 
			final int width = getWidth();
			final int height = getHeight();
			
			g2d.setColor(Color.DARK_GRAY);
			
			for (final double value : values) {
				final int x1 = (axis == Y) ? 0 : w2s(value, width);
				final int x2 = (axis == Y) ? width : w2s(value,width);
				final int y1 = (axis == Y) ? w2s(value, height) : 0;
				final int y2 = (axis == Y) ? w2s(value, height) : height;
				g2d.drawLine(x1, y1, x2, y2);
			}
		}
	} ;
	
	/**
	 * Compute the screen coordinate given a proportional value and the size of the 
	 * relevant dimension
	 * @param worldValue as percentage of axisSize
	 * @param axisSize size of the axis 
	 * @return
	 */
	protected int w2s(double worldValue, int axisSize) { 
		return (int) Math.floor(worldValue * axisSize);
	}
	
	abstract protected void drawCustom(Graphics g);
	
	private int getMinTimestamp() { 
		final int minX = getMinTimestamp(X);
		final int minY = getMinTimestamp(Y);
		if (minX < minY)
			return minX;
		return minY;
	}
	
	private int getMinTimestamp(int axis) { 
		final long min = entries[axis].get(0).timestamp;
		return (int) Math.floor(min / 1000);
	}
	
	private int getMaxTimestamp() { 
		long maxX = entries[X].get(entries[X].size() - 1).timestamp;
		long maxY = entries[Y].get(entries[Y].size() - 1).timestamp;
		if (maxX > maxY)
			return (int) Math.floor(maxX / 1000);
		return (int) Math.floor(maxY / 1000);
	}
	
	final int PAUSE = 0;
	final int PLAY = 1;
	
	private void togglePlayButton (int state) { 
		if (state == PAUSE) { 
			// Pause ( do nothing if already paused )
			playButton.setText(PLAYTEXT);
			if (tt != null)
				tt.cancel();
		} else { 
			playButton.setText(PAUSETEXT);
			scheduleTimer();
		}
	}
	
	private void scheduleTimer () {
		if (tt != null)
			tt.cancel();
		tt = new TimerTask() {
			@Override
			public void run() {
				advanceTimeStamp(currentTimeStamp + advanceAmount);
			}};
		timer.scheduleAtFixedRate(tt, delay, delay);	
	}
	
	/**
	 * Call init after using the super() of this class.  
	 * 
	 * @throws HeadlessException
	 */
	public LogPlayer(boolean standalone) throws HeadlessException   {
		super(TITLE);
		setBackground(Color.white);
		addKeyListener(this);
		this.standalone = standalone;

		slider = new JSlider(JSlider.HORIZONTAL);
		playButton = new JButton(PLAYTEXT);
		
	}
	
	
	protected void init (String logPath, String filename) { 
		
		entries[X] = new ArrayList<LogEntry>();
		entries[Y] = new ArrayList<LogEntry>();
		
		for (int axis = X ; axis <= Y ; axis++) {
			try {
				loadLog(logPath, filename, axis);
			} catch (IOException e) {
				System.err.println("!! Error loading log file.");
				e.printStackTrace();
			}
			System.out.println("-- Min timestamp for " + getAxisName(axis) + ": " + getMinTimestamp(axis));
		}
		
		currentIndex[X] = 0;
		currentIndex[Y] = 0;
		
		// pick earliest timestamp from X/Y axis
		currentTimeStamp = (entries[X].get(0).timestamp < entries[Y].get(0).timestamp) ? entries[X].get(0).timestamp : entries[Y].get(0).timestamp;

//		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		getContentPane().setLayout(new MigLayout("fill",  "[][]", "[][]"));
		drawPanel.setBackground(Color.white);
		getContentPane().add(drawPanel, "grow, span, wrap");
		
		final int minTimeStamp = getMinTimestamp();
		final int maxTimeStamp = getMaxTimestamp();
		slider.setMinimum(minTimeStamp);
		slider.setMaximum(maxTimeStamp);
		slider.setValue(minTimeStamp);
		slider.addChangeListener(new ChangeListener () {
			public void stateChanged(ChangeEvent e) {
//				System.out.println("-- Slider value: " + slider.getValue());
				if (disableSlider)
					return;
				if (tt != null)
					tt.cancel(); 
				
				advanceTimeStamp((long) slider.getValue() * 1000);
				
				if (playButton.getText() == PAUSETEXT) { 
					scheduleTimer();
				}
			}
		} );
		
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (playButton.getText().equals(PLAYTEXT))
					togglePlayButton(PLAY);
				else
					togglePlayButton(PAUSE);
			}
		});
		
		getContentPane().add(slider, "growx");
		getContentPane().add(playButton, "");
		
		setSize(800, 600);
				
		setVisible(true);
	}
	
	private void loadLog(String path, String filename, int axis) throws IOException { 
		final String axisname = (axis == X) ? "X" : "Y";
		final String fullpath = path + "/" + filename + "_" + axisname + LOGFILESUFFIX;  
		
		final File f = new File(fullpath);
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);
		String line;
		int linecount = 0;
		while ( (line = br.readLine()) != null ) {
			if (linecount == 0) {
				// skip first line always
				linecount++;
				continue;
			}
			// check if it is a user defined header
			if (line.length() >= 2) { 
				if (line.charAt(0) == '#' && line.charAt(1) == '#' ) { 
					customDataHandler(fullpath, line);
					continue;
				}
			}
			
			final String [] linearray = line.split(",");
			final long timestamp = Long.parseLong(linearray[0]);
			final double [] values = new double[linearray.length - 1]; 
			for (int i = 1 ; i < linearray.length ; i++)
				values[i - 1] = Double.parseDouble(linearray[i]);
			entries[axis].add(new LogEntry(timestamp, values));
			linecount++;
		}
	}

	/**
	 * Handle any custom headers added by the application on a line 
	 * 
	 * @param line
	 */
	protected abstract void customDataHandler(String fullfilepath, String line);
	
	/**
	 * Return list of lines at current time stamp
	 * @param axis {@link #X} or {@link #Y}
	 * 
	 * @return
	 */
	protected double[] getCurrentValues(final int axis) {
		if (axis < X || axis > Y)
			throw new IndexOutOfBoundsException("Axis must be X or Y");
		return entries[axis].get(currentIndex[axis]).values;
	}

	/**
	 * After the timestamp has been moved, check to see if we have a new log line
	 * to show.  Repaint. 
	 * 
	 * @param newTimeStamp
	 */
	private void advanceTimeStamp (long newTimeStamp) { 
		for (int axis = X ; axis <= Y ; axis++) { 
			
			final ArrayList axisEntries = entries[axis];
			final int searchIndex = Collections.binarySearch(axisEntries, (Long) newTimeStamp);
			int newIndex = currentIndex[axis];
			
			if (searchIndex < 0) { 
				newIndex = (searchIndex * -1) - 1;
			} else { 
				newIndex = searchIndex;				
			}
			
			if (newIndex >= axisEntries.size()) { 
				newIndex = axisEntries.size() - 1;
				togglePlayButton(PAUSE);
			}
			
//			if (currentIndex[axis] != newIndex)
//				System.out.println("-- " + getAxisName(axis) + " index: " + newIndex );			
			currentIndex[axis] = newIndex;
		}
		currentTimeStamp = newTimeStamp;
		
		disableSlider = true;
		slider.setValue((int) Math.floor(currentTimeStamp / 1000));
		disableSlider = false;
		
		repaint();
	}
	
	private String getAxisName(int axis) { 
		if (axis == X)
			return "X";
		else 
			return "Y";
			
	}
	

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Wuh?");
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent e) {
		System.out.println("Boo!");
		final char c = e.getKeyChar();
		if (c == KeyEvent.VK_SPACE) { 
			System.out.println("-- slider value: " + slider.getValue());
		}
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosing(WindowEvent e) {
		if (standalone)
			System.exit(0);		
		timer.cancel();
		dispose();
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	

}
