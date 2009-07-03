/**
 * Created on 18-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.olduvai.treejuxtaposer;
import java.awt.BorderLayout ;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;

/**
 * Display help and other information about TJ.  We use the release README file for dialog.
 * @author jslack
 *
 */
public class Help {

	/** Class state needed by the internal declaration of ActionListener for the help window:
	 * The JFrame. */
	static public JFrame aboutFrame;
	/** Class state needed by the internal declaration of ActionListener for the help window:
	 * The filename for the readme file in this directory. */
	private static String filename = "README";

	/**
	 * Helper function used by {@link #getReadme()}, to load the text file resource.
	 * @param resource The name of the readme file.
	 * @return A reader object that the calling function will use to parse out the text for the help display.
	 */
	final protected static BufferedReader getResourceAsReader( String resource )
	{
		InputStream is=Help.class.getResourceAsStream(resource) ;

		BufferedReader reader=new BufferedReader(new InputStreamReader(is)) ;
		return reader ;
	}
	/** Load a resource (readme file) and return its contents as a string
	 * 	@return		Resource contents on success as a string; null on failure 
	 */
	protected static String getReadme()
	{
		BufferedReader br = getResourceAsReader(filename);

		try {
			String temp ="";
			StringBuffer buf = new StringBuffer();

			while((temp = br.readLine())!= null)
				buf.append(temp + "\n");
			return buf.toString();
		}
		catch(Exception e){
			System.out.println("Error: " + e.toString());
			return "";
		}

	}

	/**
	 * Show the help file in a frame.  Event handlers for closing the window defined inside here.
	 *
	 */
	public static void showAboutFrame()
	{
		aboutFrame=new JFrame() ;
		javax.swing.text.DefaultStyledDocument doc=new javax.swing.text.DefaultStyledDocument() ;

		String credits=getReadme() ;
		try {
			doc.insertString(0,credits,null) ;
		} catch( javax.swing.text.BadLocationException ble ) {
		}
		JTextPane jed = new JTextPane();
		jed.setDocument(doc) ;
		jed.setEditable(false) ;
		JScrollPane scroller = new JScrollPane(jed);
		scroller.setMinimumSize(new java.awt.Dimension(600,300)) ;
		scroller.setPreferredSize(new java.awt.Dimension(600,650)) ;
		aboutFrame.getContentPane().add(BorderLayout.CENTER, scroller) ;
		JButton ok=new JButton("Ok");
		ok.addActionListener(new ActionListener()
			{ public void actionPerformed(ActionEvent e)
				{  aboutFrame.dispose(); }});
		aboutFrame.getContentPane().add(BorderLayout.SOUTH, ok) ;
		aboutFrame.pack() ;
		aboutFrame.setLocation(570,30);
		aboutFrame.setVisible(true) ;
	}

}

